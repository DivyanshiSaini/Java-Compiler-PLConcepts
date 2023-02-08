package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {


    public NumLitToken(int l, int b, int c, int lin, char[] s) {
        super(Kind.NUM_LIT,l,b,c,lin,s);
    }

    @Override
    public int getValue() {
        return 0;
    } //string to num
}
