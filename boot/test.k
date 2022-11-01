;logic
assert 1
assert !0
assert !!9

assert "a"
assert !""

assert !(0 || 0)
assert 0 || 1
assert 1 || 0
assert 1 || 1

assert !(0 && 0)
assert !(0 && 1)
assert !(1 && 0)
assert 1 && 1

;bitwise
assert !(0 & 0)
assert !(0 & 1)
assert !(1 & 0)
assert 1 & 1

assert !(0 | 0)
assert 0 | 1
assert 1 | 0
assert 1 | 1

assert !(0 ^ 0)
assert 0 ^ 1
assert 1 ^ 0
assert !(1 ^ 1)

assert 1<<8==256
assert  256>>8==1

assert ~0==-1

a=1
assert ~a==-2

;numbers
assert 0xff==255
assert 0Xfe+2==256
assert 0b10000==16
assert 0o100==64

;comparison
assert 1==1
assert 1!=2

assert  "abc"=="abc"
assert  "abc"!="abd"

assert 1<2
assert !(1<1)
assert !(2<1)

assert 2>1
assert !(1>1)
assert !(1>2)

assert 1<=2
assert 1<=1
assert !(2<=1)

assert 2>=1
assert 1>=1
assert !(1>=2)

;arithmetic
assert 1 + 2 == 3
assert 10 - 1 == 9
assert 2 * 5 == 10
assert 10 % 7 == 3
assert 1 - 3 == -2
assert 10//3==3

assert 1 + 2 * 3 == 7
assert 1 - 2 - 3 == -4
assert 1 - (2 - 3) == 2

;lists and strings
assert "ABC"==[65,66,67]
assert "A"==[65,]

assert "ABC"!0==65
assert "ABC"!1==66
assert "ABC"!2==67

assert len "ABC"==3
assert  "ab"@"cd"=="abcd"
assert [1+2,3+4]==[3,7]

assert range 1,5==[1,2,3,4]
assert range 5,1==[]
assert range 5==[0,1,2,3,4]

assert (1,2,3)==[1,2,3]

assert slice "abcd",1,3=="bc"

assert from"abcde",1=="bcde"
assert from"abcde",-1=="e"

assert in 8,[6,7,8]
assert! in 9,[6,7,8]

assert"""abc"""=="abc"

s="""\
	a
	b
	c
"""
assert s=="a\nb\nc\n"

assert startswith "qwerty","qwe"
assert !startswith "qwerty","qwz"

assert endswith "qwerty","rty"
assert !endswith "ty","rty"
assert !endswith "qwerty","ety"

assert lower 97==97
assert lower 65==97
assert lower "ABC..."=="abc..."

assert upper 97==65
assert upper 65==65
assert upper "abc..."=="ABC..."

assert #"\n"=="\\n"

assert utf8 0x24==(0x24,)
assert utf8 0xa3==(0xc2,0xa3)
assert utf8 0x939==(0xe0,0xa4,0xb9)
assert utf8 0x20ac==(0xe2,0x82,0xac)
assert utf8 0xd55c==(0xed,0x95,0x9c)
assert utf8 0x10348==(0xf0,0x90,0x8d,0x88)

assert parseint "123",10==123
assert parseint "123"==123
assert parseint "ff",16==255
assert parseint "FFFF",16==65535

assert isdigit ("9"!0,10)
assert isdigit ("9"!0,16)

assert !isdigit ("f"!0,10)
assert isdigit ("f"!0,16)

assert !isdigit ("*"!0,10)
assert !isdigit ("*"!0,16)

/*
assert unesc "abc"=="abc"
assert unesc #"\tabc\n"=="\tabc\n"
assert unesc #"\x41\x42"=="AB"
*/

;variables
x = 5
assert x == 5
x = 6
assert x == 6
y = 7
assert y == 7
a = b = 9
assert a == 9
assert b == 9

;augmented assignment
i = 3
i += 4
assert i == 7
i -= 1
assert i == 6
i=32
i += 10
j = i
assert i == 42
assert j == 42

