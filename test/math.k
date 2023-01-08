{
OUT
1.5
}

assert true!=1
assert 1.0!=1.0f

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
assert 0xff_ff==65535

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

assert 2.0**3.0==8.0
assert 2.0f**3.0===8.0
assert 2.0**3.0f===8.0
assert 2.0f**3.0f===8.0

assert 2**3==8

assert (2/1)**(3/1)===8

;parsing numbers
assert parseint("1000")==1000
assert parseint("1000",9+1)==1000
assert parseint("1000",15+1)==4096

assert parsefloat("1.5")==1.5f
assert parsedouble("1.5")==1.5

assert parserat("2/3")==2/3
assert parserat("2/1")==2/1

;big numbers
a=100000000000000000000000000000000000000000000000000000000000000000000
b=99999999999999999999999999999999999999999999999999999999999999999999
assert a==b+1

s="100000000000000000000000000000000000000000000000000000000000000000000"
assert str(a)==s
