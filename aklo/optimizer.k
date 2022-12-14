fn labelUsers(body,label)
	r=[]
	for i:range(body)
		a=body[i]
		case a
			'if',_,label1
			'goto',label1
				if label==label1
					r<<i
	^r

fn deadVar(F)
	loc,name,params,rtype,vars,body=F
	vars=filter(
		\(a)(
			_,x,@_=a
			^localUsers(body,x)
		)
		vars
	)
	^loc,name,params,rtype,vars,body

fn mapBody(f,fns)
	^map(
		\(F)(
			loc,name,params,rtype,vars,body=F
			body=f(body)
			^loc,name,params,rtype,vars,body
		)
		fns
	)

fn redundantLoc(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'loc',@_
				case body[i+1]
					'loc',@_
						s<<i
	^filter(\(_,i)(!in(i,s)),body)


fn localUsers(body,a)
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

fn redundantGoto(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'goto',label
				case body[i+1]
					':',label1
						if label==label1
							s<<i
	^filter(\(_,i)(!in(i,s)),body)

fn deadLabel(body)
	^filter(
		\(a)(
			case a
				':',label
					^labelUsers(body,label)
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
				'bitAnd',@_
				'bitOr',@_
				'bitXor',@_
				'bitNot',@_
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

fn labelIdx(body,label)
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
					g<<i,labelIdx(body,label)
				'if',_,label
					g<<i,labelIdx(body,label)
					g<<i,i+1
				_
					g<<i,i+1
	^g

fn deadCode(body)
	g=cfg(body)
	^filter(\(a,i)(graph.reaches(g,0,i)),body)

fn ifConst(body)
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
				'bitNot',x
					^bitNot(x)
				'bitAnd',x,y
					^bitAnd(x,y)
				'bitOr',x,y
					^bitOr(x,y)
				'bitXor',x,y
					^bitXor(x,y)
				'shl',x,y
					^shl(x,y)
				'shr',x,y
					^shr(x,y)
			^a

fn constFolding(body)
	^map(
		\(a)(
			case a
				'=',y,x
					^'=',y,eval(x)
			^a
		)
		body
	)

fn dataFlow(body)
	;https://en.wikipedia.org/wiki/Data-flow_analysis
	;https://en.wikipedia.org/wiki/Reaching_definition
	g=cfg(body)

	;in and out sets start off empty at all nodes
	in=replicate(len(g),[])
	out=in

	fn join(ss)
		^uniq(concat(ss))

	fn gen(i)
		case  body[i]
			'=',@_
				^[i]
		^[]

	fn kill(i,s)
		case  body[i]
			'=',x,_
				;if this node assigns a value to a certain variable
				; any incoming definition of that variable can be killed
				r=[]
				for j:s
					case body[j]
						'=',y,_
							;only keep the definitions that are not killed
							if x!=y
								r<<j
				^r
		^s

	fn trans(i,s)
		^union(gen(i),difference(s,kill(i,s)))

	;iterate to fixed point
	dowhile old!=(in,out)
		old=in,out
		in1=[]
		out1=[]
		for i:range(g)
			in1<<join(map(\(j)(out[j]),graph.predecessors(g,i)))
			out1<<trans(i,in[i])
		in=in1
		out=out1
	^in

fn deadStore(body)
	^map(
		\(a)(
			case a
				'=',y,x
					if !localUsers(body,y)
						^x
			^a
		)
		body
	)

fn optimize(vars,fns)
	dowhile old!=(vars,fns)
		old=vars,fns
		fns=mapBody(redundantLoc,fns)
		fns=mapBody(deadCode,fns)
		fns=mapBody(redundantGoto,fns)
		fns=mapBody(deadLabel,fns)
		fns=mapBody(deadStore,fns)
		fns=mapBody(nop,fns)
		fns=mapBody(ifConst,fns)
		fns=mapBody(constFolding,fns)
		fns=map(deadVar,fns)
		;TODO jump threading
	^vars,fns
