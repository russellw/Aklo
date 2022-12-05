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
	show	 file
	r@="#line "@str(line)@" \""@file@"\""
	if fname
		r@=" // "@fname
	r@="\n"

fn stmt(a)
	case a
		'var',name,type,loc
			location(loc)
			type1(type)
			r@=" "
			r@=str(name)
			r@=";\n"

fn compose (program)
	r:=[]
	r@="#include <stdio.h>\n"
	r@="#include <stdlib.h>\n"
	map(stmt,program)
	show program
	show r
	^r
