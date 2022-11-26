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

	;parameters are local variables
	locals=keys(params)

	;parameters are located at the function declaration
	'.loc',@loc=body[0]
	locs=map(\(x)(x,loc),locals)

	fn addlocal(loc,x)
					locs<<x,loc
					locals<<x

	fn addnonlocal(loc,x)
					locs<<x,loc
					nonlocals<<x



	;gather all the explicit and implicit local variables
	; and all the assignments to nonlocal variables
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

	;is any variable assigned both nonlocally and locally?
	conflicts=intersection(nonlocals,locals)
	if conflicts
		x=conflicts[0]
		err(get (locs,x),str(x)@" assigned both nonlocally and locally")

	;return a set of unique local variable names
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

	show localfns(module)
	^module

fn norm modules
	modules=mapsecond(renlocals,modules)
	^modules
