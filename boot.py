import inspect
import os
import subprocess
import sys


here = os.path.dirname(os.path.realpath(__file__))


def each(f, a):
    for b in a:
        f(b)


def eachr(f, a):
    f(a)
    match a:
        case [*_]:
            for b in a:
                eachr(f, b)


def partition(f, a):
    b = [x for x in a if f(x)]
    c = [x for x in a if not f(x)]
    return b, c


def show(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    sys.stderr.write(f"{info.filename}:{info.function}:{info.lineno}: {a}\n")


syms = {}


def gensym(s):
    s = "_" + s
    if s not in syms:
        syms[s] = 0
        return s
    i = syms[s] + 1
    syms[s] = i
    return s + str(i)


# parser
def unquote(s):
    s = s[1:-1]
    return s.encode("utf-8").decode("unicode_escape")


def quoteSym(s):
    return "intern", ["List.of"] + [ord(c) for c in s]


def parse(fil):
    text = open(fil).read()
    ti = 0
    line = 1

    dentc = None
    cols = [0]
    dedents = 0

    tok = None

    def err(msg):
        raise Exception(f"{fil}:{line}: {msg}")

    # tokenizer
    def lex():
        nonlocal dedents
        nonlocal dentc
        nonlocal line
        nonlocal ti
        nonlocal tok

        if dedents:
            dedents -= 1
            tok = ".dedent"
            return

        while ti < len(text):
            i = ti
            tok = text[ti]

            # newline
            if text[ti] == "\n":
                # next line
                ti += 1
                if ti == len(text):
                    return
                line += 1

                # measure indent
                col = 0
                while text[ti] == "\t" or text[ti] == " ":
                    if text[ti] != dentc and dentc:
                        err("indented with tabs and spaces in same file")
                    dentc = text[ti]
                    ti += 1
                    col += 1

                # nothing important on this line, keep going
                if text[ti] in ("\n", ";") or text[ti : ti + 2] == "/*":
                    continue

                # one indent
                if col > cols[-1]:
                    cols.append(col)
                    tok = ".indent"
                    return

                # zero or more dedents
                while col < cols[-1]:
                    del cols[-1]
                    dedents += 1
                if col != cols[-1]:
                    err("inconsistent indent")
                return

            # space
            if text[ti].isspace():
                ti += 1
                continue

            # comment
            if text[ti] == ";":
                while text[ti] != "\n":
                    ti += 1
                continue
            if text[ti : ti + 2] == "/*":
                ti += 2
                line1 = line
                while text[ti : ti + 2] != "*/":
                    if ti == len(text):
                        line = line1
                        err("unclosed block comment")
                    if text[ti] == "\n":
                        line += 1
                    ti += 1
                ti += 2
                continue

            # word or number
            if text[ti].isalnum() or text[ti] == "_":
                while text[ti].isalnum() or text[ti] in ("_", "?"):
                    ti += 1
                tok = text[i:ti]
                return

            # quote
            if text[ti] in ("'", '"'):
                q = text[ti]
                ti += 1
                while text[ti] != q:
                    # TODO: handle or disallow multiline strings
                    if text[ti] == "\\":
                        ti += 1
                    ti += 1
                ti += 1
                tok = text[i:ti]
                return

            # punctuation
            punct = (
                "!=",
                "&&",
                "++",
                "+=",
                "--",
                "-=",
                "//",
                "<=",
                "==",
                "==",
                ">=",
                "||",
            )
            for s in punct:
                if text[ti : ti + len(s)] == s:
                    ti += len(s)
                    tok = s
                    return

            ti += 1
            return

        # end of file
        tok = ".dedent"

    def lex1():
        s = tok
        lex()
        return s

    # parser
    def eat(s):
        if tok == s:
            lex()
            return 1

    def expect(s):
        if not eat(s):
            err(f"{repr(tok)}: expected {repr(s)}")

    def word():
        if tok[0].isalpha() or tok[0] == "_":
            return lex1()
        err(f"{repr(tok)}: expected word")

    # expressions
    def commas(a, end):
        while 1:
            if eat(end):
                break
            a.append(expr())
            if eat(end):
                break
            expect(",")

    def isPrimary():
        return tok[0].isalnum() or tok[0] in ("_", "'", '"')

    def primary():
        # word or number
        if tok[0].isalpha() or tok[0] == "_":
            return lex1()

        # number
        if tok[0].isdigit():
            return int(lex1())

        # symbol
        if tok[0] == "'":
            s = unquote(lex1())
            return quoteSym(s)

        # string
        if tok[0] == '"':
            s = unquote(lex1())
            return ["List.of"] + [ord(c) for c in s]

        # parenthesized expression
        if eat("("):
            a = tuple1()
            expect(")")
            return a

        # list
        if eat("["):
            a = ["List.of"]
            commas(a, "]")
            return a

        # none of the above
        err(f"{repr(tok)}: expected expression")

    def postfix():
        a = primary()
        while 1:
            match tok:
                case "(":
                    lex()
                    a = [a]
                    commas(a, ")")
                    continue
                case "[":
                    a = lex1(), a, expr()
                    expect("]")
                    continue
                case ".":
                    lex()
                    a = "get", a, quoteSym(word())
                    continue
                case "++" | "--":
                    return "post" + lex1(), a
            if isPrimary():
                a = [a, prefix()]
                while eat(","):
                    a.append(prefix())
            return a

    def params():
        a = []
        match tok:
            case ":" | ".indent":
                pass
            case "(":
                lex()
                commas(a, ")")
            case _:
                while 1:
                    a.append(word())
                    if not eat(","):
                        break
        return a

    def prefix():
        match tok:
            case "!" | "++" | "--":
                return lex1(), prefix()
            case "-":
                lex()
                return "Etc.neg", prefix()
            case "\\":
                a = [lex1(), params()]
                expect(":")
                a.append(assignment())
                return a
        return postfix()

    # operator precedence parser
    prec = 99
    ops = {}

    def init(s):
        ops[s] = prec

    init("%")
    init("*")
    init("//")

    prec -= 1
    # TODO: @ for list concat?
    init("+")
    init("-")

    prec -= 1
    init("!=")
    init("<")
    init("<=")
    init("==")
    init(">")
    init(">=")

    prec -= 1
    init("&&")

    prec -= 1
    init("||")

    def infix(prec):
        a = prefix()
        while 1:
            if tok not in ops or ops[tok] < prec:
                return a
            op = lex1()
            b = infix(ops[op] + 1)
            match op:
                case ">":
                    a = "<", b, a
                case ">=":
                    a = "<=", b, a
                case _:
                    a = op, a, b

    def expr():
        return infix(0)

    # statements
    def tuple1():
        a = expr()
        if tok != ",":
            return a
        a = ["List.of", a]
        while eat(","):
            a.append(expr())
        return a

    def assignment():
        a = tuple1()
        if tok in ("=", "+=", "-="):
            return lex1(), a, assignment()
        return a

    def block(a):
        expect(".indent")
        while not eat(".dedent"):
            a.append(stmt())

    def block1():
        a = []
        block(a)
        return a

    def if1():
        assert tok in ("if", "elif")
        lex()
        a = ["if", expr(), block1()]
        match tok:
            case "elif":
                a.append(if1())
            case "else":
                lex()
                a.append(block1())
        return a

    def stmt():
        a = [tok]
        match tok:
            case "assert":
                lex()
                a.append(expr())
                expect("\n")
                return a
            case "dowhile" | "while":
                lex()
                a.append(expr())
                block(a)
                return a
            case "for":
                lex()
                a.append(word())
                a.append(tuple1())
                block(a)
                return a
            case "fn":
                lex()
                a.append(word())
                a.append(params())
                block(a)
                return a
            case "if":
                return if1()
            case "nonlocal":
                lex()
                a.append(word())
                expect("\n")
                return a
            case "return":
                lex()
                if eat("\n"):
                    return "return", 0
                a.append(tuple1())
                expect("\n")
                return a
        a = assignment()
        expect("\n")
        return a

    # module
    lex()
    eat("\n")
    a = []
    while tok != ".dedent":
        a.append(stmt())
    return a


global1 = parse(os.path.join(here, "global.k"))
program = parse(sys.argv[1])


# intermediate representation
def getTypes(a):
    types = {}

    def f(a):
        match a:
            case (
                ("++", x)
                | ("--", x)
                | ("post++", x)
                | ("post--", x)
                | ("+=", x, _)
                | ("-=", x, _)
            ):
                types[x] = "int"

    eachr(f, a)
    return types


def localVars(params, a):
    nonlocals = set()

    # dict keeps deterministic order
    vs = {}

    def f(a):
        match a:
            case "nonlocal", x:
                nonlocals.add(x)
            case "=", x, _:
                if x not in params and x not in nonlocals:
                    vs[x] = 1

    eachr(f, a)
    return list(vs.keys())


def ir(a):
    match a:
        case "push", x, y:
            return ir(("=", x, ("cat", x, ("List.of", y))))
        case "pushs", x, y:
            return ir(("=", x, ("cat", x, y)))
        case "range", x:
            return ir(("range", 0, x))
        case ("&&", *args) | ("||", *args) | ("!", *args) | ("assert", *args):
            args = [("Etc.truth", ir(x)) for x in args]
            return a[0], *args
        case ("dowhile", test, *body) | ("while", test, *body) | ("if", test, *body):
            test = "Etc.truth", ir(test)
            body = list(map(ir, body))
            return a[0], test, *body
        case "fn", name, params, *body:
            modifiers = []
            t = "Object"

            # recur
            body = list(map(ir, body))

            # separate the local functions
            fs, body = partition(lambda a: a[0] == "fn", body)

            # get the local variables
            types = getTypes(body)
            vs = localVars(params, body)
            body = [a for a in body if a[0] != "nonlocal"]
            vs = [(".var", [], types.get(x, "Object"), x, 0) for x in vs]

            # parameter types
            params = [(types.get(x, "Object"), x) for x in params]

            # if the trailing return is implicit, make it explicit
            if not body:
                body = [0]
            a = body[-1]
            match a:
                case "assert", _:
                    body.append(("return", 0))
                case "return", _:
                    pass
                case _:
                    body[-1] = "return", a

            # if there are local functions, we need to generate a class
            if fs:
                ctor = ["fn", [], "", name, params]
                for _, x in params:
                    ctor.append(("=", "this." + x, x))
                fs.append(ctor)

                run = ["fn", [], "Object", "run", params]
                run.extend(body)
                fs.append(run)

                return ".class", modifiers, name, params, *(vs + fs)

            # otherwise, we still just have a function
            return "fn", modifiers, t, name, params, *(vs + body)
        case [*_]:
            return list(map(ir, a))
    return a


def ir1(name, module):
    return ir(("fn", name, [], *module))


global1 = ir1("Global", global1)
program = ir1("Main1", program)


# output
sys.stdout.write(open(os.path.join(here, "boot.java")).read())


def separate(f, a, separator):
    more = 0
    for b in a:
        if more:
            emit(separator)
        more = 1
        f(b)


def emit(a, separator=" "):
    if isinstance(a, int):
        a = str(a)
    if not isinstance(a, str):
        separate(emit, a, separator)
        return
    if a.endswith("?"):
        a = a[:-1] + "p"
    sys.stdout.write(a)


def fcast(params):
    match len(params):
        case 0:
            emit("(Supplier<Object>)")
        case 1:
            emit("(UnaryOperator<Object>)")
        case 2:
            emit("(BinaryOperator<Object>)")


globals1 = set()
for a in global1:
    match a:
        case "fn", modifiers, t, name, params, *body:
            globals1.add(name)


def expr(a):
    match a:
        case ("post++", x) | ("post--", x):
            expr(x)
            emit(a[0][4:])
        case "\\", params, body:
            fcast(params)
            emit("(")
            emit(params, ",")
            emit(") ->")
            expr(body)
        case "//", x, y:
            expr(x)
            emit("/")
            expr(y)
        case "<", x, y:
            expr(("Etc.lt", x, y))
        case "<=", x, y:
            expr(("Etc.le", x, y))
        case "+", x, y:
            expr(("Etc.add", x, y))
        case "*", x, y:
            expr(("Etc.mul", x, y))
        case "-", x, y:
            expr(("Etc.sub", x, y))
        case "==", x, y:
            expr(("Etc.eq", x, y))
        case "intern", x:
            expr(("Sym.intern", x))
        case "gensym",:
            expr(("new Sym",))
        case (
            ("len", *args)
            | ("cat", *args)
            | ("get", *args)
            | ("append", *args)
            | ("range", *args)
            | ("num?", *args)
            | ("sym?", *args)
            | ("list?", *args)
            | ("str", *args)
            | ("print", *args)
        ):
            expr(("Etc." + a[0], *args))
        case "[", x, y:
            expr(("Etc.subscript", x, y))
        case f, *args:
            if f[0].isalpha():
                if len(f) == 1:
                    emit("(")
                    fcast(args)
                    emit(f)
                    emit(")")
                    if len(args):
                        emit(".apply")
                    else:
                        emit(".get")
                else:
                    if f in globals1:
                        emit("Global.")
                    emit(f)
                emit("(")
                separate(expr, args, ",")
                emit(")")
                return
            match args:
                case x,:
                    emit(f)
                    expr(x)
                case x, y:
                    expr(x)
                    emit(f)
                    expr(y)
                case _:
                    raise Exception(a)
        case _:
            if isinstance(a, str) or isinstance(a, int):
                emit(a)
                return
            raise Exception(a)


def stmt(a):
    match a:
        case "for", x, s, *body:
            emit("for (var ")
            expr(x)
            emit(":")
            expr(s)
            emit(") {\n")
            each(stmt, body)
            emit("}\n")
        case "while", test, *body:
            emit("while (")
            expr(test)
            emit(") {\n")
            each(stmt, body)
            emit("}\n")
        case "dowhile", test, *body:
            emit("do {\n")
            each(stmt, body)
            emit("} while (")
            expr(test)
            emit(");\n")
        case "if", test, yes, no:
            emit("if (")
            expr(test)
            emit(") {\n")
            each(stmt, yes)
            emit("} else {\n")
            each(stmt, no)
            emit("}\n")
        case "if", test, yes:
            emit("if (")
            expr(test)
            emit(") {\n")
            each(stmt, yes)
            emit("}\n")
        case "assert", x:
            emit("assert ")
            expr(x)
            emit(";\n")
        case ".var", modifiers, t, name, val:
            emit(modifiers)
            emit(" ")
            emit(t)
            emit(" ")
            emit(name)
            emit("=")
            expr(val)
            emit(";\n")
        case ".class", modifiers, name, params, *decls:
            emit(modifiers)
            emit(" class ")
            emit(name)
            emit("{\n")
            each(stmt, decls)
            emit("}\n")
        case "fn", modifiers, t, name, params, *body:
            emit(modifiers)
            emit(" ")
            emit(t)
            emit(" ")
            emit(name)
            emit("(")
            emit(params, ",")
            emit(") {\n")
            each(stmt, body)
            emit("}\n")
        case _:
            expr(a)
            emit(";\n")


stmt(global1)

emit('@SuppressWarnings("unchecked")\n')
stmt(program)
