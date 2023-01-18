case '+',1,2
	'+',x,y
		a=x+y
	'-',x,y
		a=x-y
assert a==3

case '-',1,2
	'+',x,y
		a=x+y
	'-',x,y
		a=x-y
assert a==-1

case 1,2
	a,b,c
		r="no"
	a,b
		r="yes"
assert r=="yes"

case 1
	a,b,c
		r="no"
	a
		r="yes"
assert r=="yes"

case [1,[2,3],[4,5]]
	a,(b,c),(d,e)
		r=a+b+c+d+e
assert r==15

case 1,2,3,4
	a,@s
		r=s
assert r==[2,3,4]

case 2
	1
		a="one"
	2
		a="two"
assert a=="two"

a=0
case 5
	[@s]
		a=s
assert !a

a=0
case "abc"
	[@s]
		a=s
assert a=="abc"
