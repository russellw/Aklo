{
OUT
A
}

a,b=65,66
print(a)

a,2=1,2
assert a==1

x,2,3,(y,z),6=1,2,3,[4,5],6
assert x==1
assert y==4
assert z==5

_,a,_=1,2,3
assert a==2

'q',w,e='q','w','e'
assert (w,e)==('w','e')

"foo",s="foo","bar"
assert s=="bar"

a,@b="ABC"
assert a==65
assert b=="BC"

a,b=2,3,4
assert a==2
assert b==3

a=1,2,3
assert a==[1,2,3]

a,b,c=1,2,3
assert a==1
assert b==2
assert c==3

a,(b,c),d=1,(2,3),4
assert a==1
assert b==2
assert c==3
assert d==4

a,@s=1,2,3
assert a==1
assert s==[2,3]

b=a,@s
assert b==[1,2,3]

a=[
	1
	2
	3
]
assert a==[1,2,3]

1,a,b=1,2,3
assert a==2
assert b==3

a=1
b=[2]
[a]=b
assert a==2
