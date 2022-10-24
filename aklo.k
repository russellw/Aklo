fn usage
	eprint"Usage: aklo [options] file\n"
	exit 1

file=0
i=0
while i<len argv
	s=argv[i++]
	if s[0]=='-'
		o=s
		while o&&o[0]=='-'
			o=tail o
		case o
			"h"
			"help"
				print"-h  Show help\n"
				print"-V  Show version\n"
				exit 0
			"V"
			"version"
				print"Aklo version 0\n"
				exit 0
			_
				eprint(s)
				eprint": unknown option\n"
				exit 1
	if file
		usage()
	file=s
if !file
	usage()
print(file)
print("\n")
