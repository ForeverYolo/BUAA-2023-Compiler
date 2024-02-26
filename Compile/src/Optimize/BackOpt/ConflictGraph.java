package Optimize.BackOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ConflictGraph {
    protected ActiveAnalyze activeAnalyze;
    protected HashSet<VariableOperand> nodeSet;
    protected HashSet<VariableOperand> memoryList;
    protected HashSet<VariableOperand> registerList;
    protected HashMap<VariableOperand,HashSet<VariableOperand>> graph;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<VariableOperand,Integer> CountMap;

    public ConflictGraph(ActiveAnalyze activeAnalyze) {
        graph = new HashMap<>();
        nodeSet = new HashSet<>();
        memoryList = new HashSet<>();
        registerList = new HashSet<>();
        this.activeAnalyze = activeAnalyze;
        this.normalBlocks = activeAnalyze.normalBlocks;
        this.CountMap = activeAnalyze.CountMap;
    }

    public void insertNode(VariableOperand variableOperand) {
        nodeSet.add(variableOperand);
        graph.put(variableOperand, new HashSet<>());
    }

    public void insertNodes(ArrayList<VariableOperand> nodes) {
        nodes.stream().filter(i -> !graph.containsKey(i) && isValidLeftValue(i) && CountMap.getOrDefault(i,0) > 1).forEach(this::insertNode);
    }

    private boolean isValidLeftValue(VariableOperand leftValue) {
        return leftValue.getVariableType().equals(VariableType.VARIABLE) || leftValue.getVariableType().equals(VariableType.TEMP) || leftValue.getVariableType().equals(VariableType.PARAM);
    }

    public void createEdge(VariableOperand src1, VariableOperand src2) {
        if (isValidLeftValue(src1) && isValidLeftValue(src2) && src1 != src2
                && CountMap.getOrDefault(src1,0) > 1
                && CountMap.getOrDefault(src2,0) > 1) {
            if (!src1.equals(src2)) {
                if (!nodeSet.contains(src1)) {
                    insertNode(src1);
                }
                if (!graph.containsKey(src2)) {
                    insertNode(src2);
                }
                graph.get(src1).add(src2);
                graph.get(src2).add(src1);
            }
        }
    }

    public void ProcessConflictGraph() {
        for(NormalBlock normalBlock : normalBlocks) {
            HashSet<VariableOperand> BlockIn = activeAnalyze.BlockIn.getOrDefault(normalBlock,new HashSet<>());
            BlockIn.forEach(var1 -> BlockIn.forEach(var2-> createEdge(var1,var2)));
            HashSet<VariableOperand> BlockOut = new HashSet<>(activeAnalyze.BlockOut.getOrDefault(normalBlock,new HashSet<>()));
            for (int i = normalBlock.IntermediateExpression.size() - 1; i >= 0; i--) {
                ArrayList<VariableOperand> ElementDef = normalBlock.IntermediateExpression.get(i).getDefineVariable();
                this.insertNodes(ElementDef);
                ArrayList<VariableOperand> ElementUse = normalBlock.IntermediateExpression.get(i).getUsedVariable();
                this.insertNodes(ElementUse);
                ElementDef.forEach(var1 -> BlockOut.forEach(var2 -> createEdge(var1,var2)));
                ElementDef.forEach(BlockOut::remove);
                BlockOut.addAll(ElementUse);
                //收集调用函数语句入口的活跃变量，在后续写回这些活跃变量。
                if (normalBlock.IntermediateExpression.get(i).getOperatorName().equals("Call")) {
                    normalBlock.getFuncCallActiveMap().put(i,new HashSet<>(BlockOut));
                }
            }
        }
    }

    public void DeleteNode(VariableOperand var) {
        HashSet<VariableOperand> Record = new HashSet<>(graph.get(var));
        Record.forEach(i -> graph.get(i).remove(var));
        graph.remove(var);
    }

}
