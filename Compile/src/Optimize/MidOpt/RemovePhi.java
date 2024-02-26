package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.*;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import Tools.ErrorMessage;

import java.util.*;
import java.util.stream.Collectors;

public class RemovePhi {
    protected SSAConvert ssaConvert;
    protected ArrayList<NormalBlock> normalBlocks;
    protected ControlFlowGraph controlFlowGraph;
    protected IntermediateBuilder intermediateBuilder;
    protected HashMap<NormalBlock,ArrayList<ParallelCopyElement>> ParaCopyMap;
    protected HashMap<NormalBlock,HashSet<NormalBlock>> DomBlockMap;
    public RemovePhi(SSAConvert ssaConvert,IntermediateBuilder intermediateBuilder) {
        this.ssaConvert = ssaConvert;
        this.normalBlocks = ssaConvert.normalBlocks;
        this.controlFlowGraph = ssaConvert.controlFlowGraph;
        this.intermediateBuilder = intermediateBuilder;
        this.ParaCopyMap = new HashMap<>();
        this.DomBlockMap = new HashMap<>();
    }

    public void ProcessConvertPhiToParaCopy() {
        if (!normalBlocks.isEmpty()) {
            int Now_Index = normalBlocks.get(normalBlocks.size() - 1).getId();
            ArrayList<NormalBlock> normalBlockArrayList = new ArrayList<>(normalBlocks);
            for (NormalBlock B : normalBlockArrayList) {
                ArrayList<PhiElement> PhiElements = getPhiElements(B);
                for (PhiElement phiElement : PhiElements) {
                    ArrayList<NormalBlock> PrevBlocks = new ArrayList<>(controlFlowGraph.prevMap.getOrDefault(B,new ArrayList<>()));
                    HashSet<NormalBlock> RelateBlocks = GenerateRelateBlock(phiElement);
                    GenerateDomBlocks(RelateBlocks,B);
                    for (NormalBlock E : PrevBlocks) {
                        if (controlFlowGraph.nextMap.getOrDefault(E,new ArrayList<>()).size() > 1 /* 多个后继 */
                                && PrevBlocks.size() > 1  /* 多个前驱 */
                                && SearchPhiValid(E) != null /* 含Phi */ ) {
                            NormalBlock PhiUseDefBlock = SearchPhiValid(E);
                            Now_Index = Now_Index + 1;
                            NormalBlock EmptyBlock = new NormalBlock(Now_Index,ssaConvert.controlFlowGraph.FuncBlock);
                            DomBlockMap.get(PhiUseDefBlock).add(EmptyBlock);
                            ReplaceEdge(E,B,EmptyBlock);
                        }
                    }
                    VariableOperand Define = (VariableOperand) phiElement.getDefine().get(0);
                    HashMap<NormalBlock, PrimaryOperand> hashMap = phiElement.getOriBlockDefMap();
                    PrevBlocks = controlFlowGraph.prevMap.getOrDefault(B,new ArrayList<>());
                    for(Map.Entry<NormalBlock, PrimaryOperand> entry : hashMap.entrySet()) { //遍历每一个前驱，找到所有的Phi定义块。
                        HashSet<NormalBlock> DomBlocks = DomBlockMap.getOrDefault(entry.getKey(),new HashSet<>()).stream()
                                .filter(PrevBlocks::contains).collect(Collectors.toCollection(HashSet::new));/* 找到所有支配块与前驱的交集，一般为一个，为了扩展性写的数组 */
                        for (NormalBlock targetBlock : DomBlocks) { //找到Phi定义块支配的前驱块，将对应的PhiUse替换为PC。
                            int BrSize = getBrElements(targetBlock).size();
                            int AllSize = targetBlock.IntermediateExpression.size();
                            ParallelCopyElement parallelCopy = ParallelCopyElement.CreateParallelCopyElement(Define,entry.getValue());
                            ArrayList<ParallelCopyElement> elements = ParaCopyMap.getOrDefault(targetBlock,new ArrayList<>());
                            elements.add(parallelCopy);
                            ParaCopyMap.put(targetBlock,elements);
                            targetBlock.IntermediateExpression.add(AllSize - BrSize,parallelCopy);
                        }
                    }
                    B.IntermediateExpression.remove(phiElement);
                }
            }
        }
    }

