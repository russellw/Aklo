;TODO reentrant?
modules=()

fn parse modname, file,text
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
		throw file @ ":" @ str(line) @ ": " @ msg

	fn errtok(msg)
		if tok == "\n"
			line--
		err( repr(tok)@": "@msg)

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
				if i == len(text)
					^
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
		r = tok
		lex()
		^ r

	; parser
	fn eat(s)
		if tok == s
			lex()
			^ 1

	fn expect(s)
		if ! eat(s)
			errtok("expected "@repr (s))

	fn word()
		;TODO reserve keywords?
		;depends on external function call syntax
		; External function names might need to overlap with keywords
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
			errtok("expected type")
		if tok=="&"
			^lex1(),ptype()
		^word()

	fn otype
		if !istype()
			^'any'
		^ptype()

	; expressions
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
					r =  '.list',
					while ! eat(".dedent")
						r<<expr()
						eat(",")
						expect("\n")
					expect(")")
					^ r
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
						a=intern(lex1()),a,word()
					_
						^ a

		fn param()
			^word(),otype()

		fn params()
			r = []
			case tok
				"("
					lex()
					if eat(".indent")
						while ! eat(".dedent")
							r<<param()
							eat(",")
							expect("\n")
						expect(")")
					else
						while ! eat(")")
							r<<param()
							if eat(")")
								break
							expect(",")
				".indent"
					0
				_
					while 1
						r<<param()
						if ! eat(",")
							break
			^ r

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
					r = [intern(lex1()),]

					fparams1=params()
					r<<fparams1

					expect("(")
					r<<('.loc',file,line1,'\\')
					if eat(".indent")
						r@=fbody('\\',fparams1)
						expect(".dedent")
					else
						r<<'^',commas()
					expect(")")
					^ r
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
				if!in(tok,keys(ops))
					^a
				prec1, left1 = get (ops,tok)
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
			r = ['.list', a]
			while eat(",")
				if !in(tok,(				")",".indent" , "\n"))
						r<<expr()
			^ r

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
			r=()
			while ! eat(".dedent")
				r<<('.loc',file,line,fname)
				r<<stmt(loop)
			^ r

		fn if1(loop)
			assert  in(tok, ("if", "elif"))
			lex()
			r = ["if", expr()]
			r<<block(loop)
			case tok
				"elif"
					r<<if1(loop)
				"else"
					lex()
					r<<block(loop)
			^ r

		fn stmt(loop)
			continueok,breakok=loop
			r = [intern (tok),]
			case tok
				"assert"
					r<<file
					r<<line
					r<<fname
					lex()
					x = expr()
					r<<str(x)
					r<<x
					expect("\n")
					^ r
				"show"
					r<<file
					r<<line
					r<<fname
					lex()
					x = commas()
					r<<str(x)
					r<<x
					expect("\n")
					^ r
				"case"
					lex()
					r<<commas()
					expect(".indent")
					while ! eat(".dedent")
						patterns = [commas(),]
						while eat("\n")
							patterns<<commas()
						body = block((continueok,1))
						;TODO generate more efficient form
						for pattern  patterns
							r<<(pattern, @body)
					^ r
				"dowhile"
				"while"
					lex()
					r<<expr()
					r@=block((1,1))
					^ r
				"for"
					lex()
					r<<word()
					r<<commas()
					r@=block((1,1))
					^ r
				"fn"
					line1=line
					lex()

					fname1=word()
					r<<fname1

					fparams1=params()
					r<<fparams1

					r<<otype()

					expect(".indent")
					r<<('.loc',file,line1,fname1)
					r@=fbody(fname1,fparams1)
					expect(".dedent")
					^ r
				"if"
					^ if1(loop)
				"^"
					lex()
					if eat("\n")
						^ '^', 0
					r<<commas()
					expect("\n")
					^ r
				"throw"
					lex()
					r<<commas()
					expect("\n")
					^ r
				"break"
					if!breakok
						err("break without case or loop")
					lex()
					if eat("\n")
						^'break'
					r<<word()
					expect("\n")
					^ r
				"continue"
					if!continueok
						err("continue without loop")
					lex()
					if eat("\n")
						^'continue'
					r<<word()
					expect("\n")
					^ r
			a = assignment()
			if eat(":")
				^ ":", a, stmt(loop)
			expect("\n")
			^ a

		; function/module body
		r = []
		while tok != ".dedent"
			r<<('.loc',file,line,fname)
			r<<stmt((0,0))
		^ r

	; top level
	lex()
	eat("\n")

	; imports
	;TODO figure out syntax for incorporating e.g. C++ source Fragment files verbatim
	;TODO figure out syntax for incorporating data files at compile time
	;TODO include these in the module
	while eat("import")
		name = word()
		;TODO: path
		file1= name @ ".k"
		parse(intern(name),file1,readfile  (file1))
		expect("\n")

	; module
	;TODO: module name?
	modules<<(modname,(file,fbody(modname,())))
