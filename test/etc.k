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
