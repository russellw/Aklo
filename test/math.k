{
OUT
1.5
}

assert true+2==3
assert true+2===3

assert true===1.0
assert 1===1.0
assert 1.0f===1.0
assert 1.0===1.0

assert true===1.0f
assert 1===1.0f
assert 1.0f===1.0f
assert 1.0===1.0f

assert true===1
assert 1===1
assert 1.0f===1
assert 1.0===1

assert true===true
assert 1===true
assert 1.0f===true
assert 1.0===true

println str(1.0+0.5)

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
