fn upper? c
	^ "A"[0]<=c&c<="Z"[0]

fn lower? c
	^ "a"[0]<=c&c<="z"[0]

fn digit? c
	^ "0"[0]<=c&c<="9"[0]

fn alpha? c
	^ lower? (c)|upper? (c)

fn alnum? c
	^ alpha? (c)|digit? (c)

fn print?(c)
	^" "[0]<=c&c<=126

fn ascii?(a)
	if atom?(a)|!a
		^
	for b: a
		if !int?(b)
			^
		if!print?(b)&!in(b,"\n\r\t")
			^
	^1

fn indexof(a,s)
	^find(\(b)(a==b),s)

fn lastindexof(a,s)
	^find(\(b)(a==b),s)

fn find f,s
	for i: range(s)
		if f(s[i])
			^i
	^-1

fn findlast f,s
	i=len(s)
	while i--
		if f(s[i])
			^i
	^-1

fn every f,s
	for a: s
		if!f (a)
			^
	^ 1

fn any f,s
	for a: s
		if f (a)
			^ 1

fn map f,s
	r=[]
	for a :s
		r<<f(  a)
	^ r

fn filter f,s
	r=[]
	for a :s
		if f (a)
			r<<a
	^ r

fn tail s
	^ drop ( 1, s)

fn prn s
	pr (s@"\n")

fn eprn s
	epr (s@"\n")

fn pr s
		writestream( stdout,s)

fn epr s
		writestream( stderr,s)

fn take i,s
	^ slice( s,0,i)

fn drop i,s
	^ slice (s,i,len( s))

fn in a,s
	for b: s
		if a==b
			^ 1

fn endswith s,t
	^ drop (len(s)-len( t),s)==t

fn startswith s,t
	^ take (len( t),s)==t

fn lower a
	if list?( a)
		^ map (lower,a)
	if upper? (a)
		^ a+32
	^ a

fn upper a
	if list? (a)
		^ map (upper,a)
	if lower? (a)
		^ a-32
	^ a

fn parseint s,base
	while s&digitb?(s[0],base)
		c,@s=s
		n=n*base+parsedigit (c)
	^ n

fn parsedigit c
	if digit? (c)
		^ c-"0"[0]
	if lower? (c)
		^ 10+c-"a"[0]
	if upper? (c)
		^ 10+c-"A"[0]
	^ -1

fn digitb? c,base
	n=parsedigit (c)
	^ 0<=n&n<base

fn unesc s
	i=0
	j=0
	r=""

	fn seq n,base
			while j<n
				if !digitb?( s[i+j],base)
					break
				j++
			r@=utf8(parseint (slice (s,i,i+j),base))
			i+=j

	while i<len (s)
		c=s[i++]
		if c!="\\"[0]
			r<<c
			continue
		c=s[i++]
		case [c]
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
				seq (2,16)
			"u"
				seq (4,16)
			"U"
				seq (8,16)
			_
				r<<c
	^ r

fn utf8 c
	if c<0x80
		^ [c]
	if c<0x800
		^[
			bitor( 0b11000000,shr(c,6))
			bitor( 0b10000000,bitand(c,0b111111))
		]
	if c<0x10000
		^[
			bitor( 0b11100000,shr(c,12))
			bitor( 0b10000000,bitand(shr(c,6),0b111111))
			bitor( 0b10000000,bitand(c,0b111111))
		]
	^ [
		bitor( 0b11110000,shr(c,18))
		bitor( 0b10000000,bitand(shr(c,12),0b111111))
		bitor( 0b10000000,bitand(shr(c,6),0b111111))
		bitor( 0b10000000,bitand(c,0b111111))
	]

fn atom? a
	^!list?(a)

fn num?  a
	^int?(a)|float?(a)

fn repr a
	r=[]
	fn rec a
		if atom?(a)
			r@=str(a)
			^
		if ascii?(a)
			r@="\""
			for c: a
				case [c]
					"\n"
						r@="\\n"
					"\r"
						r@="\\r"
					"\t"
						r@="\\t"
					_
						r<<c
			r@="\""
			^
		r@="["
		for i: range(a)
			if i
				r@=", "
			rec (a[i])
		r@="]"
	rec(a)
	^ r

fn last s
	^ s[len (s)-1]

fn space? c
	^ in( c," \f\n\r\t\v")

fn first s
	^ s[0]

fn second s
	^ s[1]

fn keys s
		^ map (first,s)

fn vals s
		^ map (second,s)

