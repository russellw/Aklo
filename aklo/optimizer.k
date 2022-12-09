fn insts(fns)
	r=[]
	for loc,name,params,rtype,vars,body:fns
		for i:range(body)
			r<<name,i,body[i]
	^r

fn labelusers(body,label)
	r=[]
	for i:range(body)
		a=body[i]
		case a
			'if',_,label1
			'goto',label1
				if label==label1
					r<<i,a
	^r

fn unusedvars(F)
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

fn emptylocs(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'loc',@_
				case body[i+1]
					'loc',@_
						s<<i
	^filter(\(_,i)(!in(i,s)),body)

fn next(body,i)
	while 1
		i++
		case body[i]
			'loc',@_
				0
			_
				^i

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

fn emptygotos(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'goto',label
				case body[next(body,i)]
					':',label1
						if label==label1
							s<<i
	^filter(\(_,i)(!in(i,s)),body)

fn unusedlabels(body)
	^filter(
		\(a)(
			case a
				':',label
					^labelusers(body,label)
			^1
		)
		body
	)

fn nops(body)
	^filter(
		\(a)(
			case a
				'xcall','parsedouble',_
				'quote',_
					^
			^list?(a)
		)
		body
	)

fn unusedassignments(body)
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

fn ifconsts(body)
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

fn optimize(gvars,fns)
	dowhile old!=(gvars,fns)
		old=gvars,fns
		fns=mapbody(emptylocs,fns)
		fns=mapbody(emptygotos,fns)
		fns=mapbody(unusedlabels,fns)
		fns=mapbody(unusedassignments,fns)
		fns=mapbody(nops,fns)
		fns=mapbody(ifconsts,fns)
		fns=map(unusedvars,fns)
	^gvars,fns
