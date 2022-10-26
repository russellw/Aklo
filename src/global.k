fn upper? c
	"A"!0<=c&&c<="Z"!0

fn lower? c
	"a"!0<=c&&c<="z"!0

fn digit? c
	"0"!0<=c&&c<="9"!0

fn alpha? c
	lower? c||upper? c

fn alnum? c
	alpha? c||digit? c

fn map f,a
	r=[]
	for b :a
		push r,f  b
	r

fn filter f,a
	r=[]
	for b :a
		if f b
			push r,b
	r

fn tail a
	from a,1

fn fmt(s,*args)
	i = 0
	j = 0
	r = ""
	while i < len(s)
		c = s!i++
		if [c,] != "%"
			push r, c
			continue
		c = s!i++
		case c ,
			"s"
				pushs (r, args!j++)
			"d"
				pushs r, str(args!j++)
			_
				push r, c
	r

fn printf *s
	print(apply(fmt, s))

fn eprintf *s
	eprint(apply(fmt, s))

fn println *args
	apply print,args
	print"\n"

fn eprintln *args
	apply eprint,args
	eprint"\n"

fn print *args
	for a :args
		printStream stdout,a

fn eprint *args
	for a :args
		printStream stderr,a

fn printStream stream,		a
	writeStream(stream,a)
