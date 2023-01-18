i = 1
n = 0
while i <= 10
	n += i
	i++
assert n == 55

i = 11
n = 0
while i <= 10
	n += i
	i++
assert n == 0

i = 1
n = 0
dowhile i <= 10
	n += i
	i++
assert n == 55

i = 11
n = 0
dowhile i <= 10
	n += i
	i++
assert n == 11

i = 0
while 1
	i++
	break
assert i == 1

i = 1
n = 0
while i <= 10
	n += i
	i++
	continue
assert n == 55

i = 1
a=""
while i <= 5
	a@=[65]
	i++
assert a=="AAAAA"
