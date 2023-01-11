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
    // TODO
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
              tok = NE_NUM;
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
              tok = EQ_NUM;
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
            tok = DIV_INT;
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
          if (c == '=') {
            readc();
            tok = GE;
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
              if (c == '_') readc();

              // integer
              digits(sb);
            }
            case 'x', 'X' -> {
              readc(sb);
              if (c == '_') readc();

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

  private String currentWord() throws IOException {
    if (tok == WORD) return tokString;
    return "";
  }

  private String word() throws IOException {
    var s = tokString;
    if (!eat(WORD)) throw new CompileError(file, line, "expected word");
    return s;
  }

  private final class Context {
    final Context outer;
    final Fn fn;
    final String label;
    final Block continueTarget;
    final Block breakTarget;

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

    Var mkVar(Loc loc) {
      var x = new Var(loc);
      fn.vars.add(x);
      return x;
    }

    // TODO rename?
    void addBlock(Block block) {
      fn.blocks.add(block);
    }

    Block lastBlock() {
      return fn.blocks.get(fn.blocks.size() - 1);
    }

    Term insn(Term a) {
      // TODO block should be an instruction constructor parameter
      lastBlock().insns.add(a);
      return a;
    }

    // expressions
    Term arg() throws IOException {
      expect('(');
      var a = expr();
      expect(')');
      return a;
    }

    Term arg1() throws IOException {
      expect('(');
      return expr();
    }

    Term argN() throws IOException {
      expect(',');
      var a = expr();
      expect(')');
      return a;
    }

    Term listRest(Loc loc, List<Term> s, Term t) {
      return insn(new Cat(loc, insn(new ListOf(loc, s)), t));
    }

    Term primary() throws IOException {
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
                    var t = commas();
                    expectNewline();
                    expect(']');
                    return listRest(loc, r, t);
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
                    return listRest(loc, r, t);
                  }
                  r.add(expr());
                } while (eat(','));
              }
            }
            expect(']');
            return insn(new ListOf(loc, r));
          }
          case '(' -> {
            var a = commas();
            expect(')');
            return a;
          }
          case WORD -> {
            switch (s) {
              case "bool?" -> {
                return insn(new InstanceOf(loc, arg(), Type.BOOL));
              }
              case "int?" -> {
                return insn(new InstanceOf(loc, arg(), Type.INT));
              }
              case "float?" -> {
                return insn(new InstanceOf(loc, arg(), Type.FLOAT));
              }
              case "double?" -> {
                return insn(new InstanceOf(loc, arg(), Type.DOUBLE));
              }
              case "rat?" -> {
                return insn(new InstanceOf(loc, arg(), Type.RAT));
              }
              case "list?" -> {
                return insn(new InstanceOf(loc, arg(), Type.LIST));
              }
              case "sym?" -> {
                return insn(new InstanceOf(loc, arg(), Type.SYM));
              }
              case "slice" -> {
                var t = arg1();
                expect(',');
                return insn(new Slice(loc, t, expr(), argN()));
              }
              case "parserat" -> {
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "parseRat",
                        "(Ljava/lang/Object;)Laklo/BigRational;",
                        arg()));
              }
              case "parsefloat" -> {
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "parseFloat",
                        "(Ljava/lang/Object;)Ljava/lang/Float;",
                        arg()));
              }
              case "parsedouble" -> {
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "parseDouble",
                        "(Ljava/lang/Object;)Ljava/lang/Double;",
                        arg()));
              }
              case "parseint" -> {
                var t = arg1();
                if (eat(')'))
                  return insn(
                      new Invoke(
                          loc,
                          INVOKESTATIC,
                          "aklo/Etc",
                          "parseInt",
                          "(Ljava/lang/Object;)Ljava/math/BigInteger;",
                          t));
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "parseInt",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/math/BigInteger;",
                        t,
                        argN()));
              }
              case "bitnot" -> {
                return insn(new BitNot(loc, arg()));
              }
              case "len" -> {
                return insn(new Len(loc, arg()));
              }
              case "intern" -> {
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "intern",
                        "(Ljava/lang/Object;)Laklo/Sym;",
                        arg()));
              }
              case "str" -> {
                return insn(
                    new Invoke(
                        loc,
                        INVOKESTATIC,
                        "aklo/Etc",
                        "str",
                        "(Ljava/lang/Object;)Ljava/util/List;",
                        arg()));
              }
              case "cmp" -> {
                return insn(new Cmp(loc, arg1(), argN()));
              }
              case "bitand" -> {
                return insn(new BitAnd(loc, arg1(), argN()));
              }
              case "bitor" -> {
                return insn(new BitOr(loc, arg1(), argN()));
              }
              case "bitxor" -> {
                return insn(new BitXor(loc, arg1(), argN()));
              }
              case "shl" -> {
                return insn(new Shl(loc, arg1(), argN()));
              }
              case "shr" -> {
                return insn(new Shr(loc, arg1(), argN()));
              }
              case "true" -> {
                return insn(new Const(loc, true));
              }
              case "false" -> {
                return insn(new Const(loc, false));
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

    void assignSubscript(Term y, Term x, Block fail, int i) {
      y = y.get(i);
      var loc = fail.loc;
      x = new Subscript(loc, x, new Const(x.loc, BigInteger.valueOf(i)));
      insn(x);
      assign(y, x, fail);
    }

    void assign(Term y, Term x, Block fail) {
      var loc = fail.loc;
      switch (y.tag()) {
        case CONST -> {
          var eq = new Eq(loc, y, x);
          insn(eq);
          var after = new Block(loc, "assignCheckAfter");
          insn(new If(loc, eq, after, fail));
          addBlock(after);
        }
          // TODO Var is impossible here?
        case ID, VAR -> insn(new Assign(loc, y, x));
        case LIST_OF -> {
          var n = y.size();
          for (var i = 0; i < n; i++) assignSubscript(y, x, fail, i);
        }
        case CAT -> {
          // head atoms
          if (!(y.get(0) instanceof ListOf s))
            throw new CompileError(loc, y + ": invalid assignment");
          var n = s.size();
          for (var i = 0; i < n; i++) assignSubscript(s, x, fail, i);

          // rest of the list
          var len = new Len(loc, x);
          insn(len);
          var slice = new Slice(loc, x, new Const(loc, BigInteger.valueOf(n)), len);
          insn(slice);
          assign(y.get(1), slice, fail);
        }
        default -> throw new CompileError(loc, y + ": invalid assignment");
      }
    }

    Term assign(Loc loc, Term y, Term x) {
      // assign
      var fail = new Block(loc, "assignFail");
      assign(y, x, fail);
      var after = new Block(loc, "assignAfter");
      insn(new Goto(loc, after));

      // fail
      addBlock(fail);
      insn(new Throw(loc, new Const(loc, Etc.encode("assign failed"))));

      // after
      addBlock(after);
      return x;
    }

    Term postInc(Term y, Term x) throws IOException {
      var loc = x.loc;
      lex();
      var old = mkVar(loc);
      insn(new Assign(loc, old, y));
      assign(loc, y, x);
      return old;
    }

    Term postfix() throws IOException {
      var a = primary();
      for (; ; )
        switch (tok) {
          case '[' -> {
            var loc = new Loc(file, line);
            lex();
            a = insn(new Subscript(loc, a, expr()));
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
            a = insn(new Call(loc, r));
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
            return postInc(a, new Add(loc, a, Const.ONE));
          }
          case DEC -> {
            var loc = new Loc(file, line);
            return postInc(a, new Sub(loc, a, Const.ONE));
          }
          default -> {
            return a;
          }
        }
    }

    void param() throws IOException {
      var loc = new Loc(file, line);
      var name = word();
      // O(N^2) is fast when N is sufficiently small
      for (var x : fn.params)
        if (x.name.equals(name)) throw new CompileError(loc, name + ": duplicate parameter name");
      fn.params.add(new Var(loc, name));
    }

    void params() throws IOException {
      // TODO optional ()?
      expect('(');
      if (eat(INDENT))
        do {
          param();
          expectNewline();
        } while (!eat(DEDENT));
      else if (tok != ')') do param(); while (eat(','));
      expect(')');
    }

    Term not(Loc loc, Term a) {
      var r = mkVar(loc);
      var yes = new Block(loc, "notTrue");
      var no = new Block(loc, "notFalse");
      var after = new Block(loc, "notAfter");

      // condition
      insn(new If(loc, a, yes, no));

      // true
      addBlock(yes);
      insn(new Assign(loc, r, new Const(loc, false)));
      insn(new Goto(loc, after));

      // false
      addBlock(no);
      insn(new Assign(loc, r, new Const(loc, true)));
      insn(new Goto(loc, after));

      // after
      addBlock(after);
      return r;
    }

    Term prefix() throws IOException {
      switch (tok) {
        case '\\' -> {
          var loc = new Loc(file, line);
          lex();
          var f = new Fn(loc, "lambda");
          var c = new Context(f);

          // parameters
          c.params();

          // body
          expect('(');
          var r = tok == INDENT ? c.block() : c.commas();
          loc = new Loc(file, line);
          insn(new Return(loc, r));
          expect(')');
          return f;
        }
        case INC -> {
          var loc = new Loc(file, line);
          lex();
          var y = postfix();
          return assign(loc, y, insn(new Add(loc, y, Const.ONE)));
        }
        case DEC -> {
          var loc = new Loc(file, line);
          lex();
          var y = postfix();
          return assign(loc, y, insn(new Sub(loc, y, Const.ONE)));
        }
        case '!' -> {
          var loc = new Loc(file, line);
          lex();
          return not(loc, prefix());
        }
        case '-' -> {
          var loc = new Loc(file, line);
          lex();
          return insn(new Neg(loc, prefix()));
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

    Term infix(int prec) throws IOException {
      var a = prefix();
      for (; ; ) {
        var k = tok;
        var op = ops.get(k);
        if (op == null || op.prec < prec) return a;
        var loc = new Loc(file, line);
        lex();
        var b = infix(op.prec + op.left);
        a =
            switch (k) {
              case EXP -> insn(new Exp(loc, a, b));
              case '*' -> insn(new Mul(loc, a, b));
              case '/' -> insn(new Div(loc, a, b));
              case '%' -> insn(new Rem(loc, a, b));
              case DIV_INT -> insn(new DivInt(loc, a, b));
              case '+' -> insn(new Add(loc, a, b));
              case '-' -> insn(new Sub(loc, a, b));
              case '@' -> insn(new Cat(loc, a, b));
              case '<' -> insn(new Lt(loc, a, b));
              case '>' -> insn(new Lt(loc, b, a));
              case LE -> insn(new Le(loc, a, b));
              case GE -> insn(new Le(loc, b, a));
              case EQ -> insn(new Eq(loc, a, b));
              case EQ_NUM -> insn(new EqNum(loc, a, b));
              case NE -> not(loc, insn(new Eq(loc, a, b)));
              case NE_NUM -> not(loc, insn(new EqNum(loc, a, b)));
              case '&' -> {
                var r = mkVar(a.loc);
                var yes = new Block(a.loc, "andTrue");
                var after = new Block(a.loc, "andAfter");

                // condition
                insn(new Assign(a.loc, r, a));
                insn(new If(a.loc, r, yes, after));

                // true
                addBlock(yes);
                insn(new Assign(a.loc, r, b));
                insn(new Goto(a.loc, after));

                // after
                addBlock(after);
                yield r;
              }
              case '|' -> {
                var r = mkVar(a.loc);
                var no = new Block(a.loc, "orFalse");
                var after = new Block(a.loc, "orAfter");

                // condition
                insn(new Assign(a.loc, r, a));
                insn(new If(a.loc, r, after, no));

                // false
                addBlock(no);
                insn(new Assign(a.loc, r, b));
                insn(new Goto(a.loc, after));

                // after
                addBlock(after);
                yield r;
              }
              default -> throw new IllegalStateException(Integer.toString(k));
            };
      }
    }

    Term expr() throws IOException {
      return infix(1);
    }

    Term commas() throws IOException {
      var a = expr();
      if (tok != ',') return a;
      var loc = new Loc(file, line);
      var r = new ArrayList<>(List.of(a));
      while (eat(',')) {
        if (eat('@')) return listRest(loc, r, expr());
        r.add(expr());
      }
      return insn(new ListOf(loc, r));
    }

    // statements
    Term assignment() throws IOException {
      var y = commas();
      switch (tok) {
        case ASSIGN -> {
          var loc = new Loc(file, line);
          lex();
          return assign(loc, y, assignment());
        }
        case '=' -> {
          var loc = new Loc(file, line);
          lex();
          y.walk(
              z -> {
                if (z instanceof Id z1) {
                  var name = z1.name;
                  for (var a : fn.vars) if (name.equals(a.name)) return;
                  fn.vars.add(new Var(z.loc, name));
                }
              });
          return assign(loc, y, assignment());
        }
        case ADD_ASSIGN -> {
          var loc = new Loc(file, line);
          lex();
          return assign(loc, y, insn(new Add(loc, y, assignment())));
        }
        case SUB_ASSIGN -> {
          var loc = new Loc(file, line);
          lex();
          return assign(loc, y, insn(new Sub(loc, y, assignment())));
        }
        case CAT_ASSIGN -> {
          var loc = new Loc(file, line);
          if (!(y instanceof Id)) throw new CompileError(loc, "@=: expected identifier on left");
          lex();
          return assign(loc, y, insn(new Cat(loc, y, assignment())));
        }
        case APPEND -> {
          var loc = new Loc(file, line);
          if (!(y instanceof Id)) throw new CompileError(loc, "<<: expected identifier on left");
          lex();
          return assign(
              loc, y, insn(new Cat(loc, y, insn(new ListOf(loc, List.of(assignment()))))));
        }
      }
      return y;
    }

    Term block() throws IOException {
      expectIndent();
      Term r;
      do r = stmt();
      while (!eat(DEDENT));
      return r;
    }

    Term xif() throws IOException {
      assert tok == WORD && (tokString.equals("if") || tokString.equals("elif"));
      var loc = new Loc(file, line);
      lex();
      var r = mkVar(loc);
      var yes = new Block(loc, "ifTrue");
      var no = new Block(loc, "ifFalse");
      var after = new Block(loc, "ifAfter");

      // condition
      insn(new If(loc, expr(), yes, no));

      // true
      addBlock(yes);
      insn(new Assign(loc, r, block()));
      insn(new Goto(loc, after));

      // false
      addBlock(no);
      insn(
          new Assign(
              loc,
              r,
              switch (currentWord()) {
                case "else" -> {
                  lex();
                  yield block();
                }
                case "elif" -> insn(new Assign(loc, r, xif()));
                default -> Const.ZERO;
              }));
      insn(new Goto(loc, after));

      // after
      addBlock(after);
      return r;
    }

    void xwhile(String label, boolean doWhile) throws IOException {
      var loc = new Loc(file, line);
      lex();
      var body = new Block(loc, "whileBody");
      var cond = new Block(loc, "whileCond");
      var after = new Block(loc, "whileAfter");
      var c = new Context(this, label, cond, after);

      // before
      insn(new Goto(loc, doWhile ? body : cond));

      // condition
      addBlock(cond);
      insn(new If(loc, c.expr(), body, after));

      // body
      addBlock(body);
      c.block();
      insn(new Goto(loc, cond));

      // after
      addBlock(after);
    }

    Term stmt() throws IOException {
      var loc = new Loc(file, line);
      switch (tok) {
        case '^' -> {
          // TODO change to return?
          lex();
          var a = tok == '\n' ? Const.ZERO : commas();
          expectNewline();
          insn(new Return(loc, a));
          return Const.ZERO;
        }
        case WORD -> {
          switch (tokString) {
            case "assert" -> {
              lex();
              var cond = expr();
              expectNewline();
              var no = new Block(loc, "ifFalse");
              var after = new Block(loc, "ifAfter");

              // condition
              insn(new If(loc, cond, after, no));

              // false
              addBlock(no);
              insn(
                  new Throw(
                      loc,
                      new Const(
                          loc,
                          Etc.encode(
                              String.format(
                                  "%s:%d: %s: %s",
                                  loc.file(), loc.line(), fn.name, cond.toString())))));

              // after
              addBlock(after);
              return new Const(loc, BigInteger.ZERO);
            }
            case "fn" -> {
              lex();
              var f = new Fn(loc, word());
              var c = new Context(f);
              c.params();
              c.insn(new Return(loc, c.block()));
              return f;
            }
            case "if" -> {
              return xif();
            }
            case "case" -> {
              lex();
              var r = new ArrayList<>(List.of(commas()));
              expectIndent();
              do {
                var s = new ArrayList<Term>();
                do s.add(commas());
                while (eat('\n'));
                s.add(block());
                r.add(new Case(s));
              } while (!eat(DEDENT));
              return new Case(r);
            }
            case "for" -> {
              lex();
              var x = commas();
              expect(':');
              // TODO
            }
            case "while" -> {
              xwhile(null, false);
              return Const.ZERO;
            }
            case "dowhile" -> {
              xwhile(null, true);
              return Const.ZERO;
            }
            case "break" -> {
              lex();
              Block target;
              if (tok == WORD) {
                var label = word();
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw new CompileError(loc, label + ": not found");
                  if (label.equals(c.label)) {
                    target = c.breakTarget;
                    assert target != null;
                    break;
                  }
                }
              } else
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw new CompileError(loc, "break without loop or case");
                  target = c.breakTarget;
                  if (target != null) break;
                }
              expectNewline();
              insn(new Goto(loc, target));
              addBlock(new Block(loc, "breakAfter"));
              return Const.ZERO;
            }
            case "continue" -> {
              lex();
              Block target;
              if (tok == WORD) {
                var label = word();
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw new CompileError(loc, label + ": not found");
                  if (label.equals(c.label)) {
                    target = c.continueTarget;
                    if (target == null) throw new CompileError(loc, label + ": not a loop");
                    break;
                  }
                }
              } else
                for (var c = this; ; c = c.outer) {
                  if (c == null) throw new CompileError(loc, "continue without loop");
                  target = c.continueTarget;
                  if (target != null) break;
                }
              expectNewline();
              insn(new Goto(loc, target));
              addBlock(new Block(loc, "continueAfter"));
              return Const.ZERO;
            }
            case "exit" -> {
              lex();
              var a = tok == '\n' ? new Const(loc, BigInteger.ZERO) : expr();
              expectNewline();
              // exit should ideally be a terminating instruction
              insn(new Invoke(loc, INVOKESTATIC, "aklo/Etc", "exit", "(Ljava/lang/Object;)V", a));
              return Const.ZERO;
            }
            case "print" -> {
              // TODO change to printn?
              lex();
              var a = commas();
              expectNewline();
              insn(new Invoke(loc, INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a));
              return Const.ZERO;
            }
            case "throw" -> {
              lex();
              var a = commas();
              expectNewline();
              insn(new Throw(loc, a));
              addBlock(new Block(loc, "throwAfter"));
              return Const.ZERO;
            }
            case "println" -> {
              lex();
              Term a = new Const(loc, BigInteger.TEN);
              if (tok != '\n') a = new Cat(loc, commas(), a);
              expectNewline();
              insn(new Invoke(loc, INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a));
              return Const.ZERO;
            }
          }
        }
      }
      var b = assignment();
      if (b instanceof Id b1) {
        if (!eat(':')) throw new CompileError(loc, "expected statement");
        if (tok == WORD) {
          var label = b1.name;
          switch (tokString) {
              // TODO for, case
            case "dowhile" -> {
              xwhile(label, true);
              return Const.ZERO;
            }
            case "while" -> {
              xwhile(label, false);
              return Const.ZERO;
            }
          }
        }
        throw new CompileError(loc, "expected statement after label");
      }
      expectNewline();
      return b;
    }
  }

  // top level
  public Parser(String file, Reader reader, Module module) throws IOException {
    this.file = file;
    this.reader = reader;
    readc();
    lex();
    eat('\n');

    var c = new Context(module);
    Term r = Const.ZERO;
    while (tok != -1) r = c.stmt();
    c.insn(new Return(module.loc, r));
  }
}
