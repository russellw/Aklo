r=[]

fn type1 (type)
	case type
		'&',t
			type1 (t)
			r@="*"
		'u8'
			r@="unsigned char"
		'u16'
			r@="uint16_t"
		'u32'
			r@="uint32_t"
		'u64'
			r@="uint64_t"
		'usize'
			r@="size_t"
		'f32'
			r@="float"
		'f64'
			r@="double"
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
		'loc',@loc
			location(loc)
		'var',name,type,loc
			location(loc)
			typename(name,type)
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
