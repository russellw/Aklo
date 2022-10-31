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
    i = syms.get(s, 0) + 1
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
                        a += "." + field
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
            print(separator)
        more = 1
        f(a)


def emit(a, separator=" "):
    if isinstance(a, int):
        a = str(a)
    if not isinstance(a, str):
        separate(emit, a, separator)
        return
    sys.stdout.write(a)


def args(s):
    print("(")
    separate(expr, s, ",")
    print(")")


def truth(a):
    print("Etc.truth(")
    expr(a)
    print(")")


globals1 = set()
for a in modules["global"]:
    match a:
        case "fn", name, params, *body:
            globals1.add(name)


def fref(a):
    match a:
        case "\\", params, *body:
            print("(Function<List<Object>, Object>)(args) -> {")
            for i in range(len(params)):
                print(f"{params[i]} = args.get({i});")
            each(stmt, body)
            print("}")
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


def expr(a):
    match a:
        case "argv":
            print("Etc.argv")
        case ("++", x) | ("--", x):
            print(a[0] + x)
        case ("post++", x) | ("post--", x):
            print(x + a[0][4:])
        case "\\", params, *body:
            fref(a)
        case "//", x, y:
            expr(x)
            emit("/")
            expr(y)
        case "range", x:
            expr(("range", 0, x))
        case "<", *s:
            print("Etc.lt")
            args(s)
        case "<=", *s:
            print("Etc.le")
            args(s)
        case ("+=", x, y) | ("-=", x, y) | ("@=", x, y):
            expr(("=", x, (a[0][0], x, y)))
        case ("||", x, y) | ("&&", x, y):
            truth(x)
            print(a[0])
            truth(y)
        case "!", x:
            print("!")
            truth(x)
        case "~", *s:
            print("Etc.not")
            args(s)
        case "&", *s:
            print("Etc.and")
            args(s)
        case "|", *s:
            print("Etc.or")
            args(s)
        case "%", *s:
            print("Etc.rem")
            args(s)
        case "^", *s:
            print("Etc.xor")
            args(s)
        case "<<", *s:
            print("Etc.shl")
            args(s)
        case ">>", *s:
            print("Etc.shr")
            args(s)
        case ">>>", *s:
            print("Etc.ushr")
            args(s)
        case "+", *s:
            print("Etc.add")
            args(s)
        case "*", *s:
            print("Etc.mul")
            args(s)
        case "-", *s:
            print("Etc.sub")
            args(s)
        case "==", *s:
            print("Objects.equals")
            args(s)
        case "!=", *s:
            print("!Objects.equals")
            args(s)
        case "@", *s:
            print("Etc.cat")
            args(s)
        case "intern", *s:
            print("Sym.intern")
            args(s)
        case "gensym",:
            print("new Sym()")
        case (
            ("len", *s)
            | ("get", *s)
            | ("exit", *s)
            | ("slice", *s)
            | ("range", *s)
            | ("writestream", *s)
            | ("readfile", *s)
            | ("isnum", *s)
            | ("issym", *s)
            | ("islist", *s)
            | ("str", *s)
        ):
            print("Etc." + a[0])
            args(s)
        case ("map", f, s) | ("filter", f, s):
            emit("global.")
            emit(a[0])
            emit("(")
            fref(f)
            emit(",")
            expr(s)
            emit(")")
        case "apply", f, s:
            expr(f)
            emit("(")
            expr(s)
            emit(".toArray())")
        case "=", x, y:
            print(x + "=")
            expr(y)
        case "stdout":
            emit("System.out")
        case "stderr":
            emit("System.err")
        case ".run", x:
            expr(x)
            emit(".run()")
        case "List.of", *s:
            if s:
                match s[-1]:
                    case "...", t:
                        emit("Etc.cons(")
                        for x in s[:-1]:
                            expr(x)
                            emit(",")
                        expr(t)
                        emit(")")
                        return
            emit("List.of(")
            separate(expr, s, ",")
            emit(")")
        case f, *s:
            fref(f)
            emit(".apply(List.of")
            args(s)
            emit(")")
        case _:
            print(a)


def var(x):
    match x:
        case "i" | "j" | "k":
            print("int")
        case _:
            print("Object")
    print(x + "= 0;")


def tmp(a):
    r = gensym("")
    print(f"var {r} = ")
    expr(a)
    print(";")
    return r


def assign(pattern, x):
    if isinstance(pattern, int):
        print("if (!Etc.eq(")
        expr(x)
        print(f", {pattern})) break assign_;")
        return
    match pattern:
        case "intern", ("List.of", *_):
            print("if (!Etc.eq(")
            expr(x)
            print(",")
            expr(pattern)
            print(")) break assign_;")
        case "List.of", *s:
            x = tmp(x)
            print(f"if (!({x} instanceof List)) break assign_;")
            print(f"if (((List<Object>){x}).size() < {len(s)}) break assign_;")
            for i in range(len(s)):
                match s[i]:
                    case "...", y:
                        assign(y, ("from", x, i))
                    case y:
                        assign(y, ("Etc.subscript", x, i))
        case "_":
            pass
        case _:
            print(pattern + "=")
            expr(x)
            print(";")


def stmt(a):
    match a:
        case "for", x, s, *body:
            print(f"for (var {x}: (List)")
            expr(s)
            print(") {")
            each(stmt, body)
            print("}")
        case "while", test, *body:
            print("while (")
            expr(test)
            print(") {")
            each(stmt, body)
            print("}")
        case "dowhile", test, *body:
            print("do {")
            each(stmt, body)
            print("} while (")
            expr(test)
            print(");")
        case "if", test, yes, no:
            print("if (")
            expr(test)
            print(") {")
            each(stmt, yes)
            print("} else {")
            each(stmt, no)
            print("}")
        case "if", test, yes:
            print("if (")
            expr(test)
            print(") {")
            each(stmt, yes)
            print("}")
        case "assert", x:
            print("assert ")
            expr(x)
            print(";")
        case "{", *s:
            print("{")
            each(stmt, s)
            print("}")
        case ("return", x) | ("break", x) | ("continue", x):
            print(a[0])
            expr(x)
            print(";")
        case ":", label, loop:
            print(label + ":")
            stmt(loop)
        case "case", x, *cases:
            label = gensym("case")
            print(label + ": do {")
            x = tmp(x)
            for pattern, *body in cases:
                print("assign_: do {")
                assign(pattern, x)
                each(stmt, body)
                print(f"break {label};")
                print("} while (false);")
            print("} while (false);")
        case "=", pattern, x:
            print("assign_: do {")
            assign(pattern, x)
            print("} while (false);")
        case "nonlocal", _:
            pass
        case _:
            expr(a)
            print(";")


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
    print("public Object apply(List<Object> args) {")
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
                print("static")
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
