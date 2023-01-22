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

assert digit ("9"[0])==9
assert digit ("f"[0])==15
assert digit ("*"[0])>=36

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
