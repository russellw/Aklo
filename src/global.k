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
	isalpha c||in c,"_$"

fn isidpart c
	isidstart c||isdigit c

fn map f,s
	r=[]
	for a :s
		r@=f(  a),
	r

fn filter f,s
	r=[]
	for a :s
		if f a
			r@=a,
	r

fn tail s
	from s,1

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

fn printf *args
	print(apply(fmt, args))

fn eprintf *args
	eprint(apply(fmt, args))

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

fn to s,i
	slice s,0,i

fn from s,i
	if i<0
		i+=len s
	slice s,i,len s

fn in a,s
	for b :s
		if a==b
			return 1

fn startswith s,t
	to s,len t==t
