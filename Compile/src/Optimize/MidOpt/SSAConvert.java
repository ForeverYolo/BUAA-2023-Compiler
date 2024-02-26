package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.*;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Tools.Combination;

import java.util.*;
import java.util.stream.Collectors;

public class SSAConvert {
    protected FuncOperand func;
    protected HashMap<VariableOperand,VariableOperand> GlobalReachingDef;
    protected ControlFlowGraph controlFlowGraph;
    protected HashMap<NormalBlock, HashSet<NormalBlock>> BeDomination;
    protected HashMap<NormalBlock,HashSet<NormalBlock>> Domination;
    protected HashMap<NormalBlock,HashSet<NormalBlock>> StrictDomination;
    protected HashMap<NormalBlock,NormalBlock> DirectDomination;
    protected HashMap<NormalBlock,HashSet<NormalBlock>> DominanceFrontier;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<VariableOperand,HashSet<NormalBlock>> VariableDef;
    protected HashSet<VariableOperand> TempVarNeedToBeReNameArray;
    protected HashMap<NormalBlock,NormalBlock> DeleteBlockMap;
    //--------------------------------以下内容在重新构建SSA后失效------------------------------------------------------------
    protected HashMap<VariableOperand,VariableOperand> ReachingDef;
    protected HashMap<VariableOperand, HashMap<NormalBlock,ArrayList<Combination<Integer,Integer>>>> DefDominateRange;
    protected HashSet<NormalBlock> SurplusBlockInReNameProcess;

    public SSAConvert(ControlFlowGraph controlFlowGraph,FuncOperand func) {
        this.controlFlowGraph = controlFlowGraph;
        this.BeDomination = new HashMap<>();
        this.Domination = new HashMap<>();
        this.StrictDomination = new HashMap<>();
        this.DirectDomination = new HashMap<>();
        this.DominanceFrontier = new HashMap<>();
        this.VariableDef = new HashMap<>();
        this.normalBlocks = controlFlowGraph.getNormalBlocks();
        this.DefDominateRange = new HashMap<>();
        this.ReachingDef = new HashMap<>();
        this.func = func;
        this.SurplusBlockInReNameProcess = new HashSet<>(this.normalBlocks);
        this.TempVarNeedToBeReNameArray = new HashSet<>();
        this.DeleteBlockMap = new HashMap<>();
    }

    public void setDeleteBlockMap(HashMap<NormalBlock, NormalBlock> deleteBlockMap) {
        DeleteBlockMap = deleteBlockMap;
    }

    public HashMap<NormalBlock, NormalBlock> getDeleteBlockMap() {
        return DeleteBlockMap;
    }

    public void setGlobalReachingDef(HashMap<VariableOperand, VariableOperand> globalReachingDef) {
        GlobalReachingDef = globalReachingDef;
    }

    public HashMap<VariableOperand, VariableOperand> getReachingDef() {
        return ReachingDef;
    }

    public HashSet<NormalBlock> getANormalBlockBeDomination(NormalBlock normalBlock) {
        if (!BeDomination.containsKey(normalBlock)) {
            HashSet<NormalBlock> hashSet = new HashSet<>(normalBlocks);
            BeDomination.put(normalBlock, hashSet);
        }
        return BeDomination.get(normalBlock);
    }

    public void ProcessBeDomination() {
        boolean flag = true;
        while(flag) {
            flag = false;
            for(NormalBlock target: normalBlocks) {
                ArrayList<NormalBlock> prevBlock = new ArrayList<>(controlFlowGraph.prevMap.getOrDefault(target,new ArrayList<>()));
                HashSet<NormalBlock> TargetBeDomination = getANormalBlockBeDomination(target);
                int beforeProcessSize = TargetBeDomination.size();
                TargetBeDomination.clear();
                HashMap<NormalBlock,Integer> Intersection = new HashMap<>();
                for(NormalBlock prev : prevBlock) {
                    HashSet<NormalBlock> PrevBeDomination = getANormalBlockBeDomination(prev);
                    if (!PrevBeDomination.isEmpty()) {
                        PrevBeDomination.forEach(PrevBeDominationBlock -> {
                            int count = Intersection.getOrDefault(PrevBeDominationBlock, 0) + 1;
                            Intersection.put(PrevBeDominationBlock,count);
                        });
                    } else {
                        Intersection.forEach((normalBlock, integer) -> Intersection.put(normalBlock,integer + 1));
                    }
                }
                // 如果是入口块，那么就加入一个虚拟块
                if (target == controlFlowGraph.EntryBlock) {
                    prevBlock.add(new NormalBlock(-1,null));
                }
                Intersection.forEach((PrevBeDominationBlock, integer) -> {
                    if(integer == prevBlock.size()) {
                        TargetBeDomination.add(PrevBeDominationBlock);
                    }
                });
                TargetBeDomination.add(target);
                int afterProcessSize = TargetBeDomination.size();
                flag = flag || afterProcessSize != beforeProcessSize;
            }
        }
        ProcessDomination();
    }



