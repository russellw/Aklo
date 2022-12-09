r=[]

fn type1 (type)
	case type
		'&',t
			type1 (t)
			r@="*"
		'uchar'
			r@="unsigned char"
		'any'
			;TODO placeholder
			r@="int"
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
fn unaryexpr(op,a)
			r@=op
			expr(a)
fn binaryexpr(op,a,b)
			expr(a)
			r@=op
			expr(b)
fn expr(a)
	case a
		'xcall','parsedouble',('quote',s)
			r@=s
		'xcall',name,@s
			r@=str(name)
			r@="("
			exprs(s)
			r@=")"
		'+',x,y
		'%',x,y
		'-',x,y
		'*',x,y
		'<',x,y
		'<=',x,y
		'==',x,y
			binaryexpr(str(a[0]),x,y)
		'//',x,y
			;TODO what if args are float?
			binaryexpr("/",x,y)
		'bitand',x,y
			binaryexpr("&",x,y)
		'bitor',x,y
			binaryexpr("|",x,y)
		'bitxor',x,y
			binaryexpr("^",x,y)
		'shl',x,y
			binaryexpr("<<",x,y)
		'shr',x,y
			binaryexpr(">>",x,y)
		'bitnot',x
			unaryexpr("~",x)
		'neg',x
			unaryexpr("-",x)
		[@_]
			throw repr(a)
		_
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
		'^',x
			r@="return "
			expr(x)
			r@=";\n"
		':',label
			expr(label)
			r@=":\n"
		'goto',label
			r@="goto "
			expr(label)
			r@=";\n"
		'if',test,label
			r@="if ("
			expr(test)
			r@=") goto "
			expr(label)
			r@=";\n"
		'assert',test,msg
			r@="if (!"
			expr(test)
			r@=")\n"
			stmt(('throw',msg))
		'throw',msg
			;TODO
			r@="{\n"
			r@="fputs(\""@msg@"\", stderr);\n"
			r@="exit(1);\n"
			r@="}\n"
		_
			expr(a)
			r@=";\n"

fn var1(a)
	case a
		loc,name,type
			location(loc)
			typename(name,type)
		loc,name,type,val
			location(loc)
			typename(name,type)
			r@="="
			expr(val)
		_
			throw repr(a)
	r@=";\n"

fn fn1(a)
	loc,name,params,rtype,vars,body=a
	location(loc)
	typename(name,rtype)
	r@="("
	for i:range(params)
		if i
			r@=","
		_,pname,ptype=params[i]
		typename(pname,ptype)
	r@=") {\n"
	map(var1,vars)
	map(stmt,body)
	r@="}\n"

fn compose (vars,fns)
	r:=[]
	r@="#include <stdio.h>\n"
	r@="#include <stdlib.h>\n"
	map(var1,vars)
	map(fn1,fns)
	^r
