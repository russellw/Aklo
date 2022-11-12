modules=()

fn parse modname, file,text
	;TODO: repr
	;TODO: check for already parsed
	text=replace("\r\n","\n",text)
	text=replace("\r","\n",text)
	if!endswith(text,"\n")
		text@="\n"

	i = 0
	line = 1

	dentc = 0
	cols = [0,]
	dedents = 0

	tok = 0

	fn err msg
		eprn(file @ ":" @ str(line) @ ": " @ msg)
		exit 1

	fn errtok(msg)
		if tok == "\n"
			line--
		err(repr(tok)@": "@msg)

	; tokenizer
	fn lex()
		;a single newline can produce multiple dedent tokens
		if dedents
			dedents--
			tok:= ".dedent"
			^

		while i < len(text)
			j = i

			;the simplest tokens are just one character
			tok:= text!i,

			; newline
			if tok == "\n"
				; next line
				i++
				line++

				; measure indent
				col = 0
				while in (text!i ,"\t ")
					if text!i != dentc & dentc
						err("indented with tabs and spaces in same file")
					dentc := text!i++
					col++

				; nothing important on this line, keep going
				if in (text!i ,"\n;{")
					continue

				; one indent
				if col > last(cols)
					cols<<col
					tok:= ".indent"
					^

				; zero or more dedents
				while col < last(cols)
					cols:=init(cols)
					dedents++
				if col != last(cols)
					err("inconsistent indent")
				^

			; space
			if isspace(text!i)
				i++
				continue

			; comment
			if tok == ";"
				while text!i != "\n"!0
					i++
				continue
			if tok == "{"
				i ++
				line1 = line
				while text!i != "}"!0
					if i == len(text)
						line := line1
						err("unclosed block comment")
					if text!i == "\n"!0
						line++
					i++
				i ++
				continue

			; symbol or string
			case tok
				"'"
				"\""
					q = text!i++
					while text!i != q
						if text!i == "\\"!0
							i++
						if text!i == "\n"!0
							err("unclosed quote")
						i++
					i++
					tok:= slice(text,j,i)
					^

			; raw string
			if slice(text,i,i+2) == "r\""
				i += 2
				while text!i != "\""!0
					if text!i == "\n"!0
						err("unclosed quote")
					i++
				i++
				tok:= slice(text,j,i)
				^

			; word
			if isidstart(text!i)
				while isidpart(text!i)
					i++
				tok:= slice(text,j,i)
				^

			; hexadecimal numbers are separate because they may contain 'e'
			if lower(slice(text,i,i+2)) == "0x"
				while isidpart(text!i)
					i++
				tok:= slice(text,j,i)
				^

			; other number
			if isdigit(text!i) | tok == "." & isdigit(text![i + 1])
				while isidpart(text!i)
					i++
				if text!i == "."!0
					i++
					while isidpart(text!i)
						i++
					if lower(text![i - 1]) == "e"!0 & in(text!i ,"+-")
						i++
						while isidpart(text!i)
							i++
				tok:= slice(text,j,i)
				^

			; punctuation
			punct = (
				"!=",
				":=",
				"**",
				"++",
				"+=",
				"--",
				"-=",
				"//",
				"<=",
				"==",
				">=",
				"@=",
				"<<",
			)
			for s  punct
				if slice(text,i , i + len(s)) == s
					i += len(s)
					tok:= s
					^

			if tok == "["
				tok:= "("
			if tok == "]"
				tok:= ")"
			i++
			^

		; end of file
		tok:= ".dedent"

	fn lex1()
		s = tok
		lex()
		^ s

	; parser
	fn eat(s)
		if tok == s
			lex()
			^ 1

	fn expect(s)
		if ! eat(s)
			errtok("expected "@repr s)

	fn word()
		if isidstart(tok!0)
			^ intern(lex1())
		errtok("expected word")

	;types
	fn istype()
		if tok=="&"
			^1
		^isidstart (tok!0)

	fn ptype
		if!istype()
			errtok"expected type"
		if tok=="&"
			^lex1(),ptype()
		^word()

	fn otype
		if !istype()
			^'any'
		^ptype()

	; expressions
	fn isprimary()
		^ isidpart(tok!0) | in(tok!0 ,"'\"")

	fn fbody(fname,fparams)
		fn primary()
			; symbol
			if startswith(tok,"'")
				s = unesc(slice(tok,1,len(tok)-1))
				lex()
				^ '.quote',intern(s)

			; string
			if startswith(tok,"\"")
				s = unesc(slice(tok,1,len(tok)-1))
				lex()
				^'.list',@s

			; raw string
			if startswith(tok,"r\"")
				s = slice(tok,2,len(tok)-1)
				lex()
				^'.list',@s

			; word
			case tok
				"false"
					lex()
					^ 0
				"true"
					lex()
					^ 1
			if isidstart(tok!0)
				^ lex1()

			; number
			if isdigit(tok!0)
				s=replace("_","",tok)
				case lower(take(2,s))
					"0b"
						a = parseint(drop(2,s), 2)
					"0o"
						a = parseint(drop(2,s), 8)
					"0x"
						a = parseint(drop(2,s), 16)
					_
						a = parseint(s,10)
				lex()
				^ a

			; bracketed expression or list
			if eat("(")
				if eat(".indent")
					s =  '.list',
					while ! eat(".dedent")
						s<<expr()
						eat(",")
						expect("\n")
					expect(")")
					^ s
				if eat(")")
					^ '.list',
				a = commas()
				expect(")")
				^ a

			; none of the above
			errtok("expected expression")

		fn postfix()
			a = primary()
			while 1
				case tok
					"("
						lex()
						a = a,
						if eat(".indent")
							while ! eat(".dedent")
								a<<expr()
								eat(",")
								expect("\n")
							expect(")")
							continue
						while ! eat(")")
							a<<expr()
							if eat(")")
								break
							expect(",")
					"++"
					"--"
						^ intern("post" @ lex1()), a
					"."
						lex()
						field = word()
						if in(a,keys modules)
							a='.',a,field
						else
							a = "get", a, ('.quote',field)
					_
						;TODO is this useful enough to be worth keeping?
						if isprimary()
							a = [a, prefix()]
							while eat(",")
								a<<prefix()
						^ a

		fn param()
			^word(),otype()

		fn params()
			s = []
			case tok
				"("
					lex()
					if eat(".indent")
						while ! eat(".dedent")
							s<<param()
							eat(",")
							expect("\n")
						expect(")")
					else
						while ! eat(")")
							s<<param()
							if eat(")")
								break
							expect(",")
				".indent"
				":"
					0
				_
					while 1
						s<<param()
						if ! eat(",")
							break
			^ s

		fn prefix()
			case tok
				"!"
				"++"
				"--"
				"-"
				"@"
				"*"
				"&"
					^ intern(lex1()), prefix()
				"\\"
					line1=line
					s = [intern(lex1()),]

					fparams1=params()
					s<<fparams1

					expect"("
					s<<('.loc',file,line1,'\\')
					if eat(".indent")
						s@=fbody('\\',fparams1)
						expect(".dedent")
					else
						s<<commas()
					expect(")")
					^ s
			^ postfix()

		; operator precedence parser
		prec = 99
		ops = ()

		fn mkop(op, left)
			ops<<(op,( prec, left))

		mkop("!", 1)

		prec--
		mkop("**",0)

		prec--
		mkop("%", 1)
		mkop("*", 1)
		mkop("/", 1)
		mkop("//", 1)

		prec--
		mkop("+", 1)
		mkop("-", 1)
		mkop("@", 1)

		prec--
		mkop("!=", 1)
		mkop("<", 1)
		mkop("<=", 1)
		mkop("==", 1)
		mkop(">", 1)
		mkop(">=", 1)

		prec--
		mkop("&", 1)

		prec--
		mkop("|", 1)

		fn infix(prec)
			a = prefix()
			while 1
				prec1, left1 = get ops,tok
				if prec1 < prec
					^ a
				op = lex1()
				b = infix(prec1 + left1)
				case op
					">"
						a ='<', b, a
					">="
						a ='<=', b, a
					_
						a = intern(op), a, b

		fn expr()
			^ infix(1)

		; statements
		fn commas()
			a = expr()
			if tok != ","
				^ a
			s = ['.list', a]
			while eat(",")
				if !in(tok,(				")",".indent" , "\n"))
						s<<expr()
			^ s

		fn assignment()
			;TODO clear error message if the left-hand side is not assignable
			a = commas()
			case tok
				"="
				"+="
				":="
				"-="
				"@="
				"<<"
					^ intern(lex1()), a, commas()
			^ a

		fn block(loop)
			expect(".indent")
			s=()
			while ! eat(".dedent")
				s<<('.loc',file,line,fname)
				s<<stmt(loop)
			^ s

		fn if1(loop)
			assert  in(tok, ("if", "elif"))
			lex()
			s = ["if", expr()]
			s<<block(loop)
			case tok
				"elif"
					s<<if1(loop)
				"else"
					lex()
					s<<block(loop)
			^ s

		fn stmt(loop)
			continueok,breakok=loop
			s = [intern (tok),]
			case tok
				"assert"
					s<<file
					s<<line
					s<<fname
					lex()
					x = expr()
					s<<str(x)
					s<<x
					expect("\n")
					^ s
				"show"
					s<<file
					s<<line
					s<<fname
					lex()
					x = commas()
					s<<str(x)
					s<<x
					expect("\n")
					^ s
				"case"
					lex()
					s<<commas()
					expect(".indent")
					while ! eat(".dedent")
						patterns = [commas(),]
						while eat("\n")
							patterns<<commas()
						body = block((continueok,1))
						for pattern  patterns
							s<<(pattern, @body)
					^ s
				"dowhile"
				"while"
					lex()
					s<<expr()
					s@=block((1,1))
					^ s
				"for"
					lex()
					s<<word()
					s<<commas()
					s@=block((1,1))
					^ s
				"fn"
					line1=line
					lex()

					fname1=word()
					s<<fname1

					fparams1=params()
					s<<fparams1

					s<<otype()

					expect".indent"
					s<<('.loc',file,line1,fname1)
					s@=fbody(fname1,fparams1)
					expect".dedent"
					^ s
				"if"
					^ if1(loop)
				"^"
					lex()
					if eat("\n")
						^ '^', 0
					s<<commas()
					expect("\n")
					^ s
				"break"
					if!breakok
						err"break without case or loop"
					lex()
					if eat"\n"
						^'break'
					s<<word()
					expect("\n")
					^ s
				"continue"
					if!continueok
						err"continue without loop"
					lex()
					if eat"\n"
						^'continue'
					s<<word()
					expect("\n")
					^ s
			a = assignment()
			if eat(":")
				^ ":", a, stmt(loop)
			expect("\n")
			^ a

		; function/module body
		s = []
		while tok != ".dedent"
			s<<('.loc',file,line,fname)
			s<<stmt((0,0))
		^ s

	; top level
	lex()
	eat("\n")

	; imports
	while eat("import")
		name = word()
		;TODO: path
		file1= name @ ".k"
		parse(intern(name),file1,readfile  file1)
		expect("\n")

	; module
	;TODO: module name?
	modules<<(modname,(file,fbody(modname,())))
