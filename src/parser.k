modules=()

fn parse  file,text
	nonlocal modules
	;TODO: check for already parsed

	;TODO: cr
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
		nonlocal line
		if tok == "\n":
			line -= 1
		err(fmt("%s: %s",repr(tok),msg))

	; tokenizer
	fn lex():
		nonlocal cols
		nonlocal dedents
		nonlocal dentc
		nonlocal i
		nonlocal line
		nonlocal tok

		if dedents:
			dedents -= 1
			tok = ".dedent"
			return

		while i < len(text):
			j = i
			tok = text!i,

			; newline
			if text!i == "\n"!0:
				; next line
				i += 1
				;TODO: necessary?
				if i == len(text):
					return
				line += 1

				; measure indent
				col = 0
				while in (text!i ,"\t "):
					if text!i != dentc && dentc:
						err("indented with tabs and spaces in same file")
					dentc = text!i
					;TODO: i++
					i += 1
					col += 1

				; nothing important on this line, keep going
				if in (text!i ,"\n;") || slice(text,i , i + 2) == "/*":
					continue

				; one indent
				if col > last(cols):
					cols@=col,
					tok = ".indent"
					return

				; zero or more dedents
				while col < last(cols):
					cols=to(cols,-1)
					dedents += 1
				if col != last(cols):
					err("inconsistent indent")
				return

			; space
			if isspace(text!i):
				i += 1
				continue

			; comment
			if text!i == ";"!0:
				while text!i != "\n"!0:
					i += 1
				continue
			if slice(text,i,i+2) == "/*":
				i += 2
				line1 = line
				while slice(text,i,i+2) != "*/":
					if i == len(text):
						line = line1
						err("unclosed block comment")
					if text!i == "\n"!0:
						line += 1
					i += 1
				i += 2
				continue

			; word
			if isidstart(text!i):
				while isidpart(text!i):
					i += 1
				tok = slice(text,j,i)
				return

			; hexadecimal numbers are a separate because they may contain 'e'
			if lower(slice(text,i,i+2)) == "0x":
				while isidpart(text!i):
					i += 1
				tok = slice(text,j,i)
				return

			; other number
			if isdigit(text!i) || text!i == "."!0 && isdigit(text![i + 1]):
				while isidpart(text!i):
					i += 1
				if text!i == "."!0:
					i += 1
					while isidpart(text!i):
						i += 1
					if lower(text![i - 1]) == "e"!0 && in(text!i ,"+-"):
						i += 1
						while isidpart(text!i):
							i += 1
				tok = slice(text,j,i)
				return

			; multiline string
			if slice(text,i,i+3) == "\"\"\"":
				i += 3
				while slice(text,i,i+3) != "\"\"\"":
					if text!i == "\n"!0:
						line += 1
					i += 1
				i += 3
				tok = slice(text,j,i)
				return

			; symbol or string
			case text!i,
				"'"
				"\""
					q = text!i
					i += 1
					while text!i != q:
						if text!i == "\\"!0:
							i += 1
						if text!i == "\n"!0:
							err("unclosed quote")
						i += 1
					i += 1
					tok = slice(text,j,i)
					return

			; raw string
			if slice(text,i,i+2) == "#\"":
				i += 2
				while text!i != "\""!0:
					if text!i == "\n"!0:
						err("unclosed quote")
					i += 1
				i += 1
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
			i += 1
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
		;TODO: lex() just once at the start?
		; word
		if isidstart(tok!0):
			return lex1()

		; number
		if isdigit(tok!0):
			case lower(to(tok,2)):
				"0b"
					a = parseint(from(tok,2), 2)
				"0o"
					a = parseint(from(tok,2), 8)
				"0x"
					a = parseint(from(tok,2), 16)
				_
					a = parseint(tok)
			lex()
			return a

		; symbol
		if startswith(tok,"'"):
			s = unesc(slice(tok,1,len(tok)-1))
			lex()
			return '.quote',intern(s)

		; multiline string
		if startswith(tok,"\"\"\""):
			s = unesc(slice(tok,3,len(tok)-3))
			lex()
			s = dedent(s)
			return'.list',*s

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

	fn init(op, left):
		nonlocal ops
		ops@=(op,( prec, left)),

	init("!", 1)

	prec -= 1
	init("**")

	prec -= 1
	init("%", 1)
	init("*", 1)
	init("/", 1)
	init("//", 1)

	prec -= 1
	init("+", 1)
	init("-", 1)
	init("@", 1)

	prec -= 1
	init("<<", 1)
	init(">>", 1)
	init(">>>", 1)

	prec -= 1
	init("&", 1)

	prec -= 1
	init("^", 1)

	prec -= 1
	init("|", 1)

	prec -= 1
	init("!=", 1)
	init("<", 1)
	init("<=", 1)
	init("==", 1)
	init(">", 1)
	init(">=", 1)

	prec -= 1
	init("&&", 1)

	prec -= 1
	init("||", 1)

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
		;TODO
		while ! eat(".dedent"):
			s@=stmt(),

	fn block1():
		s = []
		block(s)
		return s

	fn if1():
		assert  in(tok, ("if", "elif"))
		lex()
		s = ["if", expr()]
		eat(":")
		s@=block1(),
		case tok:
			"elif"
				s@=if1(),
			"else"
				lex()
				eat(":")
				s@=block1(),
		return s

	fn stmt():
		s = [tok,]
		case tok:
			"assert"
				msg = fmt("%s:%d: assert failed",file,line)
				lex()
				s@=expr(),
				s@=msg,
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
					body = block1()
					for pattern  patterns:
						s@=(pattern, *body),
				return s
			"dowhile"
			"while"
				lex()
				s@=expr(),
				eat(":")
				block(s)
				return s
			"for"
				lex()
				s@=word(),
				expect(":")
				s@=commas(),
				eat(":")
				block(s)
				return s
			"fn"
				lex()
				s@=word(),
				s@=params(),
				eat(":")
				block(s)
				return s
			"if"
				return if1()
			"nonlocal"
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
			"show"
				line1 = line
				lex()
				x = commas()
				s@=fmt("%s:%d: %s: ",file,line,str(x))
				s@=x,
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
