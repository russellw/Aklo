import sys


def parse(filename):
    text = open(filename).read()
    ti = 0
    line = 1
    tok = None

    def lex():
        nonlocal line
        while 1:
            c = tok[ti]

            # space
            if c.isspace():
                ti += 1
                continue

    lex()


parse(sys.argv[1])
