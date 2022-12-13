fn labelusers(body,label)
	r=[]
	for i:range(body)
		a=body[i]
		case a
			'if',_,label1
			'goto',label1
				if label==label1
					r<<i
	^r

fn deadvar(F)
	loc,name,params,rtype,vars,body=F
	vars=filter(
		\(a)(
			_,x,@_=a
			^localusers(body,x)
		)
		vars
	)
	^loc,name,params,rtype,vars,body

fn mapbody(f,fns)
	^map(
		\(F)(
			loc,name,params,rtype,vars,body=F
			body=f(body)
			^loc,name,params,rtype,vars,body
		)
		fns
	)

fn redundantloc(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'loc',@_
				case body[i+1]
					'loc',@_
						s<<i
	^filter(\(_,i)(!in(i,s)),body)


fn localusers(body,a)
	assert atom?(a)
	r=[]
	for i : range(body)
		b=body[i]
		case b
			'=',y,x
				b=x
		if occurs(a,b)
			r<<i
	^r

fn redundantgoto(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'goto',label
				case body[i+1]
					':',label1
						if label==label1
							s<<i
	^filter(\(_,i)(!in(i,s)),body)

fn deadlabel(body)
	^filter(
		\(a)(
			case a
				':',label
					^labelusers(body,label)
			^1
		)
		body
	)

fn nop(body)
	^filter(
		\(a)(
			case a
				'+',@_
				'-',@_
				'*',@_
				'%',@_
				'//',@_
				'==',@_
				'<',@_
				'<=',@_
				'neg',@_
				'bitand',@_
				'bitor',@_
				'bitxor',@_
				'bitnot',@_
				'shl',@_
				'shr',@_
				'**',@_
				'xcall','parsedouble',@_
				'quote',@_
					^
			^list?(a)
		)
		body
	)

fn deadstore(body)
	^map(
		\(a)(
			case a
				'=',y,x
					if !localusers(body,y)
						^x
			^a
		)
		body
	)

fn labelidx(body,label)
	for i,a :indexed(body)
			case a
				':',label1
					if label==label1
						^i
	throw"label not found"

fn cfg(body)
	g=[]
	for i,a :indexed(body)
			case a
				'throw',_
				'^',_
					0
				'goto',label
					g<<i,labelidx(body,label)
				'if',_,label
					g<<i,labelidx(body,label)
					g<<i,i+1
				_
					g<<i,i+1
	^g

fn deadcode(body)
	g=cfg(body)
	^filter(\(a,i)(graph.reaches(g,0,i)),body)

fn ifconst(body)
	^map(
		\(a)(
			case a
				'if',test,label
					if num?(test)
						if test
							^'goto',label
						^
			^a
		)
		body
	)

fn eval(a)
			if atom?(a)
				^a
			;TODO simplify,  and fixup bootstrap compiler accordingly
			if!every(\(b)(int?(b)),tail(a))
				^a
			case a
				'==',x,y
					^x==y
				'<',x,y
					^x<y
				'<=',x,y
					^x<=y
				'+',x,y
					^x+y
				'*',x,y
					^x*y
				'-',x,y
					^x-y
				'neg',x
					^-x
				'bitnot',x
					^bitnot(x)
				'bitand',x,y
					^bitand(x,y)
				'bitor',x,y
					^bitor(x,y)
				'bitxor',x,y
					^bitxor(x,y)
				'shl',x,y
					^shl(x,y)
				'shr',x,y
					^shr(x,y)
			^a

fn constfold(body)
	^map(
		\(a)(
			case a
				'=',y,x
					^'=',y,eval(x)
			^a
		)
		body
	)

fn optimize(vars,fns)
	dowhile old!=(vars,fns)
		old=vars,fns
		fns=mapbody(redundantloc,fns)
		fns=mapbody(deadcode,fns)
		fns=mapbody(redundantgoto,fns)
		fns=mapbody(deadlabel,fns)
		fns=mapbody(deadstore,fns)
		fns=mapbody(nop,fns)
		fns=mapbody(ifconst,fns)
		fns=mapbody(constfold,fns)
		fns=map(deadvar,fns)
		;TODO jump threading
	^vars,fns
