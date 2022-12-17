;command line
optimize=0
packages=[]

i=0
while i<len( args)
	s=args[i++]
	if startsWith(s,"-")
		o=s
		while startsWith(o,"-")
			o=tail (o)
		case slice(o,0,1)
			"O"
				optimize=parseInt(tail(o),10)
			"h"
				prn("-h         Show help")
				prn("-V         Show version")
				prn("-O<level>  Optimize")
				exit( 0)
			"V"
			"v"
				prn("Aklo version 0")
				exit( 0)
			_
				eprn (s@": unknown option")
				exit (1)
		continue
	packages<<s
if !packages
	eprn("Usage: aklo [options] packages")
	exit (1)

;standard library
libFiles=compileTimeReadFiles(\(file)(endsWith(file,".k")),".")

;read modules
modules=[]

fn add (names ,file)
	name=splitExt(file)[0]
	name=replace("-","_",name)
	text=readFile(joinDir([root]@names@[file]))
	module=parser.parse(file,text)
	modules<<map(intern,names@[name]),module

fn dir (names)
	for file,attrs: listDir(joinDir([root]@names))
		if get(attrs,'dir?')
			dir(names@[file])
		elif endsWith(file,".k")
			add (names ,file)

for  s :packages
	if endsWith(s,".k")
		root,file=splitDir(s)
		add([],file)
		continue
	root,name=splitDir(s)
	dir([name])

;convert to intermediate representation
vars,fns=ast.norm(modules)

;optimize
vars,fns=optimizer.optimize(optimize, vars,fns)

;output
writeFile("a.cc",cxx.compose(vars,fns))
