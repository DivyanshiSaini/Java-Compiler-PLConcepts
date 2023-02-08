package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.Token;
public class NumLitToken extends Token implements INumLitToken {


    public NumLitToken(int l, int b, int c, int lin, char[] s) {
        super(Kind.NUM_LIT,l,b,c,lin,s);
    }

    @Override
    public int getValue() {
        int num = 0;

        if(len==1){
            String temp = String.valueOf(source[begin]);
            num = Integer.parseInt(temp);
        }
        else{
            String temp = String.valueOf(source);
            num = Integer.parseInt(temp.substring(begin, begin+len));
        }

        return num;
    } //string to num
}
