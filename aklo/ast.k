proceed=gensym("proceed")
fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg

fn mapfns(f, g,env,params,body)
	fn rec a
		case a
			'fn',name,params,rtype,@body
				body= mapfns(f, g,env,params,body)
				^'fn',name,params,rtype,@body
			'\\',params,@body
				body= mapfns(f, g,env,params,body)
				^'\\',params,@body
			'quote',_
				;leaf node
				0
			[@_]
				;branch node
				^map(rec,a)
		;leaf node
		^a

	env=f(env)
	body=rec(body)
	^g(env,params,body)

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
			id
			\(env,params,body)(
				'loc',@loc=body[0]
				explicit=keys(params)
				if uniq(explicit)!=explicit
												err(loc,"duplicate parameter name")
				implicit=[]
				mapterms(
					\(loc,a)(
						case a
							'var',x,@_
								explicit<<x
							'when',x
							'=',x,_
								mapterms(
									\(loc,a)(
										case a
											'quote',_
												0
											'@',_
											'list',@_
												^proceed
											[@_]
												err(loc,"invalid assignment")
											_
												if sym?(a)
													implicit<<a
									)
									loc
									x
								)
						^proceed
					)
					loc
					body
				)
				s=[]
				for x  :uniq(implicit)
					if!in(x,explicit)
						s<<'var',x
				^[body[0]]@s@tail(body)
			)
			0
			[]
			module
		))
		modules
	)

	;check for inconsistent declarations
	for module:vals(modules)
		mapfns(
			id
			\(env,params,body)(
				'loc',@loc=body[0]
				nonlocals=[]
				locals=keys(params)
				fns=[]
				mapterms(
					\(loc,a)(
						case a
							'fn',x,@_
								if in(x,nonlocals)|in(x,locals)
									err(loc,str(x)@" is both a function and a variable")
								if in(x,fns)
									err(loc,str(x)@" defined twice")
								fns<<x
							'var',x,@_
								if in(x,fns)
									err(loc,str(x)@" is both a function and a variable")
								if in(x,nonlocals)
									err(loc,str(x)@" is both nonlocal and local")
								if in(x,locals)
									err(loc,str(x)@" defined twice")
								locals<<x
							':=',x,_
								mapterms(
									\(loc,a)(
										case a
											'quote',_
												0
											'@',_
											'list',@_
												^proceed
											[@_]
												err(loc,"invalid assignment")
											_
												if sym?(a)
													if in(x,fns)
														err(loc,str(x)@" is both a function and a variable")
													if in(x,locals)
														err(loc,str(x)@" is both nonlocal and local")
													nonlocals<<x
									)
									loc
									x
								)
						^proceed
					)
					loc
					body
				)
				^[]
			)
			0
			[]
			module
		)

	;Hoist declarations to start
	show modules[0]
	modules=mapsecond(
		\(module)(mapfns(
			id
			\(env,params,body)(
				'loc',@loc=body[0]
				vars=[]
				fns=[]
				body=mapterms(
					\(loc,a)(
						case a
							'var',@_
								vars<<a
								^0
							'fn',@_
								fns<<a
								^0
						^proceed
					)
					loc
					body
				)
				^[body[0]]@vars@fns@tail(body)
			)
			0
			[]
			module
		))
		modules
	)
	show modules[0]

	;rename local variables and functions to unique symbols

	^modules
