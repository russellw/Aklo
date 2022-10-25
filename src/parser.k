fn parse  file,text
	ti = 0
	line = 1

	dentc = 0
	cols = [0]
	dedents = 0

	tok = 0

	fn err msg
		eprintf("%s:%d: %s\n",file,line,msg)
		exit 1

	0
