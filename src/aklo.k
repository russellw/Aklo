import parser

fn usage
	eprn"Usage: aklo [options] file"
	exit 1

file=0
i=0
while i<len args
	s=args!i++
	if s!0=='-'
		o=s
		while o!0=='-'
			o=tail o
		case o
			"h"
			"help"
				prn"-h  Show help"
				prn"-V  Show version"
				exit 0
			"V"
			"version"
				prn"Aklo version 0"
				exit 0
			_
				eprn (s@": unknown option")
				exit 1
	if file
		usage()
	file=s
if !file
	usage()
;TODO modname?
parser.parse('program',file,readfile(file))
show parser.modules
