;names used here
;a: node
;b: node
;c: node
;g: graph
;f: function
;p: pair, arc
;r: result
;s: start node
;w: 'without' node

fn nodes(g)
	r = []
	for a,b: g
		r @= a,b
	^uniq(r)

fn preds(g, a)
	r = []
	for b,c: g
		if a==c
			r << b
	^uniq(r)

fn succs(g, a)
	r = []
	for b,c: g
		if a == b
			r << c
	^uniq(r)

fn dfs(f, g, a)
	visited = []
	fn rec(a)
		if in(a,visited)
			^
		visited<<a
		f(a)
		map(rec, succs(g, a))
	rec(a)

fn dfswithout(f,g, a, w)
	visited = []
	fn rec(a)
		if a == w
			^
		if in(a,visited)
			^
		visited<<a
		f(a)
		map(rec, succs(g, a))
	rec(a)

fn domfrontier(g, s, a)
	r = []
	for b: nodes(g)
		if strictlydominates(g, s, a, b)
			continue
		for c: preds(g, b)
			if dominates(g, s, a, c)
				r << b
	^uniq(r)

fn dominates(g, s, a, b)
	^!reacheswithout(g, s, b, a)

fn idom(g, s, b)
	for a: nodes(g)
		if isidom(g, s, a, b)
			^a
	throw"no idom"

fn isidom(g, s, a, b)
	if !strictlydominates(g, s, a, b)
		^
	for c: strictdominators(g, s, b)
		if !dominates(g, s, c, a)
			^
	^1

fn reaches(g, a, b)
	r = 0
	dfs(
		\(c)(
			if b==c
				r:=1
		)
		g
		a
	)
	^r

fn reacheswithout(g, a, b, w)
	r = 0
	dfswithout(
		\(c)(
			if b==c
				r:=1
		)
		g
		a
		w
	)
	^r

fn strictdominators(g, s, b)
	r = []
	for a: nodes(g)
		if strictlydominates(g, s, a, b)
			r << a
	^r

fn strictlydominates(g, s, a, b)
	^a != b & dominates(g, s, a, b)

fn transsuccs(g, a)
	r = []
	dfs(
		\(b)(
			if a!=b
				r<<b
		)
		g
		a
	)
	^uniq(r)



;TODO move tests to test module
; https://tanujkhattar.wordpress.com/2016/01/11/dominator-tree-of-a-directed-graph/
ns = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'r']
s='r'

g=[]
g<<'a', 'd'
g<<'b', 'a'
g<<'b', 'd'
g<<'b', 'e'
g<<'c', 'f'
g<<'c', 'g'
g<<'d', 'l'
g<<'e', 'h'
g<<'f', 'i'
g<<'g', 'i'
g<<'g', 'j'
g<<'h', 'e'
g<<'h', 'k'
g<<'i', 'k'
g<<'j', 'i'
g<<'k', 'i'
g<<'k', 'r'
g<<'l', 'h'
g<<'r', 'a'
g<<'r', 'b'
g<<'r', 'c'

assert eqsets (nodes (g),ns)
assert preds( g,'c')==['r']
assert succs (g,'a')==['d']

;x dominates x
for x :ns
	assert dominates( g,s,x,x)

;s dominates x
for x :ns
	assert dominates( g,s,s,x)

;immediate dominators
for y :ns
	if y=='r'
		continue
	x=idom( g,s,y)
	case y
		'f'
		'g'
			assert x=='c'
		'j'
			assert  x=='g'
		'l'
			assert x=='d'
		_
			assert x=='r'

; tiger book page 439
ns = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
s = 1

g=[]
g<<1, 2
g<<1, 5
g<<1, 9
g<<2, 3
g<<3, 3
g<<3, 4
g<<4, 13
g<<5, 6
g<<5, 7
g<<6, 4
g<<6, 8
g<<7, 8
g<<7, 12
g<<8, 13
g<<8, 5
g<<9, 10
g<<9, 11
g<<10, 12
g<<11, 12
g<<12, 13

assert eqsets( nodes (g),ns)

;dominance frontier
assert eqsets(domfrontier(g,s,5),[4,5,12,13])

; https://users.aalto.fi/~tjunttil/2020-DP-AUT/notes-sat/cdcl.html
ns = ['x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9', 'x10', 'x11', 'x12', false]

g=[]
g<<'x1', 'x2'
g<<'x1', 'x3'
g<<'x2', 'x5'
g<<'x2', 'x8'
g<<'x3', 'x4'
g<<'x4', 'x5'
g<<'x5', 'x7'
g<<'x6', 'x7'
g<<'x7', 'x8'
g<<'x8', 'x9'
g<<'x8', 'x10'
g<<'x9', 'x11'
g<<'x10', 'x11'
g<<'x10', 'x12'
g<<'x11', false
g<<'x12', false

assert eqsets (nodes (g),ns)

; first UIP
assert idom(g,'x6', false) == 'x8'
assert eqsets(transsuccs(g,'x6'), ['x7', 'x8', 'x9', 'x10', 'x11', 'x12', false])
