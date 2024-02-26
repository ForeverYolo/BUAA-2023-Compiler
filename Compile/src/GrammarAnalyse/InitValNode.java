package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.MemoryElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class InitValNode extends TreeNode{
    private ArrayList<TreeNode> children;
    private VariableOperand addrMem;
    private boolean NeedAddFour = false;
    private String childrenType;
    public InitValNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = new ArrayList<>();
    }

    public void setType(String type) {
        this.childrenType = type;
    }

    public void addChildren(TreeNode children) {
        this.children.add(children);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for(TreeNode treeNode:children) {
            if (childrenType.equals("Exp")) {
                treeNode.treePrint(stringBuilder);
            } else {
                if (count == 0) {
                    stringBuilder.append("{");
                    treeNode.treePrint(stringBuilder);
                } else {
                    stringBuilder.append(", ");
                    treeNode.treePrint(stringBuilder);
                }
            }
            count++;
        }
        if (childrenType.equals("InitVal")) {
            stringBuilder.append("}");
        }
    }

    @Override
    public Word getThisNodeLine() {
        return children.get(0).getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for (TreeNode treeNode:children) {
            treeNode.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        if (childrenType.equals("Exp")) {
            VariableOperand variableOperand = Father instanceof VarDefNode ? (VariableOperand) Father.getDst() :
                                                intermediateBuilder.putTempVariableAndReturn();
            children.get(0).ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand src1 = (VariableOperand) (children.get(0)).getDst();
            PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            VariableOperand temp_1 = intermediateBuilder.putTempVariableAndReturn();
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_1,src1,const_0));
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(variableOperand,temp_1,const_0));
            dst = variableOperand;
        } else {
            VarDefNode varDefNode = (VarDefNode) Father;
            PrimaryOperand Dst = varDefNode.getDst();
            VariableOperand temp_addr = intermediateBuilder.putTempVariableAndReturn();
            PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_addr,Dst,Const_0));
            addrMem = temp_addr;
            NeedAddFour = false;
            IntermediateInitValNode(intermediateBuilder,symbolTable);
        }
    }

    public void IntermediateInitValNode(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        if(childrenType.equals("InitVal")) {
            for(TreeNode treeNode:children) {
                ((InitValNode)treeNode).addrMem = addrMem;
                ((InitValNode)treeNode).NeedAddFour = NeedAddFour;
                ((InitValNode)treeNode).IntermediateInitValNode(intermediateBuilder,symbolTable);
                if (Father instanceof InitValNode) {
                    ((InitValNode)Father).addrMem = addrMem;
                    ((InitValNode)Father).NeedAddFour = NeedAddFour;
                }
            }
        } else {
            TreeNode treeNode = children.get(0);
            children.get(0).ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand value = (VariableOperand) (treeNode).dst;
            if (NeedAddFour) {
                PrimaryOperand Const_4 = intermediateBuilder.putIntConstAndReturnVariable(4);
                VariableOperand newTempAddr = intermediateBuilder.putTempVariableAndReturn();
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(newTempAddr,Const_4,addrMem));
                intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateStoreElement(value,newTempAddr));
                ((InitValNode)Father).addrMem = newTempAddr;
            } else {
                intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateStoreElement(value,addrMem));
                ((InitValNode)Father).NeedAddFour = true;
            }
        }
    }

    public void RunInitValParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("LBRACE")) {
            word = WordProvider.GetNextWord();
            if (word != null && !word.getCategoryCode().equals("RBRACE")) {
                WordProvider.RollBackWord(1);
                InitValNode initValNode = new InitValNode(this,printDeep);
                addChildren(initValNode);
                setType("InitVal");
                initValNode.RunInitValParser();
                while(true) {
                    word = WordProvider.GetNextWord();
                    if (word != null && word.getCategoryCode().equals("COMMA")) {
                        InitValNode initValNode1 = new InitValNode(this,printDeep);
                        addChildren(initValNode1);
                        initValNode1.RunInitValParser();
                    } else {
                        WordProvider.RollBackWord(1);
                        break;
                    }
                }
                WordProvider.CheckEndSign(null,this.getClass(),-1,"RBRACE");
            }
        } else {
            setType("Exp");
            WordProvider.RollBackWord(1);
            ExpNode expNode = new ExpNode(this,printDeep);
            addChildren(expNode);
            expNode.RunExpParser();
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<InitVal>");
    }
}
