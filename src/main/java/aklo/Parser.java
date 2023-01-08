package aklo;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.*;

public final class Parser {
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
  private static final int DIV_INTEGERS = -12;
  private static final int INC = -13;
  private static final int INDENT = -14;
  private static final int LE = -15;
  private static final int NE = -16;
  private static final int EQ_NUMBERS = -17;
  private static final int NE_NUMBERS = -18;
  private static final int PREPEND = -19;
  private static final int STR = -20;
  private static final int SUB_ASSIGN = -21;
  private static final int SYM = -22;
  private static final int INTEGER = -23;
  private static final int FLOAT = -24;
  private static final int DOUBLE = -25;
  private static final int RAW = -26;

  // File state
  private final String file;
  private final Reader reader;
  private int c;
  // TODO use LineNumberReader?
  private int line = 1;

  private int dentc;
  private final List<Integer> cols = new ArrayList<>(List.of(0));
  private int dedents;

  private int tok;
  private String tokString;

  private static String unesc(String s) {
    var sb = new StringBuilder();
    for (var i = 0; i < s.length(); ) {
      var c = s.charAt(i++);
      if (c == '\\' && i < s.length()) {
        c = s.charAt(i++);
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
            var n = 0;
            for (int j = 0; j < 3 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 8);
              if (d < 0) break;
              i++;
              n = n * 8 + d;
            }
            c = (char) n;
          }
          case 'u' -> {
            var n = 0;
            for (int j = 0; j < 4 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'x' -> {
            var n = 0;
            for (int j = 0; j < 2 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'U' -> {
            var n = 0;
            for (int j = 0; j < 8 && i < s.length(); j++) {
              var d = Character.digit(s.charAt(i), 16);
              if (d < 0) break;
              i++;
              n = n * 16 + d;
            }
            sb.appendCodePoint(n);
            continue;
          }
        }
      }
      sb.append(c);
    }
    return sb.toString();
  }

  // Tokenizer
  private void readc() throws IOException {
    c = reader.read();
  }

  private void readc(StringBuilder sb) throws IOException {
    sb.append((char) c);
    readc();
  }

  private void digits(StringBuilder sb) throws IOException {
    while (Character.isDigit(c)) {
      readc(sb);
      if (c == '_') readc();
    }
  }

  private void lexQuote() throws IOException {
    var quote = c;
    var sb = new StringBuilder();
    readc();
    while (c != quote) {
      if (c == '\\') readc(sb);
      if (c < ' ') throw new CompileError(file, line, "unclosed quote");
      readc(sb);
    }
    readc();
    tokString = sb.toString();
  }

  private void lex() throws IOException {
    // a single newline can produce multiple dedent tokens
    if (dedents > 0) {
      dedents--;
      tok = DEDENT;
      return;
    }
    for (; ; ) {
      // the simplest tokens are just one character
      tok = c;
      switch (c) {
        case ';' -> {
          do readc();
          while (c != '\n' && c >= 0);
          continue;
        }
        case '{' -> {
          var line1 = line;
          do {
            readc();
            switch (c) {
              case '\n' -> line1++;
              case -1 -> throw new CompileError(file, line, "unmatched '{'");
            }
          } while (c != '}');
          readc();
          line = line1;
          continue;
        }
        case '\n' -> {
          // next line
          readc();
          line++;

          // measure indent
          var col = 0;
          while (c == '\t' || c == ' ') {
            if (c != dentc) {
              if (dentc != 0)
                throw new CompileError(file, line, "indented with tabs and spaces in same file");
              dentc = c;
            }
            readc();
            col++;
          }

          // nothing important on this line, keep going
          switch (c) {
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
        }
        case ' ', '\f', '\r', '\t' -> {
          readc();
          continue;
        }
        case '!' -> {
          readc();
          if (c == '=') {
            readc();
            tok = NE;
            if (c == '=') {
              readc();
              tok = NE_NUMBERS;
            }
          }
        }
        case ':' -> {
          readc();
          if (c == '=') {
            readc();
            tok = ASSIGN;
          }
        }
        case '@' -> {
          readc();
          if (c == '=') {
            readc();
            tok = CAT_ASSIGN;
          }
        }
        case '=' -> {
          readc();
          if (c == '=') {
            readc();
            tok = EQ;
            if (c == '=') {
              readc();
              tok = EQ_NUMBERS;
            }
          }
        }
        case '*' -> {
          readc();
          if (c == '*') {
            readc();
            tok = EXP;
          }
        }
        case '/' -> {
          readc();
          if (c == '/') {
            readc();
            tok = DIV_INTEGERS;
          }
        }
        case '+' -> {
          readc();
          switch (c) {
            case '=' -> {
              readc();
              tok = ADD_ASSIGN;
            }
            case '+' -> {
              readc();
              tok = INC;
            }
          }
        }
        case '-' -> {
          readc();
          switch (c) {
            case '=' -> {
              readc();
              tok = SUB_ASSIGN;
            }
            case '-' -> {
              readc();
              tok = DEC;
            }
          }
        }
        case '<' -> {
          readc();
          switch (c) {
            case '=' -> {
              readc();
              tok = LE;
            }
            case '<' -> {
              readc();
              tok = APPEND;
            }
          }
        }
        case '>' -> {
          readc();
          switch (c) {
            case '=' -> {
              readc();
              tok = GE;
            }
            case '>' -> {
              readc();
              tok = PREPEND;
            }
          }
        }
        case '#' -> {
          readc();
          if (c != '"') throw new CompileError(file, line, "stray '#'");
          lexQuote();
          tok = RAW;
        }
        case '"' -> {
          lexQuote();
          tok = STR;
        }
        case '\'' -> {
          lexQuote();
          tok = SYM;
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
            '$',
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
          var sb = new StringBuilder();
          do readc(sb);
          while (Character.isJavaIdentifierPart(c) || c == '?');
          tok = WORD;
          tokString = sb.toString().toLowerCase(Locale.ROOT);
        }
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> {
          var sb = new StringBuilder();

          // leading digits
          digits(sb);
          tok = INTEGER;

          // prefix
          switch (c) {
            case 'b', 'B', 'o', 'O' -> {
              readc(sb);

              // integer
              digits(sb);
            }
            case 'x', 'X' -> {
              readc(sb);

              // integer part
              while (Character.digit(c, 16) >= 0) {
                readc(sb);
                if (c == '_') readc();
              }

              // decimal part
              if (c == '.') {
                readc(sb);
                digits(sb);
                tok = DOUBLE;
              }

              // exponent
              switch (c) {
                case 'p', 'P' -> {
                  readc(sb);
                  switch (c) {
                    case '+', '-' -> readc(sb);
                  }
                  digits(sb);
                  tok = DOUBLE;
                }
              }
            }
            default -> {
              // integer part, if any, is already done
              // decimal part
              if (c == '.') {
                readc(sb);
                digits(sb);

                // a decimal point with no digits before or after, is just a dot
                if (sb.length() == 1) return;

                // now we know we have a floating-point number, though not which precision
                tok = DOUBLE;
              }

              // exponent
              switch (c) {
                case 'e', 'E' -> {
                  readc(sb);
                  switch (c) {
                    case '+', '-' -> readc(sb);
                  }
                  digits(sb);
                  tok = DOUBLE;
                }
              }
            }
          }

          // suffix
          if (tok == DOUBLE)
            switch (c) {
              case 'f', 'F' -> {
                readc();
                tok = FLOAT;
              }
            }

          tokString = sb.toString();
        }
        default -> readc();
      }
      return;
    }
  }

  // parser
  private boolean eat(int k) throws IOException {
    if (tok != k) return false;
    lex();
    return true;
  }

  private void expect(char k) throws IOException {
    if (!eat(k)) throw new CompileError(file, line, String.format("expected '%c'", k));
  }

  private void expectIndent() throws IOException {
    if (!eat(INDENT)) throw new CompileError(file, line, "expected indented block");
  }

  private void expectNewline() throws IOException {
    if (!eat('\n')) throw new CompileError(file, line, "expected newline");
  }

  private String word() throws IOException {
    var s = tokString;
    if (!eat(WORD)) throw new CompileError(file, line, "expected word");
    return s;
  }

  // expressions
  private Term arg(Fn f) throws IOException {
    expect('(');
    var a = expr(f);
    expect(')');
    return a;
  }

  private Term arg1(Fn f) throws IOException {
    expect('(');
    return expr(f);
  }

  private Term argN(Fn f) throws IOException {
    expect(',');
    var a = expr(f);
    expect(')');
    return a;
  }

  private Term primary(Fn f) throws IOException {
    var loc = new Loc(file, line);

    // having noted the current line, factor out the moving to the next token
    var k = tok;
    var s = tokString;
    lex();

    try {
      switch (k) {
        case '[' -> {
          var r = new ArrayList<Term>();
          switch (tok) {
            case INDENT -> {
              lex();
              do {
                if (eat('@')) {
                  r.add(commas(f));
                  expectNewline();
                  expect(']');
                  return new ListRest(loc, r);
                }
                r.add(commas(f));
                expectNewline();
              } while (!eat(DEDENT));
            }
            case ']' -> {}
            default -> {
              do {
                if (eat('@')) {
                  r.add(expr(f));
                  expect(']');
                  return new ListRest(loc, r);
                }
                r.add(expr(f));
              } while (eat(','));
            }
          }
          expect(']');
          return new ListOf(loc, r);
        }
        case '(' -> {
          var a = commas(f);
          expect(')');
          return a;
        }
        case WORD -> {
          switch (s) {
            case "bool?" -> {
              return new InstanceOf(loc, arg(f), Type.BOOL);
            }
            case "int?" -> {
              return new InstanceOf(loc, arg(f), Type.INT);
            }
            case "float?" -> {
              return new InstanceOf(loc, arg(f), Type.FLOAT);
            }
            case "double?" -> {
              return new InstanceOf(loc, arg(f), Type.DOUBLE);
            }
            case "rat?" -> {
              return new InstanceOf(loc, arg(f), Type.RAT);
            }
            case "list?" -> {
              return new InstanceOf(loc, arg(f), Type.LIST);
            }
            case "sym?" -> {
              return new InstanceOf(loc, arg(f), Type.SYM);
            }
            case "slice" -> {
              var t = arg1(f);
              expect(',');
              var i = expr(f);
              return new Slice(loc, t, i, argN(f));
            }
            case "parserat" -> {
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "parseRat",
                  "(Ljava/lang/Object;)Laklo/BigRational;",
                  arg(f));
            }
            case "parsefloat" -> {
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "parseFloat",
                  "(Ljava/lang/Object;)Ljava/lang/Float;",
                  arg(f));
            }
            case "parsedouble" -> {
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "parseDouble",
                  "(Ljava/lang/Object;)Ljava/lang/Double;",
                  arg(f));
            }
            case "parseint" -> {
              var t = arg1(f);
              if (eat(')'))
                return new Invoke(
                    loc,
                    INVOKESTATIC,
                    "aklo/Etc",
                    "parseInt",
                    "(Ljava/lang/Object;)Ljava/math/BigInteger;",
                    t);
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "parseInt",
                  "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/math/BigInteger;",
                  t,
                  argN(f));
            }
            case "bitnot" -> {
              return new BitNot(loc, arg(f));
            }
            case "len" -> {
              return new Len(loc, arg(f));
            }
            case "intern" -> {
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "intern",
                  "(Ljava/lang/Object;)Laklo/Sym;",
                  arg(f));
            }
            case "str" -> {
              return new Invoke(
                  loc,
                  INVOKESTATIC,
                  "aklo/Etc",
                  "str",
                  "(Ljava/lang/Object;)Ljava/util/List;",
                  arg(f));
            }
            case "cmp" -> {
              return new Cmp(loc, arg1(f), argN(f));
            }
            case "bitand" -> {
              return new BitAnd(loc, arg1(f), argN(f));
            }
            case "bitor" -> {
              return new BitOr(loc, arg1(f), argN(f));
            }
            case "bitxor" -> {
              return new BitXor(loc, arg1(f), argN(f));
            }
            case "shl" -> {
              return new Shl(loc, arg1(f), argN(f));
            }
            case "shr" -> {
              return new Shr(loc, arg1(f), argN(f));
            }
            case "true" -> {
              return new Const(loc, true);
            }
            case "false" -> {
              return new Const(loc, false);
            }
          }
          return new Id(loc, s);
        }
        case FLOAT -> {
          return new Const(loc, Float.parseFloat(s));
        }
        case DOUBLE -> {
          return new Const(loc, Double.parseDouble(s));
        }
        case INTEGER -> {
          if (s.charAt(0) == '0' && s.length() > 1)
            switch (s.charAt(1)) {
              case 'b', 'B' -> {
                return new Const(loc, new BigInteger(s.substring(2), 2));
              }
              case 'o', 'O' -> {
                return new Const(loc, new BigInteger(s.substring(2), 8));
              }
              case 'x', 'X' -> {
                return new Const(loc, new BigInteger(s.substring(2), 16));
              }
            }
          return new Const(loc, new BigInteger(s));
        }
        case STR -> {
          return new Const(loc, Etc.encode(unesc(s)));
        }
        case SYM -> {
          return new Const(loc, Sym.intern(unesc(s)));
        }
        case RAW -> {
          return new Const(loc, Etc.encode(s));
        }
      }
    } catch (NumberFormatException e) {
      throw new CompileError(loc, e.toString());
    }

    throw new CompileError(loc, "expected expression");
  }

  private Term postInc(Fn f, Term y, Term x) throws IOException {
    var loc = x.loc;
    lex();
    var old = new Var(loc);
    f.vars.add(old);
    return new Do(loc, List.of(new Assign(loc, old, y), new Assign(loc, y, x), old));
  }

  private Term postfix(Fn f) throws IOException {
    var a = primary(f);
    for (; ; )
      switch (tok) {
        case '[' -> {
          var loc = new Loc(file, line);
          lex();
          a = new Subscript(loc, a, expr(f));
          expect(']');
        }
        case '(' -> {
          var loc = new Loc(file, line);
          lex();
          var r = new ArrayList<>(List.of(a));
          switch (tok) {
            case INDENT -> {
              lex();
              do {
                r.add(commas(f));
                expectNewline();
              } while (!eat(DEDENT));
            }
            case ')' -> {}
            default -> {
              do r.add(expr(f));
              while (eat(','));
            }
          }
          expect(')');
          a = new Call(loc, r);
        }
        case '.' -> {
          var loc = new Loc(file, line);
          if (!(a instanceof Id a1)) throw new CompileError(loc, "expected identifier");
          var r = new ArrayList<>(List.of(a1.name));
          while (eat('.')) r.add(word());
          a = new Dot(loc, r);
        }
        case INC -> {
          var loc = new Loc(file, line);
          return postInc(f, a, new Add(loc, a, new Const(loc, BigInteger.ONE)));
        }
        case DEC -> {
          var loc = new Loc(file, line);
          return postInc(f, a, new Sub(loc, a, new Const(loc, BigInteger.ONE)));
        }
        default -> {
          return a;
        }
      }
  }

  private void param(Fn f) throws IOException {
    var loc = new Loc(file, line);
    var name = word();
    // O(N^2) is fast when N is sufficiently small
    for (var x : f.params)
      if (x.name.equals(name)) throw new CompileError(loc, name + ": duplicate parameter name");
    f.params.add(new Var(loc, name));
  }

  private void params(Fn f) throws IOException {
    // TODO optional ()?
    expect('(');
    if (eat(INDENT))
      do {
        param(f);
        expectNewline();
      } while (!eat(DEDENT));
    else if (tok != ')') do param(f); while (eat(','));
    expect(')');
  }

  private Term prefix(Fn f) throws IOException {
    switch (tok) {
      case '\\' -> {
        var loc = new Loc(file, line);
        lex();
        f = new Fn(loc, "lambda");

        // parameters
        params(f);

        // body
        expect('(');
        f.body = tok == INDENT ? block(f) : commas(f);
        expect(')');
        return f;
      }
      case INC -> {
        var loc = new Loc(file, line);
        lex();
        var y = postfix(f);
        return new Assign(loc, y, new Add(loc, y, new Const(loc, BigInteger.ONE)));
      }
      case DEC -> {
        var loc = new Loc(file, line);
        lex();
        var y = postfix(f);
        return new Assign(loc, y, new Sub(loc, y, new Const(loc, BigInteger.ONE)));
      }
      case '!' -> {
        var loc = new Loc(file, line);
        lex();
        return new Not(loc, prefix(f));
      }
      case '-' -> {
        var loc = new Loc(file, line);
        lex();
        return new Neg(loc, prefix(f));
      }
    }
    return postfix(f);
  }

  // operator precedence parser
  private record Op(int prec, int left) {}

  private static int prec = 99;
  private static final Map<Integer, Op> ops = new HashMap<>();

  private static void init(int k, int left) {
    ops.put(k, new Op(prec, left));
  }

  static {
    init(EXP, 0);

    prec--;
    init('*', 1);
    init('/', 1);
    init('%', 1);
    init(DIV_INTEGERS, 1);

    prec--;
    init('+', 1);
    init('-', 1);
    init('@', 1);

    prec--;
    init('<', 1);
    init(LE, 1);
    init('>', 1);
    init(GE, 1);
    init(EQ, 1);
    init(NE, 1);
    init(EQ_NUMBERS, 1);
    init(NE_NUMBERS, 1);

    prec--;
    init('&', 1);

    prec--;
    init('|', 1);
  }

  private Term infix(Fn f, int prec) throws IOException {
    var a = prefix(f);
    for (; ; ) {
      var k = tok;
      var op = ops.get(k);
      if (op == null || op.prec < prec) return a;
      var loc = new Loc(file, line);
      lex();
      var b = infix(f, op.prec + op.left);
      a =
          switch (k) {
            case EXP -> new Exp(loc, a, b);
            case '*' -> new Mul(loc, a, b);
            case '/' -> new Div(loc, a, b);
            case '%' -> new Rem(loc, a, b);
            case DIV_INTEGERS -> new DivInt(loc, a, b);
            case '+' -> new Add(loc, a, b);
            case '-' -> new Sub(loc, a, b);
            case '@' -> new Cat(loc, a, b);
            case '<' -> new Lt(loc, a, b);
            case '>' -> new Lt(loc, b, a);
            case LE -> new Le(loc, a, b);
            case GE -> new Le(loc, b, a);
            case EQ -> new Eq(loc, a, b);
            case EQ_NUMBERS -> new EqNum(loc, a, b);
            case NE -> new Not(loc, new Eq(loc, a, b));
            case NE_NUMBERS -> new Not(loc, new EqNum(loc, a, b));
            case '&' -> new And(loc, a, b);
            case '|' -> new Or(loc, a, b);
            default -> throw new IllegalStateException(Integer.toString(k));
          };
    }
  }

  private Term expr(Fn f) throws IOException {
    return infix(f, 1);
  }

  private Term commas(Fn f) throws IOException {
    var a = expr(f);
    if (tok != ',') return a;
    var loc = new Loc(file, line);
    var r = new ArrayList<>(List.of(a));
    while (eat(',')) {
      if (eat('@')) {
        r.add(expr(f));
        return new ListRest(loc, r);
      }
      r.add(expr(f));
    }
    return new ListOf(loc, r);
  }

  // statements
  private Term assignment(Fn f) throws IOException {
    var y = commas(f);
    switch (tok) {
      case ASSIGN -> {
        var loc = new Loc(file, line);
        lex();
        return new Assign(loc, y, assignment(f));
      }
      case '=' -> {
        var loc = new Loc(file, line);
        lex();
        y.walk(
            z -> {
              if (z instanceof Id z1) {
                var name = z1.name;
                for (var a : f.vars) if (name.equals(a.name)) return;
                f.vars.add(new Var(z.loc, name));
              }
            });
        return new Assign(loc, y, assignment(f));
      }
      case ADD_ASSIGN -> {
        var loc = new Loc(file, line);
        lex();
        var x = assignment(f);
        return new Assign(loc, y, new Add(loc, y, x));
      }
      case SUB_ASSIGN -> {
        var loc = new Loc(file, line);
        lex();
        var x = assignment(f);
        return new Assign(loc, y, new Sub(loc, y, x));
      }
      case CAT_ASSIGN -> {
        var loc = new Loc(file, line);
        lex();
        var x = assignment(f);
        return new Assign(loc, y, new Cat(loc, y, x));
      }
      case APPEND -> {
        var loc = new Loc(file, line);
        // TODO do we need this restriction?
        if (!(y instanceof Id)) throw new CompileError(loc, "<<: expected identifier on left");
        lex();
        var x = assignment(f);
        return new Assign(loc, y, new Cat(loc, y, new ListOf(loc, new Term[] {x})));
      }
      case PREPEND -> {
        var loc = new Loc(file, line);
        lex();
        var x = new Id(loc, word());
        return new Assign(loc, x, new Cat(loc, new ListOf(loc, new Term[] {y}), x));
      }
    }
    return y;
  }

  private Do block(Fn f) throws IOException {
    var loc = new Loc(file, line);
    expectIndent();
    var r = new ArrayList<Term>();
    do r.add(stmt(f));
    while (!eat(DEDENT));
    return new Do(loc, r);
  }

  private IfStmt parseIf(Fn f) throws IOException {
    assert tok == WORD && (tokString.equals("if") || tokString.equals("elif"));
    lex();
    var cond = expr(f);
    var yes = block(f);
    Term no = null;
    if (tok == WORD)
      switch (tokString) {
        case "else" -> {
          lex();
          no = block(f);
        }
        case "elif" -> {
          lex();
          no = parseIf(f);
        }
      }
    if (no == null) no = new Const(cond.loc, BigInteger.ZERO);
    return new IfStmt(cond, yes, no);
  }

  private Term stmt(Fn f) throws IOException {
    var loc = new Loc(file, line);
    switch (tok) {
      case '^' -> {
        lex();
        var a = tok == '\n' ? new Const(loc, BigInteger.ZERO) : commas(f);
        expectNewline();
        return new Return(loc, a);
      }
      case WORD -> {
        switch (tokString) {
          case "assert" -> {
            lex();
            var cond = expr(f);
            expectNewline();
            var s = String.format("%s:%d: %s: %s", loc.file(), loc.line(), f.name, cond.toString());
            return new IfStmt(
                cond,
                new Const(loc, BigInteger.ZERO),
                new Throw(loc, new Const(loc, Etc.encode(s))));
          }
          case "fn" -> {
            lex();
            f = new Fn(loc, word());
            params(f);
            f.body = block(f);
            return f;
          }
          case "if" -> {
            return parseIf(f);
          }
          case "case" -> {
            lex();
            var r = new ArrayList<>(List.of(commas(f)));
            expectIndent();
            do {
              var s = new ArrayList<Term>();
              do s.add(commas(f));
              while (eat('\n'));
              s.add(block(f));
              r.add(new Case(s));
            } while (!eat(DEDENT));
            return new Case(r);
          }
          case "for" -> {
            lex();
            var x = commas(f);
            expect(':');
            return new For(x, commas(f), block(f));
          }
          case "while" -> {
            lex();
            return new While(false, expr(f), block(f));
          }
          case "dowhile" -> {
            lex();
            return new While(true, expr(f), block(f));
          }
          case "break" -> {
            lex();
            var label = tok == WORD ? word() : null;
            expectNewline();
            return new ContinueBreak(loc, true, label);
          }
          case "continue" -> {
            lex();
            var label = tok == WORD ? word() : null;
            expectNewline();
            return new ContinueBreak(loc, false, label);
          }
          case "exit" -> {
            lex();
            var a = tok == '\n' ? new Const(loc, BigInteger.ZERO) : expr(f);
            expectNewline();
            return new Invoke(loc, INVOKESTATIC, "aklo/Etc", "exit", "(Ljava/lang/Object;)V", a);
          }
          case "print" -> {
            lex();
            var a = commas(f);
            expectNewline();
            return new Invoke(loc, INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a);
          }
          case "throw" -> {
            lex();
            var a = commas(f);
            expectNewline();
            return new Throw(loc, a);
          }
          case "println" -> {
            lex();
            Term a = new Const(loc, BigInteger.TEN);
            if (tok != '\n') a = new Cat(loc, commas(f), a);
            expectNewline();
            return new Invoke(loc, INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a);
          }
        }
      }
    }
    var b = assignment(f);
    if (b instanceof Id b1) {
      if (eat(':')) {
        if (tok == WORD)
          switch (tokString) {
              // TODO other statements
            case "while", "dowhile" -> {
              var a = (While) stmt(f);
              a.label = b1.name;
              return a;
            }
          }
        throw new CompileError(loc, "expected loop after label");
      }
      throw new CompileError(loc, "expected statement");
    }
    expectNewline();
    return b;
  }

  // top level
  public Parser(String file, Reader reader, Module module) throws IOException {
    this.file = file;
    this.reader = reader;
    readc();
    lex();
    eat('\n');

    var loc = new Loc(file, line);
    var r = new ArrayList<Term>();
    do r.add(stmt(module));
    while (tok != -1);
    module.body = new Do(loc, r);
  }
}
