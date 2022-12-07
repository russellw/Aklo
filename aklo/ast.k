proceed=gensym("proceed")
fn splitdecls (s)
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

fn mapfns(f,params,loc,body)
	;recursively transform local functions
	body=mapterms(
		\(_,a)(
			case a
				'fn',name,params,rtype,loc,@body
					body= mapfns(f,params,loc,body)
					^'fn',name,params,rtype,loc,@body
				'lambda',params,loc,@body
					body= mapfns(f,params,loc,body)
					^'lambda',params,loc,@body
			^proceed
		)
		loc
		body
	)

	;transform the current function
	^f(params,loc,body)

fn mapterms (f,loc,a)
	fn rec( a)
		case a
			'loc',@loc1
				;leaf node that will not be processed
				loc:=loc1
				^a
			'/',name,@s
				;branch node that may also be processed
				b=f(loc,a)
				if b==proceed
					^'/',name,@map(rec,s)
				^b
			'fn',@_
			'lambda',@_
			'quote',_
				;leaf node that may still be processed
				0
			[op,@s]
				;branch node that may also be processed
				b=f(loc,a)
				if b==proceed
					^op,@map(rec,s)
				^b
		;leaf node that may still be processed
		b=f(loc,a)
		if b==proceed
			^a
		^b

	^rec(a)


