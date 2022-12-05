r=[]

fn type1 (type)
	case type
		'&',t
			type1 (t)
			r@="*"
		'uchar'
			r@="unsigned char"
		_
			r@=str(type)

fn location(loc)
	file,line,fname=loc
	r@="#line "@str(line)@" \""@file@"\""
	if fname
		r@=" // "@fname
	r@="\n"

fn typename(name,type)
			type1(type)
			r@=" "
			r@=str(name)

fn expr(a)
			r@=str(a)

fn exprs(s)
	for i:range(s)
		if i
			r@=","
		expr(s[i])

fn stmt(a)
	case a
		'=',x,y
			expr(x)
			r@="="
			expr(y)
			r@=";\n"
		'loc',@loc
			location(loc)
		'var',name,type,loc
			location(loc)
			typename(name,type)
			r@=";\n"
		'var',name,type,loc,val
			location(loc)
			typename(name,type)
			r@="="
			expr(val)
			r@=";\n"
		'^',x
			r@="return "
			expr(x)
			r@=";\n"
		'fn',name,params,rtype,@body
			;TODO should unadorned loc be part of fn?
			typename(name,rtype)
			r@="("
			for pname,ptype:params
				typename(pname,ptype)
			r@=") {\n"
			map(stmt,body)
			r@="}\n"
		'/',name,@s
			r@=str(name)
			r@="("
			exprs(s)
			r@=");\n"
		_
			if int?(a)
				^
			throw repr(a)

fn compose (program)
	r:=[]
	r@="#include <stdio.h>\n"
	r@="#include <stdlib.h>\n"
	map(stmt,program)
	show program
	show r
	^r
