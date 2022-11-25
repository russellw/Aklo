import argparse
import inspect
import os
import subprocess
import sys


parser = argparse.ArgumentParser(description="Aklo bootstrap compiler")
parser.add_argument("file")
args = parser.parse_args()


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
syms = 0


def gensym():
    global syms
    syms += 1
    return f"_{syms}"


# library files need to be read from where the compiler is
here = os.path.dirname(os.path.realpath(__file__))


# parser
modules = {}


def isidstart(c):
    return c.isalpha() or c in "_$"


def isidpart(c):
    return isidstart(c) or c.isdigit() or c == "?"


def unesc(s):
    return s.encode("utf-8").decode("unicode_escape")


def quotesym(s):
    return "intern", ["List.of"] + [ord(c) for c in s]


def parse(modname, file):
    # TODO: enforce initialization of local variables?
    if modname in modules:
        return

    text = open(file).read()
    i = 0
    line = 1

    dentc = 0
    cols = [0]
    dedents = 0

    tok = 0

    # google-java-format sometimes doesn't like comments with backslashes
    # which will occur in Windows filenames
    # the easiest solution is to just convert to / at the start
    file = file.replace("\\", "/")

    def err(msg):
        raise Exception(f"{file}:{line}: {msg}")

    def errtok(msg):
        nonlocal line
        if tok == "\n":
            line -= 1
        err(f"{repr(tok)}: {msg}")

    # tokenizer
    def lex():
        nonlocal dedents
        nonlocal dentc
        nonlocal i
        nonlocal line
        nonlocal tok

        # a single newline can produce multiple dedent tokens
        if dedents:
            dedents -= 1
            tok = ".dedent"
            return

        while i < len(text):
            j = i

            # the simplest tokens are just one character
            tok = text[i]

            # newline
            if tok == "\n":
                # next line
                i += 1
                if i == len(text):
                    return
                line += 1

                # measure indent
                col = 0
                while text[i] in "\t ":
                    if text[i] != dentc and dentc:
                        err("indented with tabs and spaces in same file")
                    dentc = text[i]
                    i += 1
                    col += 1

                # nothing important on this line, keep going
                if text[i] in "\n;{":
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
            if tok.isspace():
                i += 1
                continue

            # comment
            if tok == ";":
                while text[i] != "\n":
                    i += 1
                continue
            if tok == "{":
                i += 1
                line1 = line
                while text[i] != "}":
                    if i == len(text):
                        line = line1
                        err("unclosed block comment")
                    if text[i] == "\n":
                        line += 1
                    i += 1
                i += 1
                continue

            # symbol or string
            match tok:
                case "'" | '"':
                    i += 1
                    while text[i] != tok:
                        if text[i] == "\\":
                            i += 1
                        if text[i] == "\n":
                            err("unclosed quote")
                        i += 1
                    i += 1
                    tok = text[j:i]
                    return

            # raw string
            if text[i : i + 2] == 'r"':
                i += 2
                while text[i] != '"':
                    if text[i] == "\n":
                        err("unclosed quote")
                    i += 1
                i += 1
                tok = text[j:i]
                return

            # word
            if isidstart(tok):
                while isidpart(text[i]):
                    i += 1
                tok = text[j:i]
                return

            # hexadecimal numbers are separate because they may contain 'e'
            if text[i : i + 2].lower() == "0x":
                while isidpart(text[i]):
                    i += 1
                tok = text[j:i]
                return

            # other number
            if tok.isdigit() or tok == "." and text[i + 1].isdigit():
                while isidpart(text[i]):
                    i += 1
                if text[i] == ".":
                    i += 1
                    while isidpart(text[i]):
                        i += 1
                    if text[i - 1].lower() == "e" and text[i] in "+-":
                        i += 1
                        while isidpart(text[i]):
                            i += 1
                tok = text[j:i]
                return

            # punctuation
            punct = (
                "!=",
                ":=",
                "**",
                "++",
                "+=",
                "--",
                "-=",
                "//",
                "<=",
                "==",
                ">=",
                "@=",
                "<<",
            )
            for s in punct:
                if text[i : i + len(s)] == s:
                    i += len(s)
                    tok = s
                    return
            i += 1
            return

        # end of file
        tok = ".dedent"

    def lex1():
        r = tok
        lex()
        return r

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
            return lex1().replace("?", "p")
        errtok("expected word")

    # expressions
    def primary():
        # symbol
        if tok.startswith("'"):
            s = unesc(tok[1:-1])
            lex()
            return quotesym(s)

        # string
        if tok.startswith('"'):
            s = unesc(tok[1:-1])
            lex()
            return ["List.of"] + [ord(c) for c in s]

        # raw string
        if tok.startswith('r"'):
            s = tok[2:-1]
            lex()
            return ["List.of"] + [ord(c) for c in s]

        # word
        if eat("ctreadfiles"):
            expect("(")

            # in the full language, the first argument is a filter function
            # but in compiling the main compiler, this function can be ignored
            # because we know it will look for *.k
            expr()
            expect(",")

            # the second argument is the path relative to the current source file
            if tok[0] != '"':
                errtok("expected string")
            dir1 = os.path.dirname(file) + "/" + unesc(lex1()[1:-1])
            expect(")")
            return "ctreadfiles", dir1
        if isidstart(tok[0]):
            return word()

        # number
        if tok[0].isdigit():
            s = tok.replace("_", "")
            match s[:2].lower():
                case "0b":
                    a = int(s[2:], 2)
                case "0o":
                    a = int(s[2:], 8)
                case "0x":
                    a = int(s[2:], 16)
                case _:
                    a = int(s)
            lex()
            return a

        # bracketed expression
        if eat("("):
            a = commas()
            expect(")")
            return a

        # list
        if eat("["):
            r = ["List.of"]
            if eat(".indent"):
                while not eat(".dedent"):
                    r.append(commas())
                    expect("\n")
                expect("]")
                return r
            while not eat("]"):
                r.append(expr())
                if eat("]"):
                    break
                expect(",")
            return r

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
                            a.append(commas())
                            expect("\n")
                        expect(")")
                        continue
                    while not eat(")"):
                        a.append(expr())
                        if eat(")"):
                            break
                        expect(",")
                    continue
                case "[":
                    lex()
                    a = "subscript", a, expr()
                    expect("]")
                case "++" | "--":
                    return "post" + lex1(), a
                case ".":
                    lex()
                    field = word()
                    if a in modules:
                        a += "." + field
                    else:
                        a = "get", a, quotesym(field)
                    continue
            return a

    def params():
        r = []
        match tok:
            case "(":
                lex()
                if eat(".indent"):
                    while not eat(".dedent"):
                        r.append(word())
                        expect("\n")
                    expect(")")
                else:
                    while not eat(")"):
                        r.append(word())
                        if eat(")"):
                            break
                        expect(",")
            case ".indent":
                0
            case _:
                # TODO should the brackets be optional?
                while 1:
                    r.append(word())
                    if not eat(","):
                        break
        return r

    def prefix():
        match tok:
            case "!" | "++" | "-" | "--" | "@":
                return lex1(), prefix()
            case "\\":
                line1 = line
                r = [lex1()]

                # parameters
                r.append(params())

                # body
                expect("(")
                r.append((".loc", file, line1, "\\"))
                if eat(".indent"):
                    while not eat(".dedent"):
                        r.append((".loc", file, line, "\\"))
                        r.append(stmt("\\"))
                else:
                    r.append(("^", commas()))
                expect(")")

                return r
        return postfix()

    # operator precedence parser
    prec = 99
    ops = {}

    def mkop(op, left):
        ops[op] = prec, left

    mkop("**", 0)

    prec -= 1
    mkop("%", 1)
    mkop("*", 1)
    mkop("/", 1)
    mkop("//", 1)

    prec -= 1
    mkop("+", 1)
    mkop("-", 1)
    mkop("@", 1)

    prec -= 1
    mkop("!=", 1)
    mkop("<", 1)
    mkop("<=", 1)
    mkop("==", 1)
    mkop(">", 1)
    mkop(">=", 1)

    prec -= 1
    mkop("&", 1)

    prec -= 1
    mkop("|", 1)

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
                case ">":
                    a = "<", b, a
                case ">=":
                    a = "<=", b, a
                case _:
                    a = op, a, b

    def expr():
        return infix(1)

    # statements
    def commas():
        a = expr()
        if tok != ",":
            return a
        r = ["List.of", a]
        while eat(","):
            r.append(expr())
        return r

    def assignment():
        # TODO inline
        a = commas()
        match tok:
            case "=" | "+=" | "-=" | "@=" | "<<" | ":=":
                return lex1(), a, commas()
        return a

    def block(fname):
        expect(".indent")
        r = []
        while not eat(".dedent"):
            r.append((".loc", file, line, fname))
            r.append(stmt(fname))
        return r

    def if1(fname):
        assert tok in ("if", "elif")
        lex()
        r = ["if", expr()]
        r.append(block(fname))
        match tok:
            case "elif":
                r.append([if1(fname)])
            case "else":
                lex()
                r.append(block(fname))
        return r

    def stmt(fname):
        r = [tok]
        match tok:
            case "assert":
                r.append(file)
                r.append(line)
                r.append(fname)
                lex()
                x = expr()
                r.append(str(x))
                r.append(x)
                expect("\n")
                return r
            case "show":
                r.append(file)
                r.append(line)
                r.append(fname)
                lex()
                x = commas()
                r.append(str(x))
                r.append(x)
                expect("\n")
                return r
            case "case":
                lex()
                r.append(commas())
                expect(".indent")
                while not eat(".dedent"):
                    patterns = [commas()]
                    while eat("\n"):
                        patterns.append(commas())
                    body = block(fname)
                    for pattern in patterns:
                        r.append((pattern, *body))
                return r
            case "dowhile" | "while":
                lex()
                r.append(expr())
                r.extend(block(fname))
                return r
            case "for":
                lex()
                r.append(word())
                expect(":")
                r.append(commas())
                r.extend(block(fname))
                return r
            case "fn":
                line1 = line
                lex()

                # name
                fname = word()
                r.append(fname)

                # parameters
                r.append(params())

                # body
                expect(".indent")
                r.append((".loc", file, line1, fname))
                while not eat(".dedent"):
                    r.append((".loc", file, line, fname))
                    r.append(stmt(fname))

                return r
            case "if":
                return if1(fname)
            case "break" | "continue":
                lex()
                if eat("\n"):
                    return r[0]
                r.append(word())
                expect("\n")
                return r
            case "^":
                lex()
                if eat("\n"):
                    return "^", 0
                r.append(commas())
                expect("\n")
                return r
            case "throw":
                lex()
                r.append(commas())
                expect("\n")
                return r
            case "tron":
                lex()
                if eat("\n"):
                    return r
                while 1:
                    r.append(word())
                    if not eat(","):
                        break
                expect("\n")
                return r
        a = assignment()
        if eat(":"):
            return ":", a, stmt(fname)
        expect("\n")
        return a

    # top level
    lex()
    eat("\n")

    # imports
    while eat("import"):
        name = word()
        parse(name, f"{here}/../aklo/{name}.k")
        expect("\n")

    # module
    r = []
    while tok != ".dedent":
        r.append((".loc", file, line, modname))
        r.append(stmt(modname))
    modules[modname] = r


