fn convert program
	r=()
	fn stmt (a)
		case a
			'while',test,@body
				0
			_
				throw a
	^r
