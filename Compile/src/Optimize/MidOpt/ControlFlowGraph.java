package Optimize.MidOpt;

import IntermediateCode.Container.FuncBlock;
import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.*;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ControlFlowGraph {
    protected FuncOperand FuncOperand;
    protected FuncBlock FuncBlock;
    protected NormalBlock EntryBlock;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> prevMap;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> nextMap;
    protected HashMap<NormalBlock, ArrayList<TagOperand>> normalBlockStartTag;
    protected HashMap<NormalBlock, ArrayList<TagOperand>> normalBlockEndTag;
    protected ArrayList<NormalBlock> normalBlocks;
    public ControlFlowGraph() {
        this.EntryBlock = null;
        this.prevMap = new HashMap<>();
        this.nextMap = new HashMap<>();
        this.normalBlockStartTag = new HashMap<>();
        this.normalBlockEndTag = new HashMap<>();
        this.normalBlocks = new ArrayList<>();
    }

    public HashMap<NormalBlock, ArrayList<NormalBlock>> getPrevMap() {
        return prevMap;
    }

    public HashMap<NormalBlock, ArrayList<NormalBlock>> getNextMap() {
        return nextMap;
    }

    public IntermediateCode.Container.FuncBlock getFuncBlock() {
        return FuncBlock;
    }

    public IntermediateCode.Operands.FuncOperand getFuncOperand() {
        return FuncOperand;
    }

    public void GenerateControlFlowGraph(FuncOperand funcOperand, FuncBlock funcBlock) {
        this.FuncOperand = funcOperand;
        this.FuncBlock = funcBlock;
        //------------------------------------------------
        this.EntryBlock = funcBlock.getStartBlock();
        this.normalBlocks = funcBlock.getNormalBlocks();
        initializeTagAndConstructOrderEdge(normalBlocks);
        normalBlocks.forEach(this::ProcessCFG);
        //-----------------------死代码块删除-----------------------
        DeleteDeadBlockInControlFlowGraph(normalBlocks);
        //在这里做其实主要是避免SSA赋值时Phi产生问题，毕竟按照定义这里无法处理
    }

    public void GenerateControlFlowGraph(FuncOperand funcOperand, FuncBlock funcBlock,NormalBlock EntryBlock) {
        this.FuncOperand = funcOperand;
        this.FuncBlock = funcBlock;
        //------------------------------------------------
        this.EntryBlock = EntryBlock;
        this.normalBlocks = funcBlock.getNormalBlocks();
        initializeTagAndConstructOrderEdge(normalBlocks);
        normalBlocks.forEach(this::ProcessCFG);
        //-----------------------死代码块删除-----------------------
        DeleteDeadBlockInControlFlowGraph(normalBlocks);
        //在这里做其实主要是避免SSA赋值时Phi产生问题，毕竟按照定义这里无法处理
    }

    public NormalBlock getEntryBlock() {
        return EntryBlock;
    }

    public void DeleteDeadBlockInControlFlowGraph(ArrayList<NormalBlock> normalBlocks) {
        int startNum;
        int endNum;
        do {
            startNum = normalBlocks.size();
            ArrayList<NormalBlock> newNormalBlock = new ArrayList<>(normalBlocks);
            newNormalBlock.stream().filter(normalBlock -> normalBlock != EntryBlock && prevMap.getOrDefault(normalBlock,new ArrayList<>()).isEmpty())
                    .forEach(this::DeleteBlockFromControlFlowGraph);
            endNum = normalBlocks.size();
        } while (startNum != endNum);
    }

    public void DeleteBlockFromControlFlowGraph(NormalBlock normalBlock) {
        //---------------- 认为删除中间节点后，上下节点自动相连 --------------------------
        ArrayList<NormalBlock> nextBlockss = nextMap.getOrDefault(normalBlock,new ArrayList<>());
        ArrayList<NormalBlock> prevBlockss = prevMap.getOrDefault(normalBlock,new ArrayList<>());
        nextBlockss.forEach(nextBlock -> prevMap.get(nextBlock).addAll(prevBlockss.stream().filter(prevBlock ->
                !prevMap.get(nextBlock).contains(prevBlock)).collect(Collectors.toCollection(ArrayList::new))));
        prevBlockss.forEach(prevBlock -> nextMap.get(prevBlock).addAll(nextBlockss.stream().filter(nextBlock ->
                !nextMap.get(prevBlock).contains(nextBlock)).collect(Collectors.toCollection(ArrayList::new))));
        //----------------------- 删除此节点的所有关系----------------------------------
        prevMap.remove(normalBlock);
        nextMap.remove(normalBlock);
        normalBlockEndTag.remove(normalBlock);
        normalBlockStartTag.remove(normalBlock);
        normalBlocks.remove(normalBlock);
        prevMap.forEach((Block, prevBlocks) ->
                prevMap.put(Block,prevBlocks.stream()
                        .filter(prevBlock -> prevBlock != normalBlock)
                        .collect(Collectors.toCollection(ArrayList::new))));
        nextMap.forEach((Block,nextBlocks) ->
                nextMap.put(Block,nextBlocks.stream()
                        .filter(nextBlock -> nextBlock != normalBlock)
                        .collect(Collectors.toCollection(ArrayList::new))));
        //----------------------- 如果是入口块 ----------------------------------------

    }

    public void GenerateControlFlowGraph(NormalBlock normalBlock) {
        this.EntryBlock = normalBlock;
        this.normalBlocks.add(normalBlock);
    }



    public void ProcessCFG(NormalBlock nowBlock) {
        ArrayList<TagOperand> targetTags = normalBlockEndTag.getOrDefault(nowBlock,new ArrayList<>());
        targetTags.forEach(targetTag -> MatchTag(targetTag,nowBlock));
    }

    public ArrayList<NormalBlock> getNormalBlocks() {
        return normalBlocks;
    }

    public void MatchTag(TagOperand targetTag, NormalBlock PrevBlock) {
        normalBlockStartTag.forEach((NextBlock, CompareTagOperands) -> CompareTagOperands.forEach(CompareTag -> {
            if(CompareTag.equals(targetTag)) {
                addBlockToPrevMap(NextBlock,PrevBlock);
                addBlockToNextMap(PrevBlock,NextBlock);
            }
        }));
    }

    public void addBlockToPrevMap(NormalBlock source,NormalBlock prev) {
        if (!prevMap.containsKey(source)) {
            prevMap.put(source,new ArrayList<>());
        }
        prevMap.get(source).add(prev);
    }

    public void addBlockToNextMap(NormalBlock source,NormalBlock next) {
        if (!nextMap.containsKey(source)) {
            nextMap.put(source,new ArrayList<>());
        }
        nextMap.get(source).add(next);
    }

    public void initializeTagAndConstructOrderEdge(ArrayList<NormalBlock> normalBlocks) {
        for (int i = 0; i < normalBlocks.size(); i++) {
            NormalBlock normalBlock = normalBlocks.get(i);
            ArrayList<TagOperand> startTags = new ArrayList<>();
            if (!normalBlock.IntermediateExpression.isEmpty()) {
                PrimaryElement fistElement = normalBlock.getFirstMediateExpression();
                if (fistElement.getOperatorName().equals("Tag")) {
                    TagOperand startTag = (TagOperand) ((OtherElements)fistElement).getOperand();
                    startTags.add(startTag);
                }
                normalBlockStartTag.put(normalBlock,startTags);
                ArrayList<TagOperand> endTags = new ArrayList<>();
                PrimaryElement LastElement = normalBlock.getLastMediateExpression();
                if (LastElement instanceof BranchElement branchElement) {
                    TagOperand t1 = branchElement.getTag1();
                    TagOperand t2 = branchElement.getTag2();
                    endTags.add(t1);
                    endTags.add(t2);
                } else if (LastElement.getOperatorName().equals("Jump")) {
                    TagOperand t1 = (TagOperand) ((OtherElements)LastElement).getOperand();
                    endTags.add(t1);
                } else if (i < normalBlocks.size() - 1) {
                    NormalBlock nextBlock = normalBlocks.get(i + 1);
                    addBlockToNextMap(normalBlock,nextBlock);
                    addBlockToPrevMap(nextBlock,normalBlock);
                }
                normalBlockEndTag.put(normalBlock,endTags);
            } else if (i < normalBlocks.size() - 1) {
                NormalBlock nextBlock = normalBlocks.get(i + 1);
                addBlockToNextMap(normalBlock,nextBlock);
                addBlockToPrevMap(nextBlock,normalBlock);
            }
        }
    }

    public ControlFlowGraph ReverseControlFlowGraph() {
        ControlFlowGraph ReverseControlFlowGraph = new ControlFlowGraph();
        int size = normalBlocks.size();
        if(size - 1 == -1) {
            System.out.println("ReverseControlFlowGraph: size = 1");
        }
        ReverseControlFlowGraph.EntryBlock = normalBlocks.get(size - 1);
        ReverseControlFlowGraph.nextMap = prevMap;
        ReverseControlFlowGraph.prevMap = nextMap;
        ReverseControlFlowGraph.normalBlockStartTag = normalBlockStartTag;
        ReverseControlFlowGraph.normalBlockEndTag = normalBlockEndTag;
        ReverseControlFlowGraph.normalBlocks = normalBlocks;
        return ReverseControlFlowGraph;
    }

}
