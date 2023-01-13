assert "ABC"==[65,66,67]
assert "A"==[65]

assert "ABC"[0]==65
assert "ABC"[1]==66
assert "ABC"[2]==67

assert "ABC"[false]==65
assert "ABC"[true]==66

assert len( "")==0
assert len( [""])==1
assert len( "ABC")==3

assert  "ab"@"cd"=="abcd"
assert [1+2,3+4]==[3,7]

assert slice("abcd",1,3)=="bc"

s="abc"
s@="de"
assert s=="abcde"

s="BC"
s<<68
assert s=="BCD"
