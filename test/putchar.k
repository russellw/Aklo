{
OUT
A
}

var c int=65
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

/putchar(d)
/putchar(10)
