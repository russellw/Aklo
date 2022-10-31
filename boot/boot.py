import inspect
import os
import subprocess
import sys
import textwrap


def each(f, s):
    for a in s:
        f(a)


def eachr(f, a):
    match a:
        case [*_]:
            for b in a:
                eachr(f, b)
    f(a)


def mapr(f, a):
    match a:
        case [*_]:
            a = [mapr(f, b) for b in a]
    b = f(a)
    if b is None:
        return a
    return b


def partition(f, s):
    q = [a for a in s if f(a)]
    r = [a for a in s if not f(a)]
    return q, r


def show(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    sys.stderr.write(f"{info.filename}:{info.function}:{info.lineno}: {a}\n")


# in the bootstrap compiler, strings substitute for symbols
syms = {}


def gensym(s):
    s = "_" + s
    if s not in syms:
        syms[s] = 0
        return s
    i = syms[s] + 1
    syms[s] = i
    return s + str(i)


# library files need to be read from where the compiler is
here = os.path.dirname(os.path.realpath(__file__))


# parser
modules = {}


def isidstart(c):
    return c.isalpha() or c in ("_", "$")


def isidpart(c):
    return isidstart(c) or c.isdigit()


def unesc(s):
    return s.encode("utf-8").decode("unicode_escape")


def quotesym(s):
    return "intern", ["List.of"] + [ord(c) for c in s]


def parse(name, fil):
    if name in modules:
        return

    text = open(fil).read()
    ti = 0
    line = 1

    dentc = 0
    cols = [0]
    dedents = 0

    tok = 0

    def err(msg):
        raise Exception(f"{fil}:{line}: {msg}")

    def errtok(msg):
        nonlocal line
        if tok == "\n":
            line -= 1
        err(f"{repr(tok)}: {msg}")

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
                while text[ti] in ("\t", " "):
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

            # word
            if isidstart(text[ti]):
                while isidpart(text[ti]):
                    ti += 1
                tok = text[i:ti]
                return

            # hexadecimal numbers are a separate case because they may contain 'e'
            if text[ti : ti + 2].lower() == "0x":
                while isidpart(text[ti]):
                    ti += 1
                tok = text[i:ti]
                return

            # other number
            if text[ti].isdigit() or text[ti] == "." and text[ti + 1].isdigit():
                while isidpart(text[ti]):
                    ti += 1
                if text[ti] == ".":
                    ti += 1
                    while isidpart(text[ti]):
                        ti += 1
                    if text[ti - 1].lower() == "e" and text[ti] in ("+", "-"):
                        ti += 1
                        while isidpart(text[ti]):
                            ti += 1
                tok = text[i:ti]
                return

            # multiline string
            if text[ti : ti + 3] == '"""':
                ti += 3
                while text[ti : ti + 3] != '"""':
                    if text[ti] == "\n":
                        line += 1
                    ti += 1
                ti += 3
                tok = text[i:ti]
                tok = '"""' + textwrap.dedent(tok[3:-3]) + '"""'
                return

            # symbol or string
            match text[ti]:
                case "'" | '"':
                    q = text[ti]
                    ti += 1
                    while text[ti] != q:
                        if text[ti] == "\\":
                            ti += 1
                        if text[ti] == "\n":
                            err("unclosed quote")
                        ti += 1
                    ti += 1
                    tok = text[i:ti]
                    return

            # raw string
            if text[ti : ti + 2] == '#"':
                ti += 2
                while text[ti] != '"':
                    if text[ti] == "\n":
                        err("unclosed quote")
                    ti += 1
                ti += 1
                tok = text[i:ti]
                return

            # punctuation
            punct = (
                # 4 characters
                ">>>=",
                # 3 characters
                "**=",
                "//=",
                "<<=",
                ">>=",
                ">>>",
                # 2 characters
                "!=",
                "%=",
                "&&",
                "&=",
                "**",
                "*=",
                "++",
                "+=",
                "--",
                "-=",
                "//",
                "/=",
                "<<",
                "<=",
                "==",
                ">=",
                ">>",
                "@=",
                "^=",
                "|=",
                "||",
            )
            for s in punct:
                if text[ti : ti + len(s)] == s:
                    ti += len(s)
                    tok = s
                    return

            if tok == "[":
                tok = "("
            if tok == "]":
                tok = ")"
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
            errtok(f"expected {repr(s)}")

    def word():
        if isidstart(tok[0]):
            return lex1()
        errtok("expected word")

    # expressions
    def isprimary():
        return isidpart(tok[0]) or tok[0] in ("'", '"', "#")

    def primary():
        # word
        if isidstart(tok[0]):
            return lex1()

        # number
        if tok[0].isdigit():
            match tok[:2].lower():
                case "0x":
                    a = int(tok[2:], 16)
                case "0o":
                    a = int(tok[2:], 8)
                case "0b":
                    a = int(tok[2:], 2)
                case _:
                    a = int(tok)
            lex()
            return a

        # symbol
        if tok.startswith("'"):
            s = unesc(tok[1:-1])
            lex()
            return quotesym(s)

        # multiline string
        if tok.startswith('"""'):
            s = unesc(tok[3:-3])
            lex()
            s = textwrap.dedent(s)
            return ["List.of"] + [ord(c) for c in s]

        # string
        if tok.startswith('"'):
            s = unesc(tok[1:-1])
            lex()
            return ["List.of"] + [ord(c) for c in s]

        # raw string
        if tok.startswith('#"'):
            s = tok[2:-1]
            lex()
            return ["List.of"] + [ord(c) for c in s]

        # bracketed expression or list
        if eat("("):
            if eat(".indent"):
                s = ["List.of"]
                while not eat(".dedent"):
                    s.append(expr())
                    eat(",")
                    expect("\n")
                expect(")")
                return s
            if eat(")"):
                return ["List.of"]
            a = commas()
            expect(")")
            return a

        # none of the above
        errtok("expected expression")

    def postfix():
        a = primary()
        while 1:
            match tok:
                case "(":
                    lex()
                    a = [a]
                    if eat(".indent"):
                        while not eat(".dedent"):
                            a.append(expr())
                            eat(",")
                            expect("\n")
                        expect(")")
                        continue
                    while not eat(")"):
                        a.append(expr())
                        if eat(")"):
                            break
                        expect(",")
                    continue
                case ".":
                    lex()
                    field = word()
                    if a in modules:
                        a = a.title() + "." + field
                    else:
                        a = "get", a, quotesym(field)
                    continue
                case "++" | "--":
                    return "post" + lex1(), a
            if isprimary():
                a = [a, prefix()]
                while eat(","):
                    a.append(prefix())
            return a

    def param():
        return word()

    def params():
        s = []
        match tok:
            case ":" | ".indent":
                pass
            case "(":
                lex()
                if eat(".indent"):
                    while not eat(".dedent"):
                        s.append(param())
                        eat(",")
                        expect("\n")
                    expect(")")
                else:
                    while not eat(")"):
                        s.append(param())
                        if eat(")"):
                            break
                        expect(",")
            case _:
                while 1:
                    s.append(param())
                    if not eat(","):
                        break
        return s

    def prefix():
        match tok:
            case "!" | "~" | "++" | "--":
                return lex1(), prefix()
            case "-":
                lex()
                return "Etc.neg", prefix()
            case "*":
                lex()
                return "...", prefix()
            case "\\":
                s = [lex1(), params()]
                eat(":")
                if not eat("("):
                    s.append(expr())
                    return s
                if eat(".indent"):
                    while not eat(".dedent"):
                        s.append(stmt())
                    expect(")")
                    return s
                if tok != ")":
                    s.append(commas())
                expect(")")
                return s
        return postfix()

    # operator precedence parser
    prec = 99
    ops = {}

    def init(op, left):
        ops[op] = prec, left

    init("!", 1)

    prec -= 1
    init("**", 0)

    prec -= 1
    init("%", 1)
    init("*", 1)
    init("//", 1)
    init("/", 1)

    prec -= 1
    init("+", 1)
    init("-", 1)
    init("@", 1)

    prec -= 1
    init("<<", 1)
    init(">>", 1)
    init(">>>", 1)

    prec -= 1
    init("&", 1)

    prec -= 1
    init("^", 1)

    prec -= 1
    init("|", 1)

    prec -= 1
    init("!=", 1)
    init("<", 1)
    init("<=", 1)
    init("==", 1)
    init(">", 1)
    init(">=", 1)

    prec -= 1
    init("&&", 1)

    prec -= 1
    init("||", 1)

    def infix(prec):
        a = prefix()
        while 1:
            if tok not in ops:
                return a
            prec1, left1 = ops[tok]
            if prec1 < prec:
                return a
            op = lex1()
            b = infix(prec1 + left1)
            match op:
                case "!":
                    a = "Etc.subscript", a, b
                case ">":
                    a = "<", b, a
                case ">=":
                    a = "<=", b, a
                case _:
                    a = op, a, b

    def expr():
        return infix(0)

    # statements
    def commas():
        a = expr()
        if tok != ",":
            return a
        s = ["List.of", a]
        while eat(","):
            match tok:
                case ")" | "\n" | ".indent":
                    pass
                case _:
                    s.append(expr())
        return s

    def assignment():
        a = commas()
        if tok.endswith("="):
            return lex1(), a, assignment()
        return a

    def block(s):
        expect(".indent")
        while not eat(".dedent"):
            s.append(stmt())

    def block1():
        s = []
        block(s)
        return s

    def if1():
        assert tok in ("if", "elif")
        lex()
        s = ["if", expr()]
        eat(":")
        s.append(block1())
        match tok:
            case "elif":
                s.append(if1())
            case "else":
                lex()
                eat(":")
                s.append(block1())
        return s

    def stmt():
        s = [tok]
        match tok:
            case "case":
                lex()
                s.append(commas())
                eat(":")
                expect(".indent")
                while not eat(".dedent"):
                    patterns = [commas()]
                    while eat("\n"):
                        patterns.append(commas())
                    body = block1()
                    for pattern in patterns:
                        s.append((pattern, *body))
                return s
            case "assert":
                lex()
                s.append(expr())
                expect("\n")
                return s
            case "dowhile" | "while":
                lex()
                s.append(expr())
                eat(":")
                block(s)
                return s
            case "for":
                lex()
                s.append(word())
                expect(":")
                s.append(commas())
                eat(":")
                block(s)
                return s
            case "fn":
                lex()
                s.append(word())
                s.append(params())
                eat(":")
                block(s)
                return s
            case "if":
                return if1()
            case "nonlocal":
                lex()
                s.append(word())
                expect("\n")
                return s
            case "return":
                lex()
                if eat("\n"):
                    return "return", 0
                s.append(commas())
                expect("\n")
                return s
        a = assignment()
        if eat(":"):
            return ":", a, stmt()
        expect("\n")
        return a

    # top level
    lex()
    eat("\n")

    # imports
    while eat("import"):
        name1 = word()
        parse(name1, os.path.join(here, "..", "src", name1 + ".k"))
        expect("\n")

    # module
    s = []
    while tok != ".dedent":
        s.append(stmt())
    modules[name] = s


parse("global", os.path.join(here, "..", "src", "global.k"))
parse("program", sys.argv[1])


# output
sys.stdout.write(open(os.path.join(here, "prefix.java")).read())


def localvars(params, a):
    nonlocals = set()

    # dict keeps deterministic order
    r = {x: 1 for x in params}

    def lhs(a):
        match a:
            case "..." | "List.of":
                pass
            case _:
                if isinstance(a, str) and a not in nonlocals:
                    r[a] = 1

    def f(a):
        match a:
            case "nonlocal", x:
                nonlocals.add(x)
            case (
                ("++", x)
                | ("--", x)
                | ("post++", x)
                | ("post--", x)
                | ("+=", x, _)
                | ("=", x, _)
                | ("-=", x, _)
            ):
                eachr(lhs, x)

    eachr(f, a)
    return list(r)


def separate(f, s, separator):
    more = 0
    for a in s:
        if more:
            emit(separator)
        more = 1
        f(a)


def emit(a, separator=" "):
    if isinstance(a, int):
        a = str(a)
    if not isinstance(a, str):
        separate(emit, a, separator)
        return
    sys.stdout.write(a)


globals1 = set()
for a in modules["global"]:
    match a:
        case "fn", name, params, *body:
            globals1.add(name)


def expr(a):
    show(a)
    match a:
        case "argv":
            emit("Etc.argv")
        case ("post++", x) | ("post--", x):
            expr(x)
            emit(a[0][4:])
        case "\\", params, *body:
            fcast(params)
            emit("(")
            emit(params, ",")
            emit(") ->")
            if len(body) == 1:
                expr(body[0])
                return
            emit("{\n")
            each(stmt, body)
            emit("}")
        case "//", x, y:
            expr(x)
            emit("/")
            expr(y)
        case "range", x:
            expr(("range", 0, x))
        case "<", x, y:
            expr(("Etc.lt", x, y))
        case "<=", x, y:
            expr(("Etc.le", x, y))
        case ("+=", x, y) | ("-=", x, y) | ("@=", x, y):
            expr(("=", x, (a[0][0], x, y)))
        case "~", x:
            expr(("Etc.not", x))
        case "&", x, y:
            expr(("Etc.and", x, y))
        case "|", x, y:
            expr(("Etc.or", x, y))
        case "^", x, y:
            expr(("Etc.xor", x, y))
        case "<<", x, y:
            expr(("Etc.shl", x, y))
        case ">>", x, y:
            expr(("Etc.shr", x, y))
        case ">>>", x, y:
            expr(("Etc.ushr", x, y))
        case "+", x, y:
            expr(("Etc.add", x, y))
        case "*", x, y:
            expr(("Etc.mul", x, y))
        case "-", x, y:
            expr(("Etc.sub", x, y))
        case "==", x, y:
            expr(("Objects.equals", x, y))
        case "@", x, y:
            expr(("Etc.cat", x, y))
        case "!=", x, y:
            expr(("!", ("==", x, y)))
        case "intern", x:
            expr(("Sym.intern", x))
        case "gensym",:
            expr(("new Sym",))
        case (
            ("len", *args)
            | ("get", *args)
            | ("exit", *args)
            | ("slice", *args)
            | ("range", *args)
            | ("writestream", *args)
            | ("readfile", *args)
            | ("isnum", *args)
            | ("issym", *args)
            | ("islist", *args)
            | ("str", *args)
        ):
            expr(("Etc." + a[0], *args))
        case ("map", f, s) | ("filter", f, s):
            emit("Global.")
            emit(a[0])
            emit("(")
            if f[0] == "\\":
                expr(f)
            else:
                fcast(1)
                if f in globals1:
                    emit("Global")
                else:
                    emit(moduleName)
                emit("::")
                expr(f)
            emit(",")
            expr(s)
            emit(")")
        case "apply", f, s:
            expr(f)
            emit("(")
            expr(s)
            emit(".toArray())")
        case "stdout":
            emit("System.out")
        case "stderr":
            emit("System.err")
        case ".run", x:
            expr(x)
            emit(".run()")
        case "List.of", *args:
            if args:
                match args[-1]:
                    case "...", s:
                        emit("Etc.cons(")
                        for x in args[:-1]:
                            expr(x)
                            emit(",")
                        expr(s)
                        emit(")")
                        return
            emit("List.of(")
            separate(expr, args, ",")
            emit(")")
        case f, *args:
            if f[0].isalpha():
                if len(f) == 1:
                    print(f"((Function<List<Object>, Object>){f})")
                elif f in globals1:
                    print(f"new global.{f}().apply")
                else:
                    print(f"new {f}().apply")
                emit(".apply(List.of(")
                separate(expr, args, ",")
                emit("))")
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


def var(x):
    match x:
        case "i" | "j" | "k":
            emit("int")
        case _:
            emit("Object")
    emit(" ")
    emit(x)
    emit("= 0;\n")


def stmt(a):
    show(a)
    match a:
        case "for", x, s, *body:
            emit("for (var ")
            expr(x)
            emit(": (List)")
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
        case "{", *s:
            emit("{\n")
            each(stmt, s)
            emit("}\n")
        case ("break", label) | ("continue", label):
            emit(a[0])
            emit(" ")
            emit(label)
            emit(";\n")
        case ":", label, loop:
            emit(label)
            emit(":")
            stmt(loop)
        case "case", x, *cases:
            outerLabel = gensym("outer")
            innerLabel = gensym("inner")
            x1 = gensym("x")

            q = ["dowhile", "false", ("=", x1, ir(x))]
            for pattern, *body in cases:
                body = list(map(ir, body))
                r = ["dowhile", "false"]

                def assign(pattern, x):
                    if isinstance(pattern, int):
                        r.append(("if", ("!=", x, pattern), [("break", innerLabel)]))
                        return
                    match pattern:
                        case "intern", ("List.of", *_):
                            r.append(
                                ("if", ("!=", x, pattern), [("break", innerLabel)])
                            )
                        case "List.of", *params:
                            args = gensym("x")
                            r.append(("=", args, x))
                            r.append(
                                ("if", ("!", ("islist", args)), [("break", innerLabel)])
                            )
                            r.append(
                                (
                                    "if",
                                    ("<", ("len", args), len(params)),
                                    [("break", innerLabel)],
                                )
                            )
                            for i in range(len(params)):
                                match params[i]:
                                    case "...", y:
                                        assign(y, ("from", args, i))
                                    case y:
                                        assign(y, ("Etc.subscript", args, i))
                        case "_":
                            pass
                        case _:
                            r.append(("=", pattern, x))

                assign(pattern, x1)
                r.extend(body)
                r.append(("break", outerLabel))
                q.append((":", innerLabel, r))
            return ":", outerLabel, q
        case "=", pattern, x:

            def assign(pattern, x):
                match pattern:
                    case "List.of", *params:
                        x1 = gensym("x")
                        stmt(("=", x1, x))
                        for i in range(len(params)):
                            match params[i]:
                                case "...", y:
                                    assign(y, ("from", x1, i))
                                case y:
                                    assign(y, ("Etc.subscript", x1, i))
                    case _:
                        expr(("=", pattern, x))
                        print(";")

            assign(pattern, x)
        case "nonlocal", _:
            pass
        case _:
            expr(a)
            emit(";\n")


def fn(name, params, body):
    print(f"class {name} implements Function<List<Object>, Object> {{")

    # local functions
    r = []
    for a in body:
        match a:
            case "fn", name1, params1, *body1:
                fn(name1, params1, body1)
            case _:
                r.append(a)
    body = r

    # local variables
    each(var, localvars(params, body))

    # if the trailing return is implicit, make it explicit
    if not body:
        body = [0]
    a = body[-1]
    match a:
        case ("assert", _) | ("for", *_):
            body.append(("return", 0))
        case "return", _:
            pass
        case _:
            body[-1] = "return", a

    # body
    print("Object apply(List<Object> args) {")
    for i in range(len(params)):
        print(f"{params[i]} = args.get({i});")
    each(stmt, body)
    print("}")

    print("}")


for moduleName, module in modules.items():
    print('@SuppressWarnings("unchecked")')
    print(f"class {moduleName} {{")

    # local functions
    r = []
    for a in module:
        match a:
            case "fn", name, params, *body:
                fn(name, params, body)
            case _:
                r.append(a)
    module = r

    # local variables
    for x in localvars([], module):
        print("static")
        var(x)

    # body
    print("static void run() {")
    each(stmt, module)
    print("}")

    print("}")
