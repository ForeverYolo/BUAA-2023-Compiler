package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.TagOperand;
import Tools.ErrorMessage;

import java.util.ArrayList;

public class BranchFixAndOpt {
    protected SSAConvert ssaConvert;
    protected ArrayList<NormalBlock> normalBlocks;

    public BranchFixAndOpt(SSAConvert ssaConvert) {
        this.ssaConvert = ssaConvert;
        this.normalBlocks = ssaConvert.normalBlocks;
    }

    public void ProcessFixBranchBlock() {
        ArrayList<NormalBlock> normalBlockArrayList = new ArrayList<>(normalBlocks);
        for (int i = 0 ; i < normalBlockArrayList.size(); i++) {
            NormalBlock normalBlock = normalBlockArrayList.get(i);
            if (!normalBlock.IntermediateExpression.isEmpty()) {
                PrimaryElement element = normalBlock.getLastMediateExpression();
                if (element instanceof BranchElement branchElement) {
                    TagOperand tag = branchElement.getTag1();
                    NormalBlock NoBranchBlock = FindBlockByEnterTag(tag);
                    CheckAndKeepImplicitJumpValid(NoBranchBlock);
                    if (NoBranchBlock != normalBlock) {
                        normalBlocks.remove(NoBranchBlock);
                        int index = FindBlockIndexByElement(element) + 1;
                        normalBlocks.add(index,NoBranchBlock);
                    } else { /* 我删我自己 */
                        int index = FindBlockIndexByElement(element) + 1;
                        NormalBlock NewBlock = new NormalBlock(normalBlocks.get(normalBlocks.size() - 1).getId(),ssaConvert.getControlFlowGraph().FuncBlock);
                        NewBlock.IntermediateExpression.add(OtherElements.createJumpElement(tag));
                        normalBlocks.add(index,NewBlock);
                    }

                }
            }
        }
    }

    public void CheckAndKeepImplicitJumpValid(NormalBlock normalBlock) {
        NormalBlock nextBlock = null;
        NormalBlock prevBlock = null;
        for(int i = 0; i < normalBlocks.size(); i++) {
            NormalBlock block = normalBlocks.get(i);
            if (block == normalBlock && i < normalBlocks.size() - 1) {
                nextBlock = normalBlocks.get(i + 1);
                break;
            }
        }
        for(int i = 0; i < normalBlocks.size(); i++) {
            NormalBlock block = normalBlocks.get(i);
            if (block == normalBlock && i > 0) {
                prevBlock = normalBlocks.get(i - 1);
                break;
            }
        }
        if (nextBlock != null) {
            PrimaryElement element = normalBlock.IntermediateExpression.isEmpty() ? null : normalBlock.getLastMediateExpression();
            if (!(element instanceof BranchElement || (element instanceof OtherElements otherElement
                    && OtherElements.isJump(otherElement)))) {
                PrimaryElement nextBlockElement = nextBlock.getFirstMediateExpression();
                TagOperand tag = (TagOperand) ((OtherElements)nextBlockElement).getOperand();
                normalBlock.IntermediateExpression.add(OtherElements.createJumpElement(tag));
            }
        }
        if (prevBlock != null) {
            PrimaryElement element = prevBlock.IntermediateExpression.isEmpty() ? null : prevBlock.getLastMediateExpression();
            if (!(element instanceof BranchElement || (element instanceof OtherElements otherElement
                    && OtherElements.isJump(otherElement)))) {
                PrimaryElement prevBlockElement = normalBlock.getFirstMediateExpression();
                TagOperand tag = (TagOperand) ((OtherElements)prevBlockElement).getOperand();
                prevBlock.IntermediateExpression.add(OtherElements.createJumpElement(tag));
            }
        }
    }

    public int FindBlockIndexByElement(PrimaryElement element) {
        for (int i = 0; i < normalBlocks.size(); i++) {
            NormalBlock normalBlock = normalBlocks.get(i);
            if (normalBlock.IntermediateExpression.contains(element)) {
                return i;
            }
        }
        ErrorMessage.handleSelfCheckError(this.getClass());
        return -1;
    }

