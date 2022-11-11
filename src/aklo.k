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
		while o!0=='-'
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
				eprintln (s@": unknown option")
				exit 1
	if file
		usage()
	file=s
if !file
	usage()
;TODO modname?
parser.parse('program',file,readfile(file))
show parser.modules
