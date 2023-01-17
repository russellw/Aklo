;logic
assert 1
assert !0
assert !!9

assert "a"
assert !""

assert !0.0
assert 1.0

assert !0.0f
assert 1.0f

assert !(0/10)
assert (1/10)

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

assert 5|(1/0)==5
