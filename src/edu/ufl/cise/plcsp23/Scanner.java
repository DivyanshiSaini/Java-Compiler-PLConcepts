package edu.ufl.cise.plcsp23;

import java.util.Arrays;

import static edu.ufl.cise.plcsp23.IToken.Kind.EOF;
import static edu.ufl.cise.plcsp23.IToken.Kind.NUM_LIT;
import static edu.ufl.cise.plcsp23.IToken.Kind.TIMES;
import static edu.ufl.cise.plcsp23.IToken.Kind.IDENT;
import static edu.ufl.cise.plcsp23.IToken.Kind;
import static java.util.Arrays.copyOf;


public class Scanner implements IScanner {


    final String input;
    //array containing input chars, terminated with extra char 0
    final char[] inputChars;
    //invariant ch == inputChars[pos]
    int pos; //position of ch
    char ch; //next char

    //constructor
    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        pos = 0;
        ch = inputChars[pos];
    }

    void nextChar() {
        //define next char function
        // sets ch
        pos++;
        ch = inputChars[pos];
    }

    boolean isZero(char c) {
        if (c == '0'){
            return true;
        }
        else {
            return false;
        }
    }
    boolean isDigit(char c){
        if(c == '1'|| c=='2' || c=='3' || c=='4'|| c=='5' || c=='6'|| c=='7'|| c=='8' || c=='9'){
            return true;
        }
        else {
            return false;
        }
    }
    boolean isIdentStart(char c) {
        if (Character.isLetter(c)) {
            return true;
        } else{
            return false;
        }
    }
    void error(String s){
        System.out.println(s);
    };

    private enum State {
        START,
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT
    }

    @Override
    public IToken next() throws LexicalException {
        //changes to constructor
        State state = State.START;
        int tokenStart = -1;
        while (true) { //read chars, loop terminates when a Token is returned
            switch (state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        case 0 -> { //end of input
                            return new Token(EOF, tokenStart, 0, inputChars);
                        }
                        case ' ', '\n', '\r', '\t','f' -> nextChar();
                        case '*' -> {
                            nextChar();
                            return new Token(TIMES, tokenStart, 1, inputChars);
                        }
                        case '0' -> {
                            nextChar();
                            return new Token(NUM_LIT, tokenStart, 1, inputChars);
                        }
                        case '=' -> {
                            state = State.HAVE_EQ;
                            nextChar();
                        }
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {//char is nonzero digit
                            state = State.IN_NUM_LIT;
                            nextChar();
                        }
                        default -> {
                            if (isIdentStart(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                            } else error("illegal char with ascii value: " + (int) ch);
                        }
                    }
                }
                case HAVE_EQ -> {
                    if (ch == '=') {
                        state = state.START;
                        nextChar();
                        return new Token(Kind.EQ, tokenStart, 2, inputChars);
                    } else {
                        error("expected =");
                    }
                }
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {//char is digit, continue in IN_NUM_LIT state
                        nextChar();
                    } else {
                        //current char belongs to next token, so don't get next char
                        int length = pos - tokenStart;
                        return new Token(Kind.NUM_LIT, tokenStart, length, inputChars);
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        nextChar();
                    } else {//
                        //current char belongs to next token, so don't get next char
                        int length = pos - tokenStart;
                        //determine if this is a reserved word. If not, it is an ident.
                        String text = input.substring(tokenStart, tokenStart + length);
                        /*Kind kind = reservedWords.get(text);
                        if (kind == null) {
                            kind = IDENT;
                        }
                        return new Token(kind, tokenStart, length, inputChars);*/
                    }
                }
            }
        }
    }
}

