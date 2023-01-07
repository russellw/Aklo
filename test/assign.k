{
OUT
A
}

a,b=65,66
println(a)

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
