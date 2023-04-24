/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the yyycourse.
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.*;
import static edu.ufl.cise.plcsp23.IToken.Kind;

public class TypeCheck implements ASTVisitor {

    public static class SymbolTable {

        //store
        // use record
        record TableNode(NameDef n, Integer s) {
        }

        //stack
        // tablenode scope != scope id => error
        int current_num =0;
        int next_num =0;

        Stack<Integer> scope_stack = new Stack<>();
        HashMap<String, List<TableNode>> sTable = new HashMap<>();

        //returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef namDef,int scope) throws TypeCheckException {
            if (!sTable.containsKey(name)) {
                sTable.put(name, new ArrayList<TableNode>());
            }

            else if (sTable.containsKey(name)) { //if the scope of this is the same as the scope that's being inserted?
                //System.out.println(sTable.get(name).size());
                for (int i = 0; i < sTable.get(name).size(); i++) {
                    if (sTable.get(name).get(i).s == scope) {
                        throw new TypeCheckException("Error Scope Exist");
                    }
                }
            }
            TableNode tn = new TableNode(namDef, scope);
            sTable.get(name).add(tn);

            return true;
        }

        //returns Declaration if present, or null if name not declared.
        public NameDef lookup(String name) throws TypeCheckException {
            NameDef temp = null;
            boolean c = false;
            Stack<Integer> tempStack = (Stack<Integer>) scope_stack.clone();
           // Iterator scopeCheck = scope_stack.iterator();
            // store scope #
            if(!sTable.containsKey(name)){
                throw new TypeCheckException("Lookup key not found");
            }
            while (!tempStack.empty()){
                for (int i = 0; i < sTable.get(name).size(); i++) {

                    if (sTable.get(name).get(i).s == tempStack.peek()) {
                        temp = sTable.get(name).get(i).n;
                        c = true;
                        return temp;
                    }
                }
                tempStack.pop();
            }
            if(!c){
                throw new TypeCheckException("Lookup failed");
            }

            return temp;
        }


