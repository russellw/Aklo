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

;lists
assert "ABC"==[65,66,67]
assert "A"==[65]

assert "ABC"[0]==65
assert "ABC"[1]==66
assert "ABC"[2]==67

assert len "ABC"==3
assert  cat"ab","cd"=="abcd"
assert append([1,2],3)==[1,2,3]
assert [1+2,3+4]==[3,7]

assert range 1,5==[1,2,3,4]
assert range 5,1==[]
assert range 5==[0,1,2,3,4]

assert (1,2,3)==[1,2,3]

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
x = 3
x += 4
assert x == 7
x -= 1
assert x == 6
x=32
y = x += 10
assert x == 42
assert y == 42

;increment and decrement
x = 0

y = ++x
assert x == 1
assert y == 1

y = --x
assert x == 0
assert y == 0

y = x++
assert x == 1
assert y == 0

y = x--
assert x == 0
assert y == 1

;symbols
assert 'a'=='a'
assert 'a'!='b'

assert 'a'==intern"a"

a=gensym()
b=gensym()
assert a==a
assert a!=b

;types
assert num?(0)
assert !sym?(0)
assert !list?(0)

assert !num?('a')
assert sym?('a')
assert !list?('a')

assert !num?([])
assert !sym?([])
assert list?([])

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
	break
assert n == 55

i = 1
a=""
b=[]
while i <= 5
	push a,65
	pushs b,[65]
	i++
assert a=="AAAAA"
assert b=="AAAAA"

n=0
m=0
for c "ABC"
	n++
	m+=c
assert n==3
assert m==65+66+67

;goto
i = 1
n = 0
:loop
n += i
i++
if i <= 10
	goto loop

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

assert fn?(f)
assert !fn?(9)

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

/* signal that tests actually completed */
writeStream(stdout,"ok\n")
