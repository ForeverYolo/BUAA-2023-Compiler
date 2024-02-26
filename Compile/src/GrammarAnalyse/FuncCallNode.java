package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.*;
import Tools.Combination;
import Tools.ErrorMessage;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;
import java.util.HashSet;

public class FuncCallNode extends TreeNode{
    //这是一个虚拟的非终结点
    Word ident;
    FuncRParamsNode funcRParamsNode;
    FuncDefNode funcDefNode;
    public FuncCallNode(TreeNode Father,int deep) {
        super(Father,deep);
        ident = null;
        funcRParamsNode = new FuncRParamsNode(this,deep);
        funcDefNode = null;
    }

    public Word getIdent() {
        return ident;
    }

    public void setIdent(Word ident) {
        this.ident = ident;
    }

    public void setFuncRParamsNode(FuncRParamsNode funcRParamsNode) {
        this.funcRParamsNode = funcRParamsNode;
    }

    public void treePrint(StringBuilder stringBuilder) {
        // funcname (Params)\n
        stringBuilder.append(ident.OriginWord);
        stringBuilder.append("(");
        funcRParamsNode.treePrint(stringBuilder);
        if (SearchPrintfNode()) {
            stringBuilder.append(")");
        } else {
            stringBuilder.append(")");
        }
    }

    public boolean SearchPrintfNode() {
        TreeNode node = Father;
        while (node != null) {
            if(node instanceof PrintfNode) {
                return true;
            }
            node = node.Father;
        }
        return false;
    }

