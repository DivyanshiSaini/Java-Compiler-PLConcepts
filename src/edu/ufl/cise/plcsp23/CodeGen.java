package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import java.lang.*;
import java.lang.reflect.Parameter;
import static edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.List;

public class CodeGen implements ASTVisitor {
    String packageName ;
    CodeGen (String pName){
        packageName = pName;
    }


    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();

        //HELP what else to add
        sB.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO; \n");
        sB.append("import java.lang.Math; \n \n");

        sB.append("public class ");
        program.getIdent().visit(this, arg); //gets the name
        sB.append(program.getIdent().getName());

        sB.append(" { \n");
        sB.append("\t public static ");

        program.getType();

        String s = program.getType().name().toLowerCase().toString().replaceAll("string", "String");



        sB.append(s);

        sB.append(" apply(");

        List<NameDef> pL = program.getParamList();
        for (int i = 0; i < pL.size(); i++) {
            pL.get(i).visit(this, arg);
            String si = pL.get(i).getType().toString().toLowerCase().replaceAll("string", "String");

            sB.append(si + " ");

            sB.append(pL.get(i).getIdent().getName().toLowerCase());
            if (i != pL.size() - 1) sB.append(" , ");
        }

        sB.append(") { \n");
        sB.append(program.getBlock().visit(this, arg));
        sB.append(" \t } \n }");

        return sB.toString();
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        StringBuilder sB =new StringBuilder();

        List<Declaration> bL = block.getDecList();
        for(int i = 0; i < bL.size(); i++){
            sB.append("\t \t");
            sB.append(bL.get(i).visit(this,arg));
            sB.append("; \n");
        }

        List<Statement> sL = block.getStatementList();
        for(int i = 0; i < sL.size(); i++){
            sB.append("\t \t");
            sB.append(sL.get(i).visit(this,arg));
            sB.append("; \n");

        }
        return sB.toString();
       // return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();

        declaration.getNameDef().visit(this,arg);
        String s = declaration.getNameDef().getType().name().toLowerCase().replaceAll("string", "String");
        sB.append(s + " ");


        sB.append(declaration.getNameDef().getIdent().getName()+"_"+declaration.getNameDef().decNumber);

        if(declaration.getInitializer() != null) {

        }
        sB.append(" = ");
        if(s.toString() == "String"){
            Object temp = declaration.getInitializer().visit(this,arg);
            sB.append(" \"" + declaration.getInitializer().visit(this,arg) + "\" ");
        }else {
            sB.append(declaration.getInitializer().visit(this,arg));
        }



        return sB.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        //nameDef.getType();
        sB.append(nameDef.getType().name());
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
        //append
        sB.append("( (");
        sB.append(conditionalExpr.getGuard().visit(this,arg));
        sB.append(" != 0) ? "); // only case
        sB.append(conditionalExpr.getTrueCase().visit(this,arg));
        sB.append(" : ");
        sB.append(conditionalExpr.getFalseCase().visit(this,arg));
        sB.append(") ");

        //question
        return sB.toString();
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("(");

        Kind op = binaryExpr.getOp();
        String opStore = "";
        switch(op) {
            //|,&
            case BITOR ->opStore = "|";
            case BITAND->opStore = "&";
            //||
            case OR ->opStore = "||";
            //&&
            case AND ->opStore = "&&";
            //<
            case LT->opStore = "<";
            //>
            case GT ->opStore = ">";
            //<=
            case LE->opStore = "<=";
            //>=
            case GE ->opStore = ">=";
            // ==
            case EQ ->opStore = "==";
            // =
            case PLUS ->opStore = "+";
            // =
            case MINUS ->opStore = "-";
            // =
            case TIMES ->opStore = "*";
            // =
            case DIV ->opStore = "/";
            // =
            case MOD ->opStore = "%";
            //**
            case EXP ->opStore = "Math.pow";
        }
        if(opStore == "(int)Math.pow"){
            sB.append("(" + binaryExpr.getLeft().visit(this,arg) +"," + binaryExpr.getRight().visit(this,arg) + ")");

        } else {
            sB.append(binaryExpr.getLeft().visit(this,arg));
        }

        sB.append(opStore); //something that returns an string //switch statement
        // if op < > <= >= == && || //itok
        if (opStore == "||" || opStore == "&&"){
            //convert to bool then convert to int
        }
        else if(opStore == ">" || opStore == "<" || opStore == ">="|| opStore == "<=" || opStore == "=="){
            sB.append(binaryExpr.getRight().visit(this,arg));
            sB.append(" ? 1 : 0");
        } else{
        sB.append(binaryExpr.getRight().visit(this,arg));}
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
        sB.append("\"");
        sB.append(stringLitExpr.getValue());
        sB.append("\"");
        return sB.toString();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(identExpr.getName()+"_"+identExpr.decNumber); //_scope
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
       // sB.append(randomExpr.visit(this,arg));
        sB.append("(int)Math.floor(Math.random() *256)");
        //HELP  what does it mean when it says will require import
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
        if(lValue.getPixelSelector() == null && lValue.getColor() == null) {
            sB.append(lValue.getIdent().getName()+"_"+lValue.decNumber);
        }
        return sB.toString();
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        //check expr type is int
        //check lvalue type is string
        //if they are different type cast
        //turn int into string same as declaration
        sB.append(statementAssign.getLv().visit(this,arg));
        sB.append(" = ");
        sB.append(statementAssign.getE().visit(this,arg));
        return sB.toString();
    }
    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        // HELP what does it mean to import???
        //sB.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;");
        sB.append("ConsoleIO.write(");
        sB.append(statementWrite.getE().visit(this,arg));
        sB.append(")");
        return sB.toString();
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("while(");
        sB.append(whileStatement.getGuard().visit(this,arg));
        sB.append(" != 0) { \n");
        sB.append("\t" + whileStatement.getBlock().visit(this,arg) + "} \n");
        return sB.toString();
    }





    //return Expr
    //where EXPR is obtained by visiting the corresponding child
    //Expr ::= ConditionalExpr | BinaryExpr | UnaryExpr | StringLitExpr | IdentExpr | NumLitExpr | ZExpr | RandExpr | UnaryExprPostFix | PixelFuncExpr |PredeclaredVarExpr
    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        //check if type is int and needs to be string then type cast
        sB.append(" return ");
        sB.append(returnStatement.getE().visit(this,arg));
        return sB.toString();
    }


    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(numLitExpr.getValue());
        return sB.toString();
    }











}