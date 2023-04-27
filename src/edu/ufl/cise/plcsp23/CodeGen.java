package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.ImageOps;
import edu.ufl.cise.plcsp23.runtime.PixelOps;
//import java.lang.*;
//import java.lang.reflect.Parameter;
import static edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.List;

public class CodeGen implements ASTVisitor {
    String packageName ;
    CodeGen (String pName){
        packageName = pName;
    }
    Program p;
    boolean b = false;

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        p = program;
        sB.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO; \n");
        sB.append("import edu.ufl.cise.plcsp23.runtime.PixelOps; \n");
        sB.append("import edu.ufl.cise.plcsp23.runtime.ImageOps; \n");
        sB.append("import java.awt.image.BufferedImage; \n");
        sB.append("import edu.ufl.cise.plcsp23.runtime.FileURLIO; \n");
        sB.append("import java.lang.*; \n");
        sB.append("import java.lang.Math; \n \n");

        sB.append("public class ");
        program.getIdent().visit(this, arg); //gets the name
        sB.append(program.getIdent().getName());

        sB.append(" { \n");
        sB.append("\t public static ");

        program.getType();

        String s = program.getType().name().toLowerCase().toString().replaceAll("string", "String");
        s = s.replaceAll("image","BufferedImage");
        s = s.replaceAll("pixel","int");
        if (s == "String"){
            b = true;
        }

        sB.append(s);

        sB.append(" apply(");

        List<NameDef> pL = program.getParamList();
        for (int i = 0; i < pL.size(); i++) {
            pL.get(i).visit(this, arg);
            String si = pL.get(i).getType().toString().toLowerCase().replaceAll("string", "String");
            si = si.replaceAll("image","BufferedImage");
            si = si.replaceAll("pixel","int");
            if (s == "String"){
                b = true;
            }

            sB.append(si + " ");

            sB.append(pL.get(i).getIdent().getName().toLowerCase()+"_" +program.getIdent().decNumber);
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
            if(!(sL.get(i) instanceof WhileStatement)){
                sB.append("; \n");
            }
            else{
                sB.append("\n");
            }

        }

        return sB.toString();
        // return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        Type l = declaration.getNameDef().getType();

        declaration.getNameDef().visit(this,arg);
        String s = declaration.getNameDef().getType().name().toLowerCase().replaceAll("string", "String");
        s = s.replaceAll("image","BufferedImage");
        s = s.replaceAll("pixel","int");


        if (s == "String"){b = true;}
        sB.append(s + " ");


        sB.append(declaration.getNameDef().getIdent().getName()+"_"+declaration.getNameDef().decNumber);

