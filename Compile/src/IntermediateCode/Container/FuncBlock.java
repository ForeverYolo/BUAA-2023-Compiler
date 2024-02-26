package IntermediateCode.Container;

import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.TagOperand;
import ObjectCode.Assembly;
import Tools.GlobalSetting;

import java.util.ArrayList;
import java.util.HashSet;

public class FuncBlock extends PrimaryBlock{
    private FuncOperand funcDefine;
    private ArrayList<NormalBlock> normalBlocks;
    private NormalBlock startBlock;
    private int space;

    public FuncBlock(FuncOperand funcOperand) {
        super();
        funcDefine = funcOperand;
        normalBlocks = new ArrayList<>();
        space = 0;
    }

    public NormalBlock getStartBlock() {
        return startBlock;
    }

    public ArrayList<NormalBlock> getNormalBlocks() {
        return normalBlocks;
    }

    public void finalizeFunction() {
        HashSet<TagOperand> TagSetRecord = new HashSet<>();
        IntermediateExpression.forEach(primaryElement -> {
            if(primaryElement instanceof BranchElement) {
                TagSetRecord.add(((BranchElement) primaryElement).getTag1());
                TagSetRecord.add(((BranchElement) primaryElement).getTag2());
            } else if (primaryElement.getOperatorName().equals("Jump")) {
                TagSetRecord.add((TagOperand) ((OtherElements)primaryElement).getOperand());
            }
        });
        startBlock = new NormalBlock(normalBlocks.size(),this);
        divideIntoNormalBlock(TagSetRecord);
    }

    public void divideIntoNormalBlock(HashSet<TagOperand> TagSetRecord) {
        NormalBlock tempBlock = startBlock;
        for (PrimaryElement primaryElement:IntermediateExpression) {
            if(primaryElement instanceof BranchElement || primaryElement.getOperatorName().equals("Jump")) {
                tempBlock.AddIntermediateExpression(primaryElement);
                tempBlock = putAndReturnNormalBlock(tempBlock);
            } else if (primaryElement.getOperatorName().equals("Tag")) {
                TagOperand tagOperand = (TagOperand) ((OtherElements)primaryElement).getOperand();
                if(TagSetRecord.contains(tagOperand)) {
                    if (tempBlock.IntermediateExpression.isEmpty()) {
                        tempBlock.AddIntermediateExpression(primaryElement);
                    } else {
                        tempBlock = putAndReturnNormalBlock(tempBlock);
                        tempBlock.AddIntermediateExpression(primaryElement);
                    }
                }
            } else {
                tempBlock.AddIntermediateExpression(primaryElement);
            }
        }
        putAndReturnNormalBlock(tempBlock);
    }

    public NormalBlock putAndReturnNormalBlock(NormalBlock normalBlock) {
        normalBlocks.add(normalBlock);
        return new NormalBlock(normalBlocks.size(),this);
    }

    public void CalculateSpace() {
        space = funcDefine.getFormalParams().size();
        //normalBlocks.forEach(normalBlock -> space = normalBlock.CalculateSpace(space));
        for (NormalBlock normalBlock:normalBlocks) {
            space = normalBlock.CalculateSpace(space);
        }
        space = space + 1; // return Addr
        funcDefine.setSpace(space);
    }

    public void ToAssembly(Assembly assembly) {
        if (GlobalSetting.RegisterOptimize) {
            for(NormalBlock normalBlock : normalBlocks) {
                normalBlock.ToAssembly(assembly);
            }
        } else {
            assembly.WriteBackAllLocalRegister();
            for(NormalBlock normalBlock : normalBlocks) {
                normalBlock.ToAssembly(assembly);
            }
        }
    }
}
