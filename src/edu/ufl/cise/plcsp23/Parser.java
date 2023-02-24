package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.ZExpr;
import static edu.ufl.cise.plcsp23.IToken.Kind;

import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{
    @Override
    public AST parse() throws PLCException {
        return null;
    }

    private int current = 0; //index in the list

    IToken t;

    IScanner scanner;

    Parser(IScanner s) throws LexicalException{
        this.scanner = s;
        t = scanner.next();
    }


    protected boolean isKind(Kind kind) {
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

   /* private Kind previous() {
        //
        return tokens.get(current - 1);
    }*/
    //advance
    private void advance() throws LexicalException {
        if (!isAtEnd()) t = scanner.next();
    }
    //check



    //<expr> ::=   <conditional_expr> | <or_expr>
    Expr expression() {
        return null;
    }

    //<conditional_expr>  ::= if <expr> ? <expr> ? <expr>
    Expr conditional() {
        return null;
    }

    //<or_expr> ::=  <and_expr> (  ( | | || ) <and_expr>)*
    public Expr or() throws PLCException{ //syntax and lex. in same
        IToken firstToken = t;
        Expr expr = and();
        Expr left = null;
        Expr right = null;
        left = and();
        while ( isKind(Kind.ASSIGN, Kind.EQ)) {
            Kind op = t.getKind();
            advance();
            right = and();
            left = new BinaryExpr(firstToken,left,op,right);
        }

        return left;
    }

    //<and_expr> ::=  <comparison_expr> ( ( & | && )  <comparison_expr>)*
    public Expr and() throws PLCException{
        IToken firstToken = t;
        Expr expr = comparison();
        Expr left = null;
        Expr right = null;
        left = comparison();
        while ( isKind(Kind.ASSIGN, Kind.EQ)) {
            Kind op = t.getKind();
            advance();
            right = comparison();
            left = new BinaryExpr(firstToken,left,op,right);
        }

        return left;
    }


    //<comparison_expr> ::=   <power_expr> ( (< | > | == | <= | >=) <power_expr>)*
    public Expr comparison() throws PLCException{
        IToken firstToken = t;
        Expr expr = power();
        Expr left = null;
        Expr right = null;
        left = power();
        while (isKind(Kind.GT, Kind.GE, Kind.LT, Kind.LE, Kind.EQ)) {
            Kind op = t.getKind();
            advance();
            right = power();
            left = new BinaryExpr(firstToken,left,op,right);
        }

        return left;
    }

    // <power_expr> ::=    <additive_expr> ** <power_expr> |  <additive_expr>
    public Expr power() throws PLCException{
        return null;
    }

    // <additive_expr> ::=  <multiplicative_expr> ( ( + | - ) <multiplicative_expr> )*
    public Expr additive() throws PLCException{
        IToken firstToken = t;
        Expr expr = multiplicative();
        Expr left = null;
        Expr right = null;
        left = multiplicative();
        while(isKind(Kind.PLUS , Kind.MINUS)){
            Kind op = t.getKind();
            advance();
            right = multiplicative();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }

    // <multiplicative_expr> ::= <unary_expr> (( * | / | % ) <unary_expr>)*
    public Expr multiplicative() throws PLCException{
        IToken firstToken = t;
        Expr expr = unary();
        Expr left = null;
        Expr right = null;
        left = unary();
        while(isKind(Kind.TIMES, Kind.DIV, Kind.MOD)){
            Kind op = t.getKind();
            advance();
            right = unary();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }

    //<unary_expr> ::= ( ! | - | sin | cos | atan) <unary_expr> |   <primary_expr>
    public Expr unary() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if(isKind(Kind.BANG, Kind.MINUS, Kind.RES_sin, Kind.RES_cos ,Kind.RES_atan)){
            Kind op = t.getKind();
            advance();
            e = unary();
        } else if (isKind(Kind.LPAREN)){
            advance();
            e = primary();
            isKind(Kind.RPAREN);
        }
        // else error();

        return e;
    }

    // <primary_expr> ::= STRING_LIT | NUM_LIT | IDENT | ( <expr> ) | Z | rand
    public Expr primary() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        if(isKind(Kind.STRING_LIT)){
            e = new StringLitExpr(firstToken);
            advance();
        } else if (isKind(Kind.NUM_LIT)) {
            e = new NumLitExpr(firstToken);
            advance();
        } else if (isKind(Kind.IDENT)) {
            e =new IdentExpr(firstToken);
            advance();
        } else if (isKind(Kind.LPAREN)){
            advance();
            e = expression();
            isKind(Kind.RPAREN);
        } else if (isKind(Kind.RES_Z)) {
            e =new IdentExpr(firstToken);
            advance();
        } else if (isKind(Kind.RES_rand)) {
            e =new IdentExpr(firstToken);
            advance();
        } else {
            //error();}
        }
        return e;
    }













































}
