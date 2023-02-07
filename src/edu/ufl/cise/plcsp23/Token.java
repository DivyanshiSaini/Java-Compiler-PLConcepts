package edu.ufl.cise.plcsp23;

public class Token implements IToken{
    // Record to represent the location in the source code
    //public record SourceLocation(int line, int column) {}

    final Kind kind;
    final int pos; //col
    final int line; //line
    final char[] source;

    final String tString = "";


    //constructor
    public Token(Kind k, int p, int l, char[] s) {
        super();
        this.kind = k;
        this.pos = p;
        this.line = l;
        this.source = s;
       // this.tString = tS;
    }
// scanner pass line and col val

    @Override
    public SourceLocation getSourceLocation() {
        return new SourceLocation(pos,line);
    } //returns records  line and column

    @Override
    public Kind getKind() {return kind;} //kind of the toke-> variable

    @Override
    public String getTokenString() {
        String temp = String.valueOf(source);
        return temp.substring(pos, line);
    }// return characters in the token


}
