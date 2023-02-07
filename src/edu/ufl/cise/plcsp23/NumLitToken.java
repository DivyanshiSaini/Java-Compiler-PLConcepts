package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {


    public NumLitToken(Kind r, int p, int l, char[] s) {
        super(r, p, l, s);
    }

    @Override
    public int getValue() {
        return 0;
    } //string to num
}
