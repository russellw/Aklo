package aklo;

import static org.objectweb.asm.Opcodes.*;

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
  private final byte[] text;
  private int ti;
  private int line = 1;

  // indentation
  private int dentc;
  private final List<Integer> cols = new ArrayList<>(List.of(0));
  private int dedents;

  // current token
  private int tok;
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
              var d = digit(s.charAt(i));
              if (d >= 8) break;
              i++;
              n = n * 8 + d;
            }
            c = (char) n;
          }
          case 'u' -> {
            var n = 0;
            for (int j = 0; j < 4 && i < s.length(); j++) {
              var d = digit(s.charAt(i));
              if (d >= 16) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'x' -> {
            var n = 0;
            for (int j = 0; j < 2 && i < s.length(); j++) {
              var d = digit(s.charAt(i));
              if (d >= 16) break;
              i++;
              n = n * 16 + d;
            }
            c = (char) n;
          }
          case 'U' -> {
            var n = 0;
            for (int j = 0; j < 8 && i < s.length(); j++) {
              var d = digit(s.charAt(i));
              if (d >= 16) break;
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
  private void readc(StringBuilder sb) {
    sb.append((char) text[ti]);
    ti++;
  }

  private void digits(StringBuilder sb) {
    while (isDigit(text[ti])) {
      readc(sb);
      if (text[ti] == '_') ti++;
    }
  }

  private void lexQuote() {
    var quote = text[ti];
    var sb = new StringBuilder();
    ti++;
    while (text[ti] != quote) {
      if (text[ti] == '\\') readc(sb);
      if (text[ti] < ' ') throw new CompileError(file, line, "unclosed quote");
      readc(sb);
    }
    ti++;
    tokString = sb.toString();
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
          do ti++;
          while (text[ti] != '\n');
          continue;
        }
        case '{' -> {
          var line1 = line;
          do {
            ti++;
            if (ti == text.length) throw new CompileError(file, line, "unmatched '{'");
            if (text[ti] == '\n') line1++;
          } while (text[ti] != '}');
          ti++;
          line = line1;
          continue;
        }
        case '\n' -> {
          // next line
          ti++;
          if (ti == text.length) return;
          line++;

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
          ti++;
          if (text[ti] != '"') throw new CompileError(file, line, "stray '#'");
          lexQuote();
          tok = RAW;
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
          // TODO optimize?
          var sb = new StringBuilder();
          do readc(sb);
          while (isWord(text[ti]));
          tok = WORD;
          tokString = sb.toString().toLowerCase(Locale.ROOT);
          return;
        }
        case '.' -> {
          if (!isDigit(text[ti + 1])) break;

          var sb = new StringBuilder();
          tok = DOUBLE;

          // decimal part
          readc(sb);
          digits(sb);

          // exponent
          switch (text[ti]) {
            case 'e', 'E' -> {
              readc(sb);
              switch (text[ti]) {
                case '+', '-' -> readc(sb);
              }
              digits(sb);
            }
          }

          // suffix
          switch (text[ti]) {
            case 'f', 'F' -> {
              ti++;
              tok = FLOAT;
            }
          }

          tokString = sb.toString();
          return;
        }
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
          var sb = new StringBuilder();

          // leading digits
          digits(sb);
          tok = INTEGER;

          // prefix
          // TODO refactor
          switch (text[ti]) {
            case 'b', 'B', 'o', 'O' -> {
              readc(sb);
              if (text[ti] == '_') ti++;

              // integer
              digits(sb);
            }
            case 'x', 'X' -> {
              readc(sb);
              if (text[ti] == '_') ti++;

              // integer part
              while (digit(text[ti]) < 16) {
                readc(sb);
                if (text[ti] == '_') ti++;
              }

              // decimal part
              if (text[ti] == '.') {
                readc(sb);
                digits(sb);
                tok = DOUBLE;
              }

              // exponent
              switch (text[ti]) {
                case 'p', 'P' -> {
                  readc(sb);
                  switch (text[ti]) {
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
              if (text[ti] == '.') {
                readc(sb);
                digits(sb);

                // now we know we have a floating-point number, though not which precision
                tok = DOUBLE;
              }

              // exponent
              switch (text[ti]) {
                case 'e', 'E' -> {
                  readc(sb);
                  switch (text[ti]) {
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
            switch (text[ti]) {
              case 'f', 'F' -> {
                ti++;
                tok = FLOAT;
              }
            }

          tokString = sb.toString();
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
  private boolean eat(int k) {
    if (tok != k) return false;
    lex();
    return true;
  }

  private void expect(char k) {
    if (!eat(k)) throw new CompileError(file, line, String.format("expected '%c'", k));
  }

  private void expectIndent() {
    if (!eat(INDENT)) throw new CompileError(file, line, "expected indented block");
  }

  private void expectNewline() {
    if (!eat('\n')) throw new CompileError(file, line, "expected newline");
  }

  private String currentWord() {
    if (tok == WORD) return tokString;
    return "";
  }

  private String word() {
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

    void check(int line, String name, Object x) {
      if (locals.put(name, x) != null)
        throw new CompileError(new Loc(file, line), name + " defined twice");
    }

    void add(Block block) {
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
    Term arg() {
      expect('(');
      var a = expr();
      expect(')');
      return a;
    }

    Term arg1() {
      expect('(');
      return expr();
    }

    Term argN() {
      expect(',');
      var a = expr();
      expect(')');
      return a;
    }

    Term listRest(Loc loc, List<Term> s, Term t) {
      return insn(new Cat(loc, insn(new ListOf(loc, s)), t));
    }

    Term primary() {
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
          add(after);
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
      add(fail);
      insn(new Throw(loc, new Const(loc, Etc.encode("assign failed"))));

      // after
      add(after);
      return x;
    }

    Term postInc(Term y, Term x) {
      var loc = x.loc;
      lex();
      var old = new Var(fn.vars);
      insn(new Assign(loc, old, y));
      assign(loc, y, x);
      return old;
    }

    Term postfix() {
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
            // TODO this should probably be primary
            var loc = new Loc(file, line);
            if (!(a instanceof Id a1)) throw new CompileError(loc, "expected identifier");
            var r = new ArrayList<>(List.of(a1.name));
            while (eat('.')) r.add(word());
            a = new Dot(loc, r);
          }
          case INC -> {
            var loc = new Loc(file, line);
            return postInc(a, insn(new Add(loc, a, Const.ONE)));
          }
          case DEC -> {
            var loc = new Loc(file, line);
            return postInc(a, insn(new Sub(loc, a, Const.ONE)));
          }
          default -> {
            return a;
          }
        }
    }

    void param() {
      var line1 = line;
      var name = word();
      check(line1, name, new Var(name, fn.params));
    }

    void params() {
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
      var r = new Var(fn.vars);
      var yes = new Block(loc, "notTrue");
      var no = new Block(loc, "notFalse");
      var after = new Block(loc, "notAfter");

      // condition
      insn(new If(loc, a, yes, no));

      // true
      add(yes);
      insn(new Assign(loc, r, new Const(loc, false)));
      insn(new Goto(loc, after));

      // false
      add(no);
      insn(new Assign(loc, r, new Const(loc, true)));
      insn(new Goto(loc, after));

      // after
      add(after);
      return r;
    }

    Term prefix() {
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
          f.initVars();
          fn.fns.add(f);
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

    Term infix(int prec) {
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
                var r = new Var(fn.vars);
                var yes = new Block(a.loc, "andTrue");
                var after = new Block(a.loc, "andAfter");

                // condition
                insn(new Assign(a.loc, r, a));
                insn(new If(a.loc, r, yes, after));

                // true
                add(yes);
                insn(new Assign(a.loc, r, b));
                insn(new Goto(a.loc, after));

                // after
                add(after);
                yield r;
              }
              case '|' -> {
                var r = new Var(fn.vars);
                var no = new Block(a.loc, "orFalse");
                var after = new Block(a.loc, "orAfter");

                // condition
                insn(new Assign(a.loc, r, a));
                insn(new If(a.loc, r, after, no));

                // false
                add(no);
                insn(new Assign(a.loc, r, b));
                insn(new Goto(a.loc, after));

                // after
                add(after);
                yield r;
              }
              default -> throw new IllegalStateException(Integer.toString(k));
            };
      }
    }

    Term expr() {
      return infix(1);
    }

    Term commas() {
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
    Term assignment() {
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
                  if (!locals.containsKey(name)) locals.put(name, new Var(name, fn.vars));
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

    Term block() {
      expectIndent();
      Term r;
      do r = stmt();
      while (!eat(DEDENT));
      return r;
    }

    Term xif() {
      assert tok == WORD && (tokString.equals("if") || tokString.equals("elif"));
      var loc = new Loc(file, line);
      lex();
      var r = new Var(fn.vars);
      var yes = new Block(loc, "ifTrue");
      var no = new Block(loc, "ifFalse");
      var after = new Block(loc, "ifAfter");

      // condition
      insn(new If(loc, expr(), yes, no));

      // true
      add(yes);
      insn(new Assign(loc, r, block()));
      insn(new Goto(loc, after));

      // false
      add(no);
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
      add(after);
      return r;
    }

    void xwhile(String label, boolean doWhile) {
      var loc = new Loc(file, line);
      lex();
      var body = new Block(loc, "whileBody");
      var cond = new Block(loc, "whileCond");
      var after = new Block(loc, "whileAfter");
      var c = new Context(this, label, cond, after);

      // before
      insn(new Goto(loc, doWhile ? body : cond));

      // condition
      add(cond);
      insn(new If(loc, c.expr(), body, after));

      // body
      add(body);
      c.block();
      insn(new Goto(loc, cond));

      // after
      add(after);
    }

    Term stmt() {
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
              add(no);
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
              add(after);
              return new Const(loc, BigInteger.ZERO);
            }
            case "fn" -> {
              lex();
              var name = word();
              var f = new Fn(loc, name);
              check(loc.line(), name, f);
              var c = new Context(f);
              c.params();
              c.insn(new Return(loc, c.block()));
              f.initVars();
              fn.fns.add(f);
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
              add(new Block(loc, "breakAfter"));
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
              add(new Block(loc, "continueAfter"));
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
            case "printn" -> {
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
              add(new Block(loc, "throwAfter"));
              return Const.ZERO;
            }
            case "print" -> {
              lex();
              Term a = new Const(loc, BigInteger.TEN);
              if (tok != '\n') a = insn(new Cat(loc, commas(), a));
              expectNewline();
              insn(new Invoke(loc, INVOKESTATIC, "aklo/Etc", "print", "(Ljava/lang/Object;)V", a));
              return Const.ZERO;
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
                  throw new CompileError(loc, "expected statement after label");
                }
                case '\n' -> throw new CompileError(loc, "expected statement");
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
  public Parser(String file, byte[] text, Fn module) {
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
    Term r = Const.ZERO;
    while (tok != DEDENT) r = c.stmt();
    c.insn(new Return(module.loc, r));
    module.initVars();
  }
}
