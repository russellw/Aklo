fn isupper c
	return "A"!0<=c&c<="Z"!0

fn islower c
	return "a"!0<=c&c<="z"!0

fn isdigit c
	return "0"!0<=c&c<="9"!0

fn isalpha c
	return islower c|isupper c

fn isalnum c
	return isalpha c|isdigit c

fn isidstart c
	return isalpha c|in c,"_$"

fn isidpart c
	return isidstart c|isdigit c

fn every f,s
	for a s
		if!f a
			return
	return 1

fn any f,s
	for a s
		if f a
			return 1

fn map f,s
	r=[]
	for a s
		r<<f(  a)
	return r

fn filter f,s
	r=[]
	for a s
		if f a
			r<<a
	return r

fn tail s
	return drop  1, s

fn prn s
	pr (s@"\n")

fn eprn s
	epr (s@"\n")

fn pr s
		writestream stdout,s

fn epr s
		writestream stderr,s

fn take i,s
	return slice s,0,i

fn drop i,s
	return slice s,i,len s

fn in a,s
	for b s
		if a==b
			return 1

fn endswith s,t
	return drop (len(s)-len( t),s)==t

fn startswith s,t
	return take (len( t),s)==t

fn lower a
	if islist a
		return map lower,a
	if isupper a
		return a+32
	return a

fn upper a
	if islist a
		return map upper,a
	if islower a
		return a-32
	return a

fn parseint s,base
	while isdigitb(s!0,base)
		c,@s=s
		n=n*base+parsedigit c
	return n

fn parsedigit c
	if isdigit c!0
		return c-"0"!0
	if islower c
		return 10+c-"a"!0
	if isupper c
		return 10+c-"A"!0
	return -1

fn isdigitb c,base
	n=parsedigit c
	return 0<=n&n<base

fn unesc s
	i=0
	j=0
	r=""

	fn seq n,base
			while j<n
				if !isdigitb( s!(i+j),base)
					break
				j++
			r@=utf8(parseint (slice (s,i,i+j),base))
			i+=j

	while i<len s
		c=s!i++
		if (c,)!="\\"
			r<<c
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
				r<<27
			"x"
				seq 2,16
			"u"
				seq 4,16
			"U"
				seq 8,16
			_
				r<<c
	return r

fn utf8 c
	if c<0x80
		return c,
	if c<0x800
		return(
			bitor 0b11000000,shr(c,6)
			bitor 0b10000000,bitand(c,0b111111)
		)
	if c<0x10000
		return(
			bitor 0b11100000,shr(c,12)
			bitor 0b10000000,bitand(shr(c,6),0b111111)
			bitor 0b10000000,bitand(c,0b111111)
		)
	return (
		bitor 0b11110000,shr(c,18)
		bitor 0b10000000,bitand(shr(c,12),0b111111)
		bitor 0b10000000,bitand(shr(c,6),0b111111)
		bitor 0b10000000,bitand(c,0b111111)
	)

fn repr s
	return s

fn last s
	return s!(len (s)-1)

fn isspace c
	return in c," \f\n\r\t\v"

fn first s
	return s!0

fn second s
	return s!1

fn keys m
		return map (first,m)

fn vals m
		return map (second,m)

fn commonprefix ss
	s=ss!0
	for i range(1,len(s)+1)
		t=take i,s
		if !every(\s( startswith(s,t)),ss)
			return take (i-1,s)
	return s

fn max a,b
	if a>b
		return a
	return b

fn min a,b
	if a<b
		return a
	return b

fn abs a
	if a<0
		return -a
	return a

fn signum a
	if a<0
		return -1
	if a>0
		return 1

fn reverse s
	i=len s
	r=()
	while i--
		r<<s!i
	return r

fn reduce f , s
	a,@s=s
	return fold (f,a,s)

fn fold f ,a, s
	if !s
		return a
	b,@s=s
	return fold(f,f(a,b),s)

fn maximum s
	return reduce max,s

fn minimum s
	return reduce min,s

fn sum s
	return reduce (\(a,b)(a+b),s)

fn product s
	return fold (\(a,b)(a*b),1,s)

fn init s
	return take (len(s)-1,s)

fn concat ss
	return fold (\(s,t)(s@t),"",ss)

fn replicate n ,a
	s=[]
	for i range n
		s<<a
	return s

fn takewhile f,s
	r=""
	while s &(f(s!0))
		a,@s=s
		r<<a
	return r

fn dropwhile f,s
	while s &(f(s!0))
		s=tail s
	return s

fn span f ,s
	return takewhile (f,s),dropwhile(f,s)

fn splitat n,s
	return take(n,s),drop(n,s)

fn zip s,t
	r=()
	while s&t
		a,@s=s
		b,@t=t
		r<<(a,b)
	return r

fn zip3 s,t,u
	r=()
	while s&t&u
		a,@s=s
		b,@t=t
		c,@u=u
		r<<(a,b,c)
	return r

fn zipwith f,s,t
	r=()
	while s&t
		a,@s=s
		b,@t=t
		r<<f(a,b)
	return r

fn zipwith3 f,s,t,u
	r=()
	while s&t&u
		a,@s=s
		b,@t=t
		c,@u=u
		r<<f(a,b,c)
	return r

fn unzip s
	q=()
	r=()
	while s
		(a,b),@s=s
		q<<a
		r<<b
	return q,r

fn unzip3 s
	p=()
	q=()
	r=()
	while s
		(a,b,c),@s=s
		p<<a
		q<<b
		r<<c
	return p,q,r

fn split f,s
	r=()
	rs=()
	for a s
		if f a
			rs<<r
			r=()
		else
			r<<a
	rs<<r
	return rs

fn spliton a,s
	return split (\(b)(a==b),s)

fn lines s
	if endswith(s,"\n")
		s=init s
	return spliton("\n"!0,s)

fn unlines ss
	r=""
	for s ss
		r@=s@"\n"
	return r

fn replace from,to,s
	assert from
	r=""
	while s
		if startswith s,from
			s=drop len(from),s
			r@=to
			continue
		a,@s=s
		r<<a
	return r
