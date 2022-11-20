import parser
import ast
import cxx
import ir


i=0
pkargs=()
while i<len( args)
	s=args!i++
	if s!0=='-'
		o=s
		while o!0=='-'
			o=tail (o)
		case o
			"h"
			"help"
				prn("-h  Show help")
				prn("-V  Show version")
				exit( 0)
			"V"
			"version"
				prn("Aklo version 0")
				exit( 0)
			_
				eprn (s@": unknown option")
				exit (1)
	pkargs<<s
if !pkargs
	eprn("Usage: aklo [options] packages")
	exit (1)

libfiles=ctreadfiles(\(file)(endswith(file,".k")),".")

;TODO modname?
fn dir names
	for s: listdir(joindir((root,)@names))
		file,d?=s
		if endswith(file,".k")
			name=splitext(file)!0
			pks<<root,names@(name,)
			continue
		if d?
			dir(names@(file,))

pks=()
for  s :pkargs
	if endswith(s,".k")
		s=splitext(s)!0
		root,name=splitdir(s)
		pks<<root,(name,)
		continue
	root,name=splitdir(s)
	dir((name,))
show pks
;modules=parser.parse('program',file,readfile(file))
