fn isupper c
	"A"!0<=c&&c<="Z"!0

fn islower c
	"a"!0<=c&&c<="z"!0


fn isalpha c
	islower c||isupper c

fn isalnum c
	isalpha c||isdigit c

fn isidstart c
	isalpha c||in c,"_$"

fn isidpart c
	isidstart c||isdigit c

fn every f,s
	for a s
		if!f a
			return
	1

fn any f,s
	for a s
		if f a
			return 1

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
	drop  1, s

fn fmt(s)
	j = 1
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

fn printf
	print(apply(fmt, args))

fn eprintf
	eprint(apply(fmt, args))

fn println
	apply print,args
	print"\n"

fn eprintln
	apply eprint,args
	eprint"\n"

fn print
	for a :args
		printstream stdout,a

fn eprint
	for a :args
		printstream stderr,a

fn printstream stream,		a
	writestream(stream,a)

fn take i,s
	if i<0
		i+=len s
	slice s,0,i

fn drop i,s
	if i<0
		i+=len s
	slice s,i,len s

fn in a,s
	for b :s
		if a==b
			return 1

fn endswith s,t
	if len s<len t
		return
	drop (-len( t),s)==t

fn startswith s,t
	take (len( t),s)==t

fn lower a
	if islist a
		return map lower,a
	if isupper a
		return a+32
	a

fn upper a
	if islist a
		return map upper,a
	if islower a
		return a-32
	a

fn parseint s,base
	if!base
		base=10
	while isdigit(s!0,base)
		c,*s=s
		n=n*base+parsedigit c
	n

fn parsedigit c
	if "0"!0<=c&&c<="9"!0
		return c-"0"!0
	if islower c
		return 10+c-"a"!0
	if isupper c
		return 10+c-"A"!0
	-1

fn isdigit c,base
	if!base
		base=10
	n=parsedigit c
	0<=n&&n<base

fn unesc s
	r=""

	fn seq n,base
			^ i
			while j<n
				if !isdigit( s!(i+j),base)
					break
				j++
			r@=utf8(parseint (slice (s,i,i+j),base))
			i+=j

	while i<len s
		c=s!i++
		if (c,)!="\\"
			r@=c,
			continue
		c=s!i++
		case c,
			"\n"
				0
			"a"
				r@="\a"
			"b "
				r@="\b"
			"f"
				r@="\f"
			"n"
				r@="\n"
			"r"
				r@="\r"
			"t"
				r@="\t"
			"v"
				r@="\v"
			"e"
				r@=27,
			"x"
				seq 2,16
			"u"
				seq 4,16
			"U"
				seq 8,16
			_
				r@=c,
	r

fn utf8 c
	if c<0x80
		return c,
	if c<0x800
		return(
			0b11000000|c>>6
			0b10000000|c&0b111111
		)
	if c<0x10000
		return(
			0b11100000|c>>12
			0b10000000|(c>>6)&0b111111
			0b10000000|c&0b111111
		)
	(
		0b11110000|c>>18
		0b10000000|(c>>12)&0b111111
		0b10000000|(c>>6)&0b111111
		0b10000000|c&0b111111
	)

fn repr s
	s

fn last s
	s!(len (s)-1)

fn isspace c
	in c," \f\n\r\t\v"

fn dedent s
	;TODO: like textwrap.dedent
	s

fn fst s
	s!0

fn snd s
	s!1

fn keys m
		map (fst,m)

fn vals m
		map (snd,m)

fn commonprefix ss
	s=ss!0
	for i range(1,len(s)+1)
		t=take i,s
		if !every(\s( startswith(s,t)),ss)
			return take (i-1,s)
	s

fn max a,b
	if a>b
		return a
	b

fn min a,b
	if a<b
		return a
	b

fn abs a
	if a<0
		return -a
	a
