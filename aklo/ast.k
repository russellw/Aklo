proceed=gensym("proceed")
fn splitdecls s
	vars=[]
	fns=[]
	body=[]
	for a:s
		case a
			'var',@_
				vars<<a
			'fn',@_
				fns<<a
			_
				body<<a
	^vars,fns,body
fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg

;TODO does this need to exist?
fn mapfns(f,env,params,body)
	fn rec a
		case a
			'fn',name,params,rtype,@body
				params,body= mapfns(f,env,params,body)
				^'fn',name,params,rtype,@body
			'\\',params,@body
				params,body= mapfns(f,env,params,body)
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
	^f(env,params,body)

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
	fn mapmodules(f,env)
		^mapsecond(
			\(module)(
				;processing a module is like processing a function body with no parameters
				_,module=mapfns(f,env,[],module)
				^module
			)
			modules
		)

	;generate explicit variable declarations
	modules=mapmodules(
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
						'for',x,@_
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
			^params,[body[0]]@s@tail(body)
		)
		0
	)

	;check for inconsistent declarations
	mapmodules(
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
			^params,body
		)
		0
	)

	;expand lambdas
	modules=mapmodules(
		\(env,params,body)(
			'loc',@loc=body[0]
			body=mapterms(
				\(loc,a)(
					case a
						'\\',params,@body
							y=gensym("lambda")
							^'do',('fn',y,params,'any',@body),y
					^proceed
				)
				loc
				body
			)
			^params,body
		)
		0
	)

	;Hoist declarations to start
	modules=mapmodules(
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
			^params,[body[0]]@vars@fns@tail(body)
		)
		0
	)

	;generate new symbols for global names
	;this must be done for all modules before resolving names in any of them
	; otherwise there would be a problem when
	; an earlier module refers to a name defined in a later one
	globals=[]
	for  modname,module:modules
		for a  :module
						case a
							'var',x,@_
							'fn',x,@_
								y=gensym(str(x))
								globals<<modname@[x],y

	;ubiquitous names are in the outer environment of every module
	env=[]
	for modname,y:globals
		case modname
			'aklo','ubiquitous',x
				env<<x,y

	;resolve names
	modules=map(
		\(kv)(
			;need the module name to resolve dotted names relative to current module
			modname,module=kv

			fn rec env,params,body
				;rename parameters
				params=map(
					\(x)(
						x,@s=x
						y=gensym(str(x))
						x,y>>env
						^y,@s
					)
					params
				)

				; rename locals
				;functions can  call each other
				; so need to rename all functions and update the environment appropriately
				; before recursing into any function
				vars,fns,body=splitdecls(body)
				fn decl(a)
						op,x,@s=a
						assert op=='var'|op=='fn'
						y=gensym(str(x))
						x,y>>env
						^op,y,@s
				vars=map(decl,vars)
				fns=map(decl,fns)

				;recurse into functions
				fns=map(
					\(a)(
						'fn',name,params,rtype,@body=a
						params,body=rec(env,params,body)
						^'fn',name,params,rtype,@body
					)
					fns
				)

				;resolve names in body of this function
				;TODO avoid double converting declarations, but make sure variables don't have initial value expressions
				;TODO do local variables have initial value expressions? what about global variables?
				'loc',@loc=body[0]
				body=mapterms(
					\(loc,a)(
							;TODO type names
							if sym?(a)
								b=get(env,a)
								if!b
									err(loc,str(a)@" not found")
								^b
							case a
								'.',@names
									prefix=modname
									while prefix
										prefix=init(prefix)
										r=get(globals,prefix@names)
										if  r
											^r
									err(loc,join(".",map(str,names))@" not found")
								'label',_
								'goto',_
									;labels will be resolved later
									^a
							^proceed
					)
					loc
					body
				)

				;return renamed entities as well as function body with updated names
				^params,vars@fns@body

			_,module=rec(env,[],module)
			^modname,module
		)
		modules
	)

	^modules
