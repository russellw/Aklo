fn labelUse(body,label)
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
			^varUse(body,x)
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
	redundant=[]
	for i : range(len(body)-1)
		case body[i]
			'loc',@_
				case body[i+1]
					'loc',@_
						redundant<<i
	^filterIdx(\(i,_)(!in(i,redundant)),body)


fn varUse(body,a)
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
	redundant=[]
	for i : range(len(body)-1)
		case body[i]
			'goto',label
				case body[i+1]
					':',label1
						if label==label1
							redundant<<i
	^filterIdx(\(i,_)(!in(i,redundant)),body)

fn deadLabel(body)
	^filter(
		\(a)(
			case a
				':',label
					^labelUse(body,label)
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
	^filterIdx(\(i,_)(graph.reaches(g,0,i)),body)

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
	In=replicate(len(g),[])
	Out=In


	fn gen(i)
		case  body[i]
			'=',@_
				^[i]
		^[]

	fn kill(i,s)
		case  body[i]
			'=',x,_
				;if this node defines a certain variable
				r=[]
				for j:s
					case body[j]
						'=',y,_
							; any incoming definition of that variable can be killed
							if x==y
								r<<j
				^r
		^[]

	fn trans(i,s)
		^union(gen(i),difference(s,kill(i,s)))

	;iterate to fixed point
	dowhile old!=(In,Out)
		old=In,Out
		In1=[]
		Out1=[]
		for i:range(g)
			In1<<unions(map(\(j)(Out[j]),graph.predecessors(g,i)))
			Out1<<trans(i,In[i])
		In=In1
		Out=Out1
	^In

fn deadStore(body)
	^mapIdx(
		\(i,a)(
			case a
				'=',y,x
					if !defUse(body,i)
						^x
			^a
		)
		body
	)

fn defUse(body,i)
	;the variable being defined
	'=',a,_=body[i]

	;which instructions does this definition reach
	reaching= dataFlow(body)

	;which instructions use this variable
	r=[]
	for j:varUse(body,a)
		;is this definition among those reaching that instruction
		if in(i,reaching[j])
			r<<j
	^r

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
