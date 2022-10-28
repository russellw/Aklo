fn isupper c
	"A"!0<=c&&c<="Z"!0

fn islower c
	"a"!0<=c&&c<="z"!0

fn isdigit c
	"0"!0<=c&&c<="9"!0

fn isalpha c
	islower c||isupper c

fn isalnum c
	isalpha c||isdigit c

fn isidstart c
	isalpha c||c=='_'

fn isidpart c
	isidstart c||isdigit c

fn map f,a
	r=[]
	for b :a
		r@=f(  b),
	r

fn filter f,a
	r=[]
	for b :a
		if f b
			r@=b,
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
			r@= c,
			continue
		c = s!i++
		case c ,
			"s"
				r@= args!j++
			"d"
				r@= str(args!j++)
			_
				r@= c,
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
		printstream stdout,a

fn eprint *args
	for a :args
		printstream stderr,a

fn printstream stream,		a
	writestream(stream,a)

fn from a,i
	if i<0
		i+=len a
	slice a,i,len a

fn in x,s
	for y :s
		if x==y
			return 1
