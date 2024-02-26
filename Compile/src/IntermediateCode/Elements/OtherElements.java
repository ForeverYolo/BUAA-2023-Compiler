package IntermediateCode.Elements;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.*;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import ObjectCode.Template.FuncTemplate;

import java.util.ArrayList;

public abstract class OtherElements extends PrimaryElement{
    PrimaryOperand Operand;

    public OtherElements(String name,PrimaryOperand operand) {
        super(name);
        this.Operand = operand;
    }

    public PrimaryOperand getOperand() {
        return Operand;
    }

    public void setPlaceMessage(PrimaryBlock block, int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
        if (Operand != null && Operand instanceof VariableOperand) {
            ((VariableOperand) Operand).setPlaceMessage(block,Index);
        }
    }

    public static OtherElements createDeclElement(VariableOperand src) {
        return new OtherElements("Decl",src) {
            @Override
            public void setAddress(int address) {
                ((VariableOperand)this.Operand).setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.Operand.getSpace();
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                /* No implement */
            }

            public ArrayList<VariableOperand> getDefineVariable() {
                ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add((VariableOperand) Operand);
                return DefineVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add(Operand);
                return DefineVariable;
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }


            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (Operand != null && Operand == origin) {
                    Operand = target;
                }
            }

            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createScanElement(VariableOperand dst) {
        return new OtherElements("Scan",dst) {
            @Override
            public void setAddress(int address) {
                ((VariableOperand)this.Operand).setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.Operand.getSpace();
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                FuncTemplate.funcScanTemplate(dst,assembly.getObjectCode(),assembly);
                dst.setNeedWriteBack(assembly);
            }

            public ArrayList<VariableOperand> getDefineVariable() {
                ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add((VariableOperand) Operand);
                return DefineVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add(Operand);
                return DefineVariable;
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }

            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (Operand != null && Operand == origin) {
                    Operand = target;
                }
            }

            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createJumpElement(TagOperand tagOperand) {
        return new OtherElements("Jump",tagOperand) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                assembly.getObjectCode().add(PrimaryInstruction.createJump(tagOperand.getOperandName()));
            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createTagElement(TagOperand tagOperand) {
        return new OtherElements("Tag",tagOperand) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                assembly.getObjectCode().add(PrimaryInstruction.createTag(tagOperand.getOperandName()));
            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createPutStrElement(PrimaryOperand Const) {
        return new OtherElements("PutStr",Const) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                int addr = ((ConstValue)Const).getValue();
                FuncTemplate.funcPrintStrTemplate(addr,assembly.getObjectCode());
            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createPutNumberElement(VariableOperand variableOperand) {
        return new OtherElements("PutNumber",variableOperand) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                FuncTemplate.funcPrintNumTemplate(Operand,assembly.getObjectCode(),assembly);
            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (Operand instanceof VariableOperand) {
                    UsedVariable.add((VariableOperand) this.Operand);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                UsedVariable.add(this.Operand);
                return UsedVariable;
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {
                if (this.Operand == operand) {
                    this.Operand = target;
                }
            }
        };
    }



    public static OtherElements createReturnElement(PrimaryOperand src) {
        return new OtherElements("Return",src) {
            @Override
            public void setAddress(int address) {
                if (Operand instanceof VariableOperand) {
                    ((VariableOperand)this.Operand).setOffset(address);
                }
            }

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                FuncTemplate.funcReturnTemplate(assembly.getCurrentFunction(),Operand,assembly.getObjectCode(),assembly);
            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (Operand instanceof VariableOperand v_src) {
                    UsedVariable.add(v_src);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                if (Operand != null) {
                    UsedVariable.add(Operand);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (Operand == origin) {
                    Operand = target;
                }
            }
        };
    }
    public static OtherElements createPushElement(VariableOperand src) {
        return new OtherElements("Push",src) {
            @Override
            public void setAddress(int address) {
                return;
            }

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                FuncTemplate.FuncPushTemplate(Operand,assembly.getRealFuncParams(),assembly);
            }

            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (Operand instanceof VariableOperand) {
                    UsedVariable.add((VariableOperand) Operand);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                UsedVariable.add(Operand);
                return UsedVariable;
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (Operand == origin) {
                    Operand = target;
                }
            }
        };
    }

    public static OtherElements createCallElement(PrimaryOperand func) {
        return new OtherElements("Call",func) {
            protected VariableOperand ReturnVariable = (VariableOperand) ((FuncOperand)func).getFormalReturnVariable();
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {

                FuncTemplate.funcCallTemplate((FuncOperand) func,assembly.getObjectCode(),assembly,assembly.getRealFuncParams(), (NormalBlock) normalBlock,normalLineIndex);

            }

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                ArrayList<VariableOperand> ReturnArray = new ArrayList<>();
                if ( ReturnVariable != null) {
                    ReturnArray.add(ReturnVariable);
                }
                return ReturnArray;
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                ArrayList<PrimaryOperand> ReturnArray = new ArrayList<>();
                if ( ReturnVariable != null) {
                    ReturnArray.add(ReturnVariable);
                }
                return ReturnArray;
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
                if ( ReturnVariable != null
                        && ReturnVariable == origin) {
                    ReturnVariable = (VariableOperand) target;
                }
            }

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }

    public static OtherElements createOperandElement(PrimaryOperand operand) {
        return new OtherElements("Operand",operand) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {}

            @Override
            public ArrayList<VariableOperand> getUsedVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> operands = new ArrayList<>();
                operands.add(this.Operand);
                return operands;
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {}
        };
    }


    @Override
    public String toString() {
        if (Operand != null) {
            return this.operatorName + " " + Operand.toString();
        } else {
            return this.operatorName;
        }
    }

    public static boolean isDecl(OtherElements element) {
        return element.operatorName.equals("Decl");
    }

    public static boolean isScan(OtherElements element) {
        return element.operatorName.equals("Scan");
    }

    public static boolean isTag(OtherElements element) {return element.operatorName.equals("Tag");}

    public static boolean isPutNumber(OtherElements elements) {return elements.operatorName.equals("PutNumber");}

    public static boolean isPutStr(OtherElements elements) {return elements.operatorName.equals("PutStr");}

    public static boolean isPush(OtherElements elements) {return elements.operatorName.equals("Push");}

    public static boolean isReturn(OtherElements elements) {return elements.operatorName.equals("Return");}

    public static boolean isCall(OtherElements elements) {return elements.operatorName.equals("Call");}

    public static boolean isJump(OtherElements elements) {return elements.operatorName.equals("Jump");}

    public static boolean isKeyElement(PrimaryElement primaryElement) {
        if (primaryElement instanceof OtherElements elements) {
            return isReturn(elements) || isPutStr(elements) || isPutNumber(elements)
                    /*|| isTag(elements)*/ || isScan(elements) || isPush(elements) || isCall(elements) /*|| isJump(elements)*/;
        }
        return false;
    }

    public void setOperand(PrimaryOperand operand) {
        Operand = operand;
    }
}
