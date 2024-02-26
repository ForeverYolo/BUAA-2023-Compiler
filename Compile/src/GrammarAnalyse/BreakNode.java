package GrammarAnalyse;

import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class BreakNode extends TreeNode{
    // 终结符 我只有爸爸
    Word BreakWord;
    ForLoopNode forLoopNode;
    public BreakNode(TreeNode Father,int deep) {
        super(Father,deep);
        BreakWord = null;
        forLoopNode = null;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("break;");
    }

    @Override
    public Word getThisNodeLine() {
        return BreakWord;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        TreeNode testTreeNode = Father;
        while(testTreeNode != null) {
            if(testTreeNode instanceof ForLoopNode) {
                forLoopNode = (ForLoopNode) testTreeNode;
                return;
            }
            testTreeNode = testTreeNode.Father;
        }
        ErrorMessage.handleError(this.getClass(),0,BreakWord);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        intermediateBuilder.AddIntermediateExpression(OtherElements.createJumpElement(forLoopNode.getStopTag()));
    }

    public void BreakParser() {
        BreakWord =  WordProvider.CheckEndSign(null,this.getClass(),-1,"BREAKTK");
        WordProvider.CheckEndSign(BreakWord,this.getClass(),1,"SEMICN");
    }
}
