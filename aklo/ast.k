proceed=sym("proceed")
fn err (loc,msg)
	file,line,fname=loc
	throw file @ ":" @ str(line)@ ": "@fname  @ ": " @ msg

fn mapFns(f,loc,params,body)
	;recursively transform local functions
	body=mapTerms(
		\(_,a)(
			case a
				'fn',loc,name,params,rtype,@body
					body= mapFns(f,loc,params,body)
					^'fn',loc,name,params,rtype,@body
				'lambda',loc,params,@body
					body= mapFns(f,loc,params,body)
					^'lambda',loc,params,@body
			^proceed
		)
		loc
		body
	)

	;transform the current function
	^f(loc,params,body)

fn mapTerms (f,loc,a)
	fn rec( a)
		case a
			'loc',@loc1
				;leaf node that will not be processed
				loc:=loc1
				^a
			'xcall',name,@s
				;branch node that may also be processed
				b=f(loc,a)
				if b==proceed
					^'xcall',name,@map(rec,s)
				^b
			'fn',@_
			'lambda',@_
			'quote',_
				;leaf node that may still be processed
				0
			[[@_],@_]
				;list of terms
				^map(rec,a)
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
	fn mapModules(f)
		^mapSecond(
			\(module)(
				;processing a module is like processing a function body with no parameters
				loc,@body=module
				body=mapFns(f,loc,[],body)
				^loc,@body
			)
			modules
		)

	;generate explicit variable declarations
	modules=mapModules(
		\(loc,params,body)(
			explicit=vals(params)
			implicit=[]
			mapTerms(
				\(loc,a)(
					case a
						'var',_,x,@_
							explicit<<x
						'for',x,@_
						'when',x
						'=',x,_
							mapTerms(
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
											if sym?(a)&!in(a,keys(implicit))
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
			for x,loc  :implicit
				if!in(x,explicit)
					s<<'var',loc,x,'any'
			^s@body
		)
	)

	;check for inconsistent declarations
	mapModules(
		\(loc,params,body)(
			nonlocals=[]
			locals=vals(params)
			;TODO more specific?
			if uniq(locals)!=locals
				err(loc,"duplicate parameter name")
			fns=[]
			mapTerms(
				\(loc,a)(
					case a
						'fn',loc,x,@_
							if in(x,nonlocals)|in(x,locals)
								err(loc,str(x)@" is both a function and a variable")
							if in(x,fns)
								err(loc,str(x)@" defined twice")
							fns<<x
						'var',loc,x,@_
							if in(x,fns)
								err(loc,str(x)@" is both a function and a variable")
							if in(x,nonlocals)
								err(loc,str(x)@" is both nonlocal and local")
							if in(x,locals)
								err(loc,str(x)@" defined twice")
							locals<<x
						':=',x,_
							mapTerms(
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
	modules=mapModules(
		\(loc,params,body)(
			^mapTerms(
				\(loc,a)(
					case a
						'lambda',loc,params,@body
							y=sym("lambda")
							^'do',('fn',loc,y,params,'any',@body),y
					^proceed
				)
				loc
				body
			)
		)
	)

	;Hoist declarations to start
	modules=mapModules(
		\(loc,params,body)(
			vars=[]
			fns=[]
			body=mapTerms(
				\(loc,a)(
					case a
						'var',loc,name,type
							vars<<loc,name,type
							^0
						'var',loc,name,type,val
							vars<<loc,name,type
							^'=',name,val
						'fn',@s
							fns<<s
							^0
					^proceed
				)
				loc
				body
			)
			^vars,fns,body
		)
	)

	;generate new symbols for global names
	;this must be done for all modules before resolving names in any of them
	; otherwise there would be a problem when
	; an earlier module refers to a name defined in a later one
	globals=[]
	for  modName,(_,vars,fns,_):modules
		for _,x,@_  :vars@fns
								y=sym(str(x))
								globals<<modName@[x],y

	;ubiquitous names are in the outer environment of every module
	env=[]
	for modName,y:globals
		case modName
			'aklo','ubiquitous',x
				env<<x,y

	;resolve names
	modules=map(
		\(kv)(
			;need the module name to resolve dotted names relative to current module
			modName,module=kv

			fn rec( env,loc,params,vars,fns,body)
				fn decl(a)
						loc,x,@s=a
						y=sym(str(x))
						x,y>>env
						^loc,y,@s

				; rename locals
				;functions can call each other
				; so need to rename all functions and update the environment appropriately
				; before recursing into any function
				params=map(decl,params)
				vars=map(decl,vars)
				fns=map(decl,fns)

				;recurse into functions
				fns=map(
					\(a)(
						loc,name,params,rtype,vars,fns,@body=a
						params,vars,fns,body=rec(env,loc,params,vars,fns,body)
						^loc,name,params,rtype,vars,fns,@body
					)
					fns
				)

				;rename labels
				labels=[]
				mapTerms(
					\(loc,a)(
							case a
								':',x
									y=sym(str(x))
									labels<<x,y
							^proceed
					)
					loc
					body
				)

				;resolve names in body of this function
				body=mapTerms(
					\(loc,a)(
							;TODO type names
							if sym?(a)
								y=get(env,a)
								if!y
									err(loc,str(a)@" not found")
								^y
							case a
								'.',@names
									prefix=modName
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
				^params,vars,fns,body

			loc,vars,fns,body=module
			_,vars,fns,body=rec(env,loc,[],vars,fns,body)
			^modName,[loc,vars,fns,body]
		)
		modules
	)

	;Having resolved names, we can concatenate modules without ambiguity
	vars=[]
	fns=[]
	body=[]
	for _,vars1,fns1,body1:vals(modules)
		;TODO Should this be doable in one operation?
		vars@=vars1
		fns@=fns1
		body@=body1

	;expand to three-address code
	loc,@_=modules[0][1]
	vars,fns,body=mapFns(
		\(loc,params,body)(
			vars,fns,body=body
			code=[]

			fn makeVar(name)
				a=sym(name)
				vars<<loc,a,'any'
				^a

			fn term (loop,a)
				fn rec(a)
					^term(loop,a)

				fn assign (y,x)
					x=rec(x)
					code<<'=',y,x

				fn if1(test,yes,no)
					yesLabel = sym("ifYes")
					noLabel = sym("ifNo")
					afterLabel = sym("ifAfter")
					r = makeVar("ifResult")

					;test
					test=rec(test)
					code<<'if',test,yesLabel
					code<<'goto',noLabel

					;yes
					code<<':',yesLabel
					assign(r,yes)
					code<<'goto',afterLabel

					;no
					code<<':',noLabel
					assign(r,no)

					;after
					code<<':',afterLabel
					^r

				case a
					'post++',y
						r=makeVar("postInc")
						assign(r,y)
						assign(y,('+',y,1))
						^r
					'post--',y
						r=makeVar("postDec")
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
					'throw',x
					'^',x
						x=rec(x)
						code<<a[0],x
					'!',x
						^if1(x,0,1)
					'if',test,yes,no
						if1(test,yes,no)
					'xcall',name,@s
						s=map(rec,s)
						r=makeVar(str(name))
						code<<'=',r,('xcall',name,@s)
						^r
					'|',x,y
						r=makeVar("or")
						assign(r,x)
						^if1(r,r,y)
					'&',x,y
						r=makeVar("and")
						assign(r,x)
						^if1(r,y,r)
					'while',test,@body
						bodyLabel = sym("whileBody")
						testLabel = sym("whileTest")
						afterLabel = sym("whileAfter")
						loop = testLabel, afterLabel

						code<<'goto',testLabel

						;body
						code<<':',bodyLabel
						map(rec,body)

						;test
						code<<':',testLabel
						test=rec(test)
						code<<'if',test,bodyLabel

						;after
						code<<':',afterLabel
					'dowhile',test,@body
						bodyLabel = sym("dowhileBody")
						testLabel = sym("dowhileTest")
						afterLabel = sym("dowhileAfter")
						loop = testLabel, afterLabel

						;body
						code<<':',bodyLabel
						map(rec,body)

						;test
						code<<':',testLabel
						test=rec(test)
						code<<'if',test,bodyLabel

						;after
						code<<':',afterLabel
					'goto',_
					':',_
						code<<a
					'bitAnd',@s
					'bitOr',@s
					'bitXor',@s
					'shl',@s
					'shr',@s
					'bitNot',@s
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
						r=makeVar("")
						code<<'=',r,(a[0],@s)
						^r
					'quote',_
						^a
					[[@_],@_]
						^map(rec,a)
					[@_]
						throw repr(a)
					_
							^a

			map(\(a)(term((0,0),a)),body)
			^vars,fns,code
		)
		loc
		[]
		vars,fns,body
	)

	;TODO: properly flatten
	body<<'^',0
	r=loc,'main',[],'int',vars,body

	vars=[]
	fns=[r]
	^vars,fns
