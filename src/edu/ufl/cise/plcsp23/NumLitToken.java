package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {


    public NumLitToken(Kind k, int p, int l, char[] s) {
        super(k, p, l, s);
    }

    @Override
    public int getValue() {
        return 0;
    } //string to num
}
