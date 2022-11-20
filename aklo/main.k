import parser
import ast
import cxx
import ir


i=0
packages=[]
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
	packages<<s
if !packages
	eprn("Usage: aklo [options] packages")
	exit (1)

libfiles=ctreadfiles(\(file)(endswith(file,".k")),".")

modules=[]

fn module names ,file
	name=splitext(file)!0
	file=joindir((root,)@names@(file,))
	names<<name
	text=readfile(file)
	modules<<names,text

fn dir names
	;TODO: should be able to write 'for file,d?: ...'?
	for s: listdir(joindir((root,)@names))
		file,d?=s
		if d?
			dir(names@(file,))
		elif endswith(file,".k")
			module (names ,file)

for  s :packages
	if endswith(s,".k")
		root,file=splitdir(s)
		module([],file)
		continue
	root,name=splitdir(s)
	dir([name,])
show modules
;modules=parser.parse('program',file,readfile(file))
