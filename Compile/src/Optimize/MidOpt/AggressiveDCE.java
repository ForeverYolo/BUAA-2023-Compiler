package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.*;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Tools.ErrorMessage;

import javax.swing.text.html.HTML;
import java.util.*;
import java.util.stream.Collectors;

public class AggressiveDCE {
    protected SSAConvert OrderSSAConvert;
    protected SSAConvert ReverseSSAConvert;
    protected HashMap<NormalBlock, HashSet<NormalBlock>> ControlDependence;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<VariableOperand,PrimaryElement> VarDefElement;
    protected HashSet<PrimaryElement> ValidElements;
    protected HashSet<NormalBlock> ValidBlocks;
    protected IntermediateBuilder intermediateBuilder;

    public AggressiveDCE(SSAConvert OrderSSAConvert,IntermediateBuilder intermediateBuilder) {
        this.OrderSSAConvert = OrderSSAConvert;
        this.ReverseSSAConvert = new SSAConvert(OrderSSAConvert.controlFlowGraph.ReverseControlFlowGraph(),OrderSSAConvert.func);
        ReverseSSAConvert.ProcessBeDomination();
        ReverseSSAConvert.ProcessStrictDomination();
        ReverseSSAConvert.ProcessDirectDomination();
        ReverseSSAConvert.ProcessDominanceFrontier();
        ControlDependence = ReverseSSAConvert.DominanceFrontier;
        normalBlocks = OrderSSAConvert.normalBlocks;
        VarDefElement = new HashMap<>();
        ValidElements = new HashSet<>();
        this.intermediateBuilder = intermediateBuilder;
        this.ValidBlocks = new HashSet<>();
    }

    public boolean ProcessADCE() {
        boolean ModifyFlag;
        Queue<PrimaryElement> WorkList = new LinkedList<>();
        initialVarDefElement();
        initialWorkList(WorkList);
        while (!WorkList.isEmpty()) {
            PrimaryElement element = WorkList.poll();
            ArrayList<VariableOperand> use = element.getUsedVariable();
            use.forEach(var -> {
                //--------------------------Def-Use分析---------------------------------------
                PrimaryElement primaryElement = VarDefElement.get(var);
                if (primaryElement != null && !ValidElements.contains(primaryElement)) {
                    ValidElements.add(primaryElement);
                    WorkList.add(primaryElement);
                }
            });
            //-------------------------控制依赖分析-----------------------------------------
            NormalBlock normalBlock = SearchThisElementInBlock(element);
            ControlDependenceAnalyze(WorkList,normalBlock);
            //-------------------------Phi 处理--------------------------------------------
            if (element instanceof PhiElement) {
                NormalBlock phiBlock = SearchThisElementInBlock(element);
                ArrayList<NormalBlock> prevBlocks = OrderSSAConvert.controlFlowGraph.prevMap.getOrDefault(phiBlock,new ArrayList<>());
                ValidBlocks.addAll(prevBlocks);
                for(NormalBlock prevBlock : prevBlocks) {
                    ControlDependenceAnalyze(WorkList,prevBlock);
                }
            }
        }
        //----------将无效化的跳转赋予新的跳转----------------------
        ProcessInValidJumpOrBr();
        //----------删除所有无效指令------------------------------
        ModifyFlag = DeleteUnUsedElement();
        //----------合并多余的跳转--------------------------------
        if (normalBlocks.size() > 1) {
            ModifyFlag |= MergeBlock();
        }
        return ModifyFlag;
    }

    public void ControlDependenceAnalyze(Queue<PrimaryElement> WorkList,NormalBlock normalBlock) {
        HashSet<NormalBlock> Dependence = ControlDependence.getOrDefault(normalBlock,new HashSet<>());
        Dependence.forEach(dependBlock -> {
            int size = dependBlock.IntermediateExpression.size();
            PrimaryElement brElement = dependBlock.IntermediateExpression.get(size - 1);
            if (brElement instanceof BranchElement branchElement && !ValidElements.contains(brElement)) {
                ValidElements.add(brElement);
                WorkList.add(brElement);
                activeTag(branchElement.getTag1(),branchElement.getTag2());
            }
        });
    }

