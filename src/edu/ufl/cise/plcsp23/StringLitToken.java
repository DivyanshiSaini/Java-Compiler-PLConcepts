package edu.ufl.cise.plcsp23;
import java.util.Arrays;
public class StringLitToken extends Token implements IStringLitToken {


    public StringLitToken(Kind k, int l, int b, int c, int lin, char[] s) {
        super(k,l,b,c,lin,s);
    }

    @Override
    public String getValue() {
        char[] arr = Arrays.copyOfRange(source, begin+1, begin+len-1);
        String retStr = String.valueOf(arr);
        retStr = retStr.replace("\\b","\b");
        retStr = retStr.replace("\\t","\t");
        retStr = retStr.replace("\\n","\n");
        retStr = retStr.replace("\\r","\r");
        retStr = retStr.replace("\\\"","\"");
        retStr = retStr.replace("\\\\","\\");
        return retStr;
    } // removed ", and converts escape sequence to actual value
}