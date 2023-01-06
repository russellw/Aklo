{
OUT
[1, 2, 3]
[-1, -2, -3]
}

assert 7==7
println  str([1,2,3])
println  str(-[1,2,3])
assert [1,2,3]+1==[2,3,4]
assert 1+[1,2,3]==[2,3,4]
assert [1,2,3]+[1,2,3]==[2,4,6]
assert 1+[[1,2,3],[1,2,3]]==[[2,3,4],[2,3,4]]

assert cmp(10,20)==-1
assert cmp(10,10)==0
assert cmp(20,10)==1
assert cmp(20,[10,10,10])==[1,1,1]

assert (10===10)==true
assert (10===20)==false
assert ([10,20,30]===[20,20,20])==[false,true,false]

assert 1<2
assert !(1<1)
assert !(2<1)

assert 1<=2
assert (1<=1)
assert !(2<=1)