    public void activeTag(TagOperand... tagOperand) {
        HashSet<TagOperand> tags = Arrays.stream(tagOperand).collect(Collectors.toCollection(HashSet::new));
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> {
            if(element instanceof OtherElements otherElement && OtherElements.isTag(otherElement)) {
                TagOperand tag = (TagOperand) otherElement.getOperand();
                if (tags.contains(tag)) {
                    ValidElements.add(element);
                }
            }
        }));
    }

    public void ProcessInValidJumpOrBr() {
        HashMap<NormalBlock,NormalBlock> ReverseDirectDomination = ReverseSSAConvert.DirectDomination;
        for(NormalBlock normalBlock : normalBlocks) {
           if (normalBlock.IntermediateExpression.isEmpty()) {
               continue;
           }
           PrimaryElement LastElement = normalBlock.getLastMediateExpression();
           if (LastElement instanceof BranchElement || (LastElement instanceof OtherElements otherElement && OtherElements.isJump(otherElement))) {
               if (!ValidElements.contains(LastElement)) {
                   RemoveBranchOrJumpFormBlock(normalBlock);
                   NormalBlock lastBlock = normalBlock;
                   NormalBlock nextBlock = ReverseDirectDomination.getOrDefault(normalBlock,null);
                   boolean isUnSolved = nextBlock != null; // 为null代表已经是最终块，删除无用死循环。
                   while (isUnSolved) {
                       if (nextBlock == null) {
                           isUnSolved = false;
                           PrimaryElement firstElement = lastBlock.getFirstMediateExpression();
                           CreateJumpOrBrTOAssociateTwoBlocks(firstElement,normalBlock,lastBlock);
                       } else {
                           for(PrimaryElement element : nextBlock.IntermediateExpression) {
                               if (ValidElements.contains(element) || ValidBlocks.contains(nextBlock)) {
                                   isUnSolved = false;
                                   PrimaryElement firstElement = nextBlock.getFirstMediateExpression();
                                   CreateJumpOrBrTOAssociateTwoBlocks(firstElement,normalBlock,nextBlock);
                                   break;
                               }
                           }
                       }
                       lastBlock = nextBlock;
                       nextBlock = ReverseDirectDomination.getOrDefault(nextBlock,null);
                   };
               }
           }
        }
    }

    public void RemoveBranchOrJumpFormBlock(NormalBlock normalBlock) {
        PrimaryElement element = normalBlock.IntermediateExpression.get(normalBlock.IntermediateExpression.size() - 1);
        if (element instanceof BranchElement branchElement) {
            TagOperand tag1 = branchElement.getTag1();
            TagOperand tag2 = branchElement.getTag2();
            OrderSSAConvert.controlFlowGraph.normalBlockStartTag.forEach((block, tags) -> {
                if (tags.contains(tag1) || tags.contains(tag2)) {
                    RemoveAssociationBetweenTwoBlocks(normalBlock,block);
                }
            });
        } else if (element instanceof OtherElements otherElement && OtherElements.isJump(otherElement)) {
            TagOperand tag = (TagOperand) otherElement.getOperand();
            OrderSSAConvert.controlFlowGraph.normalBlockStartTag.forEach((block, tags) -> {
                if (tags.contains(tag)) {
                    RemoveAssociationBetweenTwoBlocks(normalBlock,block);
                }
            });
        }
        normalBlock.IntermediateExpression.remove(normalBlock.IntermediateExpression.size() - 1);
    }

    public void CreateJumpOrBrTOAssociateTwoBlocks(PrimaryElement firstElement, NormalBlock normalBlock, NormalBlock nextBlock) {
        CreateAssociationBetweenTwoBlocks(normalBlock,nextBlock);
        if (firstElement instanceof OtherElements otherElement && OtherElements.isTag(otherElement)) {
            TagOperand tag = (TagOperand) otherElement.getOperand();
            PrimaryElement JumpElement = OtherElements.createJumpElement(tag);
            normalBlock.IntermediateExpression.add(JumpElement);
            ValidElements.add(JumpElement);
            ValidElements.add(firstElement);
        } else {
            TagOperand newTag = intermediateBuilder.putTagOperandAndReturn("ADCE-Tag");
            OrderSSAConvert.controlFlowGraph.normalBlockStartTag.get(nextBlock).add(newTag);
            PrimaryElement TagElement = OtherElements.createTagElement(newTag);
            nextBlock.IntermediateExpression.add(TagElement);
            PrimaryElement JumpElement = OtherElements.createJumpElement(newTag);
            normalBlock.IntermediateExpression.add(JumpElement);
            ValidElements.add(JumpElement);
            ValidElements.add(TagElement);
        }

    }

    //这方法写的真的丑陋，但是我不想改了，因为我不想再看一遍了。
    public boolean MergeBlock() {
        boolean ModifyFlag = false;
        boolean ModifyCheck;
        do {
            ModifyCheck = false;
            ArrayList<NormalBlock> normalBlockArrayList = new ArrayList<>(normalBlocks);
            for (NormalBlock normalBlock : normalBlockArrayList) {
                ArrayList<PrimaryElement> elements = normalBlock.IntermediateExpression;
                int TagBrSize = elements.stream().filter(element ->
                                (element instanceof OtherElements otherElement && (OtherElements.isTag(otherElement) || OtherElements.isJump(otherElement))))
                        .collect(Collectors.toCollection(ArrayList::new)).size();
                TagOperand EnterTag = null;
                TagOperand EndTag = null;
                PrimaryElement BRElement;
                PrimaryElement EnterTagElement;
                if (!normalBlock.IntermediateExpression.isEmpty()) {
                    BRElement =  normalBlock.getLastMediateExpression();
                    EnterTagElement = normalBlock.getFirstMediateExpression();
                    if (EnterTagElement instanceof OtherElements otherElement && OtherElements.isTag(otherElement)) {
                        EnterTag = (TagOperand) otherElement.getOperand();
                    }
                    if (BRElement instanceof OtherElements otherElement && OtherElements.isJump(otherElement)) {
                        EndTag = (TagOperand) otherElement.getOperand();
                    }
                } else {
                    BRElement = null;
                    EnterTagElement = null;
                }
                // 必须存在的Block(ValidBlock)即使满足条件也不能被合并，因为他们是Phi的来源，删了就破坏了Phi的结构
                if (TagBrSize == elements.size() && !ValidBlocks.contains(normalBlock)) {
                    ArrayList<NormalBlock> nextMap = OrderSSAConvert.controlFlowGraph.nextMap.getOrDefault(normalBlock,new ArrayList<>());
                    if (nextMap.size() != 1) {
                        ErrorMessage.handleSelfCheckError(this.getClass());
                    }
                    NormalBlock nextBlock = nextMap.get(0);
                    ModifyCheck = true;
                    ModifyFlag = true;
                    //------------------------确定新的入口点--------------------------------
                    if (normalBlock == OrderSSAConvert.controlFlowGraph.EntryBlock) {
                        OrderSSAConvert.controlFlowGraph.EntryBlock = nextBlock;
                    }
                    if (EnterTag == null && EndTag == null) {
                        normalBlocks.remove(normalBlock);
                        OrderSSAConvert.controlFlowGraph.DeleteBlockFromControlFlowGraph(normalBlock);
                        ChangePhiUseDefBlock(normalBlock,nextBlock);
                    } else if (EnterTag == null) {
                        ArrayList<NormalBlock> prevMap = OrderSSAConvert.controlFlowGraph.prevMap.getOrDefault(normalBlock,new ArrayList<>());
                        prevMap.forEach(prev -> prev.IntermediateExpression.add(BRElement));
                        normalBlocks.remove(normalBlock);
                        OrderSSAConvert.controlFlowGraph.DeleteBlockFromControlFlowGraph(normalBlock);
                        ChangePhiUseDefBlock(normalBlock,nextBlock);
                    } else if (EndTag == null) {
                        PrimaryElement element = nextBlock.IntermediateExpression.isEmpty() ? null : nextBlock.getFirstMediateExpression();
                        if (element instanceof OtherElements otherElement && OtherElements.isTag(otherElement)) {
                            EndTag = (TagOperand) otherElement.getOperand();
                            ReplacePrevBlockTag(normalBlock, EnterTag, EndTag);
                        } else {
                            ArrayList<TagOperand> Tags = new ArrayList<>();
                            Tags.add(EnterTag);
                            OrderSSAConvert.controlFlowGraph.normalBlockStartTag.put(nextBlock,Tags);
                            nextBlock.IntermediateExpression.add(0, EnterTagElement);
                        }
                        normalBlocks.remove(normalBlock);
                        OrderSSAConvert.controlFlowGraph.DeleteBlockFromControlFlowGraph(normalBlock);
                        ChangePhiUseDefBlock(normalBlock,nextBlock);
                    } else {
                        ReplacePrevBlockTag(normalBlock, EnterTag, EndTag);
                        normalBlocks.remove(normalBlock);
                        OrderSSAConvert.controlFlowGraph.DeleteBlockFromControlFlowGraph(normalBlock);
                        ChangePhiUseDefBlock(normalBlock,nextBlock);
                    }
                }
            }
        } while (ModifyCheck);
        //-----------------------------------------相邻块之间无需跳转-------------------------------------------------------
        do {
            ModifyCheck = false;
            ArrayList<NormalBlock> normalBlockArrayList = new ArrayList<>(normalBlocks);
            for (int i = 0; i < normalBlockArrayList.size(); i++) {
                NormalBlock normalBlock = normalBlockArrayList.get(i);
                if (normalBlock.IntermediateExpression.isEmpty()) {
                    continue;
                }
                PrimaryElement BRElement = normalBlock.getLastMediateExpression();
                if (BRElement instanceof OtherElements JumpElement && OtherElements.isJump(JumpElement)) {
                    ArrayList<NormalBlock> nextMap = OrderSSAConvert.controlFlowGraph.nextMap.get(normalBlock);
                    if (nextMap == null || nextMap.size() != 1) {
                        ErrorMessage.handleSelfCheckError(this.getClass());
                    }
                    NormalBlock nextBlock = nextMap.get(0);
                    if (i < normalBlockArrayList.size() - 1) {
                        NormalBlock FakeNextBlock =normalBlockArrayList.get(i + 1);
                        if (nextBlock == FakeNextBlock) {
                            ArrayList<NormalBlock> prevMap = OrderSSAConvert.controlFlowGraph.prevMap.get(FakeNextBlock);
                            //如果有多个前驱，合并后不为基本块，所以不能合并，只删除无效的Jump指令
                            if (prevMap.size() != 1) {
                                normalBlock.IntermediateExpression.remove(JumpElement);
                            } else  {
                                // 把前面的块合到后面的块中，而不是把后面的块合到前面的块中。
                                if (normalBlock == OrderSSAConvert.controlFlowGraph.EntryBlock) {
                                    OrderSSAConvert.controlFlowGraph.EntryBlock = nextBlock;
                                }
                                ModifyCheck = true;
                                ModifyFlag = true;
                                normalBlock.IntermediateExpression.remove(JumpElement);
                                FakeNextBlock.IntermediateExpression.remove(0);
                                normalBlock.IntermediateExpression.addAll(FakeNextBlock.IntermediateExpression);
                                FakeNextBlock.IntermediateExpression = normalBlock.IntermediateExpression;
                                normalBlocks.remove(normalBlock);
                                OrderSSAConvert.controlFlowGraph.DeleteBlockFromControlFlowGraph(normalBlock);
                                ChangePhiUseDefBlock(normalBlock,FakeNextBlock);
                            }
                        }
                    }
                }
            }
        } while (ModifyCheck);
        return ModifyFlag;
    }

    public void ChangePhiUseDefBlock(NormalBlock oldBlock,NormalBlock newBlock) {
        for(PrimaryElement element : ValidElements) {
            if (element instanceof PhiElement phiElement) {
                phiElement.ChangeMapBlock(oldBlock,newBlock);
            }
        }
    }

    public void ReplacePrevBlockTag(NormalBlock normalBlock, TagOperand EnterTag, TagOperand EndTag) {
        ArrayList<NormalBlock> prevMap = new ArrayList<>(OrderSSAConvert.controlFlowGraph.prevMap.get(normalBlock));
        for (NormalBlock prevBlock : prevMap) {
            PrimaryElement PrevLastElement = prevBlock.getLastMediateExpression();
            if (PrevLastElement instanceof BranchElement branchElement) {
                branchElement.ChangeTag(EnterTag, EndTag);
                TagOperand tag1 = branchElement.getTag1();
                TagOperand tag2 = branchElement.getTag2();
                if (tag1 == tag2) {
                    RemoveBranchOrJumpFormBlock(prevBlock);
                    NormalBlock nextBlock = SearchBlockByTag(tag1);
                    CreateAssociationBetweenTwoBlocks(prevBlock,nextBlock);
                    prevBlock.IntermediateExpression.add(OtherElements.createJumpElement(tag1));
                }
            } else if (PrevLastElement instanceof OtherElements JumpElement && OtherElements.isJump(JumpElement)) {
                TagOperand tag = (TagOperand) JumpElement.getOperand();
                NormalBlock nextBlock = SearchBlockByTag(tag);
                RemoveAssociationBetweenTwoBlocks(prevBlock,nextBlock);

                nextBlock = SearchBlockByTag(EndTag);
                CreateAssociationBetweenTwoBlocks(prevBlock,nextBlock);
                JumpElement.setOperand(EndTag);
            } else {
                RemoveAssociationBetweenTwoBlocks(prevBlock, normalBlock);
                NormalBlock nextBlock = SearchBlockByTag(EndTag);
                CreateAssociationBetweenTwoBlocks(prevBlock,nextBlock);
                prevBlock.IntermediateExpression.add(OtherElements.createJumpElement(EndTag));
            }
        }
    }

    public NormalBlock SearchBlockByTag(TagOperand tag) {
        for (Map.Entry<NormalBlock,ArrayList<TagOperand>> entry : OrderSSAConvert.controlFlowGraph.normalBlockStartTag.entrySet()) {
            if (entry.getValue().contains(tag)) {
                return entry.getKey();
            }
        }
        ErrorMessage.handleSelfCheckError(this.getClass());
        return null;
    }

    public void RemoveAssociationBetweenTwoBlocks(NormalBlock normalBlock,NormalBlock nextBlock) {
        if (!OrderSSAConvert.controlFlowGraph.prevMap.containsKey(nextBlock)) {
            OrderSSAConvert.controlFlowGraph.prevMap.put(nextBlock,new ArrayList<>());
        }
        if (!OrderSSAConvert.controlFlowGraph.nextMap.containsKey(nextBlock)) {
            OrderSSAConvert.controlFlowGraph.nextMap.put(normalBlock,new ArrayList<>());
        }
        OrderSSAConvert.controlFlowGraph.prevMap.get(nextBlock).remove(normalBlock);
        OrderSSAConvert.controlFlowGraph.nextMap.get(normalBlock).remove(nextBlock);
    }

    public void CreateAssociationBetweenTwoBlocks(NormalBlock normalBlock,NormalBlock nextBlock) {
        if (!OrderSSAConvert.controlFlowGraph.prevMap.containsKey(nextBlock)) {
            OrderSSAConvert.controlFlowGraph.prevMap.put(nextBlock,new ArrayList<>());
        }
        if (!OrderSSAConvert.controlFlowGraph.nextMap.containsKey(nextBlock)) {
            OrderSSAConvert.controlFlowGraph.nextMap.put(normalBlock,new ArrayList<>());
        }
        OrderSSAConvert.controlFlowGraph.prevMap.get(nextBlock).add(normalBlock);
        OrderSSAConvert.controlFlowGraph.nextMap.get(normalBlock).add(nextBlock);
    }

    public boolean DeleteUnUsedElement() {
        boolean ModifyFlag = false;
        for (NormalBlock normalBlock : normalBlocks) {
            ArrayList<PrimaryElement> elements = new ArrayList<>(normalBlock.IntermediateExpression);
            for (PrimaryElement element : elements) {
                if (!ValidElements.contains(element)) {
                    normalBlock.IntermediateExpression.remove(element);
                    ModifyFlag = true;
                }
            }
        }
        return ModifyFlag;
    }

    public NormalBlock SearchThisElementInBlock(PrimaryElement element) {
        for (NormalBlock normalBlock : normalBlocks) {
            if (normalBlock.IntermediateExpression.contains(element)) {
                return normalBlock;
            }
        }
        ErrorMessage.handleSelfCheckError(this.getClass());
        return null;
    }

    public void initialWorkList(Queue<PrimaryElement> WorkList) {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(
                element -> {
                    if (MemoryElements.isKeyElement(element) || OtherElements.isKeyElement(element) || CheckChangeGlobal(element)) {
                        WorkList.add(element);
                        ValidElements.addAll(WorkList);
                    }
                }));
    }

    public boolean CheckChangeGlobal(PrimaryElement element) {
        ArrayList<VariableOperand> define = element.getDefineVariable();
        for (VariableOperand var : define) {
            if (var.getVariableType().equals(VariableType.GLOBAL)) {
                return true;
            }
        }
        return false;
    }

    public void AddVarDefElementSafely(VariableOperand var, PrimaryElement element) {
        if (VarDefElement.containsKey(var)) {
            System.err.println("检测到重复定义\n");
            ErrorMessage.handleSelfCheckError(this.getClass());
        } else {
            VarDefElement.put(var,element);
        }
    }

    public void initialVarDefElement() {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach
                (element -> element.getDefineVariable().forEach
                        (var -> AddVarDefElementSafely(var,element))));
    }
}
