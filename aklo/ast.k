fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg

fn walk f,s
	loc=0

	fn rec a
		;unlike map, we here work top down
		; so the callback function can signal stop if needed
		if f(loc,a)
			^

		;recur?
		case a
			'.loc',@loc1
				;track the current source location
				loc:=loc1
			'fn',@_
			'\\',@_
				;don't recur into local functions
				0
			[@s]
				;recur into other compound terms
				map(rec,s)

	map(rec,s)

;rename local variables and functions to unique symbols
fn renlocals module
	fn assignedvars params ,body
		nonlocals=[]

		;parameters are local variables
		locals=keys(params)

		;track the current source location
		;parameters are located at the function declaration
		'.loc',@loc=body[0]
		locs=map(\(x)(x,loc),locals)

		fn addlocal(x)
						locs<<x,loc
						locals<<x

		fn addnonlocal(x)
						locs<<x,loc
						nonlocals<<x



		;gather all the explicit and implicit local variables
		; and all the assignments to nonlocal variables
		walk(
			\(loc,a)(
				r=[]
				fn lhs(a)
					case a
						'.quote',_
							0
						'@',b
							r<<b
						'.list',@s
							map(lhs,s)
						[@_]
							err(loc,"invalid assignment")
						_
							if sym?(a)
								r<<a

				case a
					'var',x,@_
						addlocal(x)
					'=',x,_
						lhs(x)
						map(addlocal,x)
					':=',x,_
						lhs(x)
						map(addnonlocal,x)
			)
			body
		)

		;is any variable assigned both nonlocally and locally?
		conflicts=intersection(nonlocals,locals)
		if conflicts
			x=conflicts[0]
			err(get (locs,x),str(x)@" assigned both nonlocally and locally")

		;return a set of unique local variable names
		^uniq(locals)

	fn renfn(context,name,params,rtype,body)
		^name,params,rtype,@body

	show assignedvars([],module)
	^module

fn norm modules
	modules=mapsecond(renlocals,modules)
	^modules
