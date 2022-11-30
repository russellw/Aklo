;command line
i=0
packages=[]
while i<len( args)
	s=args[i++]
	if s[0]=='-'
		o=s
		while o[0]=='-'
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

;standard library
libfiles=ctreadfiles(\(file)(endswith(file,".k")),".")

;read modules
modules=[]

fn add names ,file
	name=splitext(file)[0]
	name=replace("-","_",name)
	text=readfile(joindir([root]@names@[file]))
	module=parser.parse(name,text)
	modules<<names@[name],module

fn dir names
	;TODO: should be able to write 'for file,d?: ...'?
	for s: listdir(joindir([root]@names))
		file,attrs=s
		if get(attrs,'dir?')
			dir(names@[file])
		elif endswith(file,".k")
			add (names ,file)

for  s :packages
	if endswith(s,".k")
		root,file=splitdir(s)
		add([],file)
		continue
	root,name=splitdir(s)
	dir([name])

;convert to intermediate representation
modules=ast.norm(modules)
