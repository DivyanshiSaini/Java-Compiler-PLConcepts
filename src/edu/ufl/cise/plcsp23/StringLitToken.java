   package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken {


    public StringLitToken(Kind k, int p, int l, char[] s) {
        super(k, p, l, s);
    }

    @Override
    public String getValue() {
        return null;
    } // removed ", and converts escape senquence to actual value
}
