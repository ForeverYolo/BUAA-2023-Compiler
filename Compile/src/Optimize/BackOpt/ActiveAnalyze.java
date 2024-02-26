package Optimize.BackOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Optimize.MidOpt.ControlFlowGraph;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ActiveAnalyze {
    protected ControlFlowGraph controlFlowGraph;
    protected HashMap<NormalBlock,HashSet<VariableOperand>> BlockDefs;
    protected HashMap<NormalBlock,HashSet<VariableOperand>> BlockUses;
    protected HashMap<NormalBlock,HashSet<VariableOperand>> BlockIn;
    protected HashMap<NormalBlock,HashSet<VariableOperand>> BlockOut;
    protected HashMap<NormalBlock,ArrayList<NormalBlock>> prevMap;
    protected HashMap<NormalBlock,ArrayList<NormalBlock>> nextMap;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<VariableOperand,Integer> CountMap;

    public ActiveAnalyze(ControlFlowGraph controlFlowGraph) {
        this.controlFlowGraph = controlFlowGraph;
        this.BlockDefs = new HashMap<>();
        this.BlockUses = new HashMap<>();
        this.BlockOut = new HashMap<>();
        this.BlockIn = new HashMap<>();
        this.normalBlocks = controlFlowGraph.getNormalBlocks();
        this.prevMap = controlFlowGraph.getPrevMap();
        this.nextMap = controlFlowGraph.getNextMap();
        this.CountMap = new HashMap<>();
    }

    public void ProcessDefUseAnalyze() {
        normalBlocks.forEach(normalBlock -> {
            HashSet<VariableOperand> HashUse = new HashSet<>();
            HashSet<VariableOperand> HashDef = new HashSet<>();
            BlockDefs.put(normalBlock,HashDef);
            BlockUses.put(normalBlock,HashUse);
            normalBlock.IntermediateExpression.forEach(element -> {
                ArrayList<VariableOperand> ElementUse = element.getUsedVariable();
                ElementUse = ElementUse.stream().filter(Var ->
                        !Var.getVariableType().equals(VariableType.RETURN)).collect(Collectors.toCollection(ArrayList::new));
                ElementUse.stream().filter(Var -> !HashDef.contains(Var)).forEach(HashUse::add);
                ArrayList<VariableOperand> ElementDef = element.getDefineVariable();
                ElementDef = ElementDef.stream().filter(Var ->
                        !Var.getVariableType().equals(VariableType.RETURN)).collect(Collectors.toCollection(ArrayList::new));
                HashDef.addAll(ElementDef);
            });
        });
    }

    public void initializeGlobalParamMemVisit() {
//        BlockDefs.forEach((normalBlock, variableOperands) -> {
//            variableOperands.forEach(var -> {
//                if (var.getVariableType().equals(VariableType.GLOBAL) /*|| var.getVariableType().equals(VariableType.PARAM)*/) {
//                    var.setMemVisit(true);
//                }
//            });
//        });
//        BlockUses.forEach((normalBlock, variableOperands) -> {
//            variableOperands.forEach(var -> {
//                if (var.getVariableType().equals(VariableType.GLOBAL) /*|| var.getVariableType().equals(VariableType.PARAM)*/) {
//                    var.setMemVisit(true);
//                }
//            });
//        });
    }



    public void ProcessInOutAnalyze() {
        ArrayList<NormalBlock> normalBlocksReverse = new ArrayList<>(normalBlocks);
        Collections.reverse(normalBlocksReverse);
        boolean ArriveFixPoint = false;
        while (!ArriveFixPoint) {
            ArriveFixPoint = true;
            for (NormalBlock normalBlock : normalBlocksReverse) {
                ArriveFixPoint &= GenerateInOut(normalBlock);
            }
        }
    }

    public void ProcessSpaningInOut() {
        normalBlocks.forEach(normalBlock -> {
            HashMap<VariableOperand,Integer> BlockCount = new HashMap<>();
            ArrayList<PrimaryElement> elements = normalBlock.IntermediateExpression;
            for (PrimaryElement element : elements) {
                ArrayList<VariableOperand> ElementVars = new ArrayList<>();
                ElementVars.addAll(element.getUsedVariable());
                ElementVars.addAll(element.getDefineVariable());
                for(VariableOperand var : ElementVars) {
                    if (!BlockCount.containsKey(var)) {
                        BlockCount.put(var,1);
                    }
                }
            }
            BlockCount.forEach((var,count) -> {
                if (!CountMap.containsKey(var)) {
                    CountMap.put(var,count);
                } else {
                    CountMap.put(var,CountMap.get(var) + count);
                }
            });
        });
        BlockIn.forEach((block,vars) -> {
           HashSet<VariableOperand> TempVars = new HashSet<>(vars);
           TempVars.forEach(var -> {
               if (CountMap.get(var) <= 1) {
                   vars.remove(var);
               }
               //如果是全局变量或者参数，那么就不参与全局寄存器分配，也不参与局部分配，使用内存访问。
               if (var.getVariableType().equals(VariableType.GLOBAL) /*|| var.getVariableType().equals(VariableType.PARAM)*/) {
                   //var.setMemVisit(true);
                   vars.remove(var);
               }
           });
        });
        BlockOut.forEach((block,vars) -> {
            HashSet<VariableOperand> TempVars = new HashSet<>(vars);
            TempVars.forEach(var -> {
                if (CountMap.get(var) <= 1) {
                    vars.remove(var);
                }
                //如果是全局变量或者参数，仅仅在某一块活跃，那么就不参与全局寄存器分配，也不参与局部分配，使用内存访问。
                if (var.getVariableType().equals(VariableType.GLOBAL) /*|| var.getVariableType().equals(VariableType.PARAM)*/) {
                    //var.setMemVisit(true);
                    vars.remove(var);
                }
            });
        });
    }

    public boolean GenerateInOut(NormalBlock normalBlock) {
        HashSet<VariableOperand> HashOut = new HashSet<>();
        ArrayList<NormalBlock> NextBlocks = nextMap.getOrDefault(normalBlock,new ArrayList<>());
        NextBlocks.forEach(NextBlock -> HashOut.addAll(BlockIn.getOrDefault(NextBlock,new HashSet<>())));
        HashSet<VariableOperand> HashIn = new HashSet<>(HashOut);
        HashIn.removeAll(BlockDefs.getOrDefault(normalBlock,new HashSet<>()));
        HashIn.addAll(BlockUses.getOrDefault(normalBlock,new HashSet<>()));
        //和原来的In比较，如果不一样，说明还没到达不动点，至少对于这个Block来说。
        BlockOut.put(normalBlock,HashOut);
        if (HashIn.equals(BlockIn.getOrDefault(normalBlock,new HashSet<>()))) {
            return true;
        }
        BlockIn.put(normalBlock,HashIn);
        return false;
    }



}
