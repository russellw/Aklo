fn factorial (n)
	if n<=1
		^ 1
	^ n*factorial(n-1)

assert factorial (5)==120