    public NormalBlock FindBlockByEnterTag(TagOperand tag) {
        for (NormalBlock normalBlock : normalBlocks) {
            if (!normalBlock.IntermediateExpression.isEmpty()) {
                PrimaryElement element = normalBlock.getFirstMediateExpression();
                if (element instanceof OtherElements otherElements && OtherElements.isTag(otherElements)) {
                    TagOperand source = (TagOperand) otherElements.getOperand();
                    if (source == tag) {
                        return normalBlock;
                    }
                }
            }
        }
        ErrorMessage.handleSelfCheckError(this.getClass());
        return null;
    }

    public void ProcessOptimizeBranchBlock() {
        for(int i = 0; i < normalBlocks.size() - 1; i++) {
            NormalBlock nowBlock = normalBlocks.get(i);
            NormalBlock nextBlock = normalBlocks.get(i+1);
            PrimaryElement JumpElement = null;
            PrimaryElement TagElement = null;
            if (!nowBlock.IntermediateExpression.isEmpty()) {
                JumpElement = nowBlock.getLastMediateExpression();
            }
            if (!nextBlock.IntermediateExpression.isEmpty()) {
                TagElement = nextBlock.getFirstMediateExpression();
            }
            if (JumpElement instanceof OtherElements otherElements && OtherElements.isJump(otherElements) &&
                TagElement instanceof OtherElements otherElements1 && OtherElements.isTag(otherElements1)) {
                TagOperand JumpTag = (TagOperand) otherElements.getOperand();
                TagOperand EntryTag = (TagOperand) otherElements1.getOperand();
                if (JumpTag == EntryTag) {
                    nowBlock.IntermediateExpression.remove(JumpElement);
                }
            }
        }
    }

    public void ProcessOptimizeBranchInst() {
        for (NormalBlock normalBlock : normalBlocks) {
            ProcessOptimizeBranchInst(normalBlock);
        }
    }

    public void ProcessOptimizeBranchInst(NormalBlock normalBlock) {
        ArrayList<PrimaryElement> elements = normalBlock.IntermediateExpression;
        if (elements.size() > 1 && normalBlock.getLastMediateExpression().getOperatorName().equals("bnz")) {
            BranchElement branchElement = (BranchElement) normalBlock.getLastMediateExpression();
            TagOperand tag1 = branchElement.getTag1();
            TagOperand tag2 = branchElement.getTag2();
            PrimaryElement judgeElement = elements.get(elements.size() - 2);
            if (judgeElement.getOperatorName().equals("Seq")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                elements.remove(elements.size() - 1);
                elements.remove(elements.size() - 1);
                elements.add(BranchElement.createBeqElement(tag1,tag2,Use.get(0),Use.get(1)));
            } else if (judgeElement.getOperatorName().equals("Sne")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                elements.remove(elements.size() - 1);
                elements.remove(elements.size() - 1);
                elements.add(BranchElement.createBneElement(tag1,tag2,Use.get(0),Use.get(1)));
            } else if (judgeElement.getOperatorName().equals("Sgt")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                if (Use.get(0) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBltzElement(tag1,tag2,Use.get(1)));
                } else if (Use.get(1) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBgtzElement(tag1,tag2,Use.get(0)));
                }
            } else if (judgeElement.getOperatorName().equals("Sge")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                if (Use.get(0) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBlezElement(tag1,tag2,Use.get(1)));
                } else if (Use.get(1) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBgezElement(tag1,tag2,Use.get(0)));
                }
            } else if (judgeElement.getOperatorName().equals("Slt")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                if (Use.get(0) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBgtzElement(tag1,tag2,Use.get(1)));
                } else if (Use.get(1) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBltzElement(tag1,tag2,Use.get(0)));
                }
            } else if (judgeElement.getOperatorName().equals("Sle")) {
                ArrayList<PrimaryOperand> Use = judgeElement.getUsed();
                if (Use.get(0) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBgezElement(tag1,tag2,Use.get(1)));
                } else if (Use.get(1) instanceof ConstValue constValue && constValue.getValue() == 0) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    elements.add(BranchElement.createBlezElement(tag1,tag2,Use.get(0)));
                }
            }
        }
    }
}
