{
OUT
[1, 2, 3]
[-1, -2, -3]
}

assert 7==7
print  str([1,2,3])
print  str(-[1,2,3])
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

assert 1<2
assert !(1<1)
assert !(2<1)

assert 1<=2
assert (1<=1)
assert !(2<=1)

;augmented assignment
i = 3
i += 4
assert i == 7
i -= 1
assert i == 6
i=32
i += 10
j = i
assert i == 42
assert j == 42

a,b,c=1,2,3
a,b,c+=9
assert(a,b,c)==(10,11,12)

;increment and decrement
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

q=9
