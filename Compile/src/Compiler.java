

import GrammarAnalyse.CompNode;
import IntermediateCode.IntermediateBuilder;
import ObjectCode.Assembly;
import Optimize.Optimizer;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.FileProcess;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;
import WordAnalyse.WordAnalyzer;

import java.util.ArrayList;


public class Compiler {
    public static void main(String[] args) {
        GlobalSetting.setRunObjectCodeWithErrorProcessOpt(false);
        ArrayList<String> Lines = FileProcess.ReadInputFile("testfile.txt");
        ArrayList<Word> WordAnalyzerResult = new WordAnalyzer().RunWordAnalyzer(Lines);
        FileProcess.WriteWordAnalyseResultFile("output.txt",WordAnalyzerResult,false);
        WordProvider.PrintWordAnalyzerResult(WordAnalyzerResult);
        //--------------------------------------------词法分析----------------------------------------------
        WordProvider.setWordProvider(WordAnalyzerResult);
        CompNode GrammerAnalyzer = new CompNode(null,0);
        GrammerAnalyzer.RunCompUnitParser();
        FileProcess.WriteGrammarAnalyseResultFile("output.txt",WordProvider.WriteBuffer,false);
        FileProcess.WriteReserveGenerateSourceCode("ReverseSourceCode.cpp",GrammerAnalyzer,false);
        //--------------------------------------------语法分析----------------------------------------------
        SymbolTable GlobalSymbolTable = new SymbolTable(null);
        GrammerAnalyzer.RunSymbolBuilder(GlobalSymbolTable,null);
        FileProcess.WriteErrorMessageFile("error.txt", ErrorMessage.getErrorMessage(),false);
        GlobalSymbolTable.ResetSymbolTableState();
        if (!ErrorMessage.getErrorMessage().isEmpty()) {System.exit(0);}
        //---------------------------------------错误处理、符号表生成------------------------------------------
        IntermediateBuilder intermediateBuilder = new IntermediateBuilder();
        GrammerAnalyzer.ToIntermediate(intermediateBuilder,GlobalSymbolTable);
        intermediateBuilder.DivideFuncBlock();
        FileProcess.WriteOriginIntermediateCodeFile("OriginIntermediateCode.txt",intermediateBuilder,false);
        //-----------------------------------------中间代码生成-----------------------------------------------
        Assembly assembly = new Assembly();
        Optimizer optimizer = new Optimizer(intermediateBuilder,assembly);
        optimizer.ProcessOptimize();
        FileProcess.WriteSSAIntermediateCodeFile("OptimizedIntermediateCode.txt",intermediateBuilder,false);
        //-------------------------------------------中端优化-----------------------------------------------
        intermediateBuilder.toAssembly(assembly);
        FileProcess.WriteObjectCodeFile("mips.txt",assembly.getObjectCode(),false);
        //-----------------------------------------目标代码生成-----------------------------------------------
    }
}