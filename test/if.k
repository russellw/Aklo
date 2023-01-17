if 1
	a=2
else
	a=3

assert a==2

a = 0

if 1
	a = 1
else
	a = 2
assert a == 1

if 0
	a = 3
else
	a = 4
assert a == 4

if 1
	a = 5
assert a == 5

if 0
	a = 3
elif 0
	a = 4
else
	a=9
assert a == 9
