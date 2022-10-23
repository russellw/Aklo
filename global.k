fn upper? c
	"A"[0]<=c&&c<="Z"[0]

fn lower? c
	"a"[0]<=c&&c<="z"[0]

fn digit? c
	"0"[0]<=c&&c<="9"[0]

fn alpha? c
	lower? c||upper? c

fn alnum? c
	alpha? c||digit? c

fn map f,a
	r=[]
	for b a
		push r,f  b
	r