parse("global", f"{here}/../aklo/global.k")
parse("program", args.file)


# output
sys.stdout.write(open(here + "/prefix.java").read())


# expressions
def pargs(s):
    print("(")
    more = 0
    for a in s:
        if more:
            print(",")
        more = 1
        expr(a)
    print(")")


def truth(a):
    print("Etc.truth(")
    expr(a)
    print(")")


hofs = set()
for name, module in modules.items():
    for a in module:
        match a:
            case "fn", fname, ["f", *_], *_:
                hofs.add(fname)


def expr(a):
    match a:
        case "ctreadfiles", dir1:
            print(f'Etc.ctreadfiles("{dir1}")')
        case "args" | "windowsp":
            print("Etc." + a)
        case ("++", x) | ("--", x):
            print(a[0] + x)
        case ("post++", x) | ("post--", x):
            print(x + a[0][4:])
        case "\\", params, *body:
            fref(a)
        case "//", x, y:
            expr(x)
            print("/")
            expr(y)
        case "range", x:
            expr(("range", 0, x))
        case "<", *s:
            print("Etc.lt")
            pargs(s)
        case "<=", *s:
            print("Etc.le")
            pargs(s)
        case ("|", x, y) | ("&", x, y):
            truth(x)
            print(a[0] * 2)
            truth(y)
        case "!", x:
            print("!")
            truth(x)
        case "-", x:
            print("Etc.neg")
            pargs([x])
        case "-", x, y:
            print("Etc.sub")
            pargs((x, y))
        case "%", *s:
            print("Etc.rem")
            pargs(s)
        case "+", *s:
            print("Etc.add")
            pargs(s)
        case "*", *s:
            print("Etc.mul")
            pargs(s)
        case "==", *s:
            print("Objects.equals")
            pargs(s)
        case "!=", *s:
            print("!Objects.equals")
            pargs(s)
        case "@", *s:
            print("Etc.cat")
            pargs(s)
        case "floatp", _:
            print("false")
        case "intern", *s:
            print("Sym.intern")
            pargs(s)
        case "gensym", *s:
            print("new Sym")
            pargs(s)
        case (
            ("len", *s)
            | ("get", *s)
            | ("subscript", *s)
            | ("exit", *s)
            | ("slice", *s)
            | ("range", *s)
            | ("writestream", *s)
            | ("readfile", *s)
            | ("intp", *s)
            | ("symp", *s)
            | ("listp", *s)
            | ("dirp", *s)
            | ("str", *s)
            | ("shl", *s)
            | ("shr", *s)
            | ("bitnot", *s)
            | ("bitand", *s)
            | ("listdir", *s)
            | ("bitor", *s)
            | ("bitxor", *s)
        ):
            print("Etc." + a[0])
            pargs(s)
        case "apply", f, s:
            fref(f)
            print(".apply(")
            expr(s)
            print(")")
        case "=", x, y:
            print(x + "=")
            expr(y)
        case "stdout":
            print("System.out")
        case "stderr":
            print("System.err")
        case "List.of", *s:
            if s:
                match s[-1]:
                    case "@", t:
                        print("Etc.cons")
                        pargs(s[:-1] + [t])
                        return
            print("List.of")
            pargs(s)
        case f, g, *s:
            fref(f)
            print(".apply(List.of(")
            if f in hofs:
                fref(g)
            else:
                expr(g)
            for x in s:
                print(",")
                expr(x)
            print("))")
        case f, *s:
            fref(f)
            print(".apply(List.of")
            pargs(s)
            print(")")
        case _:
            print(a)


