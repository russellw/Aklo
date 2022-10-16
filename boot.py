import sys

# parser
def unquote(s):
    s = s[1:-1]
    return s.encode("utf-8").decode("unicode_escape")


def parse(filename):
    text = open(filename).read()
    ti = 0
    line = 1

    dentc = None
    cols = [0]
    dedents = 0

    tok = None

    def err(msg):
        raise Exception(f"{filename}:{line}: {msg}")

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
                    break
                line += 1

                # measure indent
                col = 0
                while text[ti] == "\t" or text[ti] == " ":
                    if text[ti] != dentc and dentc:
                        err("indented with tabs and spaces in same file")
                    dentc = text[ti]
                    ti += 1
                    col += 1

                # nothing on this line
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
            err(f"'{repr(tok)}': expected '{repr(s)}'")

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
        if tok[0].isalnum() or tok[0] == "_":
            return lex1()

        # symbol
        if tok[0] == "'":
            return unquote(lex1())

        # string
        if tok[0] == '"':
            s = unquote(lex1())
            return ("'", [ord(c) for c in s])

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
        err(f"'{repr(tok)}': expected expression")

    def postfix():
        a = expr()
        while 1:
            if eat("("):
                a = [a]
                commas(a, ")")
                continue
            if eat("["):
                a = ["[", a, expr()]
                expect("]")
                continue
            if tok in ("++", "--"):
                return "post" + lex1(), a
            if isPrimary():
                a = [a, expr()]
                while eat(","):
                    a.append(expr())
            return a

    def prefix():
        if tok in ("!", "++", "--"):
            return lex1(), prefix()
        if eat("-"):
            return "neg", prefix()
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

    # module
    lex()
    eat("\n")
    print(tok)


parse(sys.argv[1])
