proceed=gensym("proceed")
fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg
fn localfns body
	'loc',@loc=body[0]
	r=[]
	mapterms(
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

fn mapfns( f,params,body)
	fn rec a
		case a
			'fn',name,params,rtype,@body
				body= mapfns( f,params,body)
				^'fn',name,params,rtype,@body
			'\\',params,@body
				body= mapfns( f,params,body)
				^'\\',params,@body
			'quote',_
				;leaf node
				0
			[@_]
				;branch node
				^map(rec,a)
		;leaf node
		^a

	body=rec(body)
	^f(params,body)

fn mapterms f,loc,a
	fn rec a
		case a
			'loc',@loc1
				loc:=loc1
				^a
			'fn',@_
			'\\',@_
			'quote',_
				;leaf node
				0
			[op,@s]
				b=f(loc,a)
				if b==proceed
					;branch node
					^op,@map(rec,s)
				;leaf node
				^b
		;leaf node
		b=f(loc,a)
		if b==proceed
			^a
		^b

	^rec(a)


fn norm modules
	;generate explicit variable declarations
	modules=mapsecond(
		\(module)(mapfns(
			\(params,body)(
				nonlocals=[]
				locals=keys(params)
				'loc',@loc=body[0]

				fn addnonlocal(loc,x)
								if in(x,locals)
									err(loc,str(x)@" assigned both nonlocally and locally")
								nonlocals<<x


				fn addlocal(loc,x)
								if in(x,nonlocals)
									err(loc,str(x)@" assigned both nonlocally and locally")
								locals<<x

				mapterms(
					\(loc,a)(
						fn lhs(f,a)
							mapterms(
								\(loc,a)(
									case a
										'quote',_
											^0
										'@',_
										'list',@_
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
							'when',x
							'=',x,_
								lhs(addlocal,x)
							':=',x,_
								lhs(addnonlocal,x)
						^proceed
					)
					loc
					body
				)
				s=[]
				for x  :uniq(locals)
					s<<'var',x
				^s@body
			)
			[]
			module
		))
		modules
	)
	show modules[0]

	;rename local variables and functions to unique symbols
	fn renlocals module
		fn renfn(env,name,params,rtype,body)
			^name,params,rtype,@body

		show localfns(module)
		^module

	;modules=mapsecond(renlocals,modules)

	^modules
