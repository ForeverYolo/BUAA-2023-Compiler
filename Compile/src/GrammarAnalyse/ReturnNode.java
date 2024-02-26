package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.FuncDefTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class ReturnNode extends TreeNode{
    private Word returnTK;
    private ExpNode expChildren;
    private PrimaryOperand InnerDst;
    private PrimaryOperand OuterDst;
    public ReturnNode(TreeNode Father,int deep) {
        super(Father,deep);
        expChildren = null;
        InnerDst = null;
        OuterDst = null;
    }

    public PrimaryOperand getInnerDst() {
        return InnerDst;
    }

    public PrimaryOperand getOuterDst() {
        return OuterDst;
    }

    public ExpNode getExpChildren() {
        return expChildren;
    }

    public void setExpChildren(ExpNode expChildren) {
        this.expChildren = expChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        if (expChildren != null) {
            stringBuilder.append("return ");
            expChildren.treePrint(stringBuilder);
        } else {
            stringBuilder.append("return");
        }
        stringBuilder.append(";");
    }

    public PrimaryTuple SearchFuncDefNode() {
        TreeNode node = this.Father;
        while(node != null) {
            if (node instanceof FuncDefNode) {
                return ((FuncDefNode)node).getFuncDefTuple();
            }
            if (node instanceof MainFuncDefNode) {
                return ((MainFuncDefNode)node).getFuncDefTuple();
            }
            node = node.Father;
        }
        ErrorMessage.handleError(null,-1,null);
        return null;
    }

    @Override
    public Word getThisNodeLine() {
        return returnTK;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        TreeNode node = this;
        while(node != null) {
            if (node instanceof FuncDefNode) {
                if (((FuncDefNode)node).ReturnValueType().equals("VOIDTK") && expChildren != null) {
                    ErrorMessage.handleError(this.getClass(),0,getThisNodeLine());
                    return;
                }
            }
            node = node.Father;
        }
        if (expChildren != null) {
            expChildren.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        if(GlobalSetting.FunctionInLineOptimize && !inLineStack.isEmpty()) {
            if (expChildren != null) {
                expChildren.ToIntermediate(intermediateBuilder,symbolTable);
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(inLineStack.getLast().getValue(),
                        expChildren.dst, intermediateBuilder.putIntConstAndReturnVariable(0)));
            }
            intermediateBuilder.AddIntermediateExpression(OtherElements.createJumpElement(inLineStack.getLast().getKey()));
        } else {
            if(expChildren != null) {
                expChildren.ToIntermediate(intermediateBuilder,symbolTable);
                VariableOperand dst = (VariableOperand) expChildren.getDst();
                intermediateBuilder.AddIntermediateExpression(OtherElements.createReturnElement(dst));
                FuncDefTuple funcDefNode = (FuncDefTuple) SearchFuncDefNode();
                this.InnerDst = dst;
                this.dst = funcDefNode.getFormalReturnOperand() == null ? intermediateBuilder.putReturnVariableAndReturn(false) :
                        funcDefNode.getFormalReturnOperand();
                this.OuterDst = this.dst;
                funcDefNode.setFormalReturnOperand(this.dst);
            } else {
                intermediateBuilder.AddIntermediateExpression(OtherElements.createReturnElement(null));
                this.InnerDst = null;
            }
        }
    }

    public void ReturnParser() {
        returnTK = WordProvider.CheckEndSign(null,this.getClass(),-1,"RETURNTK");
        Word word = WordProvider.GetNextWord();
        if (word != null && WordProvider.FindInExpFirst(word)) {
            WordProvider.RollBackWord(1);
            ExpNode expNode = new ExpNode(this,printDeep);
            setExpChildren(expNode);
            expNode.RunExpParser();
            WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),1,"SEMICN");
        } else if (word != null && !word.getCategoryCode().equals("SEMICN")) {
            WordProvider.RollBackWord(1);
            ErrorMessage.handleError(this.getClass(),2,WordProvider.GetLastWord());
        }
    }
}
