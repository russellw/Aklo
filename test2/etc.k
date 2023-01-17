
assert max( 2,3 )==3
assert max( 3,2 )==3

assert min (2,3)==2
assert min (3,2)==2

assert abs  (10)==10
assert abs  (0)==0
assert abs (-20)==20

assert signum (10)==1
assert signum (0)==0
assert signum (-20)==-1

;lists and strings
assert range( 1,5)==[1,2,3,4]
assert range( 5,1)==[]
assert range( 5)==[0,1,2,3,4]

assert slice ("abcd",1,3)=="bc"

assert drop (1,"abcde")=="bcde"

assert init("abcde")=="abcd"
assert init("")==""

assert in (8,[6,7,8])
assert! in (9,[6,7,8])

assert startsWith ("qwerty","qwe")
assert !startsWith ("qwerty","qwz")
assert !startsWith ("qw","qwer")

assert endsWith( "qwerty","rty")
assert !endsWith( "ty","rty")
assert !endsWith ("qwerty","ety")

assert lower (97)==97
assert lower (65)==97
assert lower ("ABC...")=="abc..."

assert upper (97)==65
assert upper (65)==65
assert upper ("abc...")=="ABC..."

assert #"\n"=="\\n"

assert utf8 (0x24)==[0x24]
assert utf8 (0xa3)==[0xc2,0xa3]
assert utf8 (0x939)==[0xe0,0xa4,0xb9]
assert utf8 (0x20ac)==[0xe2,0x82,0xac]
assert utf8 (0xd55c)==[0xed,0x95,0x9c]
assert utf8 (0x10348)==[0xf0,0x90,0x8d,0x88]

assert parseInt( "123",10)==123
assert parseInt ("ff",16)==255
assert parseInt ("FFFF",16)==65535

assert digitBase? ("9"[0],10)
assert digitBase? ("9"[0],16)

assert !digitBase? ("f"[0],10)
assert digitBase? ("f"[0],16)

assert !digitBase? ("*"[0],10)
assert !digitBase? ("*"[0],16)

