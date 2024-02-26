package Optimize.BackOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Operands.VariableOperand;
import Tools.ErrorMessage;

import java.util.*;
import java.util.stream.Collectors;

public class GraphColor {
    protected ConflictGraph conflictGraph;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<Integer,VariableOperand> GlobalRegisters;
    protected HashSet<VariableOperand> memoryList;
    protected ArrayList<VariableOperand> registerList;
    protected HashMap<VariableOperand,HashSet<VariableOperand>> RecordGraph;
    public GraphColor(ConflictGraph conflictGraph, HashMap<Integer,VariableOperand> GlobalRegisters) {
        this.conflictGraph = conflictGraph;
        this.normalBlocks = conflictGraph.normalBlocks;
        this.GlobalRegisters = GlobalRegisters;
        this.memoryList = conflictGraph.memoryList;
        this.registerList = new ArrayList<>(conflictGraph.registerList);
        this.RecordGraph = new HashMap<>();
    }

    public void ProcessAnalyzeByStack() {
        HashSet<VariableOperand> InStack = new HashSet<>();
        int Size = conflictGraph.graph.keySet().size();
        //--------------------------------------保存图----------------------------------------
        conflictGraph.graph.forEach((key,value) -> RecordGraph.put(key,new HashSet<>(value)));
        while (InStack.size() != Size) {
            int minEdge = Integer.MAX_VALUE;
            int maxEdge = 0;
            VariableOperand minNode = null;
            VariableOperand maxNode = null;
            HashSet<VariableOperand> checkVars = conflictGraph.graph.keySet().
                    stream().filter(i -> !InStack.contains(i)).collect(Collectors.toCollection(HashSet::new));
            for (VariableOperand var : checkVars) {
                int Edge = conflictGraph.graph.get(var).size();
                if (minEdge > Edge) {
                    minEdge = Edge;
                    minNode = var;
                }
                if (maxEdge < Edge) {
                    maxEdge = Edge;
                    maxNode = var;
                }
            }
            if (GlobalRegisters.size() > minEdge) {
                InStack.add(minNode);
                registerList.add(minNode);
                conflictGraph.DeleteNode(minNode);
            } else {
                InStack.add(maxNode);
                memoryList.add(maxNode);
                maxNode.setMemVisit(true);
                conflictGraph.DeleteNode(maxNode);
            }
        }
    }

    public void ProcessColor() {
        HashMap<VariableOperand,HashSet<Integer>> ValidColor = new HashMap<>();
        registerList.forEach(i -> {
            HashSet<Integer> Record = new HashSet<>(GlobalRegisters.keySet());
            ValidColor.put(i,Record);
        });
        Collections.reverse(registerList);
        registerList.forEach(i -> {
            int color = ValidColor.get(i).iterator().next();
            i.setAllocatedGlobalRegister(color);
            if (i.isMemVisit()) {
                ErrorMessage.handleSelfCheckError(this.getClass());
            }
            i.setMemVisit(false);
            RecordGraph.get(i).stream().filter(ValidColor::containsKey).forEach(j ->
                    ValidColor.get(j).remove(color));
        });
        conflictGraph.graph = RecordGraph;
    }

}
