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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeCheck implements ASTVisitor{

    public static class SymbolTable {

        //store
        // use record
        record TableNode(NameDef n, Integer i){};
        int current_num;
        int next_num;
        HashMap<String,List<TableNode>> entries = new HashMap<>();
        //lib long look stack with hasmap
        //returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef n) {
            if(!entries.containsKey(name)){
                entries.put(name, new ArrayList<TableNode>());
            }
            TableNode  tn = new TableNode(n,current_num);

            return entries.get(name).add(tn);
        }
        //returns Declaration if present, or null if name not declared.
        public Declaration lookup(String name) {
            return entries.get(name);
        }
    }

    // somehow implement scope_stack ig
   /* void enterScope()
    {
        current_num = next_num++;
        scope_stack.push(current_num);
    }
    void closeScope(){
        current_num = scope_stack.pop();
    }*/


    SymbolTable symbolTable = new SymbolTable();
    private void check(boolean condition, AST node, String message)  throws TypeCheckException {
        if (! condition) { throw new TypeCheckException(message, node.getSourceLoc()); }
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
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException{
        return null;
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException{
        return null;
    }

    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitProgram(Program program, Object arg) throws PLCException{
        return null;
    }

    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException{
        return null;
    }

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException{
        return null;
    }

    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException{
        return null;
    }

    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException{
        return null;
    }

    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException{
        // probably not a list
        List<AST> decsAndStatements = program.getDecsAndStatements();
        for (AST node : decsAndStatements) {
            node.visit(this, arg);
        }
        return zExpr;
    }

}
