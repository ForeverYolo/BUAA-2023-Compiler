package Optimize;

import IntermediateCode.Container.FuncBlock;
import IntermediateCode.Container.NormalBlock;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.FuncOperand;
import ObjectCode.Assembly;
import Optimize.BackOpt.ActiveAnalyze;
import Optimize.BackOpt.ConflictGraph;
import Optimize.BackOpt.GraphColor;
import Optimize.BackOpt.MergeSpecifyVar;
import Optimize.MidOpt.*;
import Tools.FileProcess;
import Tools.GlobalSetting;

import java.util.HashMap;

public class Optimizer {
    protected HashMap<String,ControlFlowGraph> controlFlowGraphs;
    protected HashMap<String,FuncOperand> funcNameMap;
    protected ControlFlowGraph GlobalFlowGraphs;
    protected HashMap<String,SSAConvert> ssaConverts;
    protected SSAConvert GlobalSSAConvert;
    protected IntermediateBuilder intermediateBuilder;
    private Assembly assembly;

    public Optimizer(IntermediateBuilder intermediateBuilder,Assembly assembly) {
        controlFlowGraphs = new HashMap<>();
        ssaConverts = new HashMap<>();
        this.intermediateBuilder = intermediateBuilder;
        this.funcNameMap = new HashMap<>();
        this.assembly = assembly;
    }

    public void ProcessControlFlowGraph(FuncOperand funcOperand, FuncBlock funcBlock) {
        if (GlobalSetting.ControlFlowGraphBuild) {
            ControlFlowGraph controlFlowGraph = new ControlFlowGraph();
            controlFlowGraph.GenerateControlFlowGraph(funcOperand,funcBlock);
            controlFlowGraphs.put(funcOperand.getOperandName(),controlFlowGraph);
        }
    }


    public void ProcessGlobalControlFlowGraph(String name, NormalBlock normalBlock) {
        if (GlobalSetting.ControlFlowGraphBuild) {
            ControlFlowGraph controlFlowGraph = new ControlFlowGraph();
            controlFlowGraph.GenerateControlFlowGraph(normalBlock);
            GlobalFlowGraphs = controlFlowGraph;
        }
    }

    public void ProcessSSAConvert(String s,ControlFlowGraph controlFlowGraph) {
        if (GlobalSetting.SSABuild) {
            SSAConvert ssaConvert = new SSAConvert(controlFlowGraph,funcNameMap.get(s));
            SSAConvert GlobalConvert = GlobalSSAConvert;
            ssaConvert.setGlobalReachingDef(GlobalConvert.getReachingDef());
            ssaConvert.ProcessBeDomination();
            ssaConvert.ProcessStrictDomination();
            ssaConvert.ProcessDirectDomination();
            ssaConvert.ProcessDominanceFrontier();
            ssaConvert.ProcessVariableDefBlocks(intermediateBuilder);
            ssaConvert.ProcessInsertPhi();
            ssaConvert.ProcessRename(intermediateBuilder);
            ssaConverts.put(s,ssaConvert);
        }
    }

    public void ReStructSSAConvert(String s,ControlFlowGraph controlFlowGraph) {
        SSAConvert ssaConvert = new SSAConvert(controlFlowGraph,funcNameMap.get(s));
        SSAConvert GlobalConvert = GlobalSSAConvert;
        ssaConvert.setGlobalReachingDef(GlobalConvert.getReachingDef());
        ssaConvert.ProcessBeDomination();
        ssaConvert.ProcessStrictDomination();
        ssaConvert.ProcessDirectDomination();
        ssaConvert.ProcessDominanceFrontier();
        ssaConvert.ProcessVariableDefBlocks(intermediateBuilder);
        ssaConverts.put(s,ssaConvert);
    }


    public void ProcessGlobalSSAConvert(String s,ControlFlowGraph controlFlowGraph) {
        if (GlobalSetting.SSABuild) {
            SSAConvert ssaConvert = new SSAConvert(controlFlowGraph,null);
            ssaConvert.setGlobalReachingDef(new HashMap<>());
            ssaConvert.ProcessBeDomination();
            ssaConvert.ProcessStrictDomination();
            ssaConvert.ProcessDirectDomination();
            ssaConvert.ProcessDominanceFrontier();
            //ssaConvert.ProcessVariableDefBlocks(intermediateBuilder);
            //ssaConvert.ProcessInsertPhi();
            //ssaConvert.ProcessRename(intermediateBuilder);
            GlobalSSAConvert = ssaConvert;
        }
    }


