r=[]

fn composeType (type)
	case type
		'&',t
			composeType (t)
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

fn typeName(name,type)
			composeType(type)
			r@=" "
			r@=str(name)
fn unaryExpr(op,a)
			r@=op
			expr(a)
fn binaryExpr(op,a,b)
			expr(a)
			r@=op
			expr(b)
fn expr(a)
	case a
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
			binaryExpr(str(a[0]),x,y)
		'//',x,y
			;TODO what if args are inexact?
			binaryExpr("/",x,y)
		'bitAnd',x,y
			binaryExpr("&",x,y)
		'bitOr',x,y
			binaryExpr("|",x,y)
		'bitXor',x,y
			binaryExpr("^",x,y)
		'shl',x,y
			binaryExpr("<<",x,y)
		'shr',x,y
			binaryExpr(">>",x,y)
		'bitNot',x
			unaryExpr("~",x)
		'neg',x
			unaryExpr("-",x)
		'quote',x
			r@=repr(x)
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
		'throw',msg
			;TODO
			r@="fputs("
			expr(msg)
			r@=", stderr);\n"
			r@="exit(1);\n"
		_
			expr(a)
			r@=";\n"

fn defVar(a)
	case a
		loc,name,type
			location(loc)
			typeName(name,type)
		loc,name,type,val
			location(loc)
			typeName(name,type)
			r@="="
			expr(val)
		_
			throw repr(a)
	r@=";\n"

fn defFn(a)
	loc,name,params,rtype,vars,body=a
	location(loc)
	typeName(name,rtype)
	r@="("
	for i:range(params)
		if i
			r@=","
		_,pname,ptype=params[i]
		typeName(pname,ptype)
	r@=") {\n"
	map(defVar,vars)
	map(stmt,body)
	r@="}\n"

fn compose (vars,fns)
	r:=[]
	r@="#include <stdio.h>\n"
	r@="#include <stdlib.h>\n"
	map(defVar,vars)
	map(defFn,fns)
	^r