        if(declaration.getInitializer() != null){
            Type r = declaration.getInitializer().getType();
            sB.append(" = ");
            if(l == Type.STRING && r == Type.INT){
                sB.append("String.valueOf(" + declaration.getInitializer().visit(this,arg) + ")");
            } else if(l == Type.STRING && r == Type.PIXEL){
                sB.append("PixelOps.packedToString(" + declaration.getInitializer().visit(this,arg) + ")");
            }
            else if (l == Type.IMAGE) {
                //image with no dimension
                if (declaration.getNameDef().getDimension() == null) {
                    if (r == Type.STRING) {
                        sB.append("FileURLIO.readImage(");
                        sB.append(declaration.getInitializer().visit(this, arg) + ")");
                    }
                    else if (r == Type.IMAGE) {
                        sB.append("ImageOps.cloneImage(");
                        sB.append(declaration.getInitializer().visit(this, arg) + ")");
                    }
                }
                //image with dimension
                else if (declaration.getNameDef().getDimension() != null) {
                    //default image
                    /*sB.append("ImageOps.makeImage(");
                    sB.append(declaration.getNameDef().getDimension().getWidth().visit(this, arg) + ",");
                    sB.append(declaration.getNameDef().getDimension().getHeight().visit(this, arg) + "); \n");*/

                    if (r == Type.STRING) {
                        sB.append("FileURLIO.readImage(");
                        sB.append(declaration.getInitializer().visit(this, arg) + ",");
                        sB.append(declaration.getNameDef().getDimension().getWidth().visit(this, arg) + ",");
                        sB.append(declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");
                    } else if (r == Type.IMAGE) {
                        sB.append("ImageOps.copyAndResize(");
                        sB.append(declaration.getInitializer().visit(this, arg) + ",");
                        sB.append(declaration.getNameDef().getDimension().getWidth().visit(this, arg) + ",");
                        sB.append(declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");
                    }else{
                        sB.append("ImageOps.makeImage(");
                        sB.append(declaration.getNameDef().getDimension().getWidth().visit(this, arg) + ",");
                        sB.append(declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");

                    }
                }
            } else {
                    sB.append(declaration.getInitializer().visit(this,arg));
            }
        } else if (declaration.getInitializer() == null) {
            if(l == Type.IMAGE && declaration.getNameDef().getDimension()!= null){
                    //default image
                sB.append(" = ");
                sB.append("ImageOps.makeImage(");
                sB.append(declaration.getNameDef().getDimension().getWidth().visit(this, arg) + ",");
                sB.append(declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");
            }

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
        StringBuilder sB = new StringBuilder();

        Type pExpr = unaryExprPostfix.getPrimary().getType();
        boolean p = false;
        boolean c = false;
        if(unaryExprPostfix.getPixel() != null){p = true;}
        if(unaryExprPostfix.getColor() != null){c = true;}

        if(pExpr == Type.IMAGE){
            //HELP IS SYNTAX CORRECT?
            //p, no c
            if(p && !c){
                sB.append("ImageOps.getRGB(");
                sB.append(unaryExprPostfix.getPrimary().visit(this,arg) + ",");
                sB.append(unaryExprPostfix.getPixel().getX().visit(this,arg) + ",");
                sB.append(unaryExprPostfix.getPixel().getY().visit(this,arg) + ")");
            } //p & c
            else if(p && c){
                ColorChannel color = unaryExprPostfix.getColor();
                //red, green , blue
                if(color == ColorChannel.red){sB.append("PixelOps.red(ImageOps.getRGB(");}
                else if(color == ColorChannel.grn){sB.append("PixelOps.grn(ImageOps.getRGB(");}
                else if(color == ColorChannel.blu){sB.append("PixelOps.blu(ImageOps.getRGB(");}

                sB.append(unaryExprPostfix.getPrimary().visit(this,arg) + ",");
                sB.append(unaryExprPostfix.getPixel().getX().visit(this,arg) + ",");
                sB.append(unaryExprPostfix.getPixel().getY().visit(this,arg) + "))");
            }
            //no p c
            else if (!p && c) {
                ColorChannel color = unaryExprPostfix.getColor();
                //red, green, blue
                if(color == ColorChannel.red){sB.append("ImageOps.extractRed(");}
                else if(color == ColorChannel.grn){sB.append("ImageOps.extractGrn(");}
                else if(color == ColorChannel.blu){sB.append("ImageOps.extractBlu(");}

                sB.append(unaryExprPostfix.getPrimary().visit(this,arg) + ")");
            }

        }
        else if (pExpr == Type.PIXEL) {
            if (!p && c) {
                ColorChannel color = unaryExprPostfix.getColor();
                //red, green,blue
                if (color == ColorChannel.red) {sB.append("PixelOps.red(");}
                else if (color == ColorChannel.grn) {sB.append("PixelOps.grn(");}
                else if (color == ColorChannel.blu) {sB.append("PixelOps.blu(");}
                sB.append(unaryExprPostfix.getPrimary().visit(this, arg) + ")");
            }
        }

        return sB.toString();
    }
    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }
    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        if(predeclaredVarExpr.getKind() == Kind.RES_x){
            sB.append("x");
        } else if(predeclaredVarExpr.getKind() == Kind.RES_y){
            sB.append("y");
        }else if(predeclaredVarExpr.getKind() == Kind.RES_a){
            sB.append("a");
        }else if(predeclaredVarExpr.getKind() == Kind.RES_r){
            sB.append("r");
        }
        return sB.toString();
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

        Type expr0 = binaryExpr.getLeft().getType();
        Type expr1 = binaryExpr.getRight().getType();

        Kind op = binaryExpr.getOp();
        String opStore = "";
        ImageOps.OP op1 = null;
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
            case PLUS ->{opStore = "+"; op1 = ImageOps.OP.PLUS;}
            // =
            case MINUS ->{opStore = "-"; op1 = ImageOps.OP.MINUS;}
            // =
            case TIMES ->{opStore = "*"; op1 = ImageOps.OP.TIMES;}
            // =
            case DIV ->{opStore = "/";op1 = ImageOps.OP.DIV;}
            // =
            case MOD ->{opStore = "%";op1 = ImageOps.OP.MOD;}
            //**
            case EXP ->opStore = "(int)Math.pow";
        }
        if(opStore == "(int)Math.pow"){
            sB.append(opStore + "(" + binaryExpr.getLeft().visit(this,arg) +"," + binaryExpr.getRight().visit(this,arg) + ")");
            sB.append(")");

        }
        // if op < > <= >= == && || //itok
        else if (opStore == "||" || opStore == "&&"){
            //convert to bool then convert to int
            sB.append("(" + binaryExpr.getLeft().visit(this,arg) + ")");
            sB.append(" != 0 ");
            sB.append(" " + opStore + " ");
            sB.append("(" + binaryExpr.getRight().visit(this,arg) + ")");
            sB.append(" != 0 ? "); // only case
            sB.append(" 1 : 0)");
        }
        else if(opStore == ">" || opStore == "<" || opStore == ">="|| opStore == "<=" || opStore == "=="){
            sB.append(binaryExpr.getLeft().visit(this,arg));
            sB.append(" " + opStore + " ");
            sB.append(binaryExpr.getRight().visit(this,arg));
            sB.append(" ? 1 : 0");
            sB.append(")");
        }
        else if ((opStore == "+" || opStore == "-" ||opStore == "*" ||opStore == "/" ||opStore == "%" ) &&  (expr0 == Type.IMAGE ||expr0 == Type.PIXEL)) {
            if(expr0 == Type.IMAGE && expr1 == Type.IMAGE) {
                sB.append("ImageOps.binaryImageImageOp(" +"ImageOps.OP." + op1 + ",");
            }
            else if (expr0 == Type.IMAGE && expr1 == Type.INT ) {
                sB.append("ImageOps.binaryImageScalarOp(" +"ImageOps.OP." + op1 + ",");
            }
            else if (expr0 == Type.PIXEL && expr1 == Type.PIXEL){
                sB.append("ImageOps.binaryPackedPixelPixelOp(" + "ImageOps.OP." + op1 + ",");
            }
            else if (expr0 == Type.PIXEL && expr1 == Type.INT){
                sB.append("ImageOps.binaryPackedPixelIntOp(" + "ImageOps.OP." + op1 + ",");
            }
            sB.append(binaryExpr.getLeft().visit(this, arg) + ",");
            sB.append(binaryExpr.getRight().visit(this, arg) + "))");
        }
        else {
            sB.append(binaryExpr.getLeft().visit(this,arg));
            sB.append(" " + opStore + " "); //something that returns an string //switch statement
            sB.append(binaryExpr.getRight().visit(this,arg));
            sB.append(")");
        }

        return sB.toString();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();

        Kind op = unaryExpr.getOp();
        Type unaryType = unaryExpr.getE().getType();

        switch(op) {
            //!,-
            case BANG -> {
                if (unaryType == Type.INT) {
                    sB.append("((");
                    sB.append(unaryExpr.getE().visit(this,arg));
                    sB.append(" == 0) ? "); // only case
                    sB.append(" 1 : 0)");
                }
            } //HELP what's the minus do? is it implemented correctly?
            case MINUS->{
                if(unaryType == Type.INT) {
                    sB.append("(-");
                    //visiting should append it
                    sB.append(unaryExpr.getE().visit(this,arg) + ")");
                }
            }
        }


        return sB.toString();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("\"" + stringLitExpr.getValue() + "\"");
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

        //use a wrapper integer function
        sB.append((int)Math.floor(Math.random() *256));
        //HELP  what does it mean when it says will require import
        return sB.toString();
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(pixelSelector.getX().visit(this,arg) + ",");
        sB.append(pixelSelector.getY().visit(this,arg));
        return sB.toString();
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append("PixelOps.pack(");
        sB.append(expandedPixelExpr.getRedExpr().visit(this,arg) + ",");
        sB.append(expandedPixelExpr.getGrnExpr().visit(this,arg) + ",");
        sB.append(expandedPixelExpr.getBluExpr().visit(this,arg));
        sB.append(")");
        return sB.toString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        sB.append(dimension.getHeight().visit(this,arg));
        sB.append(" , ");
        sB.append(dimension.getWidth().visit(this,arg));

        return sB.toString();
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
        Type l = statementAssign.getLv().getIdent().getDef().getType();
        Type r = statementAssign.getE().getType();


        //sB.append(" = ");
        if (l == Type.STRING && r == Type.INT){
            sB.append(statementAssign.getLv().visit(this,arg));
            sB.append(" = ");
            sB.append("String.valueOf(" + statementAssign.getE().visit(this,arg) + ")");
        } else if(l == Type.STRING && r == Type.PIXEL){
            sB.append(statementAssign.getLv().visit(this,arg));
            sB.append(" = ");
            sB.append("PixelOps.packedToString(" + statementAssign.getE().visit(this,arg) + ")");
        }

        else if (l == Type.PIXEL) {
            sB.append(statementAssign.getLv().visit(this,arg));
            sB.append(" = ");
            sB.append(statementAssign.getE().visit(this,arg));
        }
        else if (l == Type.IMAGE) {
            boolean p = false;
            boolean c = false;
            if (statementAssign.getLv().getPixelSelector() != null){ p = true;}
            if (statementAssign.getLv().getColor() != null){ c = true;}

           //no p and no c
            if(!p && !c){
                //HELP WHAT"S happenign here
                if(r == Type.STRING){
                    //HELP where are we reading it in from?
                    //sB.append(" = ");
                    sB.append("ImageOps.copyInto(FileURLIO.readImage(" + statementAssign.getE().visit(this,arg));
                    sB.append("), " +  statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ")");
                } else if ( r == Type.IMAGE) {
                    //sB.append(" = ");
                    sB.append("ImageOps.copyInto(" + statementAssign.getE().visit(this,arg));
                    sB.append(", " +  statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ")");
                } else if (r == Type.PIXEL) {
                    sB.append("ImageOps.setAllPixels(" +  statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber);
                    sB.append("," + statementAssign.getE().visit(this,arg) + ")");
                }
            } else if (p && !c) {
               // sB.append("int hshift = 0; \n" + "int vshift = (h/2); \n");
                sB.append("for(int y = 0; y !=" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ".getHeight(); y++) { \n \t\t") ;
                sB.append("for(int x = 0; x !=" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ".getWidth(); x++) { \n \t\t");
                sB.append("ImageOps.setRGB(" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ",");
                sB.append(statementAssign.getLv().getPixelSelector().visit(this,arg) + ",");
                sB.append(statementAssign.getE().visit(this,arg)); //visit UEPF
                sB.append("); \n\t\t } \t\t\n }");

            } else if (p && c) {
                ColorChannel color = statementAssign.getLv().getColor();
                String temp = "";
                if(color == ColorChannel.red){temp = "PixelOps.setRed(";}
                if(color == ColorChannel.grn){temp = "PixelOps.setGrn(";}
                if(color == ColorChannel.blu){temp = "PixelOps.setBlu(";}

                sB.append("for(int y = 0; y !=" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ".getHeight(); y++) { \n \t\t") ;
                sB.append("for(int x = 0; x !=" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ".getWidth(); x++) { \n\t\t");
                sB.append("ImageOps.setRGB(" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ",");
                sB.append(statementAssign.getLv().getPixelSelector().visit(this,arg));
                sB.append("," + temp + " ImageOps.getRGB(" + statementAssign.getLv().getIdent().getName()+"_"+ statementAssign.decNumber + ",");
                sB.append(statementAssign.getLv().getPixelSelector().getX().visit(this,arg) + ",");
                sB.append(statementAssign.getLv().getPixelSelector().getY().visit(this,arg) + ")," + "255));");
                sB.append("\n} \n}");
            }
        } else {
            sB.append(statementAssign.getLv().visit(this,arg));
            sB.append(" = ");
            sB.append(statementAssign.getE().visit(this,arg));
        }
        return sB.toString();
    }
    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        StringBuilder sB = new StringBuilder();
        // HELP is this correct
        //sB.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;");
        if(statementWrite.getE().getType() == Type.PIXEL){
            sB.append("ConsoleIO.writePixel(");
        } else {
            sB.append("ConsoleIO.write(");
        }

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
        sB.append(" return ");
        Type t = p.getType();
        Type ty = returnStatement.getE().getType();
        //HELP WHAT DO WE DO HERE
        if(t == Type.STRING && ty == Type.INT){
            sB.append("String.valueOf(" + returnStatement.getE().visit(this,arg) + ")");
        } else if(t == Type.STRING && ty == Type.PIXEL){
            sB.append("PixelOps.packedToString(" + returnStatement.getE().visit(this,arg) + ")");
        } else if(t == Type.STRING && ty == Type.IMAGE){
            sB.append("String.valueOf(" + returnStatement.getE().visit(this,arg) + ")");
        } else {
            sB.append(returnStatement.getE().visit(this, arg));
        }
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