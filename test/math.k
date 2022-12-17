{
OUT
1.5
}

assert true+2==3
assert true+2===3

{
assert true===1.0
assert 1===1.0
assert 1.0f===1.0
assert 1.0===1.0

assert true===1.0f
assert 1===1.0f
assert 1.0f===1.0f
assert 1.0===1.0f
}

assert true===1
assert 1===1
assert 1.0f===1
assert 1.0===1

assert true===true
assert 1===true
assert 1.0f===true
assert 1.0===true

/printf("%0.1f\n",1.0+0.5)
