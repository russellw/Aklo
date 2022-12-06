{
OUT
A
}

var c int=6
var d int=c

if 1
	goto next
:next

if 1
	goto next1
else
	goto next2
:next1
:next2

:stuck
while 0
	goto stuck

while d<65
	d=d+1

/putchar(d)
/putchar(10)
