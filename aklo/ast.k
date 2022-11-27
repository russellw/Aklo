fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg

proceed=gensym("proceed")
fn mapterm f,loc,a
	fn rec a
		case a
			'.loc',@loc1
				loc:=loc1
				^a
			'fn',@_
			'\\',@_
			'.quote',_
				;leaf node
				0
			[@_]
				b=f(loc,a)
				if b==proceed
					;branch node
					^map(rec,a)
				;leaf node
				^b
		;leaf node
		b=f(loc,a)
		if b==proceed
			^a
		^b

	^rec(a)

fn localvars params ,body
	nonlocals=[]
	locals=keys(params)
	'.loc',@loc=body[0]

	fn addnonlocal(loc,x)
					if in(x,locals)
						err(loc,str(x)@" assigned both nonlocally and locally")
					nonlocals<<x


	fn addlocal(loc,x)
					if in(x,nonlocals)
						err(loc,str(x)@" assigned both nonlocally and locally")
					locals<<x

	mapterm(
		\(loc,a)(
			fn lhs(f,a)
				mapterm(
					\(loc,a)(
						case a
							'.quote',_
								^0
							'@',_
							'.list',@_
								0
							[@_]
								err(loc,"invalid assignment")
							_
								if sym?(a)
									f(loc,a)
						^proceed
					)
					loc
					a
				)

			case a
				'var',x,@_
					addlocal(x)
				'=',x,_
					lhs(addlocal,x)
				':=',x,_
					lhs(addnonlocal,x)
			^proceed
		)
		loc
		body
	)
	^uniq(locals)

fn localfns body
	'.loc',@loc=body[0]
	r=[]
	mapterm(
		\(loc,a)(
			case a
				'fn',name,@_
					if in(name,r)
						err(loc,str(name)@" defined twice")
					r<<name
			^proceed
		)
		loc
		body
	)
	^r

;rename local variables and functions to unique symbols
fn renlocals module
	fn renfn(env,name,params,rtype,body)
		^name,params,rtype,@body

	show localvars([],module)
	show localfns(module)
	^module

fn norm modules
	modules=mapsecond(renlocals,modules)
	^modules
