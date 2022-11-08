modules=()

fn parse  file,text
	^ modules

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
		eprintf("%s:%d: %s\n",file,line,msg)
		exit 1

	fn errtok(msg):
		^ line
		if tok == "\n":
			line--
		err(fmt("%s: %s",repr(tok),msg))

	; tokenizer
	fn lex():
		^ cols
		^ dedents
		^ dentc
		^ i
		^ line
		^ tok

		if dedents:
			dedents--
			tok = ".dedent"
			return

		while i < len(text):
			j = i
			tok = text!i,

			; newline
			if tok == "\n":
				; next line
				i++
				line++

				; measure indent
				col = 0
				while in (text!i ,"\t "):
					if text!i != dentc && dentc:
						err("indented with tabs and spaces in same file")
					dentc = text!i++
					col++

				; nothing important on this line, keep going
				if in (text!i ,"\n;{")
					continue

				; one indent
				if col > last(cols):
					cols@=col,
					tok = ".indent"
					return

				; zero or more dedents
				while col < last(cols):
					cols=init(cols)
					dedents++
				if col != last(cols):
					err("inconsistent indent")
				return

			; space
			if isspace(text!i):
				i++
				continue

			; comment
			if tok == ";":
				while text!i != "\n"!0:
					i++
				continue
			if tok == "{":
				i ++
				line1 = line
				while text!i != "}"!0:
					if i == len(text):
						line = line1
						err("unclosed block comment")
					if text!i == "\n"!0:
						line++
					i++
				i ++
				continue

			; word
			if isidstart(text!i):
				while isidpart(text!i):
					i++
				tok = slice(text,j,i)
				return

			; hexadecimal numbers are a separate because they may contain 'e'
			if lower(slice(text,i,i+2)) == "0x":
				while isidpart(text!i):
					i++
				tok = slice(text,j,i)
				return

			; other number
			if isdigit(text!i) || tok == "." && isdigit(text![i + 1]):
				while isidpart(text!i):
					i++
				if text!i == "."!0:
					i++
					while isidpart(text!i):
						i++
					if lower(text![i - 1]) == "e"!0 && in(text!i ,"+-"):
						i++
						while isidpart(text!i):
							i++
				tok = slice(text,j,i)
				return

			; symbol or string
			case tok
				"'"
				"\""
					q = text!i++
					while text!i != q:
						if text!i == "\\"!0:
							i++
						if text!i == "\n"!0:
							err("unclosed quote")
						i++
					i++
					tok = slice(text,j,i)
					return

			; raw string
			if slice(text,i,i+2) == "#\"":
				i += 2
				while text!i != "\""!0:
					if text!i == "\n"!0:
						err("unclosed quote")
					i++
				i++
				tok = slice(text,j,i)
				return

			; punctuation
			punct = (
				; 4 characters
				">>>=",
				; 3 characters
				"**=",
				"//=",
				"<<=",
				">>=",
				">>>",
				; 2 characters
				"!=",
				"%=",
				"&&",
				"&=",
				"**",
				"*=",
				"++",
				"+=",
				"--",
				"-=",
				"//",
				"/=",
				"<<",
				"<=",
				"==",
				">=",
				">>",
				"@=",
				"^=",
				"|=",
				"||",
			)
			for s  punct:
				if slice(text,i , i + len(s)) == s:
					i += len(s)
					tok = s
					return

			if tok == "[":
				tok = "("
			if tok == "]":
				tok = ")"
			i++
			return

		; end of file
		tok = ".dedent"

	fn lex1():
		s = tok
		lex()
		return s

	; parser
	fn eat(s):
		if tok == s:
			lex()
			return 1

	fn expect(s):
		if ! eat(s):
			errtok(fmt("expected %s",repr s))

	fn word():
		if isidstart(tok!0):
			return intern(lex1())
		errtok("expected word")

	; expressions
	fn isprimary():
		return isidpart(tok!0) || in(tok!0 ,"'\"#")

	fn primary():
		; word
		case tok
			"false"
				lex()
				return 0
			"true"
				lex()
				return 1
		if isidstart(tok!0):
			return lex1()

		; number
		if isdigit(tok!0):
			s=replace("_","",tok)
			case lower(take(2,s)):
				"0b"
					a = parseint(drop(2,s), 2)
				"0o"
					a = parseint(drop(2,s), 8)
				"0x"
					a = parseint(drop(2,s), 16)
				_
					a = parseint(s)
			lex()
			return a

		; symbol
		if startswith(tok,"'"):
			s = unesc(slice(tok,1,len(tok)-1))
			lex()
			return '.quote',intern(s)

		; string
		if startswith(tok,"\""):
			s = unesc(slice(tok,1,len(tok)-1))
			lex()
			return'.list',*s

		; raw string
		if startswith(tok,"#\""):
			s = slice(tok,2,len(tok)-1)
			lex()
			return'.list',*s

		; bracketed expression or list
		if eat("("):
			if eat(".indent"):
				s =  '.list',
				while ! eat(".dedent"):
					s@=expr(),
					eat(",")
					expect("\n")
				expect(")")
				return s
			if eat(")"):
				return '.list',
			a = commas()
			expect(")")
			return a

		; none of the above
		errtok("expected expression")

	fn postfix():
		a = primary()
		while 1:
			case tok:
				"("
					lex()
					a = a,
					if eat(".indent"):
						while ! eat(".dedent"):
							a@=expr(),
							eat(",")
							expect("\n")
						expect(")")
						continue
					while ! eat(")"):
						a@=expr(),
						if eat(")"):
							break
						expect(",")
				"++"
				"--"
					return intern("post" @ lex1()), a
				"."
					lex()
					field = word()
					if in(a,keys modules)
						a='.',a,field
					else:
						a = "get", a, ('.quote',field)
				_
					if isprimary():
						a = [a, prefix()]
						while eat(","):
							a@=prefix(),
					return a

	fn params():
		s = []
		case tok:
			"("
				lex()
				if eat(".indent"):
					while ! eat(".dedent"):
						s@=word(),
						eat(",")
						expect("\n")
					expect(")")
				else:
					while ! eat(")"):
						s@=word(),
						if eat(")"):
							break
						expect(",")
			".indent"
			":"
				0
			_
				while 1:
					s@=word(),
					if ! eat(","):
						break
		return s

	fn prefix():
		case tok:
			"!"
			"++"
			"--"
			"~"
				return intern(lex1()), prefix()
			"*"
				lex()
				return '...', prefix()
			"-"
				lex()
				return 'neg', prefix()
			"\\"
				s = [intern(lex1()), params()]
				eat(":")
				if ! eat("("):
					s@=expr(),
					return s
				if eat(".indent"):
					while ! eat(".dedent"):
						s@=stmt(),
					expect(")")
					return s
				if tok != ")":
					s@=commas(),
				expect(")")
				return s
		return postfix()

	; operator precedence parser
	prec = 99
	ops = ()

	fn mkop(op, left):
		^ ops
		ops@=(op,( prec, left)),

	mkop("!", 1)

	prec--
	mkop("**")

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
	mkop("<<", 1)
	mkop(">>", 1)
	mkop(">>>", 1)

	prec--
	mkop("&", 1)

	prec--
	mkop("^", 1)

	prec--
	mkop("|", 1)

	prec--
	mkop("!=", 1)
	mkop("<", 1)
	mkop("<=", 1)
	mkop("==", 1)
	mkop(">", 1)
	mkop(">=", 1)

	prec--
	mkop("&&", 1)

	prec--
	mkop("||", 1)

	fn infix(prec):
		a = prefix()
		while 1:
			prec1, left1 = get ops,tok
			if prec1 < prec:
				return a
			op = lex1()
			b = infix(prec1 + left1)
			case op:
				"!"
					a ='.subscript', a, b
				">"
					a ='<', b, a
				">="
					a ='<=', b, a
				_
					a = intern(op), a, b

	fn expr():
		return infix(1)

	; statements
	fn commas():
		a = expr()
		if tok != ",":
			return a
		s = ['.list', a]
		while eat(","):
			if !in(tok,(				")",".indent" , "\n")):
					s@=expr(),
		return s

	fn assignment():
		a = commas()
		if endswith(tok,"="):
			return intern(lex1()), a, assignment()
		return a

	fn block(s):
		expect(".indent")
		s=()
		while ! eat(".dedent"):
			s@=stmt(),
		s

	fn if1():
		assert  in(tok, ("if", "elif"))
		lex()
		s = ["if", expr()]
		eat(":")
		s@=block(),
		case tok:
			"elif"
				s@=if1(),
			"else"
				lex()
				eat(":")
				s@=block(),
		return s

	fn stmt():
		s = [intern (tok),]
		;TODO: fname
		case tok:
			"assert"
				s@=file,
				s@=line,
				lex()
				x = expr()
				s@=str(x),
				s@=x,
				expect("\n")
				return s
			"show"
				s@=file,
				s@=line,
				lex()
				x = commas()
				s@=str(x),
				s@=x,
				expect("\n")
				return s
			"case"
				lex()
				s@=commas(),
				eat(":")
				expect(".indent")
				while ! eat(".dedent"):
					patterns = [commas(),]
					while eat("\n"):
						patterns@=commas(),
					body = block()
					for pattern  patterns:
						s@=(pattern, *body),
				return s
			"dowhile"
			"while"
				lex()
				s@=expr(),
				eat(":")
				s@=block()
				return s
			"for"
				lex()
				s@=word(),
				expect(":")
				s@=commas(),
				eat(":")
				s@=block()
				return s
			"fn"
				line1=line
				lex()
				s@=word(),
				s@=params(),
				eat(":")
				s@=('.line',file,line1),
				s@=block()
				return s
			"if"
				return if1()
			"^"
				lex()
				s@=word(),
				expect("\n")
				return s
			"return"
				lex()
				if eat("\n"):
					return "return", 0
				s@=commas(),
				expect("\n")
				return s
		a = assignment()
		if eat(":"):
			return ":", a, stmt()
		expect("\n")
		return a

	; top level
	lex()
	eat("\n")

	; imports
	while eat("import"):
		name1 = word()
		;TODO: path
		parse(name1, name1 @ ".k")
		expect("\n")

	; module
	s = []
	while tok != ".dedent":
		s@=stmt(),
	modules@=(file,s),
