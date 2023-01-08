foo=intern("foo")
assert foo==intern("foo")
assert foo!=intern("bar")
assert str(foo)=="foo"

assert foo=='foo'

assert 'a'=='a'
assert 'a'!='b'

assert 'a'==intern("a")
