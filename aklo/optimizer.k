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
			'goto',label1
				if label==label1
					r<<i,a
	^r

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
		if body[i][0]=='loc'& body[i+1][0]=='loc'
			s<<i
	^filteri(\(i)(!in(i,s)),body)

fn next(body,i)
	dowhile body[i][0]=='loc'
		i++
	^i

fn emptygotos(body)
	s=[]
	for i : range(len(body)-1)
		case body[i]
			'goto',label
				case body[next(body,i)]
					':',label1
						if label==label1
							s<<i
	^filteri(\(i)(!in(i,s)),body)

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

fn optimize(gvars,fns)
	dowhile old!=(gvars,fns)
		old=gvars,fns
		fns=mapbody(emptylocs,fns)
		fns=mapbody(emptygotos,fns)
		fns=mapbody(unusedlabels,fns)
	^gvars,fns
