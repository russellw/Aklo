fn of(a)
	if bool?(a)
		^'bool'
	if integer?(a)
		^'integer'
	if float?(a)
		^'float'
	if double?(a)
		^'double'
	^'any'

fn compatible(type1,type2)
	if type1=='any'|type2=='any'
		^1
	^type1==type2
