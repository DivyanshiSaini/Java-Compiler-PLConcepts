package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.Expr;
import static edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.List;

public class Parser implements IParser{


    //private static final TokenType BANG_EQUAL;
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public AST parse() throws PLCException {
        return null;
    }


    //comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(Kind.GT, Kind.GE, Kind.LT, Kind.LE)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //primative operations
    private boolean isAtEnd() {
        return peek().type == Kind.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    //advance
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }


    //check
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    //match
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    //equality → comparison ( ( "!=" | "==" ) comparison )*
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, Kind.EQ)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //expression
    private Expr expression() {
        return equality();
    }


    //precedence
    // add sub
    private Expr term() {
        Expr expr = factor();

        while (match(Kind.MINUS, Kind.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // mul div
    private Expr factor() {
        Expr expr = unary();

        while (match(Kind.DIV, Kind.TIMES)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }








}
