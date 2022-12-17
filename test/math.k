{
OUT
1.5
}

assert (1==1)+2==3
assert (1==1)+2===3

{
assert (1==1)==1.0
assert 1==1.0
assert 1.0f==1.0
assert 1.0==1.0

assert (1==1)==1.0f
assert 1==1.0f
assert 1.0f==1.0f
assert 1.0==1.0f
}

assert (1==1)==1
assert 1==1
assert 1.0f==1
assert 1.0==1

assert (1==1)==(1==1)
assert 1==(1==1)
assert 1.0f==(1==1)
assert 1.0==(1==1)

/printf("%0.1f\n",1.0+0.5)
