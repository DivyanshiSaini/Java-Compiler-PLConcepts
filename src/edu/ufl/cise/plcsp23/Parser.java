package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;

import static edu.ufl.cise.plcsp23.IToken.Kind;

import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser {
    @Override
    public AST parse() throws PLCException {
        return expression();
    }

    IToken t;
    IScanner scan;

    Parser(IScanner s) throws LexicalException {
        this.scan = s;
        t = scan.next();
    }


    protected boolean isKind(Kind kind) throws LexicalException {
        return t.getKind() == kind;
    }

    protected boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
            if (k == t.getKind()) {
                return true;
            }
        }
        return false;
    }


    //primitive operations
    private boolean isAtEnd() {
        return peek().getKind() == Kind.EOF;
    }

    private IToken peek() { //IToken //return t
        return t;
    }

    private void advance() throws LexicalException {
        if (!isAtEnd()) t = scan.next();
    }

    private void match(Kind c) throws LexicalException {
        if (t.getKind() == c) {
            advance();
        } else {
            throw new LexicalException("Error");
        }
    }


    //<expr> ::=   <conditional_expr> | <or_expr>
    public Expr expression() throws PLCException {
        Expr e = null;
        if (isKind(Kind.RES_if)) {
            e = conditional();
        } else {
            e = or();
        }
        return e;
    }

    //<conditional_expr>  ::= if <expr> ? <expr> ? <expr>
    public Expr conditional() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.RES_if)) { // isKind
            advance();
            Expr gaurd = expression();
            if (isKind(Kind.QUESTION)) {
                advance();
                Expr trueC = expression();
                if (isKind(Kind.QUESTION)) {
                    advance();
                    Expr falseC = expression();
                    e = new ConditionalExpr(firstToken, gaurd, trueC, falseC);
                } else {
                    throw new SyntaxException("Error");
                }
            } else {
                throw new SyntaxException("Error");
            }
        } else {
            throw new SyntaxException("Error");
        }
        return e;
    }

    //<or_expr> ::=  <and_expr> (  ( | | || ) <and_expr>)*
    public Expr or() throws PLCException { //syntax and lex. in same
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = and();
        while (isKind(Kind.BITOR) || isKind(Kind.OR)) {
            IToken op = t;
            advance();
            right = and();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }

    //<and_expr> ::=  <comparison_expr> ( ( & | && )  <comparison_expr>)*
    public Expr and() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = comparison();
        while (isKind(Kind.BITAND) || isKind(Kind.AND)) {
            IToken op = t;
            advance();
            right = comparison();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }


    //<comparison_expr> ::=   <power_expr> ( (< | > | == | <= | >=) <power_expr>)*
    public Expr comparison() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = power();
        while (isKind(Kind.GT) || isKind(Kind.GE) || isKind(Kind.LT) || isKind(Kind.LE) || isKind(Kind.EQ)) {
            IToken op = t;
            advance();
            right = power();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }

    // <power_expr> ::=    <additive_expr> ** <power_expr> |  <additive_expr>
    // power_expr = additive_expr(**(power_expr) |  ε)
    public Expr power() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = additive();
        if (isKind(Kind.EXP)) {
            IToken op = t;
            advance();
            right = power();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }

    // <additive_expr> ::=  <multiplicative_expr> ( ( + | - ) <multiplicative_expr> )*
    public Expr additive() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = multiplicative();
        while (isKind(Kind.PLUS) || isKind(Kind.MINUS)) {
            IToken op = t;
            advance();
            right = multiplicative();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }

    // <multiplicative_expr> ::= <unary_expr> (( * | / | % ) <unary_expr>)*
    public Expr multiplicative() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = unary();
        while (isKind(Kind.TIMES) || isKind(Kind.DIV) || isKind(Kind.MOD)) {
            IToken op = t;
            advance();
            right = unary();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }

    //<unary_expr> ::= ( ! | - | sin | cos | atan) <unary_expr> |   <primary_expr>
    public Expr unary() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.BANG) || isKind(Kind.MINUS) || isKind(Kind.RES_sin) || isKind(Kind.RES_cos) || isKind(Kind.RES_atan)) {
            IToken op = t;
            advance();
            Expr right = unary();
            e = new UnaryExpr(firstToken, op.getKind(), right);
        } else {
            e = primary();
        }
        return e;
    }

    // <primary_expr> ::= STRING_LIT | NUM_LIT | IDENT | ( <expr> ) | Z | rand
    public Expr primary() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.STRING_LIT)) {
            advance();
            e = new StringLitExpr(firstToken);

        } else if (isKind(Kind.NUM_LIT)) {
            advance();
            e = new NumLitExpr(firstToken);
        } else if (isKind(Kind.IDENT)) {
            advance();
            e = new IdentExpr(firstToken);
        } else if (isKind(Kind.LPAREN)) {
            advance();
            e = expression();
            if (!isKind(Kind.RPAREN)) {
                throw new SyntaxException("Error");
            } else {
                advance();
            }
        } else if (isKind(Kind.RES_Z)) {
            advance();
            e = new ZExpr(firstToken);
        } else if (isKind(Kind.RES_rand)) {
            advance();
            e = new RandomExpr(firstToken);
        } else {
            throw new SyntaxException("Error");
        }
        return e;
    }


    //ChannelSelector ::= : red | : grn | : blu
    public void channelSelector() throws PLCException{
        IToken firstToken = t;
        //Expr e = null;
        if (isKind(Kind.RES_if)) { // isKind
            advance();
            if(isKind(Kind.RES_red)){
                IToken color = t;
                enum ColorChannel(color);
            }
            else if(isKind(Kind.RES_blu)){

            }
            else if(isKind(Kind.RES_grn)){

            }
        }
        return e;
    }

    //PixelSelector  ::= [ Expr , Expr ]
    public Expr pixelSelector() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.LSQUARE)) { // isKind
            advance();
            Expr x = expression();
            if(isKind(Kind.COMMA)){
                advance();
                Expr y = expression();
                if(isKind(Kind.RSQUARE)){
                    advance();
                    e = new PixelSelector(firstToken,x,y);
                } //else error
            } //else error
        } //else error
        return e;
    }

    //ExpandedPixel ::= [ Expr , Expr , Expr ]
    public Expr expandedPixel() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.RES_if)) { // isKind
            advance();
            Expr r = expression();
            if(isKind(Kind.COMMA)){
                advance();
                Expr g = expression();
                if (isKind(Kind.RES_if)) { // isKind
                    advance();
                    Expr b = expression();
                    if(isKind(Kind.RSQUARE)){
                        advance();
                        e = new ExpandedPixelExpr(firstToken,r,g,b);
                    } //else error
                } //else error
            } //else error
        } //else error
        return e;
    }

    //PixelFunctionExpr ::= ( x_cart | y_cart | a_polar | r_polar ) PixelSelector
    public Expr pixelFunctionExpr() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.RES_x_cart) || isKind(Kind.RES_y_cart) || isKind(Kind.RES_a_polar) || isKind(Kind.RES_r_polar)) {
            IToken op = t;
            advance();
            Expr right = pixelSelector();
            e = new UnaryExpr(firstToken, op.getKind(), right);
        }else {
            throw new SyntaxException("Error");
        }
        return e;
    } //should be solid


    //Dimension  ::= [ Expr , Expr ]
    public Expr dimension() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if (isKind(Kind.RES_if)) { // isKind
            advance();
            Expr w = expression();
            if(isKind(Kind.COMMA)){
            advance();
            Expr h = expression();
                if(isKind(Kind.RSQUARE)){
                    advance();
                    e = new Dimension(firstToken,w,h);
                } //else error
            } //else error
        } //else error
        return e;
    }




    //LValue ::= IDENT (PixelSelector | ε ) (ChannelSelector | ε )
    public Expr lValue() throws PLCException{
        IToken firstToken = t;
        Expr id = null;
        Expr e = null;
        Expr pi = null;
        Expr ch = null;
        if (isKind(Kind.IDENT)) {
            advance();
            id = new IdentExpr(firstToken);
            if(isKind(Kind.LSQUARE)){
                advance();
                pi = pixelSelector();
                //Expr pi = new PixelSelector(firstToken,id,e);
                if(isKind(Kind.COLON)){
                    advance();
                    ch = channelSelector();
                    e = new LValue(firstToken,id,pi,ch);
                }else {
                    throw new SyntaxException("Error");
                }
            }else {
                throw new SyntaxException("Error");
            }
        }else {
            throw new SyntaxException("Error");
        }
        return e;
    }



    // Statement::= LValue = Expr | write Expr | while Expr Block
    public Expr statement() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = lValue();

         if (isKind(Kind.ASSIGN)) {
             IToken op = t;
             advance();
             right = expression();
             return new BinaryExpr(firstToken, left, op.getKind(), right);
         }
         if(isKind(Kind.RES_write)){
             Expr e = expression();
             return new WriteStatement(firstToken,e);
         }
        if(isKind(Kind.RES_while)){
            advance();
            Expr l = expression();
            Expr r = block();
            return new WhileStatement(firstToken, l, r);
        }


    }

}