fn commonprefix ss
	s=ss[0]
	for i: range(1,len(s)+1)
		t=take( i,s)
		if !every(\s( startswith(s,t)),ss)
			^ take (i-1,s)
	^ s

fn max a,b
	if a>b
		^ a
	^ b

fn min a,b
	if a<b
		^ a
	^ b

fn abs a
	if a<0
		^ -a
	^ a

fn signum a
	if a<0
		^ -1
	if a>0
		^ 1

fn reverse s
	i=len (s)
	r=[]
	while i--
		r<<s[i]
	^ r

fn reduce f , s
	a,@s=s
	^ fold (f,a,s)

fn fold f ,a, s
	if !s
		^ a
	b,@s=s
	^ fold(f,f(a,b),s)

fn maximum s
	^ reduce (max,s)

fn minimum s
	^ reduce( min,s)

fn sum s
	^ fold(\(a,b)(a+b),0,s)

fn product s
	^ fold (\(a,b)(a*b),1,s)

fn init s
	^ take (len(s)-1,s)

fn concat ss
	^ fold (\(s,t)(s@t),"",ss)

fn replicate n ,a
	r=[]
	for i: range (n)
		r<<a
	^ r

fn takewhile f,s
	r=""
	while s &(f(s[0]))
		a,@s=s
		r<<a
	^ r

fn dropwhile f,s
	while s &(f(s[0]))
		s=tail (s)
	^ s

fn span f ,s
	^ takewhile (f,s),dropwhile(f,s)

fn splitat i,s
	^ take(i,s),drop(i,s)

fn zip s,t
	r=[]
	while s&t
		a,@s=s
		b,@t=t
		r<<(a,b)
	^ r

fn zip3 s,t,u
	r=[]
	while s&t&u
		a,@s=s
		b,@t=t
		c,@u=u
		r<<(a,b,c)
	^ r

fn zipwith f,s,t
	r=[]
	while s&t
		a,@s=s
		b,@t=t
		r<<f(a,b)
	^ r

fn zipwith3 f,s,t,u
	r=[]
	while s&t&u
		a,@s=s
		b,@t=t
		c,@u=u
		r<<f(a,b,c)
	^ r

fn unzip s
	q=[]
	r=[]
	while s
		(a,b),@s=s
		q<<a
		r<<b
	^ q,r

fn unzip3 s
	p=[]
	q=[]
	r=[]
	while s
		(a,b,c),@s=s
		p<<a
		q<<b
		r<<c
	^ p,q,r

fn split f,s
	r=[]
	rs=[]
	for a: s
		if f (a)
			rs<<r
			r=[]
		else
			r<<a
	rs<<r
	^ rs

fn spliton a,s
	^ split (\(b)(a==b),s)

fn lines s
	if endswith(s,"\n")
		s=init (s)
	^ spliton("\n"[0],s)

fn unlines ss
	r=""
	for s: ss
		r@=s@"\n"
	^ r

fn replace from,to,s
	assert from
	r=""
	while s
		if startswith( s,from)
			s=drop (len(from),s)
			r@=to
			continue
		a,@s=s
		r<<a
	^ r

fn uniq s
	r=[]
	for a: s
		if!in(a,r)
			r<<a
	^r

fn union s,t
	^uniq(s@t)

fn intersection s,t
	r=[]
	for a: s
		if in(a,t)
			r<<a
	^uniq(r)

fn dirseparator? c
	if windows?
		^in(c,":\\/")
	^in(c,"/")

fn splitdir s
	i=findlast(dirseparator?,s)
	;there are many edge cases in Windows filenames
	; so it is better to leave a trailing separator untouched
	^splitat(i+1,s)

fn splitext s
	i=lastindexof("."[0],s)
	if i<0
		^s,""
	^ take(i,s),drop(i+1,s)

fn join t, ss
	if!ss
		^[]
	r=ss[0]
	for s: tail(ss)
		r@=t@s
	^r

fn joindir ss
	r=ss[0]
	for s: tail(ss)
		if!(r&dirseparator?(last(r)))
			if windows?
				r@="\\"
			else
				r@="/"
		r@=s
	^r

fn mapfirst(f,s)
	^map(\(a)(f(a[0]),a[1]),s)

fn mapsecond(f,s)
	^map(\(a)(a[0],f(a[1])),s)

;TODO placeholders
fn  get(s,a)
	^get(s,a)

fn  gensym(s)
	^gensym(s)

fn  intern(s)
	^intern(s)

fn str(a)
	^str(a)
