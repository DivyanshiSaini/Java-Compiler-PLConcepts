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
        public void insert(String name, NameDef namDef, int scope) throws TypeCheckException {
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
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;

    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        //similar to cond. expr.
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type t = (Type) conditionalExpr.getGuard().visit(this, arg); //how to visit

        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        String name = nameDef.getType().name().toString();
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        pixelFuncExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
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

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        // probably not a list
      /*  List<AST> decsAndStatements = program.getDecsAndStatements();
        for (AST node : decsAndStatements) {
            node.visit(this, arg);
        }
        return zExpr;
    }*/
       // return zExpr;
        return null;
    }
}