package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class Parser {
  // Tokens
  private static final int ADD_ASSIGN = -2;
  private static final int APPEND = -3;
  private static final int ASSIGN = -4;
  private static final int CAT_ASSIGN = -5;
  private static final int DEC = -6;
  private static final int DEDENT = -7;
  private static final int EQ = -8;
  private static final int EXP = -9;
  private static final int GE = -10;
  private static final int WORD = -11;
  private static final int DIV_INT = -12;
  private static final int INC = -13;
  private static final int INDENT = -14;
  private static final int LE = -15;
  private static final int NE = -16;
  private static final int EQ_NUM = -17;
  private static final int NE_NUM = -18;
  private static final int STR = -20;
  private static final int SUB_ASSIGN = -21;
  private static final int SYM = -22;
  private static final int NUM = -23;

  // File state
  private final String file;
  private final byte[] text;
  private int ti;
  private int line = 1;

  // indentation
  private int dentc;
  private final List<Integer> cols = new ArrayList<>(List.of(0));
  private int dedents;

  // current token
  private int tok;
  private byte[] tokBytes;
  private String tokString;

  private static boolean isDigit(int c) {
    return '0' <= c && c <= '9';
  }

  private static boolean isLower(int c) {
    return 'a' <= c && c <= 'z';
  }

  private static boolean isUpper(int c) {
    return 'A' <= c && c <= 'Z';
  }

  private static boolean isAlpha(int c) {
    return isLower(c) || isUpper(c);
  }

  private static boolean isAlnum(int c) {
    return isAlpha(c) || isDigit(c);
  }

  private static boolean isWord(int c) {
    return isAlnum(c) || c == '_' || c == '?';
  }

  private static int digit(int c) {
    if (isDigit(c)) return c - '0';
    if (isLower(c)) return c - 'a' + 10;
    if (isUpper(c)) return c - 'A' + 10;
    return 99;
  }

  private static boolean isDigits(String s) {
    for (var i = 0; i < s.length(); i++) if (!isDigit(s.charAt(i))) return false;
    return true;
  }

  private static boolean isHex(String s) {
    for (var i = 0; i < s.length(); i++) if (digit(s.charAt(i)) >= 16) return false;
    return true;
  }

  private static int toLower(int c) {
    return isUpper(c) ? c + 32 : c;
  }

  // tokenizer
  private void lexQuote() {
    var quote = text[ti];
    var i = ti + 1;
    var baos = new ByteArrayOutputStream();
    while (text[i] != quote) {
      var c = text[i++] & 0xff;
      if (c < ' ') throw new CompileError(file, line, "unclosed quote");
      if (c == '\\') {
        c = text[i++] & 0xff;
        if (c < ' ') throw new CompileError(file, line, "unclosed quote");
        switch (c) {
          case 'b' -> c = '\b';
          case 'f' -> c = '\f';
          case 'n' -> c = '\n';
          case 'r' -> c = '\r';
          case 't' -> c = '\t';
          case 'a' -> c = 7;
          case 'e' -> c = 0x1b;
          case 'v' -> c = 0xb;
          case '0', '1', '2', '3', '4', '5', '6', '7' -> {
            i--;
            c = 0;
            for (var j = 0; j < 3; j++) {
              var d = digit(text[i]);
              if (d >= 8) break;
              i++;
              c = c * 8 + d;
            }
          }
          case 'x' -> {
            c = 0;
            for (var j = 0; j < 2; j++) {
              var d = digit(text[i]);
              if (d >= 16) break;
              i++;
              c = c * 16 + d;
            }
          }
          case 'u', 'U' -> {
            c = 0;
            for (var j = 0; j < 8; j++) {
              var d = digit(text[i]);
              if (d >= 16) break;
              i++;
              c = c * 16 + d;
            }
            baos.writeBytes(Character.toString(c).getBytes(StandardCharsets.UTF_8));
            continue;
          }
        }
      }
      baos.write(c);
    }
    ti = i + 1;
    tokBytes = baos.toByteArray();
  }

  private void lex() {
    // a single newline can produce multiple dedent tokens
    if (dedents > 0) {
      dedents--;
      tok = DEDENT;
      return;
    }
    while (ti < text.length) {
      // the simplest tokens are just one character
      tok = text[ti];
      switch (text[ti]) {
        case ';' -> {
          var i = ti;
          do i++;
          while (text[i] != '\n');
          ti = i;
          continue;
        }
        case '{' -> {
          var i = ti;
          var line1 = line;
          do {
            i++;
            if (i == text.length) throw new CompileError(file, line, "unmatched '{'");
            if (text[i] == '\n') line1++;
          } while (text[i] != '}');
          ti = i + 1;
          line = line1;
          continue;
        }
        case '\n' -> {
          // next line
          ti++;
          line++;
          if (ti == text.length) return;

          // measure indent
          var col = 0;
          while (text[ti] == '\t' || text[ti] == ' ') {
            if (text[ti] != dentc) {
              if (dentc != 0)
                throw new CompileError(file, line, "indented with tabs and spaces in same file");
              dentc = text[ti];
            }
            ti++;
            col++;
          }

          // nothing important on this line, keep going
          switch (text[ti]) {
            case '\n', ';', '{' -> {
              continue;
            }
          }

          // one indent
          if (col > cols.get(cols.size() - 1)) {
            cols.add(col);
            tok = INDENT;
            return;
          }

          // zero or more dedents
          while (col < cols.get(cols.size() - 1)) {
            cols.remove(cols.size() - 1);
            dedents++;
          }
          if (col != cols.get(cols.size() - 1))
            throw new CompileError(file, line, "inconsistent indent");
          return;
        }
        case ' ', '\f', '\r', '\t' -> {
          ti++;
          continue;
        }
        case '!' -> {
          if (text[ti + 1] == '=') {
            if (text[ti + 2] == '=') {
              ti += 3;
              tok = NE_NUM;
              return;
            }
            ti += 2;
            tok = NE;
            return;
          }
        }
        case ':' -> {
          if (text[ti + 1] == '=') {
            ti += 2;
            tok = ASSIGN;
            return;
          }
        }
        case '@' -> {
          if (text[ti + 1] == '=') {
            ti += 2;
            tok = CAT_ASSIGN;
            return;
          }
        }
        case '=' -> {
          if (text[ti + 1] == '=') {
            if (text[ti + 2] == '=') {
              ti += 3;
              tok = EQ_NUM;
              return;
            }
            ti += 2;
            tok = EQ;
            return;
          }
        }
        case '*' -> {
          if (text[ti + 1] == '*') {
            ti += 2;
            tok = EXP;
            return;
          }
        }
        case '/' -> {
          if (text[ti + 1] == '/') {
            ti += 2;
            tok = DIV_INT;
            return;
          }
        }
        case '+' -> {
          switch (text[ti + 1]) {
            case '=' -> {
              ti += 2;
              tok = ADD_ASSIGN;
              return;
            }
            case '+' -> {
              ti += 2;
              tok = INC;
              return;
            }
          }
        }
        case '-' -> {
          switch (text[ti + 1]) {
            case '=' -> {
              ti += 2;
              tok = SUB_ASSIGN;
              return;
            }
            case '-' -> {
              ti += 2;
              tok = DEC;
              return;
            }
          }
        }
        case '<' -> {
          switch (text[ti + 1]) {
            case '=' -> {
              ti += 2;
              tok = LE;
              return;
            }
            case '<' -> {
              ti += 2;
              tok = APPEND;
              return;
            }
          }
        }
        case '>' -> {
          if (text[ti + 1] == '=') {
            ti += 2;
            tok = GE;
            return;
          }
        }
        case '#' -> {
          if (text[ti + 1] != '"') throw new CompileError(file, line, "stray '#'");
          var i = ti + 2;
          while (text[i] != '"') {
            if (text[i] == '\\') i++;
            if ((text[i] & 0xff) < ' ') throw new CompileError(file, line, "unclosed quote");
            i++;
          }
          tok = STR;
          var n = i - (ti + 2);
          tokBytes = new byte[n];
          System.arraycopy(text, ti + 2, tokBytes, 0, n);
          ti = i + 1;
          return;
        }
        case '"' -> {
          lexQuote();
          tok = STR;
          return;
        }
        case '\'' -> {
          lexQuote();
          tok = SYM;
          return;
        }
        case 'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '_',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z' -> {
          var i = ti;
          do {
            text[i] = (byte) toLower(text[i]);
            i++;
          } while (isWord(text[i]));
          tok = WORD;
          tokString = new String(text, ti, i - ti, StandardCharsets.US_ASCII);
          ti = i;
          return;
        }
        case '.' -> {
          if (!isDigit(text[ti + 1])) break;
          var i = ti;

          // decimal part
          do i++;
          while (isWord(text[i]));

          // signed exponent
          switch (text[i]) {
            case '+', '-' -> {
              switch (text[i - 1]) {
                case 'e', 'E' -> {
                  do i++;
                  while (isWord(text[i]));
                }
              }
            }
          }

          tok = NUM;
          tokString = new String(text, ti, i - ti, StandardCharsets.US_ASCII);
          ti = i;
          return;
        }
        case '0' -> {
          var i = ti;

          // integer part
          do i++;
          while (isWord(text[i]));

          // decimal part
          if (text[i] == '.') do i++; while (isWord(text[i]));

          // signed exponent
          switch (text[ti + 1]) {
            case 'x', 'X' -> {
              switch (text[i]) {
                case '+', '-' -> {
                  switch (text[i - 1]) {
                    case 'p', 'P' -> {
                      do i++;
                      while (isWord(text[i]));
                    }
                  }
                }
              }
            }
            default -> {
              switch (text[i]) {
                case '+', '-' -> {
                  switch (text[i - 1]) {
                    case 'e', 'E' -> {
                      do i++;
                      while (isWord(text[i]));
                    }
                  }
                }
              }
            }
          }

          tok = NUM;
          tokString = new String(text, ti, i - ti, StandardCharsets.US_ASCII);
          ti = i;
          return;
        }
        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
          var i = ti;

          // integer part
          do i++;
          while (isWord(text[i]));

          // decimal part
          if (text[i] == '.') do i++; while (isWord(text[i]));

          // signed exponent
          switch (text[i]) {
            case '+', '-' -> {
              switch (text[i - 1]) {
                case 'e', 'E' -> {
                  do i++;
                  while (isWord(text[i]));
                }
              }
            }
          }

          tok = NUM;
          tokString = new String(text, ti, i - ti, StandardCharsets.US_ASCII);
          ti = i;
          return;
        }
      }
      ti++;
      return;
    }
    tok = DEDENT;
  }

  private int lookahead() {
    var ti = this.ti;
    for (; ; )
      switch (text[ti]) {
        case ' ', '\f', '\r', '\t' -> ti++;
        case '{' -> {
          do {
            ti++;
            if (ti == text.length) return this.ti;
          } while (text[ti] != '}');
          ti++;
        }
        default -> {
          return ti;
        }
      }
  }

  // parser
  private CompileError err(String msg) {
    var line1 = line;
    switch (tok) {
      case '\n', INDENT -> line1--;
    }
    return new CompileError(file, line1, msg);
  }

  private boolean eat(int k) {
    if (tok != k) return false;
    lex();
    return true;
  }

  private void expect(char k) {
    if (!eat(k)) throw err(String.format("expected '%c'", k));
  }

  private void expectIndent() {
    if (!eat(INDENT)) throw err("expected indented block");
  }

  private void expectNewline() {
    if (!eat('\n')) throw err("expected newline");
  }

  private String currentWord() {
    if (tok == WORD) return tokString;
    return "";
  }

  private String word() {
    var s = tokString;
    if (!eat(WORD)) throw err("expected word");
    return s;
  }

  private final class Context {
    final Context outer;
    final Fn fn;
    final String label;
    final Block continueTarget;
    final Block breakTarget;

    final Map<String, Object> locals = new HashMap<>();

    Context(Fn fn) {
      outer = null;
      this.fn = fn;
      label = null;
      continueTarget = null;
      breakTarget = null;
    }

    Context(Context outer, String label, Block continueTarget, Block breakTarget) {
      this.outer = outer;
      fn = outer.fn;
      this.label = label;
      this.continueTarget = continueTarget;
      this.breakTarget = breakTarget;
    }

    void local(String name, Object x) {
      if (locals.put(name, x) != null) throw err(name + " defined twice");
    }

    void add(Block block) {
      fn.blocks.add(block);
    }

    Instruction ins(Instruction a) {
      // TODO block should be an instruction constructor parameter?
      // depending on how the optimizer typically needs to work
      fn.lastBlock().instructions.add(a);
      return a;
    }

    // expressions
    Object arg() {
      expect('(');
      var a = expr();
      expect(')');
      return a;
    }

    Object arg1() {
      expect('(');
      return expr();
    }

    Object argN() {
      expect(',');
      var a = expr();
      expect(')');
      return a;
    }

    Instruction listRest(List<Object> s, Object t) {
      return ins(new Cat(ins(new ListOf(s.toArray())), t));
    }

    Object primary() {
      var loc = new Loc(file, line);

      // having noted the current line, factor out the moving to the next token
      var k = tok;
      var b = tokBytes;
      var s = tokString;
      lex();

      try {
        switch (k) {
          case '[' -> {
            var r = new ArrayList<>();
            switch (tok) {
              case INDENT -> {
                lex();
                do {
                  if (eat('@')) {
                    var t = commas();
                    expectNewline();
                    expect(']');
                    return listRest(r, t);
                  }
                  r.add(commas());
                  expectNewline();
                } while (!eat(DEDENT));
              }
              case ']' -> {}
              default -> {
                do {
                  if (eat('@')) {
                    var t = expr();
                    expect(']');
                    return listRest(r, t);
                  }
                  r.add(expr());
                } while (eat(','));
              }
            }
            expect(']');
            return ins(new ListOf(r.toArray()));
          }
          case '(' -> {
            var a = commas();
            expect(')');
            return a;
          }
          case WORD -> {
            return switch (s) {
              case "bool?" -> ins(new InstanceOf(arg(), "java/lang/Boolean"));
              case "int?" -> ins(new InstanceOf(arg(), "java/math/BigInteger"));
              case "float?" -> ins(new InstanceOf(arg(), "java/lang/Float"));
              case "double?" -> ins(new InstanceOf(arg(), "java/lang/Double"));
              case "rat?" -> ins(new InstanceOf(arg(), "aklo/BigRational"));
              case "list?" -> ins(new InstanceOf(arg(), "java/util/List"));
              case "sym?" -> ins(new InstanceOf(arg(), "aklo/Sym"));
              case "slice" -> {
                var t = arg1();
                expect(',');
                yield ins(new Slice(t, expr(), argN()));
              }
              case "parserat" -> ins(
                  new Invoke(
                      INVOKESTATIC,
                      "aklo/Etc",
                      "parseRat",
                      "(Ljava/lang/Object;)Laklo/BigRational;",
                      arg()));
              case "parsefloat" -> ins(
                  new Invoke(
                      INVOKESTATIC,
                      "aklo/Etc",
                      "parseFloat",
                      "(Ljava/lang/Object;)Ljava/lang/Float;",
                      arg()));
              case "parsedouble" -> ins(
                  new Invoke(
                      INVOKESTATIC,
                      "aklo/Etc",
                      "parseDouble",
                      "(Ljava/lang/Object;)Ljava/lang/Double;",
                      arg()));
              case "parseint" -> {
                var t = arg1();
                if (eat(')'))
                  yield ins(
                      new Invoke(
                          INVOKESTATIC,
                          "aklo/Etc",
                          "parseInt",
                          "(Ljava/lang/Object;)Ljava/math/BigInteger;",
                          t));
                yield ins(
                    new Invoke(
                        INVOKESTATIC,
                        "aklo/Etc",
                        "parseInt",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/math/BigInteger;",
                        t,
                        argN()));
              }
              case "bitnot" -> ins(new Not(arg()));
              case "len" -> ins(new Len(arg()));
              case "intern" -> ins(
                  new Invoke(
                      INVOKESTATIC, "aklo/Etc", "intern", "(Ljava/lang/Object;)Laklo/Sym;", arg()));
              case "str" -> ins(
                  new Invoke(
                      INVOKESTATIC,
                      "aklo/Etc",
                      "str",
                      "(Ljava/lang/Object;)Ljava/util/List;",
                      arg()));
              case "cmp" -> ins(new Cmp(arg1(), argN()));
              case "bitand" -> ins(new And(arg1(), argN()));
              case "bitor" -> ins(new Or(arg1(), argN()));
              case "bitxor" -> ins(new Xor(arg1(), argN()));
              case "shl" -> ins(new Shl(arg1(), argN()));
              case "shr" -> ins(new Shr(arg1(), argN()));
              case "true" -> Boolean.TRUE;
              case "false" -> Boolean.FALSE;
              default -> {
                if (tok == '.') {
                  var sb = new StringBuilder(s);
                  while (eat('.')) sb.append(word());
                  s = sb.toString();
                }
                yield s;
              }
            };
          }
          case NUM -> {
            s = s.replace("_", "");
            if (isDigits(s)) return new BigInteger(s);
            if (s.charAt(0) == '0' && s.length() > 1)
              switch (s.charAt(1)) {
                case 'b', 'B' -> {
                  return new BigInteger(s.substring(2), 2);
                }
                case 'o', 'O' -> {
                  return new BigInteger(s.substring(2), 8);
                }
                case 'x', 'X' -> {
                  var s2 = s.substring(2);
                  if (isHex(s2)) return new BigInteger(s2, 16);
                }
              }
            var i = s.length() - 1;
            return switch (s.charAt(i)) {
              case 'f', 'F' -> Float.parseFloat(s.substring(0, i));
              default -> Double.parseDouble(s);
            };
          }
          case STR -> {
            return Etc.list(b);
          }
          case SYM -> {
            return Sym.intern(new String(b, StandardCharsets.UTF_8));
          }
        }
      } catch (NumberFormatException e) {
        throw err(e.toString());
      }

      throw err("expected expression");
    }

    void assignSubscript(Object[] y, Object x, Block fail, int i) {
      var loc = fail.loc;
      assign(y[i], ins(new Subscript(x, BigInteger.valueOf(i))), fail);
    }

    void assign(Object y, Object x, Block fail) {
      var loc = fail.loc;

      // single assignment
      if (y instanceof String) {
        ins(new Assign(y, x));
        return;
      }

      // multiple assignment
      if (y instanceof ListOf y1) {
        var s = y1.args;
        for (var i = 0; i < s.length; i++) assignSubscript(s, x, fail, i);
        return;
      }

      // multiple assignment with tail
      if (y instanceof Cat y1) {
        // head atoms
        if (!(y1.arg0 instanceof ListOf s0)) throw err(y + ": invalid assignment");
        var s = s0.args;
        for (var i = 0; i < s.length; i++) assignSubscript(s, x, fail, i);

        // rest of the list
        assign(y1.arg1, ins(new Slice(x, BigInteger.valueOf(s.length), ins(new Len(x)))), fail);
        return;
      }

      // Cannot assign to any other compound expression
      if (y instanceof Instruction) throw err(y + ": invalid assignment");

      // names have not yet been resolved to variables
      // so the only way for the left-hand side to be an actual variable at this point
      // would be if it were a complex expression that generates one
      // and this possibility has just been excluded
      assert !(y instanceof Var);

      // assigning to a constant means an error check
      var after = new Block("assignAfter");
      ins(new If(ins(new Eq(y, x)), after, fail));
      add(after);
    }

    Object assign(Object y, Object x) {
      var fail = new Block("assignFail");
      var after = new Block("assignAfter");

      // assign
      assign(y, x, fail);
      ins(new Goto(after));

      // fail
      add(fail);
      ins(new Throw(Etc.encode("assign failed")));

      // after
      add(after);
      return x;
    }

    Var postInc(Object y, Instruction x) {
      lex();
      var old = new Var("old$", fn.vars);
      ins(new Assign(old, y));
      assign(y, x);
      return old;
    }

    Object postfix() {
      var a = primary();
      for (; ; )
        switch (tok) {
          case '[' -> {
            lex();
            a = ins(new Subscript(a, expr()));
            expect(']');
          }
          case '(' -> {
            lex();
            var r = new ArrayList<>(List.of(a));
            switch (tok) {
              case INDENT -> {
                lex();
                do {
                  r.add(commas());
                  expectNewline();
                } while (!eat(DEDENT));
              }
              case ')' -> {}
              default -> {
                do r.add(expr());
                while (eat(','));
              }
            }
            expect(')');
            a = ins(new Call(r.toArray()));
          }
          case INC -> {
            return postInc(a, ins(new Add(a, BigInteger.ONE)));
          }
          case DEC -> {
            return postInc(a, ins(new Sub(a, BigInteger.ONE)));
          }
          default -> {
            return a;
          }
        }
    }

    void param() {
      var line1 = line;
      var name = word();
      local(name, new Var(name, fn.params));
    }

    void params() {
      switch (tok) {
        case INDENT -> {}
        case '(' -> {
          lex();
          if (eat(INDENT))
            do {
              param();
              expectNewline();
            } while (!eat(DEDENT));
          else if (tok != ')') do param(); while (eat(','));
          expect(')');
        }
        case WORD -> {
          do param();
          while (eat(','));
        }
        default -> throw err("expected parameters");
      }
    }

    Var not(Object a) {
      var r = new Var("not$", fn.vars);
      var yes = new Block("notTrue");
      var no = new Block("notFalse");
      var after = new Block("notAfter");

      // condition
      ins(new If(a, yes, no));

      // true
      add(yes);
      ins(new Assign(r, false));
      ins(new Goto(after));

      // false
      add(no);
      ins(new Assign(r, true));
      ins(new Goto(after));

      // after
      add(after);
      return r;
    }

    Object prefix() {
      switch (tok) {
        case '\\' -> {
          lex();
          var f = new Fn("lambda");
          var c = new Context(f);

          // parameters
          c.params();

          // body
          expect('(');
          var r = tok == INDENT ? c.block() : c.commas();
          ins(new Return(r));
          expect(')');
          f.initVars();
          fn.fns.add(f);
          return f;
        }
        case INC -> {
          lex();
          var y = postfix();
          return assign(y, ins(new Add(y, BigInteger.ONE)));
        }
        case DEC -> {
          lex();
          var y = postfix();
          return assign(y, ins(new Sub(y, BigInteger.ONE)));
        }
        case '!' -> {
          lex();
          return not(prefix());
        }
        case '-' -> {
          lex();
          return ins(new Neg(prefix()));
        }
      }
      return postfix();
    }

    // operator precedence parser
    record Op(int prec, int left) {}

    static int prec = 99;
    static final Map<Integer, Op> ops = new HashMap<>();

    static void init(int k, int left) {
      ops.put(k, new Op(prec, left));
    }

    static {
      init(EXP, 0);

      prec--;
      init('*', 1);
      init('/', 1);
      init('%', 1);
      init(DIV_INT, 1);

      prec--;
      init('+', 1);
      init('-', 1);
      init('@', 1);

      prec--;
      // TODO chained comparisons like Python?
      init('<', 1);
      init(LE, 1);
      init('>', 1);
      init(GE, 1);
      init(EQ, 1);
      init(NE, 1);
      init(EQ_NUM, 1);
      init(NE_NUM, 1);

      prec--;
      init('&', 1);

      prec--;
      init('|', 1);
    }

    Object infix(int prec) {
      var a = prefix();
      for (; ; ) {
        var k = tok;
        var op = ops.get(k);
        if (op == null || op.prec < prec) return a;
        lex();
        var b = infix(op.prec + op.left);
        a =
            switch (k) {
              case EXP -> ins(new Exp(a, b));
              case '*' -> ins(new Mul(a, b));
              case '/' -> ins(new Div(a, b));
              case '%' -> ins(new Rem(a, b));
              case DIV_INT -> ins(new DivInt(a, b));
              case '+' -> ins(new Add(a, b));
              case '-' -> ins(new Sub(a, b));
              case '@' -> ins(new Cat(a, b));
              case '<' -> ins(new Lt(a, b));
              case '>' -> ins(new Lt(b, a));
              case LE -> ins(new Le(a, b));
              case GE -> ins(new Le(b, a));
              case EQ -> ins(new Eq(a, b));
              case EQ_NUM -> ins(new EqNum(a, b));
              case NE -> not(ins(new Eq(a, b)));
              case NE_NUM -> not(ins(new EqNum(a, b)));
              case '&' -> {
                var r = new Var("and$", fn.vars);
                var yes = new Block("andTrue");
                var after = new Block("andAfter");

                // condition
                ins(new Assign(r, a));
                ins(new If(r, yes, after));

                // true
                add(yes);
                ins(new Assign(r, b));
                ins(new Goto(after));

                // after
                add(after);
                yield r;
              }
              case '|' -> {
                var r = new Var("or$", fn.vars);
                var no = new Block("orFalse");
                var after = new Block("orAfter");

                // condition
                ins(new Assign(r, a));
                ins(new If(r, after, no));

                // false
                add(no);
                ins(new Assign(r, b));
                ins(new Goto(after));

                // after
                add(after);
                yield r;
              }
              default -> throw new IllegalStateException(Integer.toString(k));
            };
      }
    }

    Object expr() {
      return infix(1);
    }

    Object commas() {
      var a = expr();
      if (tok != ',') return a;
      var r = new ArrayList<>(List.of(a));
      while (eat(',')) {
        if (eat('@')) return listRest(r, expr());
        r.add(expr());
      }
      return ins(new ListOf(r.toArray()));
    }

    // statements
    Object assignment() {
      var y = commas();
      switch (tok) {
        case ASSIGN -> {
          lex();
          return assign(y, assignment());
        }
        case '=' -> {
          lex();
          Instruction.walk(
              y,
              z -> {
                if (z instanceof String name && !locals.containsKey(name))
                  locals.put(name, new Var(name, fn.vars));
              });
          return assign(y, assignment());
        }
        case ADD_ASSIGN -> {
          lex();
          return assign(y, ins(new Add(y, assignment())));
        }
        case SUB_ASSIGN -> {
          lex();
          return assign(y, ins(new Sub(y, assignment())));
        }
        case CAT_ASSIGN -> {
          lex();
          return assign(y, ins(new Cat(y, assignment())));
        }
        case APPEND -> {
          lex();
          return assign(y, ins(new Cat(y, ins(new ListOf(assignment())))));
        }
      }
      return y;
    }

    Object block() {
      expectIndent();
      Object r;
      do r = stmt();
      while (!eat(DEDENT));
      return r;
    }

    void xwhile(String label, boolean doWhile) {
      lex();
      var body = new Block("whileBody");
      var cond = new Block("whileCond");
      var after = new Block("whileAfter");
      var c = new Context(this, label, cond, after);

      // before
      ins(new Goto(doWhile ? body : cond));

      // condition
      add(cond);
      ins(new If(c.expr(), body, after));

      // body
      add(body);
      c.block();
      ins(new Goto(cond));

      // after
      add(after);
    }

    Var xif() {
      assert tok == WORD && (tokString.equals("if") || tokString.equals("elif"));
      lex();
      var r = new Var("if$", fn.vars);
      var yes = new Block("ifTrue");
      var no = new Block("ifFalse");
      var after = new Block("ifAfter");

      // condition
      ins(new If(expr(), yes, no));

      // true
      add(yes);
      ins(new Assign(r, block()));
      ins(new Goto(after));

      // false
      add(no);
      ins(
          new Assign(
              r,
              switch (currentWord()) {
                case "else" -> {
                  lex();
                  yield block();
                }
                case "elif" -> ins(new Assign(r, xif()));
                default -> BigInteger.ZERO;
              }));
      ins(new Goto(after));

      // after
      add(after);
      return r;
    }

    void checkSubscript(Object[] y, Object x, Block fail, int i) {
      check(y[i], ins(new Subscript(x, BigInteger.valueOf(i))), fail);
    }

    void check(Object y, Object x, Block fail) {
      var loc = fail.loc;

      // single assignment
      if (y instanceof String) return;

      // multiple assignment
      if (y instanceof ListOf y1) {
        var s = y1.args;
        for (var i = 0; i < s.length; i++) checkSubscript(s, x, fail, i);
        return;
      }

      // multiple assignment with tail
      if (y instanceof Cat y1) {
        // head atoms
        if (!(y1.arg0 instanceof ListOf s0)) throw err(y + ": invalid assignment");
        var s = s0.args;
        for (var i = 0; i < s.length; i++) checkSubscript(s, x, fail, i);

        // rest of the list
        check(y1.arg1, ins(new Slice(x, BigInteger.valueOf(s.length), ins(new Len(x)))), fail);
        return;
      }

      // Cannot assign to any other compound expression
      if (y instanceof Instruction) throw err(y + ": invalid assignment");

      // names have not yet been resolved to variables
      // so the only way for the left-hand side to be an actual variable at this point
      // would be if it were a complex expression that generates one
      // and this possibility has just been excluded
      assert !(y instanceof Var);

      // assigning to a constant means an error check
      // TODO factor out
      var after = new Block("checkAfter");
      ins(new If(ins(new Eq(y, x)), after, fail));
      add(after);
    }

    Object xcase(String label) {
      lex();
      var r = new Var("case$", fn.vars);
      var after = new Block("caseAfter");
      var c = new Context(this, label, continueTarget, after);

      // value
      lex();
      var x = commas();

      // default result
      ins(new Assign(r, BigInteger.ZERO));

      // alternatives
      expectIndent();
      do {
        var yes = new Block("caseYes");
        var no = new Block("caseNo");
        do {
          commas();
        } while (eat('\n'));
        add(yes);
        ins(new Assign(r, block()));
        ins(new Goto(after));
        add(no);
      } while (!eat(DEDENT));

      // after
      add(after);
      return r;
    }

    Object stmt() {
      ins(new Line(line));
      var loc = new Loc(file, line);
      switch (tok) {
        case '^' -> {
          // TODO change to return?
          lex();
          var a = tok == '\n' ? BigInteger.ZERO : commas();
          expectNewline();
          ins(new Return(a));
          return BigInteger.ZERO;
        }
        case WORD -> {
          switch (tokString) {
            case "assert" -> {
              lex();
              var no = new Block("ifFalse");
              var after = new Block("ifAfter");

              // condition
              ins(new If(expr(), after, no));
              expectNewline();

              // false
              add(no);
              ins(
                  new Throw(
                      Etc.encode(
                          String.format(
                              "%s:%d: %s: assert failed", loc.file(), loc.line(), fn.name))));

              // after
              add(after);
              return BigInteger.ZERO;
            }
            case "fn" -> {
              lex();
              var name = word();
              var f = new Fn(name);
              local(name, f);
              var c = new Context(f);
              c.params();
              c.ins(new Return(c.block()));
              f.initVars();
              fn.fns.add(f);
              return f;
            }
            case "if" -> {
              return xif();
            }
            case "case" -> {
              return xcase(null);
            }
            case "for" -> {
              lex();
              var x = commas();
              expect(':');
              // TODO
            }
            case "while" -> {
              xwhile(null, false);
              return BigInteger.ZERO;
            }
            case "dowhile" -> {
              xwhile(null, true);
              return BigInteger.ZERO;
            }
            case "break" -> {
              lex();
              Block target;
              if (tok == WORD) {
                var label = word();
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw err(label + ": not found");
                  if (label.equals(c.label)) {
                    target = c.breakTarget;
                    assert target != null;
                    break;
                  }
                }
              } else
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw err("break without loop or case");
                  target = c.breakTarget;
                  if (target != null) break;
                }
              expectNewline();
              ins(new Goto(target));
              add(new Block("breakAfter"));
              return BigInteger.ZERO;
            }
            case "continue" -> {
              lex();
              Block target;
              if (tok == WORD) {
                var label = word();
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw err(label + ": not found");
                  if (label.equals(c.label)) {
                    target = c.continueTarget;
                    if (target == null) throw err(label + ": not a loop");
                    break;
                  }
                }
              } else
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw err("continue without loop");
                  target = c.continueTarget;
                  if (target != null) break;
                }
              expectNewline();
              ins(new Goto(target));
              add(new Block("continueAfter"));
              return BigInteger.ZERO;
            }
            case "exit" -> {
              lex();
              var a = tok == '\n' ? BigInteger.ZERO : expr();
              expectNewline();
              // exit should ideally be a terminating instruction
              ins(new Invoke(INVOKESTATIC, "aklo/Etc", "exit", "(Ljava/lang/Object;)V", a));
              return BigInteger.ZERO;
            }
            case "printn" -> {
              lex();
              var a = commas();
              expectNewline();
              ins(new Invoke(INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a));
              return BigInteger.ZERO;
            }
            case "throw" -> {
              lex();
              var a = commas();
              expectNewline();
              ins(new Throw(a));
              add(new Block("throwAfter"));
              return BigInteger.ZERO;
            }
            case "print" -> {
              lex();
              Object a = BigInteger.TEN;
              if (tok != '\n') a = ins(new Cat(commas(), a));
              expectNewline();
              ins(new Invoke(INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a));
              return BigInteger.ZERO;
            }
            default -> {
              // labeling a statement needs a second token of lookahead
              // It's the only thing in the entire language that does
              // so instead of providing a general lookahead facility
              // it is simpler to provide a limited one that just works for this
              var i = lookahead();
              switch (text[i]) {
                case ':' -> {
                  if (text[i + 1] == '=') break;
                  var label = tokString;
                  lex();
                  assert tok == ':';
                  lex();
                  switch (currentWord()) {
                      // TODO for
                    case "case" -> {
                      return xcase(label);
                    }
                    case "dowhile" -> {
                      xwhile(label, true);
                      return BigInteger.ZERO;
                    }
                    case "while" -> {
                      xwhile(label, false);
                      return BigInteger.ZERO;
                    }
                  }
                  throw err("expected statement after label");
                }
                case '\n' -> throw err("expected statement");
              }
            }
          }
        }
      }
      var b = assignment();
      expectNewline();
      return b;
    }
  }

  // top level
  Parser(String file, byte[] text, Fn module) {
    // init
    this.file = file;
    if (!(text.length > 0 && text[text.length - 1] == '\n')) {
      var r = new byte[text.length + 1];
      System.arraycopy(text, 0, r, 0, text.length);
      r[text.length] = '\n';
      text = r;
    }
    this.text = text;
    lex();
    eat('\n');

    // parse
    var c = new Context(module);
    while (tok != DEDENT) c.stmt();
    c.ins(new ReturnVoid());
    module.initVars();
  }
}