;increment and decrement
i = 0

j = ++i
assert i == 1
assert j == 1

j = --i
assert i == 0
assert j == 0

j = i++
assert i == 1
assert j == 0

j = i--
assert i == 0
assert j == 1

;symbols
assert 'a'=='a'
assert 'a'!='b'

assert 'a'==intern"a"

a=gensym()
b=gensym()
assert a==a
assert a!=b

;types
assert isnum(0)
assert !issym(0)
assert !islist(0)

assert !isnum('a')
assert issym('a')
assert !islist('a')

assert !isnum([])
assert !issym([])
assert islist([])

;if
a = 0

if 1
	a = 1
else
	a = 2
assert a == 1

if 0
	a = 3
else
	a = 4
assert a == 4

if 1
	a = 5
assert a == 5

;loops
i = 1
n = 0
while i <= 10
	n += i
	i++
assert n == 55

i = 11
n = 0
while i <= 10
	n += i
	i++
assert n == 0

i = 1
n = 0
dowhile i <= 10
	n += i
	i++
assert n == 55

i = 11
n = 0
dowhile i <= 10
	n += i
	i++
assert n == 11

i = 0
while 1
	i++
	break
assert i == 1

i = 1
n = 0
while i <= 10
	n += i
	i++
	continue
assert n == 55

i = 1
a=""
while i <= 5
	a@=65,
	i++
assert a=="AAAAA"

i=0
m=0
for c: "ABC"
	i++
	m+=c
assert i==3
assert m==65+66+67

outer: while 1
	i=0
	inner: while i<10
		i++
		break outer
assert i==1

;records
a=[['x',1],['y',2]]

assert get a,'x'==1
assert get a,'y'==2
assert get a,'z'==0

assert  a.x==1
assert  a.y==2
assert  a.z==0

;lambda
f=\x,y:x+y
assert f(2,3)==5

f=\x,y(
	z=x+y
	return z
)
assert f(2,3)==5

f=\():77
assert f()==77

;functions
fn square a
	a*a

assert square 7==49

fn square1 a
	return a*a

assert square1 8==64

fn factorial n
	if n<=1
		return 1
	n*factorial(n-1)

assert factorial 5==120

fn zero
	return

assert zero()==0

fn foo bar
	bar=4
	bar

assert foo 3==4

;mutual recursion
fn even n
	assert n>=0
	if n==0
		return  1
	odd(n-1)

fn odd n
	assert n>=0
	if n==0
		return
	even(n-1)

assert even 0
assert odd 1
assert even 2
assert odd 3

;lexical scope
a=1

fn f10
	a=2

f10()
assert a==1

;nonlocal
a=1

fn f11
	nonlocal a
	a=2

f11()
assert a==2

;strings
assert str(123)=="123"
assert str('xyz')=="xyz"

assert fmt"abc"=="abc"
assert fmt"ab%scd","xy"=="abxycd"

;characters
assert isupper 65
assert !isupper 97

;higher-order functions
assert map( \x:x*x,[1,2,3])==[1,4,9]
assert map( square,[1,2,3])==[1,4,9]

assert filter( even,[1,2,3])==[2,]
assert filter( odd,[1,2,3])==[1,3]
assert filter( isdigit,"abc123")=="123"

;pattern matching assignment
a=1,2,3
assert a==[1,2,3]

a,b,c=1,2,3
assert a==1
assert b==2
assert c==3

a,(b,c),d=1,(2,3),4
assert a==1
assert b==2
assert c==3
assert d==4

a,*s=1,2,3
assert a==1
assert s==[2,3]

b=a,*s
assert b==[1,2,3]

a=(
	1
	2
	3
)
assert a==[1,2,3]

a=(
	1,
	2,
	3,
)
assert a==[1,2,3]

;case
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
	a,*s
		r=s
assert r==[2,3,4]

case 2
	1
		a="one"
	2
		a="two"
assert a=="two"

/* signal that tests actually completed */
printf("%s\n","ok")
