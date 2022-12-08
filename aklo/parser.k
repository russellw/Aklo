fn idstart?( c)
	^ alpha? (c)|in (c,"_$")

fn idpart? (c)
	^ idstart? (c)|digit? (c)|in (c,"?")


fn parse  (file,text)
	text=replace("\r\n","\n",text)
	text=replace("\r","\n",text)
	if!endswith(text,"\n")
		text@="\n"

	i = 0
	line = 1

	dentc = 0
	cols = [0]
	dedents = 0

	tok = 0

	fn err (msg)
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
			tok:=[ text[i]]

			; newline
			if tok == "\n"
				; next line
				i++
				if i == len(text)
					^
				line++

				; measure indent
				col = 0
				while in (text[i] ,"\t ")
					if text[i] != dentc & dentc
						err("indented with tabs and spaces in same file")
					dentc := text[i++]
					col++

				; nothing important on this line, keep going
				if in (text[i] ,"\n;{")
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
			if space?(text[i])
				i++
				continue

			; comment
			if tok == ";"
				while text[i] != "\n"[0]
					i++
				continue
			if tok == "{"
				i ++
				line1 = line
				while text[i] != "}"[0]
					if i == len(text)
						line := line1
						err("unclosed block comment")
					if text[i] == "\n"[0]
						line++
					i++
				i ++
				continue

			; symbol or string
			case tok
				"'"
				"\""
					q = text[i++]
					while text[i] != q
						if text[i] == "\\"[0]
							i++
						if text[i] == "\n"[0]
							err("unclosed quote")
						i++
					i++
					tok:= slice(text,j,i)
					^

			; raw string
			if slice(text,i,i+2) == "r\""
				i += 2
				while text[i] != "\""[0]
					if text[i] == "\n"[0]
						err("unclosed quote")
					i++
				i++
				tok:= slice(text,j,i)
				^

			; word
			if idstart?(text[i])
				while idpart?(text[i])
					i++
				tok:= slice(text,j,i)
				^

			; hexadecimal numbers are separate because they may contain 'e'
			if lower(slice(text,i,i+2)) == "0x"
				while idpart?(text[i])
					i++
				tok:= slice(text,j,i)
				^

			; other number
			if digit?(text[i]) | tok == "." & digit?(text[i + 1])
				while idpart?(text[i])
					i++
				if text[i] == "."[0]
					i++
					while idpart?(text[i])
						i++
					if lower(text[i - 1]) == "e"[0] & in(text[i] ,"+-")
						i++
						while idpart?(text[i])
							i++
				tok:= slice(text,j,i)
				^

			; punctuation
			punct = [
				"!="
				":="
				"**"
				"++"
				"+="
				"--"
				"-="
				"//"
				"<="
				"=="
				">="
				"@="
				"<<"
				">>"
			]
			for s  :punct
				if slice(text,i , i + len(s)) == s
					i += len(s)
					tok:= s
					^
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
		if idstart?(tok[0])
			^ intern(lex1())
		errtok("expected word")

	;types
	fn type?()
		if tok=="&"
			^1
		^idstart? (tok[0])

	fn type1()
		if!type?()
			errtok("expected type")
		if tok=="&"
			^lex1(),type1()
		^word()

	fn optionaltype()
		if !type?()
			^'any'
		^type1()

	; expressions
	fn exprs(end)
			r=[]
			if eat(".indent")
				while ! eat(".dedent")
					r<<commas()
					expect("\n")
			elif tok!=end
				dowhile  eat(",")
					r<<expr()
			expect(end)
			^r

	fn primary()
		;remember the current location in case we need to report an error
		file1=file
		line1=line

		;unless there is an error, this token will definitely be consumed
		s=tok
		lex()
		r=[intern(s)]

		;some primary expressions are indicated by specific tokens
		case s
			"/"
				r='xcall',word()
				expect("(")
				^r@exprs(")")
			"range"
				expect("(")
				r<<expr()
				if eat(",")
					r<<expr()
				else
					r=['range',0,r[1]]
				expect(")")
				^r
			"sym?"
			"list?"
			"int?"
			"float?"
			"readfile"
			"bitnot"
			"len"
			"listdir"
			"exit"
				expect("(")
				r<<expr()
				expect(")")
				^r
			"bitxor"
			"bitor"
			"bitand"
			"shr"
			"shl"
			"writestream"
			"writefile"
			"ctreadfiles"
				expect("(")
				r<<expr()
				expect(",")
				r<<expr()
				expect(")")
				^r
			"slice"
				expect("(")
				r<<expr()
				expect(",")
				r<<expr()
				expect(",")
				r<<expr()
				expect(")")
				^r
			"args"
			"stdout"
			"stderr"
			"windows?"
				^r
			"false"
				^ 0
			"true"
				^ 1
			"("
				a = commas()
				expect(")")
				^ a
			"["
				^['list']@exprs("]")

		; quoted symbol
		if startswith(s,"'")
			s = unesc(slice(s,1,len(s)-1))
			^ 'quote',intern(s)

		; string
		if startswith(s,"\"")
			s = unesc(slice(s,1,len(s)-1))
			^'list',@s

		; raw string
		if startswith(s,"r\"")
			s = slice(s,2,len(s)-1)
			^'list',@s

		; word
		if idstart?(s[0])
			^ r[0]

		; number
		if digit?(s[0])
			s=replace("_","",s)

			;integer
			case lower(take(2,s))
				"0b"
					^ parseint(drop(2,s), 2)
				"0o"
					^ parseint(drop(2,s), 8)
				"0x"
					^ parseint(drop(2,s), 16)
			if every(digit?,s)
				^ parseint(s,10)

			;floating point
			op='parsedouble'
			if endswith(lower(s),"f")
				s=init(s)
				op='parsefloat'
			^'xcall',op,('quote',s)

		; none of the above
		file:=file1
		line:=line1
		tok:=s
		errtok("expected expression")

	fn postfix()
		a = primary()
		while 1
			case tok
				"("
					lex()
					a = ('call',a)@exprs(")")
				"["
					lex()
					a='subscript',a,expr()
					expect("]")
				"++"
				"--"
					^ intern("post" @ lex1()), a
				"."
					a=['.',a]
					while eat(".")
						a<<word()
				_
					^ a

	fn param(fname)
		^(file,line,fname),word(),optionaltype()

	fn params(fname)
		expect("(")
		r = []
		if eat(".indent")
			while ! eat(".dedent")
				r<<param(fname)
				expect("\n")
		elif tok!=")"
				dowhile  eat(",")
					r<<param(fname)
		expect(")")
		^ r

	fn prefix()
		case tok
			"-"
				lex()
				^ 'neg', prefix()
			"!"
			"++"
			"--"
			"@"
			"*"
			"&"
				^ intern(lex1()), prefix()
			"\\"
				;location
				r = ['lambda',(file,line,"lambda")]
				lex()

				;parameters
				r<<params("lambda")

				;body
				expect("(")
				if eat(".indent")
					while !eat( ".dedent")
						r<<('loc',file,line,"lambda")
						r<<stmt("lambda")
				else
					r<<('loc',file,line,"lambda")
					r<<'^',commas()
				expect(")")

				^ r
		^ postfix()

	; operator precedence parser
	prec = 99
	ops = []

	fn mkop(op, left)
		ops<<(op,( prec, left))

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
		r = ['list', a]
		while eat(",")
					r<<expr()
		^ r

	fn assignment()
		a = commas()
		case tok
			"="
			"+="
			":="
			"-="
			"@="
			"<<"
			">>"
				^ intern(lex1()), a, commas()
		^ a

	fn block(fname)
		expect(".indent")
		r=[]
		while ! eat(".dedent")
			;an early pass will mangle function names
			; so we need to keep track of the original function names for debugging
			; the easiest place to do it is alongside the filename and line number
			r<<('loc',file,line,fname)
			r<<stmt(fname)
		^ r

	fn if1(fname)
		assert  in(tok, ("if", "elif"))
		lex()
		r = ['if', expr()]
		r<<'do',@block(fname)
		case tok
			"elif"
				r<<if1(fname)
			"else"
				lex()
				r<<'do',@block(fname)
			_
				r<<0
		^ r

	fn here(fname)
		r=file @ ":" @ str(line)
		if fname
			r@=": "@fname
		^r

	fn stmt(fname)
		r = [intern (tok)]
		case tok
			"assert"
				lex()
				x = expr()
				r<<x
				r<<here(fname)@": "@str(x)
				expect("\n")
				^ r
			"show"
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
					dowhile eat("\n")
						r<<'when',commas()
					r<<('do', @block(fname))
				^ r
			"dowhile"
			"while"
				lex()
				r<<expr()
				;TODO simplify
				r@=block(fname)
				^ r
			"for"
				lex()
				r<<commas()
				expect(":")
				r<<commas()
				r@=block(fname)
				^ r
			"var"
				;location
				r<<(file,line,fname)
				lex()

				;name
				r<<word()

				;type
				r<<optionaltype()

				;initial value
				if eat( "=")
					r<<commas()

				expect("\n")
				^ r
			"fn"
				;location
				line1=line
				lex()
				fname=tok
				r<<(file,line1,fname)

				;name
				r<<word()

				;parameters
				r<<params(fname)

				;return type
				r<<optionaltype()

				;body
				expect(".indent")
				while !eat( ".dedent")
					r<<('loc',file,line,fname)
					r<<stmt(fname)

				^ r
			"if"
				^ if1(fname)
			"^"
				lex()
				if eat("\n")
					^ '^', 0
				r<<commas()
				expect("\n")
				^ r
			"tron"
				lex()
				if eat("\n")
					^ r
				dowhile eat(",")
					r<<word()
				expect("\n")
				^ r
			"throw"
				lex()
				r<<commas()
				expect("\n")
				^ r
			":"
			"goto"
				lex()
				r<<word()
				expect("\n")
				^ r
			"break"
			"continue"
				lex()
				expect("\n")
				^ r
		a = assignment()
		if sym?(a)
			errtok("expected statement")
		expect("\n")
		^ a

	; top level
	lex()
	eat("\n")
	r=[]
	r<<(file,line,"")
	while tok != ".dedent"
		r<<('loc',file,line,"")
		r<<stmt("")
	^ r
