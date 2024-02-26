package Tools;

public class GlobalSetting {
    // 运行词法分析
    public static boolean RunWordAnalyzerResult = true;
    // 标准输出打印词法分析结果
    public static boolean PrintWordAnalyzerResult = false;
    // 文件输出词法分析结果
    public static boolean FileWordAnalyzerResult = false;
    // 运行语法分析
    public static boolean RunGrammarAnalyzerResult = true;
    // 标准输出打印语法分析结果
    public static boolean PrintGrammarAnalyzerResult = false;
    // 文件输出语法分析结果
    public static boolean FileGrammarAnalyzerResult = true;
    // 文件输出反向源代码生成
    public static boolean FileReserveGenerateSourceCode = false;
    // 标准输出打印反向源代码生成
    public static boolean PrintReserveGenerateSourceCode = false;
    // 标准输出打印错误信息
    public static boolean RunSymbolBuilder = false;
    public static boolean PrintErrorMessage = false;
    // 文件输出错误信息
    public static boolean FileErrorMessage = false;
    // 标准输出打印中间代码
    public static boolean RunIntermediateCode = false;
    public static boolean PrintIntermediateCode = false;
    // 文件输出中间代码
    public static boolean FileIntermediateCode = false;
    // 文件输出目标函数
    public static boolean RunObjectCode = false;
    public static boolean FileObjectCode = false;
    // 标准输出打印目标函数
    public static boolean PrintObjectCode = false;
    public static boolean Optimize = true;


    // 比较指令选择优化
    public static boolean CompareInstSelectOptimize = false;
    // 乘除优化
    public static boolean MulDivOptimize = true;
    // 函数内联
    public static boolean FunctionInLineOptimize = true;
    // 流图构建
    public static boolean ControlFlowGraphBuild = true;
    // SSA构建
    public static boolean SSABuild = true;
    // 激进死代码删除
    public static boolean AggressiveDeadCodeDeleteOptimize = true;
    // GVN
    public static boolean GlobalValueNumberingOptimize = true;
    // GCM
    public static boolean GlobalCodeMotionOptimize = true;
    // 全局变量合并
    public static boolean MergeSpecifyVarOptimize = true;
    // 消除SSA形式
    public static boolean RemoveSSA = true;
    // 图着色+局部Opt分配优化
    public static boolean RegisterOptimize = true;

    public static void CloseAllOptimize() {
        CompareInstSelectOptimize = false;
        MulDivOptimize = false;
        FunctionInLineOptimize = false;
        ControlFlowGraphBuild = false;
        SSABuild = false;
        AggressiveDeadCodeDeleteOptimize = false;
        GlobalValueNumberingOptimize = false;
        GlobalCodeMotionOptimize = false;
        MergeSpecifyVarOptimize = false;
        RemoveSSA = false;
        RegisterOptimize = false;
    }

    public static void OpenAllOptimize() {
        CompareInstSelectOptimize = true;
        MulDivOptimize = true;
        FunctionInLineOptimize = true;
        ControlFlowGraphBuild = true;
        SSABuild = true;
        AggressiveDeadCodeDeleteOptimize = true;
        GlobalValueNumberingOptimize = true;
        GlobalCodeMotionOptimize = true;
        MergeSpecifyVarOptimize = true;
        RemoveSSA = true;
        RegisterOptimize = true;
    }

    public void setPrintWordAnalyzerResult(boolean setting) {
        PrintWordAnalyzerResult = setting;
    }

    public static void setPrintGrammarAnalyzerResult(boolean printGrammarAnalyzerResult) {
        PrintGrammarAnalyzerResult = printGrammarAnalyzerResult;
    }


    public static void setRunWordAnalyzeMode(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        if (Debug) {
            PrintWordAnalyzerResult = true;
        }
        FileWordAnalyzerResult = true;
    }

    public static void setRunGrammarAnalyze(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        if (Debug) {
            PrintGrammarAnalyzerResult = true;
            PrintReserveGenerateSourceCode = true;
            FileReserveGenerateSourceCode = true;
        }
        FileGrammarAnalyzerResult = true;
    }

    public static void setRunErrorProcess(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        RunSymbolBuilder = true;
        if (Debug) {
            PrintErrorMessage = true;
        }
        FileErrorMessage = true;
    }


    public static void setRunIntermediateCode(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        RunSymbolBuilder = true;
        RunIntermediateCode = true;
        if (Debug) {
            PrintIntermediateCode = true;
        }
        FileIntermediateCode = true;
    }


    public static void setRunObjectCode(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        RunSymbolBuilder = true;
        RunIntermediateCode = true;
        RunObjectCode = true;
        if (Debug) {
            PrintObjectCode = true;
        }
        FileIntermediateCode = true;
        FileObjectCode = true;
    }

    public static void setRunObjectCodeWithErrorProcessNoOpt(boolean Debug) {
        setAllSettingFalse();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        RunSymbolBuilder = true;
        RunIntermediateCode = true;
        RunObjectCode = true;
        if (Debug) {
            PrintErrorMessage = true;
            PrintObjectCode = true;
            FileIntermediateCode = true;
        }
        FileErrorMessage = true;
        FileObjectCode = true;
    }

    public static void setRunObjectCodeWithErrorProcessOpt(boolean Debug) {
        setAllSettingFalse();
        OpenAllOptimize();
        RunWordAnalyzerResult = true;
        RunGrammarAnalyzerResult = true;
        RunSymbolBuilder = true;
        RunIntermediateCode = true;
        RunObjectCode = true;
        if (Debug) {
            PrintErrorMessage = true;
            PrintObjectCode = true;
            FileIntermediateCode = true;
        }
        FileErrorMessage = true;
        FileObjectCode = true;
    }

    public static void setAllSettingFalse() {
        RunWordAnalyzerResult = false;
        PrintWordAnalyzerResult = false;
        FileWordAnalyzerResult = false;
        RunGrammarAnalyzerResult = false;
        PrintGrammarAnalyzerResult = false;
        FileGrammarAnalyzerResult = false;
        FileReserveGenerateSourceCode = false;
        PrintReserveGenerateSourceCode = false;
        RunSymbolBuilder = false;
        PrintErrorMessage = false;
        FileErrorMessage = false;
        RunIntermediateCode = false;
        PrintIntermediateCode = false;
        FileIntermediateCode = false;
        RunObjectCode = false;
        FileObjectCode = false;
        PrintObjectCode = false;
        CloseAllOptimize();
    }
}