# statements
def isrest(s):
    if s:
        match s[-1]:
            case "@", _:
                return 1


def tmp(a):
    r = gensym()
    print(f"Object {r} = ")
    expr(a)
    print(";")
    return r


def checkcase(label, pattern, x):
    if isinstance(pattern, int):
        print("if (!Objects.equals(")
        expr(x)
        print(f", {pattern})) break {label};")
        return
    match pattern:
        case "intern", ("List.of", *_):
            print("if (!Objects.equals(")
            expr(x)
            print(",")
            expr(pattern)
            print(f")) break {label};")
        case "List.of", *s:
            x = tmp(x)
            print(f"if (!({x} instanceof List)) break {label};")
            if isrest(s):
                n = len(s) - 1
                print(f"if (((List<Object>){x}).size() < {n}) break {label};")
                for i in range(n):
                    checkcase(label, s[i], ("subscript", x, i))
                checkcase(label, s[n][1], ("drop", n, x))
                return
            n = len(s)
            print(f"if (((List<Object>){x}).size() != {n}) break {label};")
            for i in range(n):
                checkcase(label, s[i], ("subscript", x, i))


def assignconst(pattern, x):
    print("assert Objects.equals")
    pargs((pattern, x))
    print(";")


def assign(pattern, x):
    if isinstance(pattern, int):
        assignconst(pattern, x)
        return
    match pattern:
        case "intern", ("List.of", *_):
            assignconst(pattern, x)
        case "List.of", *s:
            x = tmp(x)
            if isrest(s):
                n = len(s) - 1
                for i in range(n):
                    assign(s[i], ("subscript", x, i))
                assign(s[n][1], ("drop", n, x))
                return
            n = len(s)
            for i in range(n):
                assign(s[i], ("subscript", x, i))
        case "_":
            0
        case "i" | "j" | "k":
            print(pattern + "= (int)")
            expr(x)
            print(";")
        case _:
            print(pattern + "=")
            expr(x)
            print(";")


