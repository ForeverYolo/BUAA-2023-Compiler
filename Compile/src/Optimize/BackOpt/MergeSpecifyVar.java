package Optimize.BackOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.PhiElement;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Optimize.MidOpt.SSAConvert;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MergeSpecifyVar {
    protected IntermediateBuilder intermediateBuilder;
    protected ArrayList<NormalBlock> normalBlocks;
    public MergeSpecifyVar(IntermediateBuilder intermediateBuilder, SSAConvert ssaConvert) {
        this.intermediateBuilder = intermediateBuilder;
        this.normalBlocks = ssaConvert.getNormalBlocks();
    }

    public void ProcessMerge() {
        HashMap<VariableOperand,VariableOperand> RenameMap = intermediateBuilder.getRenameToNameMap();
        normalBlocks.forEach(normalBlock -> {
            ArrayList<PrimaryElement> elements = new ArrayList<>(normalBlock.IntermediateExpression);
            elements.forEach(element -> {
                HashSet<VariableOperand> elementUse = new HashSet<>(element.getUsedVariable());
                elementUse.forEach(variableOperand -> {
                    if (variableOperand.getVariableType().equals(VariableType.GLOBAL) /*|| variableOperand.getVariableType().equals(VariableType.PARAM)*/) {
                        if (RenameMap.containsKey(variableOperand)) {
                            element.ReplaceUseVariable(variableOperand,RenameMap.get(variableOperand));
                        }
                    }
                });
                HashSet<VariableOperand> elementDef = new HashSet<>(element.getDefineVariable());
                elementDef.forEach(variableOperand -> {
                    if (variableOperand.getVariableType().equals(VariableType.GLOBAL) /*|| variableOperand.getVariableType().equals(VariableType.PARAM)*/) {
                        if (RenameMap.containsKey(variableOperand)) {
                            element.ReplaceDefineVariable(variableOperand,RenameMap.get(variableOperand));
                        }
                    }
                });
                if (element instanceof PhiElement) {
                    ArrayList<VariableOperand> elementDefine = element.getDefineVariable();
                    if (elementDefine.get(0).getVariableType().equals(VariableType.GLOBAL) /*|| elementDefine.get(0).getVariableType().equals(VariableType.PARAM)*/) {
                        normalBlock.IntermediateExpression.remove(element);
                    }
                }
            });
        });
    }
}
