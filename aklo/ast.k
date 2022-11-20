fn walk f,s
	loc=0

	fn rec a
		if f(loc,a)
			^
		case a
			'.loc',@loc1
				loc:=loc1
			'fn',@_
			'\\',@_
				0
			[@s]
				map(rec,s)

	map(rec,s)

fn assignedvars params ,body
	nonlocals=[]
	locals=keys(params)

	'.loc',@loc=body!0
	locs=map(\(x)(x,loc),locals)

	walk(
		\(loc,a)(
			case a
				'=',y,_
					locs<<y,loc
					locals<<y
				':=',y,_
					locs<<y,loc
					nonlocals<<y
		)
	)

	conflicts=intersection(nonlocals,locals)
	if conflicts
		y=conflicts!0
		file,line,fname=get (locs,y)
		throw file @ ":" @ str(line)@ ": "@fname @ ": " @ str(y)@" assigned both nonlocally and locally"

	^locals

fn norm modules
	^modules
