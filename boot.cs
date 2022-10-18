#pragma warning disable CS8321

using System;
using System.Collections.Generic;

void print(object a) {
  if (a is List<object> s) {
    foreach (var c in s)
      Console.Write((char)(int)c);
    return;
  }
  throw new ArgumentException(a.ToString());
}

List<object> ls(params object[] a) { return new List<object>(a); }

bool truth(object a) {
  if (a is int)
    return (int)a != 0;
  return true;
}

main();
Console.WriteLine("done");
