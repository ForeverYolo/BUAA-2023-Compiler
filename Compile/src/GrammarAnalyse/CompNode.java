package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class CompNode extends TreeNode {
    private SymbolTable symbolTable;
    private final ArrayList<DeclNode> declChildren;
    private final ArrayList<FuncDefNode> funcDefChildren;
    private MainFuncDefNode mainFuncDefChildren;
    public CompNode(TreeNode Father,int deep) {
        super(Father,deep);
        declChildren = new ArrayList<>();
        funcDefChildren = new ArrayList<>();
        mainFuncDefChildren = null;
    }

    public void addDeclChildren(DeclNode children) {
        this.declChildren.add(children);
    }

    public void addFuncDefChildren(FuncDefNode children) {
        this.funcDefChildren.add(children);
    }

    public void setMainFuncDefChildren(MainFuncDefNode children) {
        this.mainFuncDefChildren = children;
    }

    public ArrayList<DeclNode> getDeclChildren() {
        return declChildren;
    }

    public ArrayList<FuncDefNode> getFuncDefChildren() {
        return funcDefChildren;
    }

    public MainFuncDefNode getMainFuncDefChildren() {
        return mainFuncDefChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        for (DeclNode declNode : declChildren) {
            declNode.treePrint(stringBuilder);
        }
        for (FuncDefNode funcDefNode: funcDefChildren) {
            funcDefNode.treePrint(stringBuilder);
        }
        mainFuncDefChildren.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        if (!declChildren.isEmpty()) {
            return declChildren.get(0).getThisNodeLine();
        } else if (!funcDefChildren.isEmpty()) {
            return funcDefChildren.get(0).getThisNodeLine();
        } else {
            return mainFuncDefChildren.getThisNodeLine();
        }
    }

    public void RunCompUnitParser() {
        if (!GlobalSetting.RunGrammarAnalyzerResult) {
            return;
        }
        // {Decl}
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("CONSTTK")) {
                WordProvider.RollBackWord(1);
                DeclNode declNode = new DeclNode(this,printDeep);
                this.addDeclChildren(declNode);
                declNode.RunDeclParser();
            } else if (word != null && word.getCategoryCode().equals("INTTK")) {
                word = WordProvider.GetNextWord();
                if (word != null && word.getCategoryCode().equals("IDENFR")) {
                    word = WordProvider.GetNextWord();
                    if (word != null && !word.getCategoryCode().equals("LPARENT")) {
                        WordProvider.RollBackWord(3);
                        DeclNode declNode = new DeclNode(this,printDeep);
                        this.addDeclChildren(declNode);
                        declNode.RunDeclParser();
                    } else {
                        WordProvider.RollBackWord(3);
                        break;
                    }
                } else {
                    WordProvider.RollBackWord(2);
                    break;
                }
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        // {FuncDef}
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("VOIDTK") || word.getCategoryCode().equals("INTTK"))) {
                word = WordProvider.GetNextWord();
                if (word != null && word.getCategoryCode().equals("IDENFR")) {
                    WordProvider.RollBackWord(2);
                    FuncDefNode funcDefNode = new FuncDefNode(this,printDeep);
                    this.addFuncDefChildren(funcDefNode);
                    funcDefNode.RunFuncDefParser();
                } else {
                    WordProvider.RollBackWord(2);
                    break;
                }
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        //MainFuncDef
        MainFuncDefNode mainFuncDefNode = new MainFuncDefNode(this,printDeep);
        this.setMainFuncDefChildren(mainFuncDefNode);
        mainFuncDefNode.RunMainFuncDefParser();
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<CompUnit>");
    }


    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        if (!GlobalSetting.RunSymbolBuilder) {
            return;
        }
        this.symbolTable = symbolTable; //赋表
        for(DeclNode declNode:declChildren) {
            declNode.RunSymbolBuilder(symbolTable,null);
        }
        for(FuncDefNode funcDefNode:funcDefChildren) {
            funcDefNode.RunSymbolBuilder(symbolTable,null);
        }
        mainFuncDefChildren.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        if (!GlobalSetting.RunIntermediateCode) {
            return;
        }
        for(DeclNode declNode : declChildren) {
            declNode.ToIntermediate(intermediateBuilder,symbolTable);
        }
        for(FuncDefNode funcDefNode : funcDefChildren) {
            funcDefNode.ToIntermediate(intermediateBuilder,symbolTable);
        }
        mainFuncDefChildren.ToIntermediate(intermediateBuilder,symbolTable);

    }
}