currentfile = 0
currentline = 0
currentfname = 0


def stmt(a):
    global currentfile
    global currentline
    global currentfname
    match a:
        case "<<", x, y:
            print(x + "=")
            print("Etc.cat1")
            pargs((x, y))
            print(";")
        case ("+=", x, y) | ("-=", x, y) | ("@=", x, y):
            print(x + "=")
            expr((a[0][0], x, y))
            print(";")
        case "for", x, s, *body:
            print(f"for (var {x}: (List)")
            expr(s)
            print(") {")
            each(stmt, body)
            print("}")
        case "while", test, *body:
            print("while (")
            truth(test)
            print(") {")
            each(stmt, body)
            print("}")
        case "dowhile", test, *body:
            print("do {")
            each(stmt, body)
            print("} while (")
            truth(test)
            print(");")
        case "if", test, yes, no:
            print("if (")
            truth(test)
            print(") {")
            each(stmt, yes)
            print("} else {")
            each(stmt, no)
            print("}")
        case "if", test, yes:
            print("if (")
            truth(test)
            print(") {")
            each(stmt, yes)
            print("}")
        case "show", file, line, fname, name, val:
            fname = fname.replace("\\", "\\\\")
            print(f'Etc.show("{file}", {line}, "{fname}", "{name}",')
            expr(val)
            print(");")
        case "assert", file, line, fname, name, test:
            fname = fname.replace("\\", "\\\\")
            print("if (!")
            truth(test)
            print(
                f') throw new RuntimeException("{file}:{line}: {fname}: {name}: assert failed");'
            )
        case "throw", x:
            print("throw new RuntimeException(Etc.decode(")
            expr(x)
            print("));")
        case "{", *s:
            print("{")
            each(stmt, s)
            print("}")
        case "^", x:
            ret(x)
        case ("break", x) | ("continue", x):
            print(a[0])
            expr(x)
            print(";")
        case ".loc", file, line, fname:
            currentfile = file
            currentline = line
            currentfname = fname
            print(f"// {file}:{line}: {fname}")
        case ":", label, loop:
            print(label + ":")
            stmt(loop)
        case "case", x, *cases:
            outerLabel = gensym()
            print(outerLabel + ": do {")
            x = tmp(x)
            for pattern, *body in cases:
                innerLabel = gensym()
                print(innerLabel + ": do {")
                checkcase(innerLabel, pattern, x)
                assign(pattern, x)
                each(stmt, body)
                match body[-1]:
                    # unadorned continue will cause the Java compiler
                    # to generate an unreachable statement error
                    # this is useful because the bootstrap compiler
                    # does not actually support this within a case
                    # workaround: use a labeled loop
                    case ("break", _) | ("continue", _) | ("^", _) | ("throw", _):
                        0
                    case _:
                        print(f"break {outerLabel};")
                print("} while (false);")
            print("} while (false);")
        case ("=", pattern, x) | (":=", pattern, x):
            assign(pattern, x)
        case ("++", x) | ("post++", x):
            print(f"{x} = Etc.add({x}, 1);")
        case ("--", x) | ("post--", x):
            print(f"{x} = Etc.sub({x}, 1);")
        case "tron", *s:
            print("Etc.depth = 0;")
            print("Etc.tracing = Set.of")
            pargs(f'"{x}"' for x in s)
            print(";")
        case "troff":
            print("Etc.tracing = null;")
        case 0:
            print(";")
        case _:
            expr(a)
            print(";")


