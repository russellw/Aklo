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

fn unusedlabels(F)
			loc,name,params,rtype,vars,body=F
			body=filter(
				\(a)(
					case a
						':',label
							^labelusers(body,label)
					^1
				)
				body
			)
			^loc,name,params,rtype,vars,body

fn optimize(gvars,fns)
	fns=map(unusedlabels,fns)
	^gvars,fns
