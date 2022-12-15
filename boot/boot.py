import inspect
import os
import subprocess
import sys

here = os.path.dirname(os.path.realpath(__file__))


def each(f, s):
    # map would not be a substitute here
    # because Python's map returns a lazy iterator
    for a in s:
        f(a)


def eachTerms(f, a):
    match a:
        case ("fn", *_) | ("\\", *_):
            pass
        case [*_]:
            for b in a:
                eachTerms(f, b)
    f(a)


def partition(f, s):
    q = [a for a in s if f(a)]
    r = [a for a in s if not f(a)]
    return q, r


def dbg(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    sys.stderr.write(f"{info.filename}:{info.function}:{info.lineno}: {a}\n")


# in the bootstrap compiler, strings substitute for symbols
syms = 0


def sym():
    global syms
    syms += 1
    return f"_{syms}"


# parser
modules = {}


def isIdStart(c):
    return c.isalpha() or c in "_$"


def isIdPart(c):
    return isIdStart(c) or c.isdigit() or c == "?"


def unesc(s):
    return s.encode("utf-8").decode("unicode_escape")


def quoteSym(s):
    return "intern", ["List.of"] + [ord(c) for c in s]


def parse(file):
    text = open(f"{here}/../aklo/{file}.k").read()
    i = 0
    line = 1

    dentc = 0
    cols = [0]
    dedents = 0

    tok = 0

    def err(msg):
        raise Exception(f"{file}:{line}: {msg}")

    def errTok(msg):
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
            if isIdStart(tok):
                while isIdPart(text[i]):
                    i += 1
                tok = text[j:i]
                return

            # hexadecimal numbers are separate because they may contain 'e'
            if text[i : i + 2].lower() == "0x":
                while isIdPart(text[i]):
                    i += 1
                tok = text[j:i]
                return

            # other number
            if tok.isdigit() or tok == "." and text[i + 1].isdigit():
                while isIdPart(text[i]):
                    i += 1
                if text[i] == ".":
                    i += 1
                    while isIdPart(text[i]):
                        i += 1
                    if text[i - 1].lower() == "e" and text[i] in "+-":
                        i += 1
                        while isIdPart(text[i]):
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
                ">>",
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
            errTok(f"expected {repr(s)}")

    def word():
        if isIdStart(tok[0]):
            return lex1().replace("?", "p")
        errTok("expected word")

    # expressions
    def exprs(end):
        r = []
        if eat(".indent"):
            while not eat(".dedent"):
                r.append(commas())
                expect("\n")
        elif tok != end:
            while 1:
                r.append(expr())
                if not eat(","):
                    break
        expect(end)
        return r

    def primary():
        # symbol
        if tok.startswith("'"):
            s = unesc(tok[1:-1])
            lex()
            return quoteSym(s)

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
        if eat("compileTimeReadFiles"):
            expect("(")

            # in the full language, the first argument is a filter function
            # but in compiling the main compiler, this can be ignored
            # because we know it will look for *.k
            expr()
            expect(",")

            # the second argument is the path relative to the current source file
            # which will always be the main compiler source directory
            lex()
            expect(")")
            return "compileTimeReadFiles", here.replace("\\", "/") + "/../aklo"
        if isIdStart(tok[0]):
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
            return ["List.of"] + exprs("]")

        # none of the above
        errTok("expected expression")

    def postfix():
        a = primary()
        while 1:
            match tok:
                case "(":
                    lex()
                    a = [a] + exprs(")")
                case "[":
                    lex()
                    a = "subscript", a, expr()
                    expect("]")
                case "++" | "--":
                    return "post" + lex1(), a
                case ".":
                    a += lex1() + word()
                case _:
                    return a

    def params():
        r = []
        expect("(")
        if tok != ")":
            while 1:
                r.append(word())
                if not eat(","):
                    break
        expect(")")
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
                r.append((".loc", file, line1, "lambda"))
                if eat(".indent"):
                    while not eat(".dedent"):
                        r.append((".loc", file, line, "lambda"))
                        r.append(stmt("lambda"))
                else:
                    r.append(("^", commas()))
                expect(")")

                return r
        return postfix()

    # operator precedence parser
    prec = 99
    ops = {}

    def makeOp(op, left):
        ops[op] = prec, left

    makeOp("**", 0)

    prec -= 1
    makeOp("%", 1)
    makeOp("*", 1)
    makeOp("/", 1)
    makeOp("//", 1)

    prec -= 1
    makeOp("+", 1)
    makeOp("-", 1)
    makeOp("@", 1)

    prec -= 1
    makeOp("!=", 1)
    makeOp("<", 1)
    makeOp("<=", 1)
    makeOp("==", 1)
    makeOp(">", 1)
    makeOp(">=", 1)

    prec -= 1
    makeOp("&", 1)

    prec -= 1
    makeOp("|", 1)

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
            case "=" | "+=" | "-=" | "@=" | "<<" | ">>" | ":=":
                return lex1(), a, commas()
        return a

    def block(fname):
        expect(".indent")
        r = []
        while not eat(".dedent"):
            r.append((".loc", file, line, fname))
            r.append(stmt(fname))
        return r

    def parseIf(fname):
        assert tok in ("if", "elif")
        lex()
        r = ["if", expr()]
        r.append(block(fname))
        match tok:
            case "elif":
                r.append([parseIf(fname)])
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
            case "dbg":
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
                r.append(commas())
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
                return parseIf(fname)
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
        expect("\n")
        return a

    # top level
    lex()
    eat("\n")
    r = []
    while tok != ".dedent":
        r.append((".loc", file, line, file))
        r.append(stmt(file))
    modules[file] = r


for root, dirs, files in os.walk(here + "/../aklo"):
    for file in files:
        file, ext = os.path.splitext(file)
        if ext == ".k" and file != "libc":
            parse(file)


# output
sys.stdout.write(open(here + "/prefix.java").read())


# expressions
def printArgs(env, s):
    print("(")
    more = 0
    for a in s:
        if more:
            print(",")
        more = 1
        expr(env, a)
    print(")")


def truth(env, a):
    print("Etc.truth(")
    expr(env, a)
    print(")")


def expr(env, a):
    match a:
        case "compileTimeReadFiles", dir1:
            print(f'Etc.compileTimeReadFiles("{dir1}")')
        case "args" | "windowsp":
            print("Etc." + a)
        case ("++", x) | ("--", x):
            print(a[0] + x)
        case ("post++", x) | ("post--", x):
            print(x + a[0][4:])
        case "\\", params, *body:
            fref(env, a)
        case "//", *s:
            print("Etc.div")
            printArgs(env, s)
        case "range", x:
            expr(env, ("range", 0, x))
        case "<", *s:
            print("Etc.lt")
            printArgs(env, s)
        case "<=", *s:
            print("Etc.le")
            printArgs(env, s)
        case ("|", x, y) | ("&", x, y):
            truth(env, x)
            print(a[0] * 2)
            truth(env, y)
        case "!", x:
            print("!")
            truth(env, x)
        case "-", x:
            print("Etc.neg")
            printArgs(env, [x])
        case "-", x, y:
            print("Etc.sub")
            printArgs(env, (x, y))
        case "%", *s:
            print("Etc.rem")
            printArgs(env, s)
        case "+", *s:
            print("Etc.add")
            printArgs(env, s)
        case "*", *s:
            print("Etc.mul")
            printArgs(env, s)
        case "==", *s:
            print("Objects.equals")
            printArgs(env, s)
        case "!=", *s:
            print("!Objects.equals")
            printArgs(env, s)
        case "@", *s:
            print("Etc.cat")
            printArgs(env, s)
        case ("rationalp", _) | ("floatp", _) | ("doublep", _):
            print("false")
        case "intern", *s:
            print("Sym.intern")
            printArgs(env, s)
        case "sym", *s:
            print("new Sym")
            printArgs(env, s)
        case (
            ("len", *s)
            | ("get", *s)
            | ("subscript", *s)
            | ("exit", *s)
            | ("slice", *s)
            | ("range", *s)
            | ("writeStream", *s)
            | ("parseDouble", *s)
            | ("parseFloat", *s)
            | ("readFile", *s)
            | ("writeFile", *s)
            | ("integerp", *s)
            | ("symp", *s)
            | ("listp", *s)
            | ("dirp", *s)
            | ("str", *s)
            | ("shl", *s)
            | ("shr", *s)
            | ("bitNot", *s)
            | ("bitAnd", *s)
            | ("listDir", *s)
            | ("bitOr", *s)
            | ("bitXor", *s)
        ):
            print("Etc." + a[0])
            printArgs(env, s)
        case "apply", f, s:
            fref(f)
            print(".apply(")
            expr(env, s)
            print(")")
        case "=", x, y:
            print(x + "=")
            expr(env, y)
        case "stdout":
            print("System.out")
        case "stderr":
            print("System.err")
        case "List.of", *s:
            if s:
                match s[-1]:
                    case "@", t:
                        print("Etc.cons")
                        printArgs(env, s[:-1] + [t])
                        return
            print("List.of")
            printArgs(env, s)
        case f, *s:
            fref(env, f)
            print(".apply(List.of")
            printArgs(env, s)
            print(")")
        case _:
            if a in env:
                fref(env, a)
                return
            match a:
                case "intern":
                    print(
                        "(Function<List<Object>, Object>)(List<Object> _s) -> Sym.intern(_s.get(0))"
                    )
                case "str":
                    print(
                        "(Function<List<Object>, Object>)(List<Object> _s) -> Etc.str(_s.get(0))"
                    )
                case _:
                    print(a)


# statements
def isRest(s):
    if s:
        match s[-1]:
            case "@", _:
                return 1


def tmp(env, a):
    r = sym()
    print(f"Object {r} = ")
    expr(env, a)
    print(";")
    return r


def checkCase(env, label, pattern, x):
    if isinstance(pattern, int):
        print("if (!Objects.equals(")
        expr(env, x)
        print(f", {pattern})) break {label};")
        return
    match pattern:
        case "intern", ("List.of", *_):
            print("if (!Objects.equals(")
            expr(env, x)
            print(",")
            expr(env, pattern)
            print(f")) break {label};")
        case "List.of", *s:
            x = tmp(env, x)
            print(f"if (!({x} instanceof List)) break {label};")
            if isRest(s):
                n = len(s) - 1
                print(f"if (((List<Object>){x}).size() < {n}) break {label};")
                for i in range(n):
                    checkCase(env, label, s[i], ("subscript", x, i))
                checkCase(env, label, s[n][1], ("drop", n, x))
                return
            n = len(s)
            print(f"if (((List<Object>){x}).size() != {n}) break {label};")
            for i in range(n):
                checkCase(env, label, s[i], ("subscript", x, i))


def assignConst(env, pattern, x):
    print("assert Objects.equals")
    printArgs(env, (pattern, x))
    print(";")


def assign(env, pattern, x):
    if isinstance(pattern, int):
        assignConst(env, pattern, x)
        return
    match pattern:
        case "intern", ("List.of", *_):
            assignConst(env, pattern, x)
        case "List.of", *s:
            x = tmp(env, x)
            if isRest(s):
                n = len(s) - 1
                for i in range(n):
                    assign(env, s[i], ("subscript", x, i))
                assign(env, s[n][1], ("drop", n, x))
                return
            n = len(s)
            for i in range(n):
                assign(env, s[i], ("subscript", x, i))
        case "_":
            0
        case "i" | "j" | "k":
            print(pattern + "= (int)")
            expr(env, x)
            print(";")
        case _:
            print(pattern + "=")
            expr(env, x)
            print(";")


currentFile = 0
currentLine = 0
currentFName = 0


def stmt(env, a):
    global currentFile
    global currentLine
    global currentFName
    match a:
        case "<<", x, y:
            print(x + "=")
            print("Etc.append")
            printArgs(env, (x, y))
            print(";")
        case ">>", x, y:
            print(y + "=")
            print("Etc.prepend")
            printArgs(env, (x, y))
            print(";")
        case ("+=", x, y) | ("-=", x, y) | ("@=", x, y):
            print(x + "=")
            expr(env, (a[0][0], x, y))
            print(";")
        case "for", y, s, *body:
            x = sym()
            print(f"for (var {x}: (List)")
            expr(env, s)
            print(") {")
            assign(env, y, x)
            stmts(env, body)
            print("}")
        case "while", test, *body:
            print("while (")
            truth(env, test)
            print(") {")
            stmts(env, body)
            print("}")
        case "dowhile", test, *body:
            print("do {")
            stmts(env, body)
            print("} while (")
            truth(env, test)
            print(");")
        case "if", test, yes, no:
            print("if (")
            truth(env, test)
            print(") {")
            stmts(env, yes)
            print("} else {")
            stmts(env, no)
            print("}")
        case "if", test, yes:
            print("if (")
            truth(env, test)
            print(") {")
            stmts(env, yes)
            print("}")
        case "dbg", file, line, fname, name, val:
            print(f'Etc.dbg("{file}", {line}, "{fname}", "{name}",')
            expr(env, val)
            print(");")
        case "assert", file, line, fname, name, test:
            print("if (!")
            truth(env, test)
            print(
                f') throw new RuntimeException("{file}:{line}: {fname}: {name}: assert failed");'
            )
        case "throw", x:
            print("throw new RuntimeException(Etc.decode(")
            expr(env, x)
            print("));")
        case "{", *s:
            print("{")
            stmts(env, s)
            print("}")
        case "^", x:
            ret(env, x)
        case ("break", x) | ("continue", x):
            print(a[0])
            expr(env, x)
            print(";")
        case ".loc", file, line, fname:
            currentFile = file
            currentLine = line
            currentFName = fname
            print(f"// {file}:{line}: {fname}")
        case ":", label, loop:
            print(label + ":")
            stmt(env, loop)
        case "case", x, *cases:
            outerLabel = sym()
            print(outerLabel + ": do {")
            x = tmp(env, x)
            for pattern, *body in cases:
                innerLabel = sym()
                print(innerLabel + ": do {")
                checkCase(env, innerLabel, pattern, x)
                assign(env, pattern, x)
                stmts(env, body)
                match body[-1]:
                    # unadorned continue will cause the Java compiler
                    # to generate an unreachable statement error
                    # this is useful because the bootstrap compiler
                    # does not actually support this within a case
                    case ("break", _) | ("continue", _) | ("^", _) | ("throw", _):
                        0
                    case _:
                        print(f"break {outerLabel};")
                print("} while (false);")
            print("} while (false);")
        case ("=", pattern, x) | (":=", pattern, x):
            assign(env, pattern, x)
        case ("++", x) | ("post++", x):
            print(f"{x} = Etc.add({x}, 1);")
        case ("--", x) | ("post--", x):
            print(f"{x} = Etc.sub({x}, 1);")
        case "tron", *s:
            print("Etc.depth = 0;")
            print("Etc.tracing = Set.of")
            printArgs(env, (f'"{x}"' for x in s))
            print(";")
        case "troff":
            print("Etc.tracing = null;")
        case 0:
            print(";")
        case _:
            expr(env, a)
            print(";")


def stmts(env, s):
    for a in s:
        stmt(env, a)


# variables
def assignedVars(params, body):
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
                    eachTerms(lhs, pattern)
            case ("=", x, _) | ("for", x, *_):
                eachTerms(lhs, x)

    eachTerms(f, body)
    return r.keys()


def localVars(params, body, static=0):
    for a in assignedVars(params, body):
        if a == "_":
            continue
        if static:
            print("static")
        if a in ("i", "j", "k"):
            print("int")
        else:
            print("Object")
        print(a + "= 0;")


# functions
def getFns(env, s):
    for a in s:
        match a:
            case "fn", name, *_:
                env.add(name)


ubiquitous = set()
getFns(ubiquitous, modules["ubiquitous"])


def fbody(env, fname, params, body):
    global currentFName
    currentFName = fname
    (_, file, line, _), *body = body

    # local functions
    env = set(env)
    getFns(env, body)

    r = []
    for a in body:
        match a:
            case "fn", fname1, params1, *body1:
                fn(env, fname1, params1, body1)
            case _:
                r.append(a)
    body = r

    # local variables
    localVars(params, body)

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
        assign(env, params[i], f"_args.get({i})")
    stmts(env, body)
    print("}")

    print("}")


def fref(env, a):
    match a:
        case "\\", params, *body:
            print("new Function<List<Object>, Object>() {")
            fbody(env, "lambda", params, body)
        case [*_]:
            raise Exception(a)
        case _:
            if len(a) == 1:
                print(f"((Function<List<Object>, Object>){a})")
                return
            if a in ubiquitous:
                print(f"new ubiquitous.{a}()")
                return
            print(f"new {a}()")


def fn(env, fname, params, body):
    _, file, line, _ = body[0]
    print(f"// {file}:{line}")
    print(f"class {fname} implements Function<List<Object>, Object> {{")
    fbody(env, fname, params, body)


def ret(env, a):
    a = tmp(env, a)
    print(f'Etc.leave("{currentFile}", {currentLine}, "{currentFName}", {a});')
    print(f"return {a};")


# modules
for modName, module in modules.items():
    print('@SuppressWarnings("unchecked")')
    print(f"class {modName} {{")

    # functions
    env1 = set(ubiquitous)
    getFns(env1, module)

    r = []
    for a in module:
        match a:
            case "fn", fname, params, *body:
                print("static")
                fn(env1, fname, params, body)
            case _:
                r.append(a)
    module = r

    # variables
    localVars([], module, 1)

    # body
    print("static void run() {")
    if modName == "main":
        for name in modules:
            if name != "main":
                print(name + ".run();")
    stmts(env1, module)
    print("}")

    print("}")
