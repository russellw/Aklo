foo=intern("foo")
assert foo==intern("foo")
assert foo!=intern("bar")
assert str(foo)=="foo"

assert foo=='foo'
