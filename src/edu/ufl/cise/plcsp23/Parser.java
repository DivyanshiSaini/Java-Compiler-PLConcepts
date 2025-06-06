package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;

import static edu.ufl.cise.plcsp23.IToken.Kind;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser {
    @Override
    public AST parse() throws PLCException {
        return program();
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

    //Program::=  Type IDENT ( ParamList ) Block
    public Program program() throws PLCException{
        IToken firstToken = t;
        Type type = type();
        Ident id = null;
        Program p = null;
        //added this advance for testing, could be wrong
        //advance();
        if (isKind(Kind.IDENT)) {
            id = new Ident(t);
            advance();
            if(isKind(Kind.LPAREN)){
                advance();
                List<NameDef> pList = paramList();
                if(isKind(Kind.RPAREN)){
                    advance();
                    Block b = block();
                    p = new Program(firstToken, type, id, pList, b);
                    if(!isAtEnd()){throw new SyntaxException("Error");}
                }else{throw new SyntaxException("Error");}
            }else{throw new SyntaxException("Error");}
        }else{throw new SyntaxException("Error");}


        return p;
    }
    //Block ::= { DecList  StatementList }
    public Block block() throws PLCException{
        IToken firstToken = t;
        Block b = null;
        if (isKind(Kind.LCURLY)) { // isKind
            advance();
            List<Declaration> decList = decList();
            //could be here
            List<Statement> statList = statementList();
            if(isKind(Kind.RCURLY)){
                //might need advance();
                advance();
                b = new Block(firstToken,decList,statList);
            } else{throw new SyntaxException("Error");}
        } else{throw new SyntaxException("Error");}//else error
        return b;
    }


    //DecList ::= ( Declaration . )*
    public List<Declaration> decList() throws PLCException{
        IToken firstToken = t;
        List<Declaration> list = new ArrayList<Declaration>();
        Declaration de = null;
        while (isKind(Kind.RES_pixel) || isKind(Kind.RES_image) || isKind(Kind.RES_int) || isKind(Kind.RES_string) || isKind(Kind.RES_void)) {
            de = declaration();
            if(isKind(Kind.DOT)){
                advance();
                list.add(de);
            } else {throw new SyntaxException("Error");}
        }
        return list;
    }
    //StatementList ::= ( Statement . ) *
    public List<Statement> statementList() throws PLCException{
        IToken firstToken = t;
        Statement s = null;
        List<Statement> list = new ArrayList<Statement>();
        while (isKind(Kind.IDENT)|| isKind(Kind.RES_while) || isKind(Kind.RES_write) || isKind(Kind.COLON)) {
            s = statement();
            if(isKind(Kind.DOT)){
                advance();
                list.add(s);
            } else {throw new SyntaxException("Error");}
        }
        return list;
    }
    //ParamList ::= ε |  NameDef  ( , NameDef ) *
    public List<NameDef> paramList() throws PLCException{
        IToken firstToken = t;
        List<NameDef> list = new ArrayList<NameDef>();

        if(isKind(Kind.RES_pixel) || isKind(Kind.RES_image) || isKind(Kind.RES_int) || isKind(Kind.RES_string) || isKind(Kind.RES_void)){
            NameDef ndef = nameDef();
            list.add(ndef);
            while (isKind(Kind.COMMA)) {
                advance();
                NameDef tempDef = nameDef();
                list.add(tempDef);
            }
        }

        return list;
    }
    //NameDef ::= Type IDENT | Type Dimension IDENT
    public NameDef nameDef() throws PLCException{
        IToken firstToken = t;
        Type type = type();
        Expr e = null;
        Ident id = null;
        NameDef ndef = null;
        Dimension d = null;

        if (isKind(Kind.LSQUARE)) {
            d = dimension();
        }
        if(t.getKind() == Kind.IDENT){
            id = new Ident(t);
            advance();
            ndef = new NameDef(t, type, d, id);
        } else{throw new SyntaxException("Error");}

        return ndef;
    }
    //Type ::= image | pixel | int | string | void
    public Type type() throws PLCException{
        Type type = null;
        if(isKind(Kind.RES_pixel) || isKind(Kind.RES_image) || isKind(Kind.RES_int) || isKind(Kind.RES_string) || isKind(Kind.RES_void)){
            type = Type.getType(t);
            advance();
        } else{
            throw new SyntaxException("Error");
        }
        return type;
    }
    //Declaration::= NameDef |  NameDef = Expr
    public Declaration declaration() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        NameDef ndef = nameDef();
        if (isKind(Kind.ASSIGN)) {
            advance();
            e = expression();
            Declaration d = new Declaration(firstToken,ndef,e);
            return d;
        }
        else{
            return new Declaration(firstToken,ndef,null);
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
            Expr guard = expression();
            if (isKind(Kind.QUESTION)) {
                advance();
                Expr trueC = expression();
                if (isKind(Kind.QUESTION)) {
                    advance();
                    Expr falseC = expression();
                    e = new ConditionalExpr(firstToken, guard, trueC, falseC);
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
            e = unaryExprPostFix();
        }
        return e;
    }


    //UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
    public Expr unaryExprPostFix() throws PLCException{
        IToken firstToken = t;
        Expr id = primary();
        UnaryExprPostfix e = null;
        PixelSelector pi = null;
        ColorChannel ch = null;

        if(isKind(Kind.LSQUARE)){
            pi = pixelSelector();
        }
        if(isKind(Kind.COLON)){
            ch = channelSelector();

        }
        if (ch == null && pi == null) {
            return id;
        }
        e = new UnaryExprPostfix(firstToken,id,pi,ch);

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
        }else if (isKind(Kind.RES_Z)) {
            advance();
            e = new ZExpr(firstToken);
        }else if (isKind(Kind.RES_rand)) {
            advance();
            e = new RandomExpr(firstToken);
        }else if (isKind(Kind.RES_rand)) {
            advance();
            e = new RandomExpr(firstToken);
        }else if (isKind(Kind.RES_x)) {
            advance();
            e = new PredeclaredVarExpr(firstToken);
        }else if (isKind(Kind.RES_y)) {
            advance();
            e = new PredeclaredVarExpr(firstToken);
        }else if (isKind(Kind.RES_a)) {
            advance();
            e = new PredeclaredVarExpr(firstToken);
        } else if (isKind(Kind.RES_r)) {
            advance();
            e = new PredeclaredVarExpr(firstToken);
        }else if (isKind(Kind.LSQUARE)) {
            e = expandedPixel();
        }else if (isKind(Kind.RES_x_cart) || isKind(Kind.RES_y_cart) || isKind(Kind.RES_a_polar) || isKind(Kind.RES_r_polar)) {
            e = pixelFunctionExpr();
        }else { throw new SyntaxException("Error");}
        return e;
    }


    //ChannelSelector ::= : red | : grn | : blu
    public ColorChannel channelSelector() throws PLCException{
        IToken firstToken = t;
        ColorChannel e = null;
        if (isKind(Kind.COLON)) { // isKind
            advance();
            if(isKind(Kind.RES_red)){ //you can check in same branch
                e = ColorChannel.getColor(t);
                advance();
            }
            else if(isKind(Kind.RES_blu)){
                e = ColorChannel.getColor(t);
                advance();
            }
            else if(isKind(Kind.RES_grn)){
                e = ColorChannel.getColor(t);
                advance();
            } else{
                throw new SyntaxException("Error");
            }
        } else { throw new SyntaxException("Error");}
        return e;
    }

    //PixelSelector  ::= [ Expr , Expr ]
    public PixelSelector pixelSelector() throws PLCException{
        IToken firstToken = t;
        PixelSelector e = null;
        if (isKind(Kind.LSQUARE)) { // isKind
            advance();
            Expr x = expression();
            if(isKind(Kind.COMMA)){
                advance();
                Expr y = expression();
                if(isKind(Kind.RSQUARE)){
                    advance();
                    e = new PixelSelector(firstToken,x,y);
                } else {throw new SyntaxException("Error");}//else error
            } else {throw new SyntaxException("Error");}//else error
        } else {throw new SyntaxException("Error");}//else error
        return e;
    }

    //ExpandedPixel ::= [ Expr , Expr , Expr ]
    public Expr expandedPixel() throws PLCException{
        IToken firstToken = t;
        Expr e = null;

        if (isKind(Kind.LSQUARE)) { // isKind
            advance();
            Expr r = expression();
            if(isKind(Kind.COMMA)){
                advance();
                Expr g = expression();
                if (isKind(Kind.COMMA)) { // isKind
                    advance();
                    Expr b = expression();
                    if(isKind(Kind.RSQUARE)){
                        advance();
                        e = new ExpandedPixelExpr(firstToken,r,g,b);
                    } else {throw new SyntaxException("Error");}
                } else {throw new SyntaxException("Error");}//else error
            } else {throw new SyntaxException("Error");}//else error
        } else {throw new SyntaxException("Error");}//else error
        return e;
    }

    //PixelFunctionExpr ::= ( x_cart | y_cart | a_polar | r_polar ) PixelSelector
    public PixelFuncExpr pixelFunctionExpr() throws PLCException{
        IToken firstToken = t;
        PixelFuncExpr e = null;
        if (isKind(Kind.RES_x_cart) || isKind(Kind.RES_y_cart) || isKind(Kind.RES_a_polar) || isKind(Kind.RES_r_polar)) {
            IToken op = t;
            advance();
            PixelSelector right = pixelSelector();
            e = new PixelFuncExpr(firstToken, op.getKind(), right);
        }else {throw new SyntaxException("Error");}

        return e;
    }


    //Dimension  ::= [ Expr , Expr ]
    public Dimension dimension() throws PLCException{
        IToken firstToken = t;
        Dimension e = null;
        if (isKind(Kind.LSQUARE)) { // isKind
            advance();
            Expr w = expression();
            if(isKind(Kind.COMMA)){
            advance();
            Expr h = expression();
                if(isKind(Kind.RSQUARE)){
                    advance();
                    e = new Dimension(firstToken,w,h);
                } else{throw new SyntaxException("Error");}
            } else{throw new SyntaxException("Error");}//else error
        } else{throw new SyntaxException("Error");}//else error
        return e;
    }


    //LValue ::= IDENT (PixelSelector | ε ) (ChannelSelector | ε )
    public LValue lValue() throws PLCException{
        IToken firstToken = t;
        Ident id = null;
        LValue e = null;
        PixelSelector pi = null;
        ColorChannel ch = null;
        if (isKind(Kind.IDENT)) {
            id = new Ident(t);
            advance();
            if(isKind(Kind.LSQUARE)){
                pi = pixelSelector();
            }
            if(isKind(Kind.COLON)){
                ch = channelSelector();
            }
            e = new LValue(firstToken,id,pi,ch);
        }else {throw new SyntaxException("Error");}

        return e;
    }

    // Statement::= LValue = Expr | write Expr | while Expr Block | :Expr
    public Statement statement() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        LValue l = null;
        Statement a = null;

        if(isKind(Kind.IDENT)){
            l = lValue();
            if (isKind(Kind.ASSIGN)) {
                advance();
                e = expression();
                a = new AssignmentStatement(firstToken,l,e);
            }
        } else if(isKind(Kind.RES_write)){
             advance();
             e = expression();
             a =  new WriteStatement(firstToken,e);

         }else if(isKind(Kind.RES_while)){
            advance();
            e = expression();
            Block r = block();
            a = new WhileStatement(firstToken, e, r);
        }
        //added 3/27/2023 for P4
        else if (isKind(Kind.COLON)) { // isKind
            advance();
            e = expression();
            a = new ReturnStatement(firstToken,e);
        }
        else { throw new SyntaxException("Error");}
        return a;
    }

}