# variables
def assignedvars(params, body):
    # dict keeps deterministic order
    r = {x: 1 for x in params}

    def lhs(a):
        if isinstance(a, str):
            match a:
                case "@" | "List.of" | "_":
                    0
                case _:
                    r[a] = 1

    def f(a):
        match a:
            case "case", x, *cases:
                for pattern, *body in cases:
                    eachr(lhs, pattern)
            case "=", x, _:
                eachr(lhs, x)

    eachr(f, body)
    return r.keys()


def localvars(params, body, static=0):
    # TODO error check for same variable assigned with = and :=
    for a in assignedvars(params, body):
        if static:
            print("static")
        if a in ("i", "j", "k"):
            print("int")
        else:
            print("Object")
        print(a + "= 0;")


# functions
globals1 = set()
for a in modules["global"]:
    match a:
        case "fn", name, params, *body:
            globals1.add(name)


def fbody(fname, params, body):
    (_, file, line, _), *body = body

    # local functions
    r = []
    for a in body:
        match a:
            case "fn", fname1, params1, *body1:
                fn(fname1, params1, body1)
            case _:
                r.append(a)
    body = r

    # local variables
    localvars(params, body)

    # falling off the end of a function means returning zero
    match body[-1]:
        case ("^", _) | ("throw", _):
            0
        case _:
            body.append(("^", 0))

    # body
    print("public Object apply(List<Object> _args) {")
    print(f'Etc.enter("{file}", {line}, "{fname}", _args);')
    print(f"assert _args.size() == {len(params)};")
    for i in range(len(params)):
        assign(params[i], f"_args.get({i})")
    each(stmt, body)
    print("}")

    print("}")


