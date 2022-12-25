package aklo;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private static final int ID = -11;
  private static final int DIV_INTEGERS = -12;
  private static final int INC = -13;
  private static final int INDENT = -14;
  private static final int LE = -15;
  private static final int NE = -16;
  private static final int EQ_NUMBERS = -17;
  private static final int NE_NUMBERS = -18;
  private static final int PREPEND = -19;
  private static final int STRING = -20;
  private static final int SUB_ASSIGN = -21;
  private static final int SYM = -22;
  private static final int INTEGER = -23;
  private static final int FLOAT = -24;
  private static final int DOUBLE = -25;
  private static final int RAW_STRING = -26;

  // keywords
  private static final int ASSERT = -100;
  private static final int BREAK = -101;
  private static final int CASE = -102;
  private static final int CONTINUE = -103;
  private static final int DBG = -104;
  private static final int DOWHILE = -105;
  private static final int ELIF = -106;
  private static final int ELSE = -107;
  private static final int FALSE = -108;
  private static final int FN = -109;
  private static final int FOR = -110;
  private static final int GOTO = -111;
  private static final int IF = -112;
  private static final int THROW = -113;
  private static final int TROFF = -114;
  private static final int TRON = -115;
  private static final int TRUE = -116;
  private static final int VAR = -117;
  private static final int WHILE = -118;

  private static final Map<String, Integer> keywords = new HashMap<>();

  static {
    keywords.put("assert", ASSERT);
    keywords.put("break", BREAK);
    keywords.put("case", CASE);
    keywords.put("continue", CONTINUE);
    keywords.put("dbg", DBG);
    keywords.put("dowhile", DOWHILE);
    keywords.put("elif", ELIF);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("fn", FN);
    keywords.put("for", FOR);
    keywords.put("goto", GOTO);
    keywords.put("if", IF);
    keywords.put("throw", THROW);
    keywords.put("troff", TROFF);
    keywords.put("tron", TRON);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

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
      if (c == '\\') readc();
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
          tok = RAW_STRING;
        }
        case '"' -> {
          lexQuote();
          tok = STRING;
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
          tok = ID;
          tokString = sb.toString();
          var k = keywords.get(tokString);
          if (k != null) tok = k;
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
            switch (sb.charAt(sb.length() - 1)) {
              case 'f', 'F' -> {
                sb.deleteCharAt(sb.length() - 1);
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

  private String id() throws IOException {
    var s = tokString;
    if (!eat(ID)) throw new CompileError(file, line, "expected identifier");
    return s;
  }

  // expressions
  private void exprs(char end, List<Term> r) throws IOException {
    if (eat(INDENT))
      do {
        r.add(commas());
        expectNewline();
      } while (!eat(DEDENT));
    else if (tok != end) do r.add(expr()); while (eat(','));
    expect(end);
  }

  private Term primary() throws IOException {
    var loc = new Loc(file, line);

    // having noted the current line, factor out the moving to the next token
    var k = tok;
    var s = tokString;
    lex();

    try {
      switch (k) {
        case '[' -> {
          var r = new ArrayList<Term>();
          exprs(']', r);
          return new ListOf(loc, r);
        }
        case '(' -> {
          var a = commas();
          expect(')');
          return a;
        }
        case ID -> {
          // TODO factor out
          switch (s) {
            case "bitNot" -> {
              expect('(');
              var a = expr();
              expect(')');
              return new BitNot(loc, a);
            }
            case "intern" -> {
              expect('(');
              var a = expr();
              expect(')');
              return new Intern(loc, a);
            }
            case "bitAnd" -> {
              expect('(');
              var a = expr();
              expect(',');
              var b = expr();
              expect(')');
              return new BitAnd(loc, a, b);
            }
            case "bitOr" -> {
              expect('(');
              var a = expr();
              expect(',');
              var b = expr();
              expect(')');
              return new BitOr(loc, a, b);
            }
            case "bitXor" -> {
              expect('(');
              var a = expr();
              expect(',');
              var b = expr();
              expect(')');
              return new BitXor(loc, a, b);
            }
            case "shl" -> {
              expect('(');
              var a = expr();
              expect(',');
              var b = expr();
              expect(')');
              return new Shl(loc, a, b);
            }
            case "shr" -> {
              expect('(');
              var a = expr();
              expect(',');
              var b = expr();
              expect(')');
              return new Shr(loc, a, b);
            }
          }
          return new Id(loc, s);
        }
        case TRUE -> {
          return new True(loc);
        }
        case FALSE -> {
          return new False(loc);
        }
        case FLOAT -> {
          return new ConstFloat(loc, Float.parseFloat(s));
        }
        case DOUBLE -> {
          return new ConstDouble(loc, Double.parseDouble(s));
        }
        case INTEGER -> {
          if (s.charAt(0) == '0' && s.length() > 1)
            switch (s.charAt(1)) {
              case 'b', 'B' -> {
                return new ConstInteger(loc, new BigInteger(s.substring(2), 2));
              }
              case 'o', 'O' -> {
                return new ConstInteger(loc, new BigInteger(s.substring(2), 8));
              }
              case 'x', 'X' -> {
                return new ConstInteger(loc, new BigInteger(s.substring(2), 16));
              }
            }
          return new ConstInteger(loc, new BigInteger(s));
        }
        case STRING -> {
          return ListOf.encode(loc, Etc.unesc(s));
        }
        case SYM -> {
          return new Intern(loc, ListOf.encode(loc, Etc.unesc(s)));
        }
        case RAW_STRING -> {
          return ListOf.encode(loc, s);
        }
      }
    } catch (NumberFormatException e) {
      throw new CompileError(loc, e.toString());
    }

    throw new CompileError(loc, k + ": expected expression");
  }

  private Term postfix() throws IOException {
    var a = primary();
    for (; ; )
      switch (tok) {
        case '[' -> {
          var loc = new Loc(file, line);
          lex();
          a = new Subscript(loc, a, expr());
          expect(']');
        }
        case '(' -> {
          var loc = new Loc(file, line);
          lex();
          var r = new ArrayList<>(List.of(a));
          exprs(')', r);
          a = new Call(loc, r);
        }
        case '.' -> {
          var loc = new Loc(file, line);
          if (!(a instanceof Id a1)) throw new CompileError(loc, "expected identifier");
          var r = new ArrayList<>(List.of(a1.string));
          while (eat('.')) r.add(id());
          a = new Dot(loc, r);
        }
        case INC -> {
          var loc = new Loc(file, line);
          lex();
          return new PostInc(loc, a, 1);
        }
        case DEC -> {
          var loc = new Loc(file, line);
          lex();
          return new PostInc(loc, a, -1);
        }
        default -> {
          return a;
        }
      }
  }

  private Var param() throws IOException {
    var loc = new Loc(file, line);
    return new Var(loc, id());
  }

  private void params(List<Var> r) throws IOException {
    // TODO optional ()?
    expect('(');
    if (eat(INDENT))
      do {
        r.add(param());
        expectNewline();
      } while (!eat(DEDENT));
    else if (tok != ')') do r.add(param()); while (eat(','));
    expect(')');
  }

  private Term prefix() throws IOException {
    switch (tok) {
      case '\\' -> {
        var loc = new Loc(file, line);
        lex();
        var a = new Fn(loc);

        // parameters
        params(a.params);

        // body
        expect('(');
        if (eat(INDENT)) do a.body.add(stmt()); while (!eat(DEDENT));
        else if (tok != ')') a.body.add(new Return(loc, commas()));
        expect(')');
        return a;
      }
      case INC -> {
        var loc = new Loc(file, line);
        lex();
        return new OpAssign(loc, Tag.ADD, primary(), new ConstInteger(loc, 1));
      }
      case DEC -> {
        var loc = new Loc(file, line);
        lex();
        return new OpAssign(loc, Tag.SUB, primary(), new ConstInteger(loc, 1));
      }
      case '!' -> {
        var loc = new Loc(file, line);
        lex();
        return new Not(loc, prefix());
      }
      case '-' -> {
        var loc = new Loc(file, line);
        lex();
        return new Neg(loc, prefix());
      }
      case '@' -> {
        var loc = new Loc(file, line);
        lex();
        return new Rest(loc, prefix());
      }
    }
    return postfix();
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

  private Term infix(int prec) throws IOException {
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
            case EXP -> new Exp(loc, a, b);
            case '*' -> new Mul(loc, a, b);
            case '/' -> new Div(loc, a, b);
            case '%' -> new Rem(loc, a, b);
            case DIV_INTEGERS -> new DivIntegers(loc, a, b);
            case '+' -> new Add(loc, a, b);
            case '-' -> new Sub(loc, a, b);
            case '@' -> new Cat(loc, a, b);
            case '<' -> new Lt(loc, a, b);
            case '>' -> new Lt(loc, b, a);
            case LE -> new Le(loc, a, b);
            case GE -> new Le(loc, b, a);
            case EQ -> new Eq(loc, a, b);
            case EQ_NUMBERS -> new EqNumbers(loc, a, b);
            case NE -> new Not(loc, new Eq(loc, a, b));
            case NE_NUMBERS -> new Not(loc, new EqNumbers(loc, a, b));
            case '&' -> new And(loc, a, b);
            case '|' -> new Or(loc, a, b);
            default -> throw new IllegalStateException(Integer.toString(k));
          };
    }
  }

  private Term expr() throws IOException {
    return infix(1);
  }

  private Term commas() throws IOException {
    var a = expr();
    if (tok != ',') return a;
    var loc = new Loc(file, line);
    var r = new ArrayList<>(List.of(a));
    while (eat(',')) r.add(expr());
    return new ListOf(loc, r);
  }

  // statements
  private Term opAssignment(Tag op, Term a) throws IOException {
    var loc = new Loc(file, line);
    lex();
    return new OpAssign(loc, op, a, commas());
  }

  private Term assignment() throws IOException {
    var a = commas();
    switch (tok) {
      case ASSIGN -> {
        var loc = new Loc(file, line);
        lex();
        return new Assign(loc, a, commas());
      }
      case '=' -> {
        var loc = new Loc(file, line);
        lex();
        return new Def(loc, a, commas());
      }
      case ADD_ASSIGN -> {
        return opAssignment(Tag.ADD, a);
      }
      case SUB_ASSIGN -> {
        return opAssignment(Tag.SUB, a);
      }
      case CAT_ASSIGN -> {
        return opAssignment(Tag.CAT, a);
      }
      case APPEND -> {
        var loc = new Loc(file, line);
        if (!(a instanceof Id)) throw new CompileError(loc, "<<: expected identifier on left");
        lex();
        var b = commas();
        return new Assign(loc, a, new Cat(loc, a, new ListOf(loc, new Term[] {b})));
      }
      case PREPEND -> {
        var loc = new Loc(file, line);
        lex();
        var b = new Id(loc, id());
        return new Assign(loc, b, new Cat(loc, new ListOf(loc, new Term[] {a}), b));
      }
    }
    return a;
  }

  private void stmts(List<Term> r) throws IOException {
    expectIndent();
    do r.add(stmt());
    while (!eat(DEDENT));
  }

  private IfStmt parseIf() throws IOException {
    assert tok == IF || tok == ELIF;
    var loc = new Loc(file, line);
    lex();
    var r = new ArrayList<>(List.of(expr()));
    stmts(r);
    var then = r.size();
    switch (tok) {
      case ELSE -> {
        lex();
        stmts(r);
      }
      case ELIF -> {
        lex();
        r.add(parseIf());
      }
    }
    return new IfStmt(loc, r, then);
  }

  private Term stmt() throws IOException {
    var loc = new Loc(file, line);
    switch (tok) {
      case ASSERT -> {
        lex();
        var cond = expr();
        expectNewline();
        // TODO throw string
        return new IfStmt(loc, List.of(cond, new Throw(loc, cond)), 1);
      }
      case FN -> {
        lex();
        var a = new Fn(loc);
        a.name = id();
        params(a.params);
        stmts(a.body);
        return a;
      }
      case IF -> {
        return parseIf();
      }
      case VAR -> {
        lex();
        var a = new Var(loc, id());
        if (eat('=')) a.val = commas();
        expectNewline();
        return a;
      }
      case CASE -> {
        lex();
        var r = new ArrayList<>(List.of(commas()));
        expectIndent();
        do {
          var cb = new ArrayList<Term>();
          do cb.add(commas());
          while (eat('\n'));
          var cases = cb.size();
          stmts(cb);
          r.add(new CaseBlock(loc, cb, cases));
        } while (!eat(DEDENT));
        return new Case(loc, r);
      }
      case FOR -> {
        lex();
        var r = new ArrayList<>(List.of(commas()));
        expect(':');
        r.add(commas());
        stmts(r);
        return new For(loc, r);
      }
      case WHILE -> {
        lex();
        var r = new ArrayList<>(List.of(expr()));
        stmts(r);
        return new While(loc, false, r);
      }
      case DOWHILE -> {
        lex();
        var r = new ArrayList<>(List.of(expr()));
        stmts(r);
        return new While(loc, true, r);
      }
      case '^' -> {
        lex();
        var a = tok == '\n' ? new ConstInteger(loc, BigInteger.ZERO) : commas();
        expectNewline();
        return new Return(loc, a);
      }
      case BREAK -> {
        lex();
        var label = tok == ID ? id() : null;
        expectNewline();
        return new ContinueBreak(loc, true, label);
      }
      case CONTINUE -> {
        lex();
        var label = tok == ID ? id() : null;
        expectNewline();
        return new ContinueBreak(loc, false, label);
      }
    }
    var b = assignment();
    if (b instanceof Id b1) {
      if (eat(':'))
        switch (tok) {
            // TODO other statements
          case WHILE, DOWHILE -> {
            var a = (While) stmt();
            a.label = b1.string;
            return a;
          }
          default -> throw new CompileError(loc, "expected loop or case after label");
        }
      throw new CompileError(loc, "expected statement");
    }
    expectNewline();
    return b;
  }

  // top level
  public Parser(String file, Reader reader, List<Term> r) throws IOException {
    this.file = file;
    this.reader = reader;
    readc();
    lex();
    eat('\n');
    while (tok != -1) r.add(stmt());
  }
}
