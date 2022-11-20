r=[]

fn ptype type
	case type
		'&',t
			ptype (t)
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
			throw repr(type)

fn compose program
	r:=[]
	r@="#include <stdlib.h>\n"
	^r