        void enterScope() {
            current_num = next_num++;
            scope_stack.push(current_num);
        }
        void closeScope() {
            scope_stack.pop();
        }

    }
    Program p = null;
    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException(message);
        }
    }




    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        symbolTable.enterScope();
        p = program;
        List<NameDef> pL = program.getParamList();
        for(int i = 0; i < pL.size(); i++){
            pL.get(i).visit(this,arg);
        }
        program.getBlock().visit(this, arg);
        symbolTable.closeScope();

        return program;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> bL = block.getDecList();
        for(int i = 0; i < bL.size(); i++){
            bL.get(i).visit(this,arg);
        }
        List<Statement> sL = block.getStatementList();
        for(int i = 0; i < sL.size(); i++){
            sL.get(i).visit(this,arg);
        }

        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {

        if(declaration.getInitializer() != null) {
            Type e = (Type) declaration.getInitializer().visit(this, arg);
            Type n = (Type) declaration.getNameDef().visit(this,arg);
            //System.out.println(e + "\n" + n);

            if (n == Type.IMAGE) {
                if (e == Type.IMAGE || e == Type.PIXEL || e == Type.STRING) {
                    check(true, declaration, "declaration compatible");
                } else {
                    check(false, declaration, "declaration not compatible");
                }
            }
            else if (n == Type.PIXEL) {
                if (e == Type.PIXEL || e == Type.INT) {
                    check(true, declaration, "declaration compatible");
                } else {
                    check(false, declaration, "declaration not compatible");
                }
            }
            else if (n == Type.INT) {
                if (e == Type.PIXEL || e == Type.INT) {
                    check(true, declaration, "declaration compatible");
                } else {
                    check(false, declaration, "declaration not compatible");
                }
            }
            else if (n == Type.STRING) {
                if (e == Type.IMAGE || e == Type.PIXEL || e == Type.INT || e == Type.STRING) {
                    check(true, declaration, "declaration compatible");
                } else {
                    check(false, declaration, "declaration not compatible");
                }

            }

            //HELP do I need an else statement here
        } else{
            declaration.getNameDef().visit(this,arg);
        }

        if (declaration.getNameDef().getType() == Type.IMAGE){
            if(declaration.getInitializer() != null){
                declaration.getInitializer().visit(this,arg);
            }
            if(declaration.getNameDef().getDimension() != null){
                declaration.getNameDef().getDimension().visit(this,arg);
            }
        }


        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {

        if(nameDef.getDimension() != null) {
           //HELP is the bottom line correct to check TYPE == IMAGE
           check(nameDef.getType() == Type.IMAGE, nameDef, "Type is Image");
           nameDef.getDimension().visit(this, arg);
       }

        check(nameDef.getType() != Type.VOID, nameDef, "Type is Image");

       //HELP IS SCOPE CURRENT_NUM CORRECT??
        nameDef.decNumber = symbolTable.current_num;
        symbolTable.insert(nameDef.getIdent().getName(), nameDef, symbolTable.current_num);
       return nameDef.getType();
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        Type result = null;

        boolean p = false;
        boolean c = false;

        if(unaryExprPostfix.getPixel().getX() != null){
            unaryExprPostfix.getPixel().getX().visit(this,arg);
            p = true;
        }
        if(unaryExprPostfix.getPixel().getY() != null){
            unaryExprPostfix.getPixel().getY().visit(this,arg);
            //c = true;
        }
        if(unaryExprPostfix.getColor() != null){
             c = true;
        }

        Type t = (Type) unaryExprPostfix.getPrimary().visit(this,arg);
        //Type t = unaryExprPostfix.getPrimary().getType(); EDITMADE

        if(t == Type.PIXEL){
            if(!p && c){result = Type.INT;}
            else {check(false,unaryExprPostfix, "unary expr fail");}
        }
        else if (t == Type.IMAGE){
            if(!p && c){ result = Type.IMAGE; }
            else if(p && !c){ result = Type.PIXEL;}
            else if(p && c){result = Type.INT;}
            else {check(false,unaryExprPostfix, "unary expr fail");}
        }
        else{
            check(false, unaryExprPostfix, "un fail");
        }

        // EDITMADE
        unaryExprPostfix.setType(result);

        //HELP -> IS return type null?
        return result;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        //EDITMADE made fixes
       // pixelFuncExpr.visit(this,arg);
        pixelFuncExpr.setType(Type.INT);
        pixelFuncExpr.getSelector().visit(this,arg);
        return Type.INT;
        //return Type.INT;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type t0 = (Type) conditionalExpr.getGuard().visit(this,arg);

        //HELP AM I DOIING THIS CORRECT?
        Type t1 = (Type) conditionalExpr.getTrueCase().visit(this,arg);
        Type t2 = (Type) conditionalExpr.getFalseCase().visit(this,arg);

        check(t0 == Type.INT, conditionalExpr, "Expr is int");
        check(t1 == t2, conditionalExpr, "Expr1 is equal to Expr2");

        conditionalExpr.setType(t1);

        return t1;
    }


    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException{
        Kind op = binaryExpr.getOp();
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        //System.out.println(leftType + "\n" + rightType);
        Type resultType = null;

        switch(op) {  // HELP '=' an operator?
            //|,&
            case BITOR, BITAND ->{
                if(leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            //||, &&, <,>,<=,>=
            case OR, AND, LT, GT, LE, GE ->{
                if(leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            // ==
            case EQ ->{
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if(leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.INT;
                else if(leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.INT;
                else if(leftType == Type.STRING && rightType == Type.STRING) resultType = Type.INT;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            //**
            case EXP -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if(leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            // +
            case PLUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if(leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else if(leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
                else if(leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            //-
            case MINUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if(leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            // *, /, %
            case TIMES, DIV, MOD -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if(leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
                else if(leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
                else if(leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
                else if(leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            default -> {
                throw new TypeCheckException("compiler error");
            }
        }
        binaryExpr.setType(resultType);
        return resultType;
    }


    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Kind op = unaryExpr.getOp();
        Type unaryType = (Type) unaryExpr.getE().visit(this, arg);
        Type resultType = null;

        switch(op) {
            //|,&
            case BANG -> {
                if (unaryType == Type.INT) resultType = Type.INT;
                else if (unaryType == Type.PIXEL) resultType = Type.PIXEL;
                else check(false, unaryExpr, "incompatible types for operator");
            }
            case MINUS,RES_cos,RES_sin,RES_atan ->{
                if(unaryType == Type.INT) resultType = Type.INT;
                else check(false, unaryExpr, "incompatible types for operator");
            }
            default -> { //EDITMADE checks
                throw new TypeCheckException("compiler error");
            }
        }
        unaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
        //return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
       //HELP there is no numlit what do I do?
        numLitExpr.setType(Type.INT);
        return Type.INT;
       //return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        // < = we are good or else error
        //HELP IS THIS IMPLEMENTED CORRECTLY??

        String name = identExpr.getName();
        NameDef nDef = symbolTable.lookup(name);
        identExpr.decNumber = nDef.decNumber;
        check(nDef != null, identExpr, "undefined identifier " + name);
        identExpr.setType(nDef.getType()); //save declaration--will be useful later.

        return nDef.getType();
        //return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        //HELP what do I do here?
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
         return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        Type t0 = (Type) pixelSelector.getX().visit(this,arg);
        Type t1 = (Type) pixelSelector.getY().visit(this,arg);

        check(t0 == Type.INT, pixelSelector, "Expr 0 not an int type PS");
        check(t1 == Type.INT, pixelSelector, "Expr 1 not an int type PS");

        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Type r = (Type) expandedPixelExpr.getRedExpr().visit(this,arg);
        Type g = (Type) expandedPixelExpr.getGrnExpr().visit(this,arg);
        Type b = (Type) expandedPixelExpr.getBluExpr().visit(this,arg);

        check(r == Type.INT, expandedPixelExpr, "Expr 0 not an int type EPE");
        check(g == Type.INT, expandedPixelExpr, "Expr 1 not an int type EPE");
        check(b == Type.INT, expandedPixelExpr, "Expr 3 not an int type EPE");
        expandedPixelExpr.setType(Type.PIXEL);

        return Type.PIXEL;
    }


    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Type h = (Type) dimension.getHeight().visit(this,arg);
        Type w = (Type) dimension.getWidth().visit(this,arg);

        check(h == Type.INT, dimension,"Expr 0 not an int type Di");
        check(w == Type.INT, dimension,"Expr 1 not an int type Di");
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {

        Type i = null;
        boolean p = false;
        boolean c = false;
        Type resultType = null;


        if(lValue.getIdent() != null){
            i = (Type) lValue.getIdent().visit(this,arg);
            NameDef nDef = symbolTable.lookup(lValue.getIdent().getName()); // HELP IS THIS NEEDED?
            lValue.decNumber = nDef.decNumber;
            i = nDef.getType();
            resultType = i;
            //HELP HOW TO ACCESS CHANNEL SELECTOR
            if(lValue.getPixelSelector() != null){
                lValue.getPixelSelector().visit(this,arg);
                p = true;
            }
            if(lValue.getColor() != null){
                lValue.getColor();
                //lValue.getColor();
                c = true;
            }

            if(i == Type.IMAGE){
                if((!p && !c) || (!p && c)) resultType = Type.IMAGE;
                else if ( p && !c) resultType = Type.PIXEL;
                else if (p && c) resultType = Type.INT;
                else check(false, lValue, "lvalue error Im");
            }
            else if(i == Type.PIXEL){
                if(!p && !c) resultType = Type.PIXEL;
                else if (!p && c) resultType = Type.INT;
                else check(false, lValue, "lvalue error Pi");
            }
            else if(i == Type.STRING){
                if(!p && !c) resultType = Type.STRING;
                else check(false, lValue, "lvalue error St");
            }
            else if(i == Type.INT){
                if(!p && !c) resultType = Type.INT;
                else check(false, lValue, "lvalue error In");
            }
            else {
                check(false, lValue, "lvalue error");
            }

            lValue.getIdent().setDef(nDef);
        }


        return resultType;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
       Type l = (Type) statementAssign.getLv().visit(this,arg);
       Type e = (Type) statementAssign.getE().visit(this,arg);

       // System.out.println(l + "\n" + e);

       if(l == Type.IMAGE){
            if(e == Type.IMAGE || e == Type.PIXEL || e == Type.STRING){
                // HELP is it ok to just check true?
                check(true, statementAssign, "1 declaration compatible");
            }else {
                check(false, statementAssign, " 2 declaration not compatible");
            }
        }
        else if(l == Type.PIXEL){
           // System.out.println(e);
            if(e == Type.PIXEL || e == Type.INT){
                check(true, statementAssign, "3 declaration compatible");
            }
            else {
                check(false, statementAssign, "4 declaration not compatible");

            }

        }else if(l == Type.INT){
            if(e == Type.PIXEL || e == Type.INT){
                check(true, statementAssign, "5 declaration compatible");
            }
            else {
                check(false, statementAssign, "6 declaration not compatible");
            }
        } else if(l == Type.STRING){
            if(e == Type.IMAGE || e == Type.PIXEL || e == Type.INT || e== Type.STRING){
                check(true, statementAssign, "7 declaration compatible");
            }else {
                check(false, statementAssign, "8 declaration not compatible");
            }
        } else{
            check(false, statementAssign, "9 declaration not compatible");
        }

        return null;

    }


    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        statementWrite.getE().visit(this,arg);
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        whileStatement.getGuard().visit(this,arg);
        check(whileStatement.getGuard().getType() == Type.INT, whileStatement, "While statement type not equal");
        symbolTable.enterScope();
        whileStatement.getBlock().visit(this,arg);
        symbolTable.closeScope();
        return null;
    }


    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        Type r = (Type) returnStatement.getE().visit(this,arg);
        Type pro =  p.getType(); //HELP is this the correct way of calling program

        if(pro == Type.IMAGE){
            if(r== Type.IMAGE || r== Type.PIXEL || r== Type.STRING){
                check(true, returnStatement, "declaration compatible");
            }
            else {
                check(false, returnStatement, "declaration not compatible");
            }
        } else if(pro== Type.PIXEL){
            if(r== Type.PIXEL || r== Type.INT){
                check(true, returnStatement, "declaration compatible");
            } else {
                check(false, returnStatement, "declaration not compatible");
            }
        }else if(pro== Type.INT){
            if(r== Type.PIXEL|| r== Type.INT){
                check(true, returnStatement, "declaration compatible");
            } else {
                check(false, returnStatement, "declaration not compatible");
            }
        }else if(pro== Type.STRING){
            if(r== Type.IMAGE || r== Type.PIXEL || r== Type.INT || r== Type.STRING){
                check(true, returnStatement, "declaration compatible");
            }else {
                check(false, returnStatement, "declaration not compatible");
            }
        } else{
            check(false, returnStatement, "declaration not compatible");
        }

        return null;
    }


}