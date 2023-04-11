package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
import java.lang.reflect.Parameter;
import java.util.List;

public class CodeGen implements ASTVisitor {
    String packageName ;
    CodeGen (String pName){
        packageName = pName;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();

        sB.append("public class ");
        program.getIdent().visit(this,arg); //gets the name
        sB.append(program.getIdent().getName());

        sB.append(" { ");
        sB.append("public static ");
        program.getType();
        sB.append(program.getType().name().toLowerCase());
        sB.append(" apply(");

        List<NameDef> pL = program.getParamList();
        for(int i = 0; i < pL.size(); i++){
            pL.get(i).visit(this,arg);
            sB.append(pL.get(i).getType().toString().toLowerCase() + " " +pL.get(i).getIdent().getName().toLowerCase());

        }

        sB.append(") {");
        program.getBlock().visit(this,arg);
        sB.append("} }");

        return sB.toString();
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        StringBuilder sB =new StringBuilder();
        List<Declaration> bL = block.getDecList();
        for(int i = 0; i < bL.size(); i++){
            bL.get(i).visit(this,arg);
            //if(bL.get(i).toString() == ";"){break;}
           //Help is needed? for both declist and statement list
           /* if(bL.get(i) != null){
                sB.append(";");
            }*/
        }

        List<Statement> sL = block.getStatementList();
        for(int i = 0; i < sL.size(); i++){
            sL.get(i).visit(this,arg);
            /*if(sL.get(i) != null){
                sB.append(";");
            }*/
        }
       // return sB.toString();
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();

        declaration.getNameDef().visit(this,arg);
        if(declaration.getInitializer() != null){
            sB.append(" = ");
            declaration.getInitializer().visit(this,arg);
        }
        return sB.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        nameDef.getType();
        nameDef.getIdent().visit(this,arg);
        sB.append(nameDef.getIdent().getName());

        return sB.toString();
    }


    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
       return null;
    }
    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
       return null;
    }
    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        //HELP IS THIS CORRECT?
        sB.append("(");
        conditionalExpr.getGuard().visit(this,arg);
        sB.append("== 1 ?");
        conditionalExpr.getTrueCase().visit(this,arg);
        sB.append(" : ");
        conditionalExpr.getFalseCase().visit(this,arg);
        sB.append(" ) ");

        //question
        return sB.toString();
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("( ");
        binaryExpr.getLeft().visit(this,arg);
        sB.append(" " + binaryExpr.getOp() + " ");
        binaryExpr.getRight().visit(this,arg);
        sB.append(")");
        return sB.toString();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
       return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(stringLitExpr.getValue());
        return sB.toString();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(identExpr.getName());
        return sB.toString();
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(zExpr.getValue());
        return sB.toString();
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        //sB.append(randomExpr.visit(this,arg));
        sB.append(Math.floor(Math.random() *256));
        return sB.toString();
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
        StringBuilder sB = new StringBuilder();
        if(lValue.getPixelSelector().getX() == null && lValue.getPixelSelector().getY() == null) {
            sB.append(lValue.getIdent());
        }
        return sB.toString();
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        statementAssign.getLv().visit(this,arg);
        sB.append(" = ");
        statementAssign.getE().visit(this,arg);
        return sB.toString();
    }
    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        // HELP what does it mean to import???
        //sB.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;");
        sB.append("ConsolelO.(");
        statementWrite.getE().visit(this,arg);
        sB.append(")");
        return sB.toString();
    }


    //Generate code to invoke
    //ConsoleIO.write(EXPR)
    //where EXPR is obtained by visiting Expr.
    //This will also require an import statement
    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("while(");
        whileStatement.getGuard().visit(this,arg);
        sB.append(" == 1) {");
        whileStatement.getBlock().visit(this,arg);
        sB.append("}");
        return sB.toString();
    }





    //return Expr
    //where EXPR is obtained by visiting the corresponding child
    //Expr ::= ConditionalExpr | BinaryExpr | UnaryExpr | StringLitExpr | IdentExpr | NumLitExpr | ZExpr | RandExpr | UnaryExprPostFix | PixelFuncExpr |PredeclaredVarExpr
    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return returnStatement.getE().visit(this,arg);
    }




    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }


    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return null;
    }











}
