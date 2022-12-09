fn insts(fns)
	r=[]
	for loc,name,params,rtype,vars,body:fns
		for i:range(body)
			r<<name,i,body[i]
	^r

fn labelusers(fns,label)
	^filter(
		\(a)(
			case a
				'goto',label1
					^label==label1
		)
		insts(fns)
	)

fn unusedlabels(fns)
	^map(
		\(F)(
			loc,name,params,rtype,vars,body=F
			body=filter(
				\(a)(
					case a
						':',label
							^labelusers(fns,label)
					^1
				)
				body
			)
			^loc,name,params,rtype,vars,body
		)
		fns
	)

fn optimize(gvars,fns)
	fns=unusedlabels(fns)
	^gvars,fns