    public ControlFlowGraph getControlFlowGraph() {
        return controlFlowGraph;
    }

    public void ProcessDomination() {
        for(NormalBlock target : normalBlocks) {
            HashSet<NormalBlock> Domination = new HashSet<>();
            BeDomination.forEach((source, sourceBeDomination) -> {
                if(sourceBeDomination.contains(target)) {
                    Domination.add(source);
                }
            });
            this.Domination.put(target,Domination);
        }
    }

    public void ProcessStrictDomination() {
        for(NormalBlock target : normalBlocks) {
            HashSet<NormalBlock> Domination = this.Domination.get(target);
            HashSet<NormalBlock> StrictDom = new HashSet<>(Domination);
            StrictDom.remove(target);
            this.StrictDomination.put(target,StrictDom);
        }
    }


    public void ProcessDirectDomination() {
        for(NormalBlock target: normalBlocks) {
            HashSet<NormalBlock> TargetStrictBeDom = new HashSet<>(this.BeDomination.get(target));
            TargetStrictBeDom.remove(target);
            for(NormalBlock source: TargetStrictBeDom) {
                HashSet<NormalBlock> SourceStrictDom = this.StrictDomination.get(source);
                boolean flag = true;
                for(NormalBlock sourceDom: SourceStrictDom) {
                    if (sourceDom != target && TargetStrictBeDom.contains(sourceDom)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    this.DirectDomination.put(target,source);
                    break;
                }
            }
        }
    }

    public void ProcessDominanceFrontier() {
        HashMap<NormalBlock, ArrayList<NormalBlock>> nextMap = controlFlowGraph.nextMap;
        nextMap.forEach((a, arrayOfb) -> {
            arrayOfb.forEach(b -> {
               NormalBlock x = a;
               while(x != null && !queryStrictDominate(x,b)) {
                    addDominanceFrontier(x,b);
                    x = getNormalBlockDirectDom(x);
               }
            });
        });
    }

    public NormalBlock getNormalBlockDirectDom(NormalBlock normalBlock) {
        return DirectDomination.getOrDefault(normalBlock, null);
    }

    public void addDominanceFrontier(NormalBlock target,NormalBlock source) {
        if (DominanceFrontier.containsKey(target)) {
            DominanceFrontier.get(target).add(source);
        } else {
            HashSet<NormalBlock> sources = new HashSet<>();
            DominanceFrontier.put(target,sources);
            DominanceFrontier.get(target).add(source);
        }
    }

    // 查询 a 是否严格支配 b
    public boolean queryStrictDominate(NormalBlock a,NormalBlock b) {
        HashSet<NormalBlock> strictDomForA = this.StrictDomination.get(a);
        return strictDomForA.contains(b);
    }

    public void ProcessVariableDefBlocks(IntermediateBuilder intermediateBuilder) {
        CheckTempVarReDefine();
        AddImplicitDef(intermediateBuilder);
        ArrayList<VariableOperand> VariableArray = new ArrayList<>();
        if (func != null) {
            VariableArray.addAll(func.getFormalParams().stream().map(operand -> (VariableOperand)operand).collect(Collectors.toCollection(ArrayList::new)));
        }
        VariableArray.addAll(intermediateBuilder.getGlobalVariableArray());
        VariableArray.addAll(intermediateBuilder.getNormalVariableArray());
        VariableArray.addAll(TempVarNeedToBeReNameArray);
        VariableArray.forEach(this::GenerateVariableDefBlock);
    }

    public void AddImplicitDef(IntermediateBuilder intermediateBuilder) {
        intermediateBuilder.getGlobalVariableArray().forEach(variableOperand -> addVariableDef(controlFlowGraph.EntryBlock,variableOperand));
        if (func != null) {
            func.getFormalParams().forEach(operand -> addVariableDef(controlFlowGraph.EntryBlock,(VariableOperand) operand));
        }
    }

    public void CheckTempVarReDefine() {
        HashMap<VariableOperand,PrimaryElement> VarDef = new HashMap<>();
        normalBlocks.forEach(normalBlock -> normalBlock.IntermediateExpression.forEach
                (element -> element.getDefineVariable().forEach
                        (var -> {
                            if (var == null) {
                                System.out.println("123");
                            }
                            if (VarDef.containsKey(var) && var.getVariableType().equals(VariableType.TEMP)) {
                                TempVarNeedToBeReNameArray.add(var);
                            } else {
                                VarDef.put(var,element);
                            }
                        })));
    }

    public void GenerateVariableDefBlock(VariableOperand targetVariable) {
        //---------------------------处理正常的局部变量------------------------------
        for(NormalBlock normalBlock : normalBlocks) {
            ArrayList<PrimaryElement> expression = normalBlock.IntermediateExpression;
            for (PrimaryElement element : expression) {
                if (checkDefValid(element,targetVariable)) {
                    addVariableDef(normalBlock,targetVariable);
                }
            }
        }
    }

    public boolean checkDefValid(PrimaryElement element,VariableOperand targetVariable) {
        if (element instanceof CalculateElement calculateElement) {
            VariableOperand sourceVariable = calculateElement.getDst();
            return sourceVariable == targetVariable;
        } else if (element instanceof MemoryElements memoryElement) {
            return (MemoryElements.isLoad(memoryElement) || MemoryElements.isAlloc(memoryElement))
                    && memoryElement.getValue() == targetVariable;
        } else if (element instanceof OtherElements otherElement) {
            return (OtherElements.isDecl(otherElement) || OtherElements.isScan(otherElement))
                    && otherElement.getOperand() == targetVariable;
        }
        return false;
    }

    public void addVariableDef(NormalBlock normalBlock,VariableOperand target) {
        if (!VariableDef.containsKey(target)) {
            HashSet<NormalBlock> varDefBlock = new HashSet<>();
            VariableDef.put(target,varDefBlock);
        }
        VariableDef.get(target).add(normalBlock);
    }

    public void ProcessInsertPhi() {
        VariableDef.forEach((Variable, DefBlocks) -> {
            Queue<NormalBlock> W = new LinkedList<>(DefBlocks);
            HashSet<NormalBlock> F = new HashSet<>();
            while(!W.isEmpty()) {
                NormalBlock defBlock = W.poll();
                HashSet<NormalBlock> DFBlocks = DominanceFrontier.getOrDefault(defBlock,new HashSet<>());
                for(NormalBlock dfBlock : DFBlocks) {
                    if (!F.contains(dfBlock)) {
                        PhiElement phiElement = new PhiElement(Variable);
                        ArrayList<PrimaryElement> DFExpressions = dfBlock.IntermediateExpression;
                        if (!DFExpressions.isEmpty() && CheckTagElement(DFExpressions.get(0))) {
                            DFExpressions.add(1,phiElement);
                        } else {
                            DFExpressions.add(0,phiElement);
                        }
                        F.add(dfBlock);
                        if(!DefBlocks.contains(dfBlock)) {
                            W.add(dfBlock);
                        }
                    }
                }
            }
        });
    }

    public boolean CheckTagElement(PrimaryElement element) {
        return element instanceof OtherElements otherElement && OtherElements.isTag(otherElement);
    }


    public void ProcessRename(IntermediateBuilder intermediateBuilder) {
        //--------------------将全局定义加入并生成其控制域----------------------
        ArrayList<VariableOperand> GlobalVars = intermediateBuilder.getGlobalVariableArray();
        if (GlobalVars != null) {
            GlobalVars.forEach(oldVar -> {
                VariableOperand newVar = intermediateBuilder.giveVarReturnRenameVar(oldVar);
                ReachingDef.put(newVar, ReachingDef.get(oldVar));
                ReachingDef.put(oldVar,newVar);
                GenerateDefRange(oldVar,newVar,controlFlowGraph.EntryBlock,-1,intermediateBuilder);
            });
        }
        //--------------------将局部参数加入并生成其控制域-----------------------
        if (func != null) {
            func.getFormalParams().forEach(param -> {
                VariableOperand oldParam = (VariableOperand) param;
                VariableOperand newParam = intermediateBuilder.giveVarReturnRenameVar(oldParam);
                func.ReplaceFormalParams(oldParam,newParam);
                ReachingDef.put(newParam, ReachingDef.get(param));
                ReachingDef.put(oldParam,newParam);
                GenerateDefRange(oldParam,newParam,controlFlowGraph.EntryBlock,-1,intermediateBuilder);
            });
        }
        RenameByDfs(controlFlowGraph.EntryBlock,intermediateBuilder);
        //---------------------从Entry块开始DFS支配树--------------------------
//        while (!SurplusBlockInReNameProcess.isEmpty()) {
//            for(NormalBlock DeadBlock: SurplusBlockInReNameProcess) {
//                if (DeadBlock.getControlFGPrev().isEmpty()) {
//                    RenameByDfs(DeadBlock,intermediateBuilder);
//                    break;
//                }
//            }
//        }
        //-------------------------计算其余死块---------------------------------
    }

    public void RenameByDfs(NormalBlock normalBlock,IntermediateBuilder intermediateBuilder) {
        for(int i = 0; i < normalBlock.IntermediateExpression.size(); i++) {
            PrimaryElement element = normalBlock.IntermediateExpression.get(i);
            if (!(element instanceof PhiElement)) {
                ArrayList<VariableOperand> Use = element.getUsedVariable();
                Use = filterTempReturnVariable(Use);
                for(VariableOperand variable: Use) {
                    updateReachingDef(variable,element,normalBlock,i);
                    element.ReplaceUseVariable(variable, ReachingDef.get(variable));
                }
            }
            ArrayList<VariableOperand> Define = element.getDefineVariable();
            Define = filterTempReturnVariable(Define);
            for(VariableOperand variable: Define) {
                updateReachingDef(variable,element,normalBlock,i);
                VariableOperand newVariable = intermediateBuilder.giveVarReturnRenameVar(variable);
                //将重命名的Temp也加入进过滤白名单。
                if (newVariable.getVariableType().equals(VariableType.TEMP)) {
                    TempVarNeedToBeReNameArray.add(newVariable);
                }
                element.ReplaceDefineVariable(variable,newVariable);
                ReachingDef.put(newVariable, ReachingDef.get(variable));
                ReachingDef.put(variable,newVariable);
                GenerateDefRange(variable,newVariable,normalBlock,i,intermediateBuilder);
            }
            /*if (element instanceof PhiElement phiElement) {
                ArrayList<VariableOperand> PhiUse = phiElement.getUsedVariable();
                for(VariableOperand variable: PhiUse) {
                    updateReachingDef(variable,phiElement,normalBlock,i);
                    element.ReplaceUseVariable(variable,ReachingDef.get(variable));
                }
            }*/
        }
        SurplusBlockInReNameProcess.remove(normalBlock);
        ArrayList<NormalBlock> DirectDomArray = getBeDirectDomArray(normalBlock);
        DirectDomArray.forEach(target -> RenameByDfs(target,intermediateBuilder));
    }

    public ArrayList<VariableOperand> filterTempReturnVariable(ArrayList<VariableOperand> variables) {
        return variables.stream().filter(variable ->
                !((variable.getVariableType().equals(VariableType.TEMP) && !TempVarNeedToBeReNameArray.contains(variable))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void GenerateDefRange(VariableOperand oldVariable,VariableOperand newVariable,
                                 NormalBlock startBlock,int Index,IntermediateBuilder intermediateBuilder) {
        Queue<NormalBlock> UnCheckedBlocks = new LinkedList<>();
        UnCheckedBlocks.add(startBlock);
        int startIndex = Index + 1;
        while(!UnCheckedBlocks.isEmpty()) {
            NormalBlock unCheckBlock = UnCheckedBlocks.poll();
            ArrayList<PrimaryElement> elements = unCheckBlock.IntermediateExpression;
            int i = startIndex;
            boolean flag = true;
            while (i < elements.size() && flag) {
                PrimaryElement element = elements.get(i);
                ArrayList<VariableOperand> Define = element.getDefineVariable();
                Define = filterTempReturnVariable(Define);
                HashSet<VariableOperand> CommonVar = new HashSet<>(intermediateBuilder.getRenameMap().get(oldVariable));
                CommonVar.add(oldVariable);
                CommonVar.remove(newVariable);
                UpdatePhiUse(oldVariable,newVariable,element,startBlock);
                for(VariableOperand define : Define) {
                    if(CommonVar.contains(define)) {
                        flag = false;
                        addDomRangeElement(newVariable,unCheckBlock,startIndex,i);
                        break;
                    }
                }
                i = i + 1;
            }
            if(flag) {
                if (startIndex <= i - 1) {
                    flag = addDomRangeElement(newVariable,unCheckBlock,startIndex,i - 1);
                }
                //----------------------------支配树遍历--------------------------------
                //ArrayList<NormalBlock> DirectDom = getBeDirectDomArray(unCheckBlock);
                //UnCheckedBlocks.addAll(DirectDom);
                //-----------------------------流图遍历---------------------------------
                if (flag) {
                    ArrayList<NormalBlock> NextBlocks = controlFlowGraph.nextMap.getOrDefault(unCheckBlock,new ArrayList<>());
                    UnCheckedBlocks.addAll(NextBlocks);
                }
            }
            startIndex = 0;
        }
    }

    public void UpdatePhiUse(VariableOperand oldVariable,VariableOperand newVariable,PrimaryElement element,NormalBlock OriDefBlock) {
        if(element instanceof PhiElement phiElement && phiElement.VerifyPhi(oldVariable)) {
            phiElement.AddUseVariable(newVariable,OriDefBlock);
        }
    }


    public ArrayList<NormalBlock> getBeDirectDomArray(NormalBlock target) {
        ArrayList<NormalBlock> DirectDom = new ArrayList<>();
        DirectDomination.forEach((block, DirectDomBlock) -> {
            if(DirectDomBlock == target) {
                DirectDom.add(block);
            }
        });
        return DirectDom;
    }

    public boolean addDomRangeElement(VariableOperand variable,NormalBlock block,int StartIndex,int EndIndex) {
        if (!DefDominateRange.containsKey(variable)) {
            HashMap<NormalBlock,ArrayList<Combination<Integer,Integer>>> Temp = new HashMap<>();
            DefDominateRange.put(variable,Temp);
        }
        if (!DefDominateRange.get(variable).containsKey(block)) {
            ArrayList<Combination<Integer,Integer>> Temp = new ArrayList<>();
            DefDominateRange.get(variable).put(block,Temp);
        }
        Combination<Integer,Integer> range = new Combination<>(StartIndex,EndIndex);
        ArrayList<Combination<Integer,Integer>> ranges = DefDominateRange.get(variable).get(block);
        for(Combination<Integer,Integer> HaveRange : ranges) {
            int Upper = range.getValue();
            int Lower = HaveRange.getKey();
           if (Upper >= Lower) {
               return false;
           }
        }
        DefDominateRange.get(variable).get(block).add(range);
        return true;
    }

    public void updateReachingDef(VariableOperand variable,PrimaryElement element,NormalBlock currentBlock,int index) {
        VariableOperand r = ReachingDef.getOrDefault(variable,null);
        while (!(r == null || CheckDefDominate(r,element,currentBlock,index))) {
            r = ReachingDef.getOrDefault(r,null);
        }
        ReachingDef.put(variable,r);
    }

    public boolean CheckDefDominate(VariableOperand r,PrimaryElement element,NormalBlock currentBlock,int index) {
        HashMap<NormalBlock,ArrayList<Combination<Integer,Integer>>> ValidRange = DefDominateRange.get(r);
        if (ValidRange.containsKey(currentBlock)) {
            ArrayList<Combination<Integer,Integer>> ranges = ValidRange.get(currentBlock);
            boolean Valid = false;
            for(Combination<Integer,Integer> range : ranges) {
                Valid = Valid | (index <= range.getValue() && index >= range.getKey());
            }
            return Valid;
        }
        return false;
    }

    public ArrayList<NormalBlock> getNormalBlocks() {
        return normalBlocks;
    }
}
