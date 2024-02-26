package Tools;

import GrammarAnalyse.CompNode;
import IntermediateCode.Container.FuncBlock;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.FuncOperand;
import ObjectCode.Instruction.PrimaryInstruction;
import WordAnalyse.Word;

import java.io.*;
import java.util.*;

public class FileProcess {
    public static ArrayList<String> ReadInputFile(String FileName) {
        ArrayList<String> Lines = new ArrayList<>();
        try {
            File file = new File(FileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String Aline;
            while ( (Aline = bufferedReader.readLine()) != null) {
                Lines.add(Aline);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Lines;
    }

    public static void WriteWordAnalyseResultFile(String FileName, ArrayList<Word> words,boolean append) {
        if (!GlobalSetting.FileWordAnalyzerResult) {
            return;
        }
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
            for (Word word : words) {
                bufferedWriter.write(word.getPrintCategoryCode() + " " + word.OriginWord + '\n');
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteGrammarAnalyseResultFile(String FileName, ArrayList<String> writeBuffer,boolean append) {
        if (GlobalSetting.PrintGrammarAnalyzerResult) {
            writeBuffer.forEach(System.out::println);
        }
        if (!GlobalSetting.FileGrammarAnalyzerResult) {
            return;
        }
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
            for (String stmt: writeBuffer) {
                bufferedWriter.write(stmt + '\n');
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteReserveGenerateSourceCode(String FileName, CompNode GrammerAnalyzer, boolean append) {
        if (GlobalSetting.PrintReserveGenerateSourceCode) {
            StringBuilder stringBuilder = new StringBuilder();
            GrammerAnalyzer.treePrint(stringBuilder);
            System.out.println(stringBuilder);
        }
        if (!GlobalSetting.FileReserveGenerateSourceCode) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        GrammerAnalyzer.treePrint(stringBuilder);
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void TestFunction(String FileName, ArrayList<String> writeBuffer, ArrayList<Combination<Integer,String>> errorMessage) {
        if (!errorMessage.isEmpty()) {
            WriteErrorMessageFile("output.txt",errorMessage,false);
            return;
        }
        if (!GlobalSetting.FileGrammarAnalyzerResult) {
            return;
        }
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String stmt: writeBuffer) {
                bufferedWriter.write(stmt + '\n');
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteErrorMessageFile(String FileName,ArrayList<Combination<Integer,String>> errorMessage,boolean append) {
        if(!GlobalSetting.FileErrorMessage || errorMessage.isEmpty()) {
            return;
        }
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
            errorMessage.sort((o1, o2) -> !Objects.equals(o1.getKey(), o2.getKey()) ? o1.getKey() - o2.getKey() : o1.getValue().charAt(0) - o2.getValue().charAt(0));
            for (Combination<Integer,String> combination : errorMessage) {
                bufferedWriter.write(combination.getKey() + " " + combination.getValue() + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteOriginIntermediateCodeFile(String FileName, IntermediateBuilder builder, boolean append) {
        ArrayList<String> output = new ArrayList<>();
        HashMap<FuncOperand, FuncBlock> func = builder.getFunctionMap();
        ArrayList<PrimaryElement> GlobalElements = builder.getGlobalBlock().IntermediateExpression;
        if (!GlobalElements.isEmpty()) {
            output.add("GlobalBlock:\n");
        }
        GlobalElements.forEach(primaryElement -> output.add("\t" + primaryElement.toString() + '\n'));
        output.add("\n");
        func.forEach((funcOperand, funcBlock) -> {
            output.add(funcOperand.getOperandName() + ":\n");
            ArrayList<PrimaryElement> elements = funcBlock.getIntermediateExpression();
            elements.forEach(primaryElement -> output.add("\t" + primaryElement.toString() + '\n'));
            output.add("\n");
        });
        if (GlobalSetting.PrintIntermediateCode) {
            output.forEach(System.out::println);
        }
        if (GlobalSetting.FileIntermediateCode) {
            try {
                File file = new File(FileName);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
                for (String s : output) {
                    bufferedWriter.write(s);
                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void WriteSSAIntermediateCodeFile(String FileName, IntermediateBuilder builder, boolean append) {
        ArrayList<String> output = new ArrayList<>();
        HashMap<FuncOperand, FuncBlock> func = builder.getFunctionMap();
        ArrayList<PrimaryElement> GlobalElements = builder.getGlobalBlock().IntermediateExpression;
        if (!GlobalElements.isEmpty()) {
            output.add("GlobalBlock:\n");
        }
        GlobalElements.forEach(primaryElement -> output.add("\t" + primaryElement.toString() + '\n'));
        output.add("\n");
        func.forEach((funcOperand, funcBlock) -> {
            output.add(funcOperand.getOperandName() + ":\n");
            funcBlock.getNormalBlocks().forEach(normalBlock -> {
                output.add("\tNormalBlock " + normalBlock.toString() + ":\n");
                normalBlock.IntermediateExpression.forEach(primaryElement -> output.add("\t\t" + primaryElement.toString() + '\n'));
                output.add("\n");
            });
            output.add("\n");
        });
        if (GlobalSetting.PrintIntermediateCode) {
            output.forEach(System.out::println);
        }
        if (GlobalSetting.FileIntermediateCode) {
            try {
                File file = new File(FileName);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
                for (String s : output) {
                    bufferedWriter.write(s);
                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void WriteObjectCodeFile(String FileName, ArrayList<PrimaryInstruction> objectCode,boolean append) {
        if (GlobalSetting.PrintObjectCode) {
            StringBuilder sb = new StringBuilder();
            for (PrimaryInstruction instruction : objectCode) {
                sb.append("\t".repeat(Math.max(0, instruction.getDeep())));
                sb.append(instruction).append("\n");
                System.out.println(sb);
            }
        }
        if(!GlobalSetting.FileObjectCode) {
            return;
        }
        try {
            File file = new File(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,append));
            for (PrimaryInstruction instruction : objectCode) {
                for (int i = 0; i < instruction.getDeep(); i++) {
                    bufferedWriter.write("\t");
                }
                bufferedWriter.write(instruction + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
