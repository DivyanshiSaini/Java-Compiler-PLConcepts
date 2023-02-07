   package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken {


    public StringLitToken(Kind k, int l, int b, int c, int lin, char[] s) {
        super(k,l,b,c,lin,s);
    }

    @Override
    public String getValue() {
        return null;
    } // removed ", and converts escape sequence to actual value
}
