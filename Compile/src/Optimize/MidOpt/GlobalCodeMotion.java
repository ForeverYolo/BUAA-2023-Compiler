package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.*;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Tools.Combination;
import Tools.ErrorMessage;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalCodeMotion {
    protected SSAConvert ssaConvert;
    protected ControlFlowGraph controlFlowGraph;
    protected HashMap<NormalBlock,Integer> CircleDeep;
    protected HashMap<NormalBlock, HashSet<NormalBlock>> Domination;
    protected ArrayList<Combination<NormalBlock,NormalBlock>> BackEdge;
    protected HashMap<Combination<NormalBlock,NormalBlock>,HashSet<NormalBlock>> CircleRange;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> prevMap;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> nextMap;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<NormalBlock,Integer> DominationDeep;
    protected HashMap<VariableOperand,PrimaryElement> VarDefElement;
    protected HashMap<VariableOperand,HashSet<PrimaryElement>> VarUseElement;
    protected HashMap<PrimaryElement,NormalBlock> ScheduleEarly;
    protected HashMap<PrimaryElement,NormalBlock> OriginElementBlockMap;
    protected HashMap<PrimaryElement,NormalBlock> ScheduleLate;
    protected HashMap<PrimaryElement,NormalBlock> ScheduleResult;
    protected HashSet<PrimaryElement> ElementVisitedSet;
    protected NormalBlock EndBlock;
    protected HashMap<NormalBlock,ArrayList<PrimaryElement>> ElementRecord;
    protected ArrayList<NormalBlock> VisitOrder;
    protected ArrayList<NormalBlock> Visited;
    protected HashMap<NormalBlock,HashSet<NormalBlock>> DomBlockMap;

    public GlobalCodeMotion(SSAConvert ssaConvert) {
        this.ssaConvert = ssaConvert;
        this.controlFlowGraph = ssaConvert.controlFlowGraph;
        this.CircleDeep = new HashMap<>();
        this.Domination = ssaConvert.Domination;
        this.BackEdge = new ArrayList<>();
        this.prevMap = controlFlowGraph.prevMap;
        this.nextMap = controlFlowGraph.nextMap;
        this.CircleRange = new HashMap<>();
        this.normalBlocks = controlFlowGraph.normalBlocks;
        this.DominationDeep = new HashMap<>();
        this.VarDefElement = new HashMap<>();
        this.ScheduleEarly = new HashMap<>();
        this.ElementVisitedSet = new HashSet<>();
        this.OriginElementBlockMap = new HashMap<>();
        this.ScheduleLate = new HashMap<>();
        this.VarUseElement = new HashMap<>();
        this.ScheduleResult = new HashMap<>();
        this.ElementRecord = new HashMap<>();
        this.VisitOrder = new ArrayList<>();
        this.Visited = new ArrayList<>();
        this.DomBlockMap = new HashMap<>();
    }

    public void ProcessVisitOrder() {
        dfs_walk(controlFlowGraph.EntryBlock);
        Collections.reverse(VisitOrder);
    }

    public void dfs_walk(NormalBlock normalBlock) {
        Visited.add(normalBlock);
        ArrayList<NormalBlock> nextBlocks = nextMap.get(normalBlock);
        if (nextBlocks != null) {
            for (NormalBlock nextBlock : nextBlocks) {
                if (!Visited.contains(nextBlock)) {
                    dfs_walk(nextBlock);
                }
            }
        }
        VisitOrder.add(normalBlock);
    }

    public void searchEndBlock() {
        int deep = -1;
        NormalBlock endBlock = null;
        for (Map.Entry<NormalBlock,Integer> entry : DominationDeep.entrySet()) {
            if(entry.getValue() > deep) {
                endBlock = entry.getKey();
            }
        }
        EndBlock = endBlock;
    }

    public void ProcessRecordElementOrder() {
        for (NormalBlock normalBlock : normalBlocks) {
            ArrayList<PrimaryElement> elements = new ArrayList<>(normalBlock.IntermediateExpression);
            ElementRecord.put(normalBlock,elements);
        }
    }

    public void GenerateCircleDeep() {
        ProcessBackEdge();
        ProcessCircleRange();
        ProcessCircleDeep();
    }

    public void MarkDominationDeep() {
        Queue<NormalBlock> DeepBlocks = new LinkedList<>();
        DeepBlocks.add(controlFlowGraph.EntryBlock);
        DominationDeep.put(controlFlowGraph.EntryBlock,0);
        while(!DeepBlocks.isEmpty()) {
            NormalBlock analyzeBlock = DeepBlocks.poll();
            int NowDeep = DominationDeep.get(analyzeBlock);
            HashSet<NormalBlock> Dom = getBlockDirectDomWhichBlock(analyzeBlock);
            Dom.forEach(normalBlock -> DominationDeep.put(normalBlock,NowDeep + 1));
            DeepBlocks.addAll(Dom);
        }
        searchEndBlock();
    }

    public HashSet<NormalBlock> getBlockDirectDomWhichBlock(NormalBlock normalBlock) {
        HashSet<NormalBlock> DirectDom = new HashSet<>();
        ssaConvert.DirectDomination.forEach((BeDomBlock, DomBlock) -> {
            if (DomBlock == normalBlock) {
                DirectDom.add(BeDomBlock);
            }
        });
        return DirectDom;
    }

    public void ProcessBackEdge() {
        nextMap.forEach((block, nextBlocks) -> nextBlocks.forEach(nextBlock -> {
            HashSet<NormalBlock> Dom = Domination.getOrDefault(nextBlock,new HashSet<>());
            if (Dom.contains(block)) {
                BackEdge.add(new Combination<>(block,nextBlock));
            }
        }));
    }


    public void InitialElementBlockMap() {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> OriginElementBlockMap.put(element,normalBlock)));
    }

    public void ProcessCircleRange() {
        for (Combination<NormalBlock, NormalBlock> backEdge : BackEdge) {
            NormalBlock EndBlock = backEdge.getKey();
            NormalBlock StartBlock = backEdge.getValue();
            Queue<NormalBlock> Blocks = new LinkedList<>();
            HashSet<NormalBlock> CircleBlocks = new HashSet<>();
            Blocks.add(EndBlock);
            while (!Blocks.isEmpty()) {
                NormalBlock NowBlock = Blocks.poll();
                if (NowBlock != StartBlock) {
                    ArrayList<NormalBlock> PrevBlocks = prevMap.getOrDefault(NowBlock, new ArrayList<>());
                    Blocks.addAll(PrevBlocks.stream().filter(normalBlock ->  !CircleBlocks.contains(normalBlock)).collect(Collectors.toCollection(ArrayList::new)));
                    CircleBlocks.addAll(PrevBlocks);
                }
            }
            CircleBlocks.add(EndBlock);
            CircleRange.put(backEdge, CircleBlocks);
        }
    }

    public void ProcessCircleDeep() {
        normalBlocks.forEach(normalBlock -> CircleDeep.put(normalBlock,0));
        CircleRange.forEach((Comb, CircleBlocks) -> CircleBlocks.forEach(normalBlock -> CircleDeep.put(normalBlock,CircleDeep.get(normalBlock) + 1)));
    }

    public void InitialVarDefElement() {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach
                (element -> element.getDefineVariable().forEach
                        (var -> AddVarDefElementSafely(var,element))));
    }

    public void AddVarDefElementSafely(VariableOperand var, PrimaryElement element) {
        if (VarDefElement.containsKey(var)) {
            System.err.println("检测到重复定义\n");
            ErrorMessage.handleSelfCheckError(this.getClass());
        } else {
            VarDefElement.put(var,element);
        }
    }

    public NormalBlock SearchBlockByElement(PrimaryElement element) {
        for(NormalBlock normalBlock : normalBlocks) {
            ArrayList<PrimaryElement> elements = normalBlock.IntermediateExpression;
            if (elements.contains(element)) {
                return normalBlock;
            }
        }
        ErrorMessage.handleSelfCheckError(this.getClass());
        return null;
    }

    public void ProcessScheduleEarly() {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> {
            if (isPinned(element)) {
                ScheduleEarly.put(element,normalBlock);
                ElementVisitedSet.add(element);
            }
        }));
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> {
            if (isPinned(element)) {
                ArrayList<VariableOperand> Use = element.getUsedVariable();
                for(VariableOperand var : Use) {
                    PrimaryElement DefVar = VarDefElement.get(var);
                    if (DefVar != null) { //如果不存在定义点 就不做分析
                        ScheduleEarlyFunc(DefVar);
                    }
                }
            }
        }));
    }

    public void ScheduleEarlyFunc(PrimaryElement element) {
        if (ElementVisitedSet.contains(element)) {
            return;
        }
        ElementVisitedSet.add(element);
        ScheduleEarly.put(element,controlFlowGraph.EntryBlock);
        ArrayList<VariableOperand> Use = element.getUsedVariable();
        for(VariableOperand var : Use) {
            PrimaryElement DefVar = VarDefElement.get(var);
            if (DefVar != null) { //存在诸如全局变量等无定义点的，认为可以放到开头
                ScheduleEarlyFunc(DefVar);
                int ElementDeep =  DominationDeep.get(ScheduleEarly.get(element));
                int DefVarDeep = DominationDeep.get(ScheduleEarly.get(DefVar));
                if (ElementDeep < DefVarDeep) {
                    ScheduleEarly.put(element,ScheduleEarly.get(DefVar));
                }
            }
        }
    }

    public boolean isPinned(PrimaryElement element) {
        // jump和返回指令不可移动
        if (element instanceof OtherElements otherElement && (OtherElements.isJump(otherElement) || OtherElements.isReturn(otherElement))) {
            return true;
        }
        // phi指令不可移动
        if (element instanceof PhiElement) {
            return true;
        }
        // branch指令不可移动
        if (element instanceof BranchElement) {
            return true;
        }
        // call和push指令不可移动
        if (element instanceof OtherElements otherElement && OtherElements.isCall(otherElement)) {
            return true;
        }
        // call和push指令不可移动
        if (element instanceof OtherElements otherElement && OtherElements.isPush(otherElement)) {
            return true;
        }
        // 返回值赋值语句不可移动
        if (element.getUsedVariable().stream().anyMatch(var -> var.getVariableType().equals(VariableType.RETURN))) {
            return true;
        }
        // 输入语句不可移动
        if (element instanceof OtherElements otherElement && OtherElements.isScan(otherElement)) {
            return true;
        }
        // store语句不可移动
        if (element instanceof MemoryElements memoryElement && MemoryElements.isStore(memoryElement)) {
            return true;
        }
        // load语句不可移动
        if (element instanceof MemoryElements memoryElement && MemoryElements.isLoad(memoryElement)) {
            return true;
        }
        // Global赋值语句不可移动
        if (element.getDefineVariable().stream().anyMatch(var -> var.getVariableType().equals(VariableType.GLOBAL))) {
            return true;
        }
        // Global使用语句不可移动
        if (element.getUsedVariable().stream().anyMatch(var -> var.getVariableType().equals(VariableType.GLOBAL))) {
            return true;
        }
        //我们无法监测内存的读写，所以内存的读写语句不可移动，过早读取可能导致错误，中间可能对其进行了修改。
        return false;
    }

    public void InitialVarUseElement() {
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach
                (element -> element.getUsedVariable().forEach
                        (var -> AddVarUseElementSafely(var,element))));
    }

    public void AddVarUseElementSafely(VariableOperand var,PrimaryElement element) {
        if (!VarUseElement.containsKey(var)) {
            VarUseElement.put(var,new HashSet<>());
        }
        VarUseElement.get(var).add(element);
    }

    public void ProcessScheduleLate() {
        ElementVisitedSet.clear();
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> {
            if (isPinned(element)) {
                ScheduleLate.put(element,normalBlock);
                ElementVisitedSet.add(element);
            }
        }));
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach(element -> {
            if (isPinned(element)) {
                ArrayList<VariableOperand> Def = element.getDefineVariable();
                for(VariableOperand var : Def) {
                    HashSet<PrimaryElement> UseElement = VarUseElement.getOrDefault(var,new HashSet<>());
                    UseElement.forEach(this::ScheduleLateFunc);
                }
            }
        }));
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

    public NormalBlock SearchPhiValid(NormalBlock E) {
        for(Map.Entry<NormalBlock,HashSet<NormalBlock>> entry : DomBlockMap.entrySet()) {
            if (entry.getValue().contains(E)) {
                return entry.getKey();
            }
        }
        return null;
    }


    private HashSet<NormalBlock> GenerateRelateBlock(PhiElement phiElement) {
        HashSet<NormalBlock> hashSet = new HashSet<>();
        hashSet.addAll(phiElement.getOriBlockDefMap().keySet());
        return hashSet;
    }


    public void ScheduleLateFunc(PrimaryElement element) {
        if (ElementVisitedSet.contains(element)) {
            return;
        }
        ElementVisitedSet.add(element);
        NormalBlock lca = null;
        ArrayList<VariableOperand> Def = element.getDefineVariable();
        for(VariableOperand var : Def) {
            HashSet<PrimaryElement> DefVar = VarUseElement.getOrDefault(var,new HashSet<>());
            for(PrimaryElement UseElement : DefVar) {
                ScheduleLateFunc(UseElement);
                NormalBlock UseBlock = ScheduleLate.get(UseElement);
                if (UseElement instanceof PhiElement phiElement) {
                    ArrayList<NormalBlock> PrevBlocks = prevMap.getOrDefault(UseBlock,new ArrayList<>());
                    HashSet<NormalBlock> RelateBlocks = GenerateRelateBlock(phiElement);
                    GenerateDomBlocks(RelateBlocks,UseBlock);
                    for (NormalBlock normalBlock : PrevBlocks) {
                        ArrayList<NormalBlock> normalBlockArrayList = phiElement.OriDefBlockListGetBlockFromOperands(var);
                        for (NormalBlock perBlock : normalBlockArrayList) {
                            if (DomBlockMap.get(perBlock).contains(normalBlock)) {
                                UseBlock = normalBlock;
                                lca = FindLCA(lca,UseBlock);
                            }
                        }
                        //if (DomBlockMap.get(phiElement.OriDefBlockListGetBlockFromOperand(var)).contains(normalBlock)) {
                        //    UseBlock = normalBlock;
                        //    break;
                        //}
                    }
                } else {
                    lca = FindLCA(lca,UseBlock);
                }
            }
        }
        if (Def.isEmpty() || lca == null) {
            lca = OriginElementBlockMap.get(element);
        }
        SelectBlock(element,lca);
        ScheduleLate.put(element,lca);
    }


    public void SelectBlock(PrimaryElement element,NormalBlock lca) {
        NormalBlock ElementBlock = ScheduleResult.getOrDefault(element,ScheduleEarly.getOrDefault(element,OriginElementBlockMap.get(element)));
        NormalBlock Best = lca;
        while (lca != ElementBlock && lca != null) {
            int LcaLoopDeep = CircleDeep.get(lca);
            int BestLoopDeep = CircleDeep.get(Best);
            if (LcaLoopDeep < BestLoopDeep) {
                Best = lca;
            }
            lca = ssaConvert.DirectDomination.get(lca);
        }
        ScheduleResult.put(element,Best);
    }

    public void GenerateResult() {
        ScheduleEarly.forEach((element, normalBlock) -> {
            if (!ScheduleResult.containsKey(element)) {
                ScheduleResult.put(element,normalBlock);
            }
        });
        OriginElementBlockMap.forEach((element, normalBlock) -> {
            if (!ScheduleResult.containsKey(element)) {
                ScheduleResult.put(element,normalBlock);
            }
        });
    }

    public NormalBlock FindLCA(NormalBlock a,NormalBlock b) {
        if (a == null) {
            return b;
        }
        while (DominationDeep.get(a) > DominationDeep.get(b)) {
            a = ssaConvert.DirectDomination.get(a);
        }
        while (DominationDeep.get(b) > DominationDeep.get(a)) {
            b = ssaConvert.DirectDomination.get(b);
        }
        while (a != b) {
            a = ssaConvert.DirectDomination.get(a);
            b = ssaConvert.DirectDomination.get(b);
        }
        return a;
    }

    public void ProcessPlaceInstr() {
        HashMap<NormalBlock,ArrayList<PrimaryElement>> collects = CollectNormalBlocksElement();
        for (NormalBlock normalBlock : normalBlocks) {
            ArrayList<PrimaryElement> elements = collects.get(normalBlock);
            normalBlock.IntermediateExpression.clear();
            ArrayList<PrimaryElement> tagElements = CollectTagElements(elements);
            ArrayList<PrimaryElement> phiElements = CollectPhiElements(elements);
            ArrayList<PrimaryElement> JumpBrElements = CollectJumpBrElements(elements);
            elements.removeAll(tagElements);
            elements.removeAll(phiElements);
            elements.removeAll(JumpBrElements);
            normalBlock.IntermediateExpression.addAll(tagElements);
            normalBlock.IntermediateExpression.addAll(phiElements);
            for(NormalBlock visitBlock : VisitOrder) {
                ArrayList<PrimaryElement> RecordElements = ElementRecord.get(visitBlock);
                for (PrimaryElement element : RecordElements) {
                    if (elements.contains(element)) {
                        normalBlock.IntermediateExpression.add(element);
                        elements.remove(element);
                    }
                }
            }
            normalBlock.IntermediateExpression.addAll(JumpBrElements);
        }
    }

    public boolean CheckPlaceValid(int BaseIndex,int CheckIndex,ArrayList<PrimaryElement> elements,PrimaryElement CheckElement) {
        boolean Valid = true;
        ArrayList<VariableOperand> CheckDef = CheckElement.getDefineVariable();
        ArrayList<VariableOperand> CheckUse = CheckElement.getUsedVariable();
        while (BaseIndex < CheckIndex && BaseIndex < elements.size()) {
            PrimaryElement element = elements.get(BaseIndex);
            if (element.getUsedVariable().stream().anyMatch(CheckDef::contains)) {
                Valid = false;
                return Valid;
            }
            BaseIndex++;
        }
        while (CheckIndex < elements.size()) {
            PrimaryElement element = elements.get(CheckIndex);
            if (element.getDefineVariable().stream().anyMatch(CheckUse::contains)) {
                Valid = false;
                return Valid;
            }
            CheckIndex++;
        }
        return Valid;
    }

    public ArrayList<PrimaryElement> CollectReturnElements(ArrayList<PrimaryElement> elements) {
        return elements.stream().filter(primaryElement -> primaryElement instanceof OtherElements otherElements && OtherElements.isReturn(otherElements))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PrimaryElement> CollectPhiElements(ArrayList<PrimaryElement> elements) {
        return elements.stream().filter(primaryElement -> primaryElement instanceof PhiElement).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PrimaryElement> CollectTagElements(ArrayList<PrimaryElement> elements) {
        return elements.stream().filter(primaryElement -> primaryElement instanceof OtherElements otherElements && OtherElements.isTag(otherElements))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PrimaryElement> CollectJumpBrElements(ArrayList<PrimaryElement> elements) {
        return elements.stream().filter(primaryElement -> primaryElement instanceof BranchElement ||
                (primaryElement instanceof OtherElements otherElements && OtherElements.isJump(otherElements)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public HashMap<NormalBlock,ArrayList<PrimaryElement>> CollectNormalBlocksElement() {
        HashMap<NormalBlock,ArrayList<PrimaryElement>> hashMap = new HashMap<>();
        normalBlocks.forEach(normalBlock -> hashMap.put(normalBlock,new ArrayList<>()));
        for (Map.Entry<PrimaryElement,NormalBlock> entry : ScheduleResult.entrySet()) {
            ArrayList<PrimaryElement> elements = hashMap.getOrDefault(entry.getValue(),new ArrayList<>());
            elements.add(entry.getKey());
            hashMap.put(entry.getValue(),elements);
        }
        return hashMap;
    }
}
