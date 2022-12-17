fn of(a)
	if bool?(a)
		^'bool'
	if integer?(a)
		^'integer'
	^'any'

fn compatible(type1,type2)
	if type1=='any'|type2=='any'
		^1
	^type1==type2