    @Override
    public Word getThisNodeLine() {
        return ident;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        FuncDefTuple funcDefTuple = (FuncDefTuple) symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,false);
        if (funcDefTuple == null) {
            setNodeType("Error");
            setNodeDim(-2);
            ErrorMessage.handleError(this.getClass(),0,ident);
            //检查是否有未定义
            funcRParamsNode.getExpChildren().forEach(expNode -> expNode.RunSymbolBuilder(symbolTable,null));
        } else {
            setNodeTypeAndDim(funcDefTuple);
            funcRParamsNode.RunSymbolBuilder(symbolTable,funcDefTuple);
        }
    }

    public void setNodeTypeAndDim(FuncDefTuple funcDefTuple) {
        if (funcDefTuple.getFuncType().equals("int")) {
            setNodeType("int");
            setNodeDim(0);
        } else {
            setNodeType("Error");
            setNodeDim(-2);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        HashSet<FuncDefNode> unique = new HashSet<>();
        if (GlobalSetting.FunctionInLineOptimize) {
            funcDefNode.SearchCallers(unique);
        }
        if (GlobalSetting.FunctionInLineOptimize && !unique.contains(funcDefNode)) {
            FuncDefTuple funcDefTuple = (FuncDefTuple) symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true);
            if(funcDefTuple.getFuncType().equals("int")) {
                dst = intermediateBuilder.putNormalVariableAndReturn();
                inLineStack.add(new Combination<>(intermediateBuilder.putTagOperandAndReturn("inline_tag"),(VariableOperand) dst));
            } else {
                inLineStack.add(new Combination<>(intermediateBuilder.putTagOperandAndReturn("inline_tag"),null));
            }
            TagOperand start_inLine = intermediateBuilder.putTagOperandAndReturn("inline_start");
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(start_inLine));
            funcRParamsNode.setInLine(true);
            funcRParamsNode.ToIntermediate(intermediateBuilder,symbolTable);
            funcDefNode.getSymbolTable().ResetSymbolTableState();
            if (funcDefNode.getFuncFParamsChildren() != null) {
                for(int i = 0; i < funcDefNode.getFuncFParamsChildren().getFuncFParamChildren().size(); i++) {
                    VariableOperand ParamVar = intermediateBuilder.putNormalVariableAndReturn();
                    funcDefTuple.getFuncFParamsType().get(i).setPrimaryOperand(ParamVar);
                    PrimaryElement element = CalculateElement.createAddElement(ParamVar,
                            funcRParamsNode.getExpChildren().get(i).dst, intermediateBuilder.putIntConstAndReturnVariable(0));
                    intermediateBuilder.AddIntermediateExpression(element);
                    intermediateBuilder.getOptimizeWhiteList().add(element);
                }
            }
            funcDefNode.getBlockChildren().ToIntermediate(intermediateBuilder,symbolTable);
            Combination<TagOperand,VariableOperand> LastCombination = inLineStack.getLast();
            funcDefTuple.setRealReturnOperand(LastCombination.getValue());
            TagOperand endTag = inLineStack.removeLast().getKey();
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(endTag));
        } else {
            funcRParamsNode.setInLine(false);
            funcRParamsNode.ToIntermediate(intermediateBuilder,symbolTable);
            FuncOperand funcOperand = intermediateBuilder.getFuncOperandToCall(ident.OriginWord);
            ArrayList<PrimaryElement> PushElements = funcRParamsNode.getPushElements();
            /* 及时取走V0值,如果有的话 */
            FuncDefTuple funcDefNode = (FuncDefTuple) symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true);
            if (funcDefNode.getFuncType().equals("int")) {
                PrimaryOperand returnDst = funcDefNode.getFormalReturnOperand();
                if (returnDst == null) {
                    returnDst = intermediateBuilder.putReturnVariableAndReturn(false);
                    funcDefNode.setFormalReturnOperand(returnDst);
                }
                VariableOperand tempDst = intermediateBuilder.putTempVariableAndReturn();
                PrimaryOperand const0 = intermediateBuilder.putIntConstAndReturnVariable(0);
                funcOperand.setFormalReturnVariable(returnDst);
                PrimaryElement CallElement = OtherElements.createCallElement(funcOperand);
                intermediateBuilder.AddCallPushMap(PushElements,CallElement);
                intermediateBuilder.AddIntermediateExpression(CallElement);
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(tempDst,returnDst,const0));
                funcDefNode.setRealReturnOperand(tempDst);
            } else {
                PrimaryElement CallElement = OtherElements.createCallElement(funcOperand);
                intermediateBuilder.AddCallPushMap(PushElements,CallElement);
                intermediateBuilder.AddIntermediateExpression(CallElement);
            }
        }
    }

    public void FuncCallParser() {
        setIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));
        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");
        Word word = WordProvider.GetNextWord();
        if (word != null && WordProvider.FindInExpFirst(word)) {
            WordProvider.RollBackWord(1);
            FuncRParamsNode funcRParamsNode = new FuncRParamsNode(this,printDeep);
            setFuncRParamsNode(funcRParamsNode);
            funcRParamsNode.RunFuncRParamsParser();
            WordProvider.CheckEndSign(funcRParamsNode.getThisNodeLine(),this.getClass(),1,"RPARENT");
        } else {
            WordProvider.RollBackWord(1);
            WordProvider.CheckEndSign(ident,this.getClass(),2,"RPARENT");
        }
        if (GlobalSetting.FunctionInLineOptimize) {
            TreeNode Father = this.Father;
            boolean IsFind = false;
            //维护函数定义节点
            while (Father != null) {
                if (Father instanceof CompNode compNode) {
                    ArrayList<FuncDefNode> FunDefNodes =  compNode.getFuncDefChildren();
                    for(FuncDefNode funcDefNode : FunDefNodes) {
                        if (funcDefNode.getIdent().OriginWord.equals(ident.OriginWord)) {
                            this.funcDefNode = funcDefNode;
                            IsFind = true;
                            break;
                        }
                    }
                }
                Father = Father.Father;
                if (IsFind) {
                    break;
                }
            }
            if (!IsFind) {
                return;
            }
            //建立调用关系
            Father = this.Father;
            while(Father != null) {
                if (Father instanceof FuncDefNode FuncFather) {
                    FuncFather.getCallFuncDefNodeSet().add(this.funcDefNode);
                    break;
                }
                Father = Father.Father;
            }
        }
    }
}