assert unesc( "abc")=="abc"
assert unesc (#"\tabc\n")=="\tabc\n"
assert unesc (#"\x41\x42")=="AB"

assert reverse("abc")=="cba"

assert concat(("zxc","vb"))=="zxcvb"

assert replicate( 4,9)==[9,9,9,9]
assert replicate (-1,9)==[]

assert takeWhile (digit?,"123abc")=="123"
assert takeWhile (digit?,"123")=="123"
assert takeWhile (digit?,"abc")==""

assert dropWhile (digit?,"123abc")=="abc"
assert dropWhile (digit?,"123")==""
assert dropWhile (digit?,"abc")=="abc"

assert span (digit?,"123abc")==["123","abc"]

assert splitAt(2,"abcde")==["ab","cde"]
assert splitAt(-1,"abcde")==["","abcde"]
assert splitAt(20,"abcde")==["abcde",""]

assert zip ("abc","12")==["a1","b2"]
assert zip3 ("abc","12","xyz")==["a1x","b2y"]
assert zipWith(\(a,b)(a+b),[10,20],[1,2])==[11,22]
assert zipWith3(\(a,b,c)(a+b+c),[10,20],[1,2],[100,100])==[111,122]
assert unzip (["a1","b2","c3"])==["abc","123"]
assert unzip3 (["a1x","b2y","c3z"])==["abc","123","xyz"]

assert split(digit?,"a1b")==["a","b"]
assert split(digit?,"")==[""]
assert split(digit?,"1")==["",""]
assert split(digit?,"1zzz")==["","zzz"]
assert split(digit?,"...9")==["...",""]

assert splitOn("."[0],"123.456.")==["123","456",""]

assert lines("a\nb\nc\n\n")==["a","b","c",""]
assert lines("a\nb\nc\n")==["a","b","c"]
assert lines("a\nb\nc")==["a","b","c"]
assert lines("\n")==[""]
;the empty string is considered to consist of a single blank line
;unlike the Haskell lines function
assert lines("")==[""]

assert unlines(["ab","cd"])=="ab\ncd\n"


;sets
assert uniq("abcab")=="abc"
assert union  ("abc","bcd")=="abcd"
assert intersection  ("abc","bcd")=="bc"


;symbols

a=sym("foo")
b=sym("foo")
assert a==a
assert a!=b

;types
assert exact?(0)
assert number?(0)
assert !sym?(0)
assert !list?(0)

assert !exact?('a')
assert !number?('a')
assert sym?('a')
assert !list?('a')

assert !exact?([])
assert !number?([])
assert !sym?([])
assert list?([])

;if

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
	a@=[65]
	i++
assert a=="AAAAA"

i=0
m=0
for c: "ABC"
	i++
	m+=c
assert i==3
assert m==65+66+67

;records
a=[['x',1],['y',2]]

assert get( a,'x')==1
assert get (a,'y')==2
assert get (a,'z')==0

;lambda
f=\(x,y)(x+y)
assert f(2,3)==5

f=\(x,y)(
	z=x+y
	^ z
)
assert f(2,3)==5

f=\()(77)
assert f()==77

;functions
fn square (a)
		^ a*a

assert square (7)==49

fn factorial (n)
	if n<=1
		^ 1
	^ n*factorial(n-1)

assert factorial (5)==120

fn zero()
	^

assert zero()==0

fn foo (bar)
	bar=4
	^ bar

assert foo (3)==4

;mutual recursion
fn evenrec (n)
	assert n>=0
	if n==0
		^  1
	^ oddrec(n-1)

fn oddrec (n)
	assert n>=0
	if n==0
		^
	^ evenrec(n-1)

assert evenrec( 0)
assert oddrec (1)
assert evenrec (2)
assert oddrec (3)

;lexical scope
a=1

fn f10()
	a=2

f10()
assert a==1

;nonlocal variables
a=1

fn f11()
	a:=2

f11()
assert a==2

;strings
assert str(123)=="123"
assert str('xyz')=="xyz"

;characters
assert upper?( 65)
assert !upper?( 97)

assert printn?(65)
assert !printn?(10)

assert ascii?("abc")
assert !ascii?("abc\0")
assert !ascii?((1,2,3))
assert !ascii?(('x','y'))
assert !ascii?(789)

assert join(",",[])==""
assert join(",",["abc"])=="abc"
assert join(",",("abc","de"))=="abc,de"

;higher-order functions
assert map( \(x)(x*x),[1,2,3])==[1,4,9]
assert map( square,[1,2,3])==[1,4,9]

assert mapFirst(first,[["a",2],["b",3]])==[[97,2],[98,3]]
assert mapSecond(square,[["a",2],["b",3]])==[["a",4],["b",9]]

assert filter( digit?,"abc123")=="123"

assert every( upper?,"")
assert every (upper?,"ABC")
assert !every (upper?,"ABC.")
assert every (\(a) (a<10),(1,2,3))
assert! every (\(a)( a<10),(1,2,13))

assert !any( upper?,"")
assert any (upper?,"ABC")
assert any (upper?,"ABC.")
assert !any (upper?,".")

fn add (a,b)
	^ a+b

assert fold(add,0,(1,2,3,4))==10
assert fold(add,0,[1])==1
assert fold(add,0,[])==0

assert fold(\(s,t)(s@t),"",("abc","def","gh"))=="abcdefgh"

assert reduce(add,(1,2,3,4))==10
assert reduce(add,[1])==1

assert reduce(\(s,t)(s@t),("abc","def","gh"))=="abcdefgh"

assert maximum( "abc")=="c"[0]
assert maximum ("cba")=="c"[0]

assert minimum ("abc")=="a"[0]
assert minimum ("cba")=="a"[0]

assert sum((1,2,3,4))==10
assert sum([])==0

assert product((1,2,3,4))==24
assert product([])==1

assert replace( "a","aa","aaa")=="aaaaaa"

assert find(digit?,"abc123xyz")==3
assert find(digit?,"123xyz")==0
assert find(digit?,"abcxyz")==-1

assert findLast(digit?,"abc123xyz")==5
assert findLast(digit?,"abc123")==5
assert findLast(digit?,"abcxyz")==-1

;commonPrefix
assert commonPrefix(("a","ab","abc"))=="a"
assert commonPrefix(("ab","ab","ab"))=="ab"
assert commonPrefix(("ab","ab","az"))=="a"
assert commonPrefix(("ab","ab","b"))==""
assert commonPrefix(["ab"])=="ab"

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

;filenames
assert dirSeparator? ("/"[0])
assert !dirSeparator? ("a"[0])

assert splitDir("/")==("/","")
assert splitDir("/a")==("/","a")
assert splitDir("foo/bar")==("foo/","bar")
assert splitDir("foo/")==("foo/","")
assert splitDir("foo")==("","foo")

assert  splitExt("abcd.exe")[0]=="abcd"
assert  splitExt("abcd.exe")[1]=="exe"

assert ascii?(joinDir(["foo"]))
assert ascii?(joinDir(("foo","bar")))
assert ascii?(joinDir(("foo/","bar")))

files=keys(compileTimeReadFiles(\(file)(endsWith(file,".k")),"."))
assert files

;etc
s=[]
s<<1
s<<2
assert s==[1,2]

s=[]
1>>s
2>>s
assert s==[2,1]

var v1=10
var v2=v1

{signal that tests actually completed}
prn("ok")
