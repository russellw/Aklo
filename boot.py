import inspect
import os
import subprocess
import sys


def show(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    print(f"{info.filename}:{info.function}:{info.lineno}: {a}")


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
            return [".list"] + [ord(c) for c in s]

        # parenthesized expression
        if eat("("):
            a = tuple1()
            expect(")")
            return a

        # list
        if eat("["):
            a = [".list"]
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
            a.append((".line", fil, line))
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
                msg = f"{fil}:{line}: assert failed\n"
                a.append(expr())
                a.append(msg)
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
            case "nonlocal" | ":" | "goto":
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
        a.append((".line", fil, line))
        a.append(stmt())
    return a


# intermediate representation
syms = {}


def gensym(s):
    s = "_" + s
    if s not in syms:
        syms[s] = 0
        return s
    i = syms[s] + 1
    syms[s] = i
    return s + str(i)


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
            case ".line", fil1, line1:
                fil = fil1
                line = line1
                code.append(a)
            case "assert", test, msg:
                msg = [".list"] + list(bytes(msg, "utf8"))
                rec(("if", ("!", test), ("eprint", msg)))
            case "||", x, y:
                r = gensym("or")
                return rec((".do", ("=", r, x), ("if", r, r, y)))
            case "&&", x, y:
                r = gensym("and")
                return rec((".do", ("=", r, x), ("if", r, y, r)))
            case "if", test, yes, no:
                yesLabel = gensym("ifYes")
                noLabel = gensym("ifNo")
                afterLabel = gensym("ifAfter")
                r = gensym("ifResult")

                # test
                code.append(("if", rec(test), yesLabel))
                code.append(("goto", noLabel))

                # yes
                code.append((":", yesLabel))
                rec(("=", r, yes))
                code.append(("goto", afterLabel))

                # no
                code.append((":", noLabel))
                rec(("=", r, no))

                # after
                code.append((":", afterLabel))
                return r
            case "if", test, yes:
                return rec(("if", test, yes, 0))
            case "!", x:
                return rec(("if", x, 0, 1))
            case "dowhile", test, *body:
                bodyLabel = gensym("dowhileBody")
                testLabel = gensym("dowhileTest")
                afterLabel = gensym("dowhileAfter")
                loop = testLabel, afterLabel

                # body
                code.append((":", bodyLabel))
                block(loop, body)

                # test
                code.append((":", testLabel))
                rec(("if", test, ("goto", bodyLabel)))

                # after
                code.append((":", afterLabel))
            case "while", test, *body:
                bodyLabel = gensym("whileBody")
                testLabel = gensym("whileTest")
                afterLabel = gensym("whileAfter")
                loop = testLabel, afterLabel

                code.append(("goto", testLabel))

                # body
                code.append((":", bodyLabel))
                block(loop, body)

                # test
                code.append((":", testLabel))
                rec(("if", test, ("goto", bodyLabel)))

                # after
                code.append((":", afterLabel))
            case "for", x, s, *body:
                s1 = gensym("s")
                n = gensym("n")
                i = gensym("i")
                return rec(
                    (
                        ".do",
                        ("=", s1, s),
                        ("=", n, ("len", s1)),
                        (
                            "while",
                            ("<", i, n),
                            ("=", x, ("[", s1, ("post++", i))),
                            *body,
                        ),
                    )
                )
            case "continue":
                code.append(("goto", loop[0]))
            case "break":
                code.append(("goto", loop[1]))
            case "return":
                code.append(("return", 0))
            case ".do", *a:
                return block(loop, a)
            case ("goto", _) | (":", _):
                code.append(a)
            case ("print", *args) | ("eprint", *args):
                args = map(rec, args)
                a = a[0], *args
                code.append(a)
            case f, *args:
                args = map(rec, args)
                a = f, *args
                r = gensym("r")
                assert r not in vs
                vs[r] = fil, line, r
                code.append(("=", r, a))
                return r
            case _:
                if isinstance(a, str) or isinstance(a, int):
                    return a
                raise Exception(a)
        return 0

    def block(loop, a):
        r = 0
        for b in a:
            r = term(loop, b)
        return r

    code.append(("return", block(None, body)))
    return list(vs.values()), list(fs.values()), code


fil = sys.argv[1]
program = parse(fil)
program = ir(program)


# output
here = os.path.dirname(os.path.realpath(__file__))
lib = os.path.join(here, "boot.cs")

outf = open("a.cs", "w")
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
        case ".list", *args:
            expr(["ls"] + args)
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
        case "=", name, x:
            emit(name + "=")
            expr(x)
            emit(";\n")
        case ":", label:
            emit(f"{label}:\n")
        case "goto", label:
            emit(f"goto {label};\n")
        case "if", test, label:
            emit(f"if (truth({test})) goto {label};\n")
        case ".line", fil, line:
            # emit(f'#line {line} "{fil}"\n')
            pass
        case _:
            expr(a)
            emit(";\n")


def var(a):
    fil, line, name = a
    stmt((".line", fil, line))
    emit(f"object {name} = 0;\n")


def fn(a):
    fil, line, name, params, vs, fs, code = a
    stmt((".line", fil, line))
    emit(f"object {name}(")
    commas(emit, ("object " + x for x in params))
    emit(") {\n")
    for a in vs:
        var(a)
    for a in code:
        stmt(a)
    emit("}\n")


program = (fil, 1, "main", ()) + program
fn(program)
outf.close()


# compile
p = subprocess.Popen(
    ("csc", "-debug", "-nologo", "a.cs"),
    encoding="utf8",
    stdout=subprocess.PIPE,
)
outs, errs = p.communicate()
for s in outs.split("\n")[:10]:
    print(s)
