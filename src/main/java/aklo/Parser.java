package aklo;

import java.io.IOException;
import java.io.InputStream;
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
  private static final int IDIV = -12;
  private static final int INC = -13;
  private static final int INDENT = -14;
  private static final int LE = -15;
  private static final int NE = -16;
  private static final int NUMBER_EQ = -17;
  private static final int NUMBER_NE = -18;
  private static final int PREPEND = -19;
  private static final int STRING = -20;
  private static final int SUB_ASSIGN = -21;
  private static final int SYM = -22;

  // keywords
  private static final int ASSERT = -100;
  private static final int BREAK = -101;
  private static final int CASE = -102;
  private static final int CONTINUE = -102;
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
  private final InputStream stream;
  private int c;
  private int line = 1;

  private int dentc;
  private final List<Integer> cols = new ArrayList<>(List.of(0));
  private int dedents;

  private int tok;
  private String tokString;

  private ParseError err(String s) {
    return new ParseError(String.format("%s:%d: %s", file, line, s));
  }

  // Tokenizer
  private static boolean isIdPart(int c) {
    return Etc.isAlnum(c) || c == '?' || c == '_' || c == '$';
  }

  private void readc() throws IOException {
    c = stream.read();
  }

  private void readc(StringBuilder sb) throws IOException {
    sb.append((char) c);
    readc();
  }

  private void lexQuote() throws IOException {
    var quote = c;
    var sb = new StringBuilder();
    readc();
    while (c != quote) {
      if (c == '\\') readc();
      if (c < ' ') throw err("unclosed quote");
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
              case -1 -> {
                throw err("unmatched '{'");
              }
            }
          } while (c != '}');
          line = line1;
          continue;
        }
        case '\n' -> {
          // next line
          readc();
          if (c < 0) return;
          line++;

          // measure indent
          var col = 0;
          while (c == '\t' || c == ' ') {
            if (c != dentc) {
              if (dentc != 0) throw err("indented with tabs and spaces in same file");
              dentc = c;
            }
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
          if (col != cols.get(cols.size() - 1)) throw err("inconsistent indent");
          return;
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
              tok = NUMBER_NE;
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
              tok = NUMBER_EQ;
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
            tok = IDIV;
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
          while (isIdPart(c));
          tok = ID;
          tokString = sb.toString();
          var k = keywords.get(tokString);
          if (k != null) tok = k;
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

  // expressions

  // top level
  public Parser(String file, InputStream stream) {
    this.file = file;
    this.stream = stream;
  }
}