    public boolean ProcessAggressiveDeadCodeElimination(SSAConvert orderSSAConvert) {
        if (GlobalSetting.AggressiveDeadCodeDeleteOptimize) {
            boolean ModifyFlag;
            boolean IsChanged;
            AggressiveDCE aggressiveDCE = new AggressiveDCE(orderSSAConvert,intermediateBuilder);
            ModifyFlag = aggressiveDCE.ProcessADCE();
            IsChanged = ModifyFlag;
            while (ModifyFlag) {
                FuncBlock funcBlock = orderSSAConvert.getControlFlowGraph().getFuncBlock();
                FuncOperand funcOperand = orderSSAConvert.getControlFlowGraph().getFuncOperand();
                String name = funcOperand.getOperandName();
                ControlFlowGraph controlFlowGraph = new ControlFlowGraph();
                controlFlowGraph.GenerateControlFlowGraph(funcOperand,funcBlock,orderSSAConvert.getControlFlowGraph().getEntryBlock());
                controlFlowGraphs.put(funcOperand.getOperandName(),controlFlowGraph);
                ReStructSSAConvert(name,controlFlowGraphs.get(name));
                aggressiveDCE = new AggressiveDCE(ssaConverts.get(name),intermediateBuilder);
                ModifyFlag = aggressiveDCE.ProcessADCE();
            }
            return IsChanged;
        }
        return false;
    }


    public boolean ProcessGlobalAggressiveDeadCodeElimination() {
        if (GlobalSetting.AggressiveDeadCodeDeleteOptimize) {
            boolean ModifyFlag;
            boolean IsChanged;
            AggressiveDCE aggressiveDCE = new AggressiveDCE(GlobalSSAConvert,intermediateBuilder);
            ModifyFlag = aggressiveDCE.ProcessADCE();
            IsChanged = ModifyFlag;
            while (ModifyFlag) {
                ProcessGlobalControlFlowGraph("Global",intermediateBuilder.getGlobalBlock());
                ReStructSSAConvert("Global",GlobalFlowGraphs);
                aggressiveDCE = new AggressiveDCE(GlobalSSAConvert,intermediateBuilder);
                ModifyFlag = aggressiveDCE.ProcessADCE();
            }
            return IsChanged;
        }
        return false;
    }

    public void ProcessCodeMotion(SSAConvert ssaConvert) {
        if (GlobalSetting.GlobalCodeMotionOptimize) {
            GlobalCodeMotion globalCodeMotion = new GlobalCodeMotion(ssaConvert);
            globalCodeMotion.ProcessVisitOrder();
            globalCodeMotion.ProcessRecordElementOrder();
            globalCodeMotion.GenerateCircleDeep();
            globalCodeMotion.MarkDominationDeep();
            globalCodeMotion.InitialVarDefElement();
            globalCodeMotion.InitialVarUseElement();
            globalCodeMotion.InitialElementBlockMap();
            globalCodeMotion.ProcessScheduleEarly();
            globalCodeMotion.ProcessScheduleLate();
            globalCodeMotion.GenerateResult();
            globalCodeMotion.ProcessPlaceInstr();
        }
    }

    public boolean ProcessValueNumbering(SSAConvert ssaConvert) {
        if (GlobalSetting.GlobalValueNumberingOptimize) {
            boolean IsChanged;
            GlobalValueNumbering globalValueNumbering = new GlobalValueNumbering(ssaConvert,intermediateBuilder.getOptimizeWhiteList());
            globalValueNumbering.GenerateVisitOrder();
            IsChanged = globalValueNumbering.ProcessGlobalValueNumbering();
            return IsChanged;
        }
        return false;
    }

    public boolean ProcessGlobalValueNumbering() {
        if (GlobalSetting.GlobalValueNumberingOptimize) {
            boolean IsChanged;
            GlobalValueNumbering globalValueNumbering = new GlobalValueNumbering(GlobalSSAConvert,intermediateBuilder.getOptimizeWhiteList());
            globalValueNumbering.GenerateVisitOrder();
            IsChanged = globalValueNumbering.ProcessGlobalValueNumbering();
            return IsChanged;
        }
        return false;
    }

    public void GlobalRedoOptimize(SSAConvert ssaConvert) {
        boolean OptimizeValid = true;
        int count = 0;
        while (OptimizeValid) {
            count++;
            OptimizeValid = ProcessGlobalAggressiveDeadCodeElimination();
            //FileProcess.WriteSSAIntermediateCodeFile("GlobalAfterADCE_" + count + ".txt",intermediateBuilder,false);
            OptimizeValid |= ProcessGlobalValueNumbering();
            //FileProcess.WriteSSAIntermediateCodeFile("GlobalAfterGVN_" + count + ".txt",intermediateBuilder,false);
        }
    }

    public void RedoOptimize(String s) {
        boolean OptimizeValid = true;
        int count = 0;
        while (OptimizeValid) {
            count++;
            OptimizeValid = ProcessAggressiveDeadCodeElimination(ssaConverts.get(s));
            FileProcess.WriteSSAIntermediateCodeFile("AfterADCE_" + count + ".txt",intermediateBuilder,false);
            OptimizeValid |= ProcessValueNumbering(ssaConverts.get(s));
            //FileProcess.WriteSSAIntermediateCodeFile("AfterGVN_" + count + ".txt",intermediateBuilder,false);
            ProcessCodeMotion(ssaConverts.get(s));
            FileProcess.WriteSSAIntermediateCodeFile("AfterGCM_" + count + ".txt",intermediateBuilder,false);
        }
    }