fn norm (modules)
	fn mapmodules(f)
		^mapsecond(
			\(module)(
				;processing a module is like processing a function body with no parameters
				loc,@body=module
				body=mapfns(f,[],loc,body)
				^loc,@body
			)
			modules
		)

	;generate explicit variable declarations
	modules=mapmodules(
		\(params,loc,body)(
			explicit=keys(params)
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
												implicit<<a,loc
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
			for x  :uniq(keys(implicit))
				if!in(x,explicit)
					s<<'var',x,'any',get(implicit,x)
			^s@body
		)
	)

	;check for inconsistent declarations
	mapmodules(
		\(params,loc,body)(
			nonlocals=[]
			locals=keys(params)
			if uniq(locals)!=locals
				err(loc,"duplicate parameter name")
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
			^body
		)
	)

	;expand lambdas
	modules=mapmodules(
		\(params,loc,body)(
			^mapterms(
				\(loc,a)(
					case a
						'lambda',params,@body
							y=gensym("lambda")
							^'do',('fn',y,params,'any',loc,@body),y
					^proceed
				)
				loc
				body
			)
		)
	)

	;Hoist declarations to start
	modules=mapmodules(
		\(params,loc,body)(
			vars=[]
			fns=[]
			body=mapterms(
				\(loc,a)(
					case a
						'var',name,type,loc
							vars<<a
							^0
						'var',name,type,loc,val
							vars<<'var',name,type,loc
							^'=',name,val
						'fn',@_
							fns<<a
							^0
					^proceed
				)
				loc
				body
			)
			^vars@fns@body
		)
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

			fn rec( env,params,loc,body)
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
						'fn',name,params,rtype,loc,@body=a
						params,body=rec(env,params,loc,body)
						^'fn',name,params,rtype,loc,@body
					)
					fns
				)

				;rename labels
				labels=[]
				mapterms(
					\(loc,a)(
							case a
								':',x
									y=gensym(str(x))
									labels<<x,y
							^proceed
					)
					loc
					body
				)

				;resolve names in body of this function
				body=mapterms(
					\(loc,a)(
							;TODO type names
							if sym?(a)
								y=get(env,a)
								if!y
									err(loc,str(a)@" not found")
								^y
							case a
								'.',@names
									prefix=modname
									while prefix
										prefix=init(prefix)
										y=get(globals,prefix@names)
										if  y
											^y
									err(loc,join(".",map(str,names))@" not found")
								':',x
								'goto',x
									y=get(labels,x)
									if!y
										err(loc,str(x)@" not found")
									^a[0],y
							^proceed
					)
					loc
					body
				)

				;return renamed entities as well as function body with updated names
				^params,vars@fns@body

			loc,@body=module
			_,body=rec(env,[],loc,body)
			^modname,[loc]@body
		)
		modules
	)

	;Having resolved names, we can concatenate modules without ambiguity
	program=concat(map(tail,vals(modules)))

	;expand to three-address code
	loc=modules[0][1][0]
	program=mapfns(
		\(params,loc,body)(
			vars,fns,body=splitdecls(body)
			code=[]

			fn genvar(name)
				a=gensym(name)
				vars<<'var',a,'any',loc
				^a

			fn term (loop,a)
				fn rec(a)
					^term(loop,a)

				fn assign (y,x)
					x=rec(x)
					code<<'=',y,x

				fn if1(test,yes,no)
					yeslabel = gensym("ifyes")
					nolabel = gensym("ifno")
					afterlabel = gensym("ifafter")
					r = genvar("ifresult")

					;test
					test=rec(test)
					code<<'if',test,yeslabel
					code<<'goto',nolabel

					;yes
					code<<':',yeslabel
					assign(r,yes)
					code<<'goto',afterlabel

					;no
					code<<':',nolabel
					assign(r,no)

					;after
					code<<':',afterlabel
					^r

				case a
					'post++',y
						r=genvar("postinc")
						assign(r,y)
						assign(y,('+',y,1))
						^r
					'post--',y
						r=genvar("postdec")
						assign(r,y)
						assign(y,('-',y,1))
						^r
					'++',y
						assign(y,('+',y,1))
						^y
					'--',y
						assign(y,('-',y,1))
						^y
					'+=',y,x
						assign(y,('+',y,x))
					'-=',y,x
						assign(y,('-',y,x))
					'do',@s
						map(rec,s)
					'=',y,x
						assign(y,x)
					'loc',@loc1
						loc:=loc1
						code<<a
					'^',x
						x=rec(x)
						code<<'^',x
					'!',x
						^if1(x,0,1)
					'if',test,yes,no
						if1(test,yes,no)
					'/',name,@s
						s=map(rec,s)
						r=genvar(str(name))
						code<<'=',r,('/',name,@s)
						^r
					'|',x,y
						r=genvar("or")
						assign(r,x)
						^if1(r,r,y)
					'&',x,y
						r=genvar("and")
						assign(r,x)
						^if1(r,y,r)
					'while',test,@body
						bodylabel = gensym("whilebody")
						testlabel = gensym("whiletest")
						afterlabel = gensym("whileafter")
						loop = testlabel, afterlabel

						code<<'goto',testlabel

						;body
						code<<':',bodylabel
						map(rec,body)

						;test
						code<<':',testlabel
						test=rec(test)
						code<<'if',test,bodylabel

						;after
						code<<':',afterlabel
					'dowhile',test,@body
						bodylabel = gensym("dowhilebody")
						testlabel = gensym("dowhiletest")
						afterlabel = gensym("dowhileafter")
						loop = testlabel, afterlabel

						;body
						code<<':',bodylabel
						map(rec,body)

						;test
						code<<':',testlabel
						test=rec(test)
						code<<'if',test,bodylabel

						;after
						code<<':',afterlabel
					'assert',test,msg
						test=rec(test)
						code<<'assert',test,msg
					'goto',_
					':',_
						code<<a
					'bitand',@s
					'bitor',@s
					'bitxor',@s
					'shl',@s
					'shr',@s
					'bitnot',@s
					'neg',@s
					'%',@s
					'//',@s
					'+',@s
					'-',@s
					'*',@s
					'<',@s
					'<=',@s
					'==',@s
						s=map(rec,s)
						r=genvar("")
						code<<'=',r,(a[0],@s)
						^r
					[@_]
						throw repr(a)
					_
							^a

			map(\(a)(term((0,0),a)),body)
			^vars@fns@code
		)
		[]
		loc
		program
	)

	;TODO: properly flatten
	r='fn','main',[],'int',loc,@program
	r<<'^',0
	^[r]