def fref(a):
    match a:
        case "\\", params, *body:
            print("new Function<List<Object>, Object>() {")
            fbody("\\\\", params, body)
        case [*_]:
            raise Exception(a)
        case _:
            if len(a) == 1:
                print(f"((Function<List<Object>, Object>){a})")
                return
            if a in globals1:
                print(f"new global.{a}()")
                return
            print(f"new {a}()")


def fn(fname, params, body):
    _, file, line, _ = body[0]
    print(f"// {file}:{line}")
    print(f"class {fname} implements Function<List<Object>, Object> {{")
    fbody(fname, params, body)


def ret(a):
    fname = currentfname.replace("\\", "\\\\")
    a = tmp(a)
    print(f'Etc.leave("{currentfile}", {currentline}, "{fname}", {a});')
    print(f"return {a};")


# modules
for modname, module in modules.items():
    print('@SuppressWarnings("unchecked")')
    print(f"class {modname} {{")

    # functions
    r = []
    for a in module:
        match a:
            case "fn", fname, params, *body:
                print("static")
                fn(fname, params, body)
            case _:
                r.append(a)
    module = r

    # variables
    localvars([], module, 1)

    # body
    print("static void run() {")
    if modname == "program":
        for name in modules:
            if name != "program":
                print(name + ".run();")
    each(stmt, module)
    print("}")

    print("}")
