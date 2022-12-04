fn convert (modules)
	file=0
	line=0
	code=[]
	fn term (loop,a)
		fn rec(a)
			^term(loop,a)

		fn assign (y,x)
			code<<'=',y,rec(x)

		fn cond(test,yes,no)
			yeslabel = gensym("condyes")
			nolabel = gensym("condno")
			afterlabel = gensym("condafter")
			r = gensym("condresult")

			;test
			code<<'label',testlabel
			code<<'if',term(test),yeslabel
			code<<'go',nolabel

			;yes
			code<<'label',yeslabel
			assign(r,yes)
			code<<'go',afterlabel

			;no
			code<<'label',nolabel
			assign(r,no)

			;after
			code<<'label',afterlabel
			^r

		case a
			'.loc',file1,line1,fname1
				file=file1
				line=line1
				fname=fname1
				code<<a
			'^',x
				code<<'^',rec(x)
			'!',x
				^cond(x,0,1)
			'if',test,yes,no
				yeslabel = gensym("ifyes")
				nolabel = gensym("ifno")
				afterlabel = gensym("ifafter")

				;test
				code<<'label',testlabel
				code<<'if',term(test),yeslabel
				code<<'go',nolabel

				;yes
				code<<'label',yeslabel
				map(rec,yes)
				code<<'go',afterlabel

				;no
				code<<'label',nolabel
				map(rec,no)

				;after
				code<<'label',afterlabel
			'|',x,y
				r=gensym("or")
				assign(r,x)
				^cond(r,r,y)
			'&',x,y
				r=gensym("and")
				assign(r,x)
				^cond(r,y,r)
			'while',test,@body
				bodylabel = gensym("whilebody")
				testlabel = gensym("whiletest")
				afterlabel = gensym("whileafter")
				loop = testlabel, afterlabel

				code<<'go',testlabel

				;body
				code<<'label',bodylabel
				map(rec,body)

				;test
				code<<'label',testlabel
				code<<'if',term(test),bodylabel

				;after
				code<<'label',afterlabel
			'dowhile',test,@body
				bodylabel = gensym("dowhilebody")
				testlabel = gensym("dowhiletest")
				afterlabel = gensym("dowhileafter")
				loop = testlabel, afterlabel

				;body
				code<<'label',bodylabel
				map(rec,body)

				;test
				code<<'label',testlabel
				code<<'if',term(test),bodylabel

				;after
				code<<'label',afterlabel
			_
				throw repr(a)
	^code
