package Tools;

import GrammarAnalyse.*;
import WordAnalyse.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class ErrorMessage {
    private static final ArrayList<Combination<Integer,String>> errorMessage = new ArrayList<>();
    private static final HashMap<String,String> errorIDTransform = new HashMap<>(){
        {
            put("a","Invalid Symbol");
            put("b","Redefine Name");
            put("c","Undefine Name");
            put("d","Func Number Of Params Unmatched");
            put("e","Func type Of Params Unmatched");
            put("f","Void Func Exist Return Stmt");
            put("g","Int Func Miss Return Stmt");
            put("h","Try To Change Const Value");
            put("i","Missing SEMICN -> ';'");
            put("j","Missing RPARENT -> ')'");
            put("k","Missing RBRACK -> ']'");
            put("l","Printf Number Of Params Unmatched");
            put("m","Use Break/Continue In Uncirculate Block");
        }
    };
    public static final HashMap<Combination<Class<? extends TreeNode>, Integer>, Consumer<Word>> hanlders = new HashMap<>(){
        {
            // --------------------------------------------PrintfNode---------------------------------------------------
            put(new Combination<>(PrintfNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"l"));
            put(new Combination<>(PrintfNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"a"));
            put(new Combination<>(PrintfNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(PrintfNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //----------------------------------------------BreakNode---------------------------------------------------
            put(new Combination<>(BreakNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"m"));
            put(new Combination<>(BreakNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //---------------------------------------------ConstDeclNode------------------------------------------------
            put(new Combination<>(ConstDeclNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //---------------------------------------------ConstDefNode-------------------------------------------------
            put(new Combination<>(ConstDefNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"k"));
            put(new Combination<>(ConstDefNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
            put(new Combination<>(ConstDefNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
            //---------------------------------------------ContinueNode-------------------------------------------------
            put(new Combination<>(ContinueNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"m"));
            put(new Combination<>(ContinueNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //---------------------------------------------ForLoopNode--------------------------------------------------
            put(new Combination<>(ForLoopNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,4), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,5), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,6), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ForLoopNode.class,7), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(ForLoopNode.class,8), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            //---------------------------------------------FuncCallNode-------------------------------------------------
            put(new Combination<>(FuncCallNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"c"));
            put(new Combination<>(FuncCallNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(FuncCallNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(FuncCallNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            put(new Combination<>(FuncCallNode.class,4), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            //---------------------------------------------FuncDefNode--------------------------------------------------
            put(new Combination<>(FuncDefNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(FuncDefNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
            put(new Combination<>(FuncDefNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"g"));
            put(new Combination<>(FuncDefNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            //--------------------------------------------FuncFParamNode------------------------------------------------
            put(new Combination<>(FuncFParamNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"k"));
            put(new Combination<>(FuncFParamNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"k"));
            put(new Combination<>(FuncFParamNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
            //--------------------------------------------FuncRParamsNode----------------------------------------------
            put(new Combination<>(FuncRParamsNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"d"));
            put(new Combination<>(FuncRParamsNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            put(new Combination<>(FuncRParamsNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            put(new Combination<>(FuncRParamsNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            //-----------------------------------------------IFNode-----------------------------------------------------
            put(new Combination<>(IFNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            //----------------------------------------------LvalNode----------------------------------------------------
            put(new Combination<>(LvalNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"k"));
            put(new Combination<>(LvalNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"c"));
            put(new Combination<>(LvalNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            //-----------------------------------------------NumberNode-------------------------------------------------
            put(new Combination<>(NumberNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"e"));
            //--------------------------------------------MainFuncDefNode-----------------------------------------------
            put(new Combination<>(MainFuncDefNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"g"));
            put(new Combination<>(MainFuncDefNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            //--------------------------------------------PrimaryExpNode------------------------------------------------
            put(new Combination<>(PrimaryExpNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            //--------------------------------------------ReturnNode----------------------------------------------------
            put(new Combination<>(ReturnNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"f"));
            put(new Combination<>(ReturnNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(ReturnNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //--------------------------------------------StmtLvalExpNode-----------------------------------------------
            put(new Combination<>(StmtLvalExpNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"c"));
            put(new Combination<>(StmtLvalExpNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"h"));
            //----------------------------------------------StmtNode----------------------------------------------------
            put(new Combination<>(StmtNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"j"));
            put(new Combination<>(StmtNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(StmtNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(StmtNode.class,3), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            put(new Combination<>(StmtNode.class,4), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //--------------------------------------------VarDeclNode---------------------------------------------------
            put(new Combination<>(VarDeclNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"i"));
            //--------------------------------------------VarDefNode----------------------------------------------------
            put(new Combination<>(VarDefNode.class,0), word -> ErrorMessage.AddErrorMessage(word.AtLine,"k"));
            put(new Combination<>(VarDefNode.class,1), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
            put(new Combination<>(VarDefNode.class,2), word -> ErrorMessage.AddErrorMessage(word.AtLine,"b"));
        }
    };

    public static void AddErrorMessage(int line,String error) {
        errorMessage.add(new Combination<>(line,error));
    }

    public static ArrayList<Combination<Integer,String>> getErrorMessage() {
        return errorMessage;
    }
    public static void handleError(Class<? extends TreeNode> Class,int errorIdInClass,Word word) {
        Combination<java.lang.Class<? extends TreeNode>, Integer> combination = new Combination<>(Class,errorIdInClass);
        boolean hasHandler = hanlders.containsKey(combination);
        if (hasHandler) {
            hanlders.get(combination).accept(word);
        }
        String errorMessage = hasHandler ? " Processed By Special Handler" : " Processed By Normal Handler";
        if (GlobalSetting.PrintErrorMessage) {
            System.err.println("Error! Which has been happened in " + Class + errorMessage);
        }
        if (!hasHandler) {
            System.err.println("Compiler Terminated");
            IOException e = new IOException();
            e.printStackTrace();
            System.exit(0);
        } else {
            String ErrorType = ErrorMessage.errorMessage.get(ErrorMessage.errorMessage.size() - 1).getValue();
            int ErrorLine = ErrorMessage.errorMessage.get(ErrorMessage.errorMessage.size() - 1).getKey();
            if (GlobalSetting.PrintErrorMessage) {
                System.err.println("     Error ID In Class: " + errorIdInClass + "\n     This Error ID: " + ErrorType + "\n     The Description is : " + ErrorMessage.errorIDTransform.get(ErrorType) + "\n     Error At Source Code Line : " + ErrorLine + "\n");
            }
        }
    }

    public static void handleSelfCheckError(Class Class) {
        System.err.println("An Unexpected Error Happend In Class: " + Class.getName());
        System.err.println("Compiler Terminated");
        IOException e = new IOException();
        e.printStackTrace();
        System.exit(0);
    }
}
