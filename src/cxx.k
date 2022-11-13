outs=()

fn emit s
	outs@=s

fn ptype type
	case type
		'&',t
			ptype t
			emit("*")
		'i8'
			emit("unsigned char")
		'i16'
			emit("uint16_t")
		'i32'
			emit("uint32_t")
		'i64'
			emit("uint64_t")
		'isize'
			emit("size_t")

fn compose program
	outs:=()
	^outs
