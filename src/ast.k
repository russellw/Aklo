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
			@s,
				map(rec,s)

	map(rec,s)
