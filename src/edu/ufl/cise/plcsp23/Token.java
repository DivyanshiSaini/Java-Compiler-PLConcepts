package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    // Record to represent the location in the source code
    //public record SourceLocation(int line, int column) {}

    final Kind kind;
    final int begin;
    final int len;
    final int col; //col
    final int line; //line
    final char[] source;


    final String tString = "";


    //constructor
    public Token(Kind k, int b, int l, int c, int lin, char[] s) {
        super();
        this.kind = k;
        this.begin = b;
        this.len = l;
        this.col = c;
        this.line = lin;
        this.source = s;
    }
// scanner pass line and col val

    @Override
    public SourceLocation getSourceLocation() {
        return new SourceLocation(col,line);
    } //returns records line and column

    @Override
    public Kind getKind() {return kind;} //kind of the toke-> variable

    @Override
    public String getTokenString() {
        String temp = String.valueOf(source);
        return temp.substring(begin, len);
    }// return characters in the token


}
