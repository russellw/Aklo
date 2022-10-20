import inspect
import os
import subprocess
import sys


def each(f, a):
    for b in a:
        f(b)


def show(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    print(f"{info.filename}:{info.function}:{info.lineno}: {a}")


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
            return unquote(lex1())

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
                    a = "get", a, ("'", word())
                    continue
                case "++" | "--":
                    return "post" + lex1(), a
            if isPrimary():
                a = [a, expr()]
                while eat(","):
                    a.append(expr())
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
                return "neg", prefix()
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
            a = op, a, b

    def expr():
        return infix(0)

    # statements
    def tuple1():
        a = expr()
        if tok != ",":
            return a
        a = [".list", a]
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
        a = [".do"]
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
                    return a
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


program = parse(sys.argv[1])


# intermediate representation
def ir(body):
    fil = 0
    line = 0

    vs = {}
    fs = {}
    code = []

    def term(loop, a):
        nonlocal fil
        nonlocal line

        def rec(a):
            return term(loop, a)

        match a:
            case "'", x:
                x = [".list"] + list(bytes(x, "utf8"))
                return rec(("intern", x))
            case "\\", params, body:
                name = gensym("lambda")
                return rec(("fn", name, params, body))
            case "fn", name, params, *body:
                if name in fs:
                    raise Exception(name)
                fs[name] = fil, line, name, params, ir(body)
                return name
            case "=", name, x:
                if name not in vs:
                    vs[name] = fil, line, name
                x = rec(x)
                code.append(("=", name, x))
                return name
        return 0


def ir(a):
    match a:
        case "while", test, *body:
            test = ir(test)
            body = list(map(ir, body))
            return "while", test, *body
        case "fn", name, params, *body:
            modifiers = []
            t = "Object"
            params = [("Object", x) for x in params]
            body = list(map(ir, body))
            return "fn", modifiers, t, name, params, *body
        case _, *_:
            return list(map(ir, a))
    return a


program = "fn", "Main1", [], *program
program = ir(program)


# output
here = os.path.dirname(os.path.realpath(__file__))
lib = os.path.join(here, "boot.java")

outf = open("a.java", "w")
outf.write(open(lib).read())


def emit(a):
    if isinstance(a, int):
        a = str(a)
    if a.endswith("?"):
        a = a[:-1] + "p"
    outf.write(a)


def commas(f, a):
    more = 0
    for b in a:
        if more:
            emit(",")
        more = 1
        f(b)


def expr(a):
    match a:
        case "//", x, y:
            expr(x)
            emit("/")
            expr(y)
        case "+", x, y:
            expr(("add", x, y))
        case "-", x, y:
            expr(("sub", x, y))
        case "==", x, y:
            expr(("eq", x, y))
        case "[", x, y:
            expr(("subscript", x, y))
        case f, *args:
            if f[0].isalpha():
                emit(f)
                emit("(")
                commas(expr, args)
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
        case _:
            expr(a)
            emit(";\n")


stmt(program)
