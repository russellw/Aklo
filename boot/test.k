;the identity function
assert id 12345==12345

;logic
assert 1
assert !0
assert !!9

assert "a"
assert !""

assert !(0 | 0)
assert 0 | 1
assert 1 | 0
assert 1 | 1

assert !(0 & 0)
assert !(0 & 1)
assert !(1 & 0)
assert 1 & 1

assert !(false & false)
assert !(false & true)
assert !(true & false)
assert true & true

;bitwise
assert !bitand(0 , 0)
assert !bitand(0 , 1)
assert !bitand(1 , 0)
assert bitand 1 , 1

assert !bitor(0 , 0)
assert bitor 0 , 1
assert bitor 1 , 0
assert bitor 1 , 1

assert !bitxor(0 , 0)
assert bitxor 0 , 1
assert bitxor 1 , 0
assert !bitxor(1 , 1)

assert shl 1,8==256
assert shr 256,8==1

assert bitnot 0==-1

a=1
assert bitnot a==-2

;numbers
assert 0xff==255
assert 0Xfe+2==256
assert 0b10000==16
assert 0o100==64
assert 1_234==1234

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

assert max 2,3==3
assert max 3,2==3

assert min 2,3==2
assert min 3,2==2

assert abs 10==10
assert abs 0==0
assert abs (-20)==20

assert signum 10==1
assert signum 0==0
assert signum (-20)==-1

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

assert even 0
assert odd 1
assert even 2
assert odd 3

;lists and strings
assert "ABC"==[65,66,67]
assert "A"==[65,]

assert "ABC"!0==65
assert "ABC"!1==66
assert "ABC"!2==67

assert (1,2)^3==(1,2,3)

assert len "ABC"==3
assert  "ab"@"cd"=="abcd"
assert [1+2,3+4]==[3,7]

assert range 1,5==[1,2,3,4]
assert range 5,1==[]
assert range 5==[0,1,2,3,4]

assert (1,2,3)==[1,2,3]

assert slice "abcd",1,3=="bc"

assert drop 1,"abcde"=="bcde"

assert init"abcde"=="abcd"
assert init""==""

assert in 8,[6,7,8]
assert! in 9,[6,7,8]

assert startswith "qwerty","qwe"
assert !startswith "qwerty","qwz"
assert !startswith "qw","qwer"

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

assert unesc "abc"=="abc"
assert unesc #"\tabc\n"=="\tabc\n"
assert unesc #"\x41\x42"=="AB"

assert reverse"abc"=="cba"

assert concat(("zxc","vb"))=="zxcvb"

assert replicate 4,9==(9,9,9,9)
assert replicate (-1,9)==()

assert takewhile isdigit,"123abc"=="123"
assert takewhile isdigit,"123"=="123"
assert takewhile isdigit,"abc"==""

assert dropwhile isdigit,"123abc"=="abc"
assert dropwhile isdigit,"123"==""
assert dropwhile isdigit,"abc"=="abc"

assert span isdigit,"123abc"==("123","abc")

assert splitat(2,"abcde")==("ab","cde")
assert splitat(-1,"abcde")==("","abcde")
assert splitat(20,"abcde")==("abcde","")

assert zip ("abc","12")==("a1","b2")
assert zip3 ("abc","12","xyz")==("a1x","b2y")
assert zipwith(\(a,b)(a+b),(10,20),(1,2))==(11,22)
assert zipwith3(\(a,b,c)(a+b+c),(10,20),(1,2),(100,100))==(111,122)
assert unzip (("a1","b2","c3"))==("abc","123")
assert unzip3 (("a1x","b2y","c3z"))==("abc","123","xyz")

assert split(isdigit,"a1b")==("a","b")
assert split(isdigit,"")==("",)
assert split(isdigit,"1")==("","")
assert split(isdigit,"1zzz")==("","zzz")
assert split(isdigit,"...9")==("...","")

assert spliton("."!0,"123.456.")==("123","456","")

assert lines("a\nb\nc\n\n")==("a","b","c","")
assert lines("a\nb\nc\n")==("a","b","c")
assert lines("a\nb\nc")==("a","b","c")
assert lines("\n")==("",)
;the empty string is considered to consist of a single blank line
;unlike the Haskell lines function
assert lines("")==("",)

assert unlines(("ab","cd"))=="ab\ncd\n"

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
for c "ABC"
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
f=\x,y(x+y)
assert f(2,3)==5

f=\x,y(
	z=x+y
	return z
)
assert f(2,3)==5

f=\()(77)
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
fn evenrec n
	assert n>=0
	if n==0
		return  1
	oddrec(n-1)

fn oddrec n
	assert n>=0
	if n==0
		return
	evenrec(n-1)

assert evenrec 0
assert oddrec 1
assert evenrec 2
assert oddrec 3

;lexical scope
a=1

fn f10
	a=2

f10()
assert a==1

;nonlocal variables
a=1

fn f11
	^ a
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
assert map( succ,[1,2,3])==[2,3,4]
assert map( \x(x*x),[1,2,3])==[1,4,9]
assert map( square,[1,2,3])==[1,4,9]

assert filter( even,[1,2,3])==[2,]
assert filter( odd,[1,2,3])==[1,3]
assert filter( isdigit,"abc123")=="123"

assert every isupper,""
assert every isupper,"ABC"
assert !every isupper,"ABC."
assert every (\a (a<10),(1,2,3))
assert! every (\a( a<10),(1,2,13))

assert !any isupper,""
assert any isupper,"ABC"
assert any isupper,"ABC."
assert !any isupper,"."

fn add a,b
	a+b

assert fold(add,0,(1,2,3,4))==10
assert fold(add,0,(1))==1
assert fold(add,0,())==0

assert fold(\(s,t)(s@t),"",("abc","def","gh"))=="abcdefgh"

assert reduce(add,(1,2,3,4))==10
assert reduce(add,(1))==1
assert reduce(add,())==0

assert reduce(\(s,t)(s@t),("abc","def","gh"))=="abcdefgh"

assert maximum "abc"=="c"!0
assert maximum "cba"=="c"!0

assert minimum "abc"=="a"!0
assert minimum "cba"=="a"!0

assert sum((1,2,3,4))==10
assert sum(())==0

assert product((1,2,3,4))==24
assert product(())==1

assert replace "a","aa","aaa"=="aaaaaa"

;commonprefix
assert commonprefix(("a","ab","abc"))=="a"
assert commonprefix(("ab","ab","ab"))=="ab"
assert commonprefix(("ab","ab","az"))=="a"
assert commonprefix(("ab","ab","b"))==""
assert commonprefix(("ab",))=="ab"
;TODO: what should be the common prefix of the empty list of lists?
;assert commonprefix(())==""

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

{signal that tests actually completed}
printf("%s\n","ok")
