import parser

fn usage
	eprintln"Usage: aklo [options] file"
	exit 1

file=0
i=0
while i<len argv
	s=argv!i++
	if s!0=='-'
		o=s
		while o&&o!0=='-'
			o=tail o
		case o
			"h"
			"help"
				println"-h  Show help"
				println"-V  Show version"
				exit 0
			"V"
			"version"
				println"Aklo version 0"
				exit 0
			_
				eprintf"%s: unknown option\n",s
				exit 1
	if file
		usage()
	file=s
if !file
	usage()
parser.parse(file,readfile(file))