    public NormalBlock SearchPhiValid(NormalBlock E) {
        for(Map.Entry<NormalBlock,HashSet<NormalBlock>> entry : DomBlockMap.entrySet()) {
            if (entry.getValue().contains(E)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private HashSet<NormalBlock> GenerateRelateBlock(ArrayList<PhiElement> phiElements) {
        HashSet<NormalBlock> hashSet = new HashSet<>();
        for (PhiElement phiElement : phiElements) {
            hashSet.addAll(phiElement.getOriBlockDefMap().keySet());
        }
        return hashSet;
    }

    private HashSet<NormalBlock> GenerateRelateBlock(PhiElement phiElement) {
        HashSet<NormalBlock> hashSet = new HashSet<>();
        hashSet.addAll(phiElement.getOriBlockDefMap().keySet());
        return hashSet;
    }

    public void ProcessConvertParaCopyToMove() {
        ParaCopyMap.forEach((normalBlock, elements) -> {
            int TagSize = getTagElements(normalBlock).size();
            Queue<ParallelCopyElement> elementQueue = new LinkedList<>(elements);
            while (!elementQueue.isEmpty()) {
                ParallelCopyElement element = elementQueue.poll();
                VariableOperand Define = element.getDefineVariable().get(0);
                PrimaryOperand Use = element.getUsed().get(0);
                PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
                if (CheckDefUseParallel(Define,elementQueue)) {
                    VariableOperand temp = intermediateBuilder.putTempVariableAndReturn();
                    normalBlock.IntermediateExpression.add(TagSize,CalculateElement.createAddElement(temp,Define,Const_0));
                    //解决并行问题的放在前面
                    ReplaceAllUse(Define,temp,elementQueue);
                    elementQueue.add(element);
                } else {
                    int AllSize = normalBlock.IntermediateExpression.size();
                    int BrSize = getBrElements(normalBlock).size();
                    normalBlock.IntermediateExpression.remove(element);
                    normalBlock.IntermediateExpression.add(AllSize-BrSize-1,CalculateElement.createAddElement(Define,Use,Const_0));
                    //直接由PC转来的放在后面
                }
            }
        });
    }

    public void GenerateDomBlocks(HashSet<NormalBlock> UnCheckBlocks,NormalBlock CoreBlock) {
        DomBlockMap.clear();
        HashSet<NormalBlock> AllBlocks = new HashSet<>(UnCheckBlocks);
        AllBlocks.add(CoreBlock);
        UnCheckBlocks.forEach(CheckBlock -> GeneratePieceDomBlock(AllBlocks.stream()
                .filter(Block -> Block != CheckBlock)
                .collect(Collectors.toCollection(ArrayList::new)),CheckBlock));
    }

    public void GeneratePieceDomBlock(ArrayList<NormalBlock> endBlocks,NormalBlock Block) {
        Queue<NormalBlock> UnCheckBlock = new LinkedList<>();
        HashSet<NormalBlock> DomBlocks = new HashSet<>();
        UnCheckBlock.add(Block);
        while (!UnCheckBlock.isEmpty()) {
            NormalBlock CheckingBlock = UnCheckBlock.poll();
            if (!endBlocks.contains(CheckingBlock)) {
                DomBlocks.add(CheckingBlock);
                ArrayList<NormalBlock> NextBlocks = controlFlowGraph.nextMap.getOrDefault(CheckingBlock,new ArrayList<>());
                NextBlocks.stream().filter(normalBlock -> !DomBlocks.contains(normalBlock)).forEach(UnCheckBlock::add);
            }
        }
        DomBlockMap.put(Block,DomBlocks);
    }

    public void ReplaceAllUse(VariableOperand oldVar,VariableOperand newVar,Queue<ParallelCopyElement> elementQueue) {
        for (ParallelCopyElement element : elementQueue) {
            if (element.getUsedVariable().contains(oldVar)) {
                element.ReplaceUseVariable(oldVar,newVar);
            }
        }
    }

    public boolean CheckDefUseParallel(VariableOperand Define, Queue<ParallelCopyElement> elementQueue) {
        for (ParallelCopyElement element : elementQueue) {
            if (element.getUsedVariable().contains(Define)) {
                return true;
            }
        }
        return false;
    }
    public ArrayList<PhiElement> getPhiElements(NormalBlock normalBlock) {
        return normalBlock.IntermediateExpression.stream()
                .filter(element -> element instanceof PhiElement)
                .map(element -> (PhiElement) element)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PrimaryElement> getBrElements(NormalBlock normalBlock) {
        return normalBlock.IntermediateExpression.stream()
                .filter(element -> element instanceof BranchElement || (element instanceof OtherElements otherElement
                        && OtherElements.isJump(otherElement)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PrimaryElement> getTagElements(NormalBlock normalBlock) {
        return normalBlock.IntermediateExpression.stream()
                .filter(element ->  (element instanceof OtherElements otherElement
                        && OtherElements.isTag(otherElement)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void ReplaceEdge(NormalBlock prevBlock,NormalBlock Block,NormalBlock NewBlock) {
        //-------------------------------处理中间四元式--------------------------------------
        PrimaryElement PrevElement = prevBlock.IntermediateExpression.isEmpty() ? null : prevBlock.getLastMediateExpression();
        TagOperand newTag = intermediateBuilder.putTagOperandAndReturn("RemovePhi_NewBlock");
        if (PrevElement instanceof BranchElement branchElement) {
            OtherElements BlockElement = (OtherElements) Block.getFirstMediateExpression();
            TagOperand BlockTag = (TagOperand) BlockElement.getOperand();
            branchElement.ChangeTag(BlockTag,newTag);
            NewBlock.IntermediateExpression.add(OtherElements.createTagElement(newTag));
            NewBlock.IntermediateExpression.add(OtherElements.createJumpElement(BlockTag));
        } else if (PrevElement instanceof OtherElements otherElement && OtherElements.isJump(otherElement)) {
            OtherElements BlockElement = (OtherElements) prevBlock.getFirstMediateExpression();
            TagOperand BlockTag = (TagOperand) BlockElement.getOperand();
            prevBlock.IntermediateExpression.remove(PrevElement);
            prevBlock.IntermediateExpression.add(OtherElements.createJumpElement(newTag));
            NewBlock.IntermediateExpression.add(OtherElements.createTagElement(newTag));
            NewBlock.IntermediateExpression.add(OtherElements.createJumpElement(BlockTag));
        } else {
            TagOperand BlockTag = intermediateBuilder.putTagOperandAndReturn("RemovePhi_NextBlock");
            prevBlock.IntermediateExpression.add(OtherElements.createJumpElement(newTag));
            NewBlock.IntermediateExpression.add(OtherElements.createTagElement(newTag));
            NewBlock.IntermediateExpression.add(OtherElements.createJumpElement(BlockTag));
            Block.IntermediateExpression.add(OtherElements.createTagElement(BlockTag));
        //-------------------------------维护图结构----------------------------------------------
            ArrayList<TagOperand> tags = new ArrayList<>();
            tags.add(BlockTag);
            controlFlowGraph.normalBlockStartTag.put(Block,tags);
        }
        controlFlowGraph.prevMap.get(Block).remove(prevBlock);
        controlFlowGraph.nextMap.get(prevBlock).remove(Block);
        ArrayList<NormalBlock> newBlockPrev = new ArrayList<>();
        newBlockPrev.add(prevBlock);
        ArrayList<NormalBlock> newBlockNext = new ArrayList<>();
        newBlockNext.add(Block);
        controlFlowGraph.nextMap.get(prevBlock).add(NewBlock);
        controlFlowGraph.prevMap.put(NewBlock,newBlockPrev);
        controlFlowGraph.nextMap.put(NewBlock,newBlockNext);
        controlFlowGraph.prevMap.get(Block).add(NewBlock);
        ArrayList<TagOperand> tags = new ArrayList<>();
        tags.add(newTag);
        controlFlowGraph.normalBlockStartTag.put(NewBlock,tags);
        //------------------------------插入到normalBlock中--------------------------------------
        for(int i = 0; i < normalBlocks.size(); i++) {
            NormalBlock normalBlock = normalBlocks.get(i);
            if (normalBlock == prevBlock) {
                normalBlocks.add(i+1,NewBlock);
                break;
            }
        }
    }
}
