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

assert !bitAnd(0 , 0)
assert !bitAnd(0 , 1)
assert !bitAnd(1 , 0)
assert bitAnd( 1 , 1)

assert !bitOr(0 , 0)
assert bitOr( 0 , 1)
assert bitOr( 1 , 0)
assert bitOr( 1 , 1)

assert !bitXor(0 , 0)
assert bitXor (0 , 1)
assert bitXor (1 , 0)
assert !bitXor(1 , 1)

assert shl( 1,8)==256
assert shr (256,8)==1

assert bitNot (0)==-1

a=1
assert bitNot (a)==-2

assert 0xff==255
assert 0Xfe+2==256
assert 0b10000==16
assert 0o100==64
assert 1_234==1234

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

assert 1 + 2 == 3
assert 10 - 1 == 9
assert 2 * 5 == 10
assert 10 % 7 == 3
assert 1 - 3 == -2
assert 10//3==3

assert 1 + 2 * 3 == 7
assert 1 - 2 - 3 == -4
assert 1 - (2 - 3) == 2

i=3
i+=4
assert i==7

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

/putchar(d)
/putchar(10)
