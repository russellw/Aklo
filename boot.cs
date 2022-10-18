#pragma warning disable CS8321

using System;
using System.Collections.Generic;
using System.IO;

int add(object a, object b) { return (int)a + (int)b; }

List<object> append(object a, object b) {
  var a1 = (List<object>)a;
  var r = new List<object>(a1);
  r.Add(b);
  return r;
}

List<object> cat(object a, object b) {
  var a1 = (List<object>)a;
  var b1 = (List<object>)b;
  var r = new List<object>(a1);
  r.AddRange(b1);
  return r;
}

void eprint(object a) { fprint(Console.Error, a); }

bool eq(object a, object b) { return a.Equals(b); }

void fprint(TextWriter writer, object a) {
  if (a is List<object> s) {
    foreach (var c in s)
      writer.Write((char)(int)c);
    return;
  }
  throw new ArgumentException(a.ToString());
}

int len(object a) {
  var a1 = (List<object>)a;
  return a1.Count;
}

List<object> ls(params object[] a) { return new List<object>(a); }

int neg(object a) { return -(int)a; }

void print(object a) { fprint(Console.Out, a); }

int sub(object a, object b) { return (int)a - (int)b; }

object subscript(object a, object i) {
  var a1 = (List<object>)a;
  var i1 = (int)i;
  return a1[i1];
}

bool truth(object a) {
  if (a is int)
    return (int)a != 0;
  return true;
}

main();
Console.WriteLine("done");
