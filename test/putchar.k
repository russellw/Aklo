{
OUT
A
}

var c int=6
var d int=c

if 1
	goto next
:next

if 1
	goto next1
else
	goto next2
:next1
:next2

:stuck
while 0
	goto stuck

while d<65
	d=d+1

assert 1

i=0
while i<10
	i=i+1
assert i==10

assert 1
assert !0
assert !!9

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

assert !bitand(0 , 0)
assert !bitand(0 , 1)
assert !bitand(1 , 0)
assert bitand( 1 , 1)

assert !bitor(0 , 0)
assert bitor( 0 , 1)
assert bitor( 1 , 0)
assert bitor( 1 , 1)

assert !bitxor(0 , 0)
assert bitxor (0 , 1)
assert bitxor (1 , 0)
assert !bitxor(1 , 1)

assert shl( 1,8)==256
assert shr (256,8)==1

assert bitnot (0)==-1

a=1
assert bitnot (a)==-2

/putchar(d)
/putchar(10)
