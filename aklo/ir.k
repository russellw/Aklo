fn fns(program)
	r=[]
	for  a:program
		case a
			'fn',@_
				r<<a
	^r

fn insts(program)
	r=[]
	for 'fn',name,params,rtype,loc,@body:fns(program)
		for i:range(body)
			r<<name,i,body[i]
	^r