    public void ProcessRemovePhi(SSAConvert ssaConvert) {
        RemovePhi removePhi = new RemovePhi(ssaConvert,intermediateBuilder);
        removePhi.ProcessConvertPhiToParaCopy();
        removePhi.ProcessConvertParaCopyToMove();
    }

    public void ProcessReplaceBranchBlock(SSAConvert ssaConvert) {
        BranchFixAndOpt branchFixAndOpt = new BranchFixAndOpt(ssaConvert);
        branchFixAndOpt.ProcessFixBranchBlock();
        branchFixAndOpt.ProcessOptimizeBranchBlock();
    }

    public void ProcessBranchInstOptimize(SSAConvert ssaConvert) {
        BranchFixAndOpt branchFixAndOpt = new BranchFixAndOpt(ssaConvert);
        branchFixAndOpt.ProcessOptimizeBranchInst();
    }

    public void ProcessGraphColor(SSAConvert ssaConvert) {
        if (GlobalSetting.RegisterOptimize) {
            ActiveAnalyze activeAnalyze = new ActiveAnalyze(ssaConvert.getControlFlowGraph());
            activeAnalyze.ProcessDefUseAnalyze();
            activeAnalyze.initializeGlobalParamMemVisit();
            activeAnalyze.ProcessInOutAnalyze();
            activeAnalyze.ProcessSpaningInOut();
            ConflictGraph conflictGraph = new ConflictGraph(activeAnalyze);
            conflictGraph.ProcessConflictGraph();
            GraphColor graphColor = new GraphColor(conflictGraph,assembly.getGlobalRegisterPool());
            graphColor.ProcessAnalyzeByStack();
            graphColor.ProcessColor();
        }
    }

    public void ProcessMergeGlobalVariable(SSAConvert ssaConvert) {
        if (GlobalSetting.MergeSpecifyVarOptimize) {
            MergeSpecifyVar mergeSpecifyVar = new MergeSpecifyVar(intermediateBuilder,ssaConvert);
            mergeSpecifyVar.ProcessMerge();
        }
    }



    public void ProcessOptimize() {
        intermediateBuilder.getFunctionMap().forEach((funcOperand, funcBlock) -> funcNameMap.put(funcOperand.getOperandName(),funcOperand));
        //---------------------------------准备工作------------------------------------
        ProcessGlobalControlFlowGraph("Global",intermediateBuilder.getGlobalBlock());
        intermediateBuilder.getFunctionMap().forEach(this::ProcessControlFlowGraph);
        //---------------------------------流图构建-------------------------------------
        ProcessGlobalSSAConvert("Global",GlobalFlowGraphs);
        controlFlowGraphs.forEach(this::ProcessSSAConvert);
        //---------------------------------SSA转化-------------------------------------
        FileProcess.WriteSSAIntermediateCodeFile("AfterSSA.txt",intermediateBuilder,false);
        //ProcessGlobalAggressiveDeadCodeElimination();
        ssaConverts.forEach((s, ssaConvert) -> ProcessAggressiveDeadCodeElimination(ssaConvert));
        FileProcess.WriteSSAIntermediateCodeFile("AfterADCE.txt",intermediateBuilder,false);
        //--------------------------------激进死代码删除---------------------------------
        ssaConverts.forEach((s, ssaConvert) -> ProcessValueNumbering(ssaConvert));
        //ProcessValueNumbering(GlobalSSAConvert);
        FileProcess.WriteSSAIntermediateCodeFile("AfterGVN.txt",intermediateBuilder,false);
        //------------------------------------GVN-------------------------------------
        //ssaConverts.forEach((s, ssaConvert) -> ProcessBranchInstOptimize(ssaConvert));
        //------------------------------------优化跳转-----------------------------------
        ssaConverts.forEach((s, ssaConvert) -> ProcessCodeMotion(ssaConvert));
        FileProcess.WriteSSAIntermediateCodeFile("AfterGCM.txt",intermediateBuilder,false);
        //------------------------------------GCM--------------------------------------
        ssaConverts.forEach((s, ssaConvert) -> RedoOptimize(s));
        GlobalRedoOptimize(GlobalSSAConvert);
        //-----------------------------------反复优化------------------------------------
        ssaConverts.forEach((s, ssaConvert) -> ProcessMergeGlobalVariable(ssaConvert));
        ssaConverts.forEach((s,ssaConvert) -> ProcessRemovePhi(ssaConvert));
        //------------------------------------消除Phi------------------------------------
        ssaConverts.forEach((s, ssaConvert) -> ProcessReplaceBranchBlock(ssaConvert));
        //------------------------------------调整次序-------------------------------------
        ssaConverts.forEach((s, ssaConvert) -> ProcessGraphColor(ssaConvert));
    }
}
