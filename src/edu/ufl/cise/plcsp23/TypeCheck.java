/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the course.
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.*;

public class TypeCheck implements ASTVisitor {

    public static class SymbolTable {

        //store
        // use record
        record TableNode(NameDef n, Integer s) {
        }

        //stack
        // tablenode scope != scope id => error
        int current_num;

        Stack<Integer> scope_stack;
        HashMap<String, List<TableNode>> sTable = new HashMap<>();

        //returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef namDef, int scope) throws TypeCheckException {
            if (!sTable.containsKey(name)) {
                sTable.put(name, new ArrayList<TableNode>());
            } else if (sTable.containsKey(name)) { //if the scope of this is the same as the scope that's being inserted?
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
            Iterator scopeCheck = scope_stack.iterator();

            if(!sTable.containsKey(name)){
                throw new TypeCheckException("Lookup key not found");
            }

            for (int i = 0; i < sTable.get(name).size(); i++) {
                while (scopeCheck.hasNext()){
                    if (sTable.get(name).get(i).s == scopeCheck.next()) {
                        temp = sTable.get(name).get(i).n;
                        c = true;
                    }
                }
            }
            if(!c){
                throw new TypeCheckException("Lookup failed");
            }

            return temp;
        }

        // want to go from top of the stack and then down

        // somehow implement scope_stack ig
        void enterScope() {
            current_num = current_num++;
            scope_stack.push(current_num);
        }

        void closeScope() {
            current_num = scope_stack.pop();
        }

    }
    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException(message);
        }
    }

    //lib long symbol tabel
    //key -> string
    // unique scope id
    // name def

    //tree
    // set type field
    // ident has scopes
    // look up in symbol table

    // decelaration -> added into symbol table
    // ident -> look into symbol table
    // usiquie id in stack

    // work form top to bottom
    // dec -> namdef





    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        symbolTable.enterScope();
        List<NameDef> pList = program.getParamList();
        for (NameDef node : pList) {
            node.visit(this, arg);
        }
        Block b = program.getBlock();
        b.visit(this,arg);

        symbolTable.closeScope();
        return program;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> dList = block.getDecList();
        for (Declaration node : dList) {
            node.visit(this, arg);
        }
        List<Statement> sList = block.getStatementList();
        for (Statement node : sList) {
            node.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {

        NameDef ndef = declaration.getNameDef();
        ndef.visit(this,arg);
        //If present, Expr.type must be properly
        //typed and assignment compatible with
        //NameDef.type. It is not allowed to
        //refer to the name being defined.

        if(declaration != null){
            declaration.visit(this, arg);
            // check
        }


        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        String name = nameDef.getType().name().toString();
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        pixelFuncExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type t = (Type) conditionalExpr.getGuard().visit(this, arg); //how to visit
        //ch
        return null;
    }


    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        // Kind op = binaryExpr.getOp().getKind();
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;
        /*switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE
            case EQUALS,NOT_EQUALS -> {
                check(leftType == rightType, binaryExpr, "incompatible types for comparison");
                resultType = Type.BOOLEAN;
            }
            case PLUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
                else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case MINUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case TIMES -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case DIV -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case LT, LE, GT, GE -> {
                if (leftType == rightType) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            default -> {
                throw new Exception("compiler error");
            }
        }*/
        binaryExpr.setType(resultType);
        return resultType;
    }


    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        //return Type.STRING;
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
       /* Declaration dec = symbolTable.lookup(name);
        check(dec != null, identExpr, "undefined identifier " + name);
        check(dec.isAssigned(), identExpr, "using uninitialized variable");
        identExpr.setDec(dec); //save declaration--will be useful later.
        Type type = dec.getType();
        identExpr.setType(type);*/
        //return type;
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        //return Type.INT;
        return null;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
        // return Type.INT;
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }


    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;

    }


    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }


    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return null;
    }


}