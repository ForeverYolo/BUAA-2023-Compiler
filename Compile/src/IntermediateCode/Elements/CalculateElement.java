package IntermediateCode.Elements;

import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Template.BranchTemplate;
import ObjectCode.Template.CalTemplate;
import Tools.ErrorMessage;

import java.util.ArrayList;

public abstract class CalculateElement extends PrimaryElement{

    protected PrimaryOperand src1;
    protected PrimaryOperand src2;
    protected VariableOperand dst;
    protected TransformToAssembly action;

    protected interface TransformToAssembly {
        void accept(VariableOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly);
    }

    public abstract int Calculate();

    public abstract int Hash();

    public CalculateElement(String name,VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        super(name);
        this.src1 = src1;
        this.src2 = src2;
        this.dst = dst;
        this.action = null;
    }

    public CalculateElement(String name,VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2,TransformToAssembly action) {
        super(name);
        this.src1 = src1;
        this.src2 = src2;
        this.dst = dst;
        this.action = action;
    }


    public void setPlaceMessage(PrimaryBlock block, int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
        if (dst != null) {
            dst.setPlaceMessage(block,Index);
        }
        if (src1 != null && src1 instanceof VariableOperand) {
            ((VariableOperand) src1).setPlaceMessage(block,Index);
        }
        if (src2 != null && src2 instanceof VariableOperand) {
            ((VariableOperand) src2).setPlaceMessage(block,Index);
        }
    }


    @Override
    public void ToAssembly(Assembly assembly) {
        action.accept(dst,src1,src2,assembly);
        dst.setNeedWriteBack(assembly);
    }

    public static CalculateElement createAddElement(VariableOperand dst, PrimaryOperand src1, PrimaryOperand src2) {
        return new CalculateElement("Add",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.addTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() + ((ConstValue)src2).getValue();
            }

            public int Hash() {
                return src1.GetOperandHash() + src2.GetOperandHash();
            }
        };
    }

    public static CalculateElement createSubElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Sub",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.subTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() - src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() - ((ConstValue)src2).getValue();
            }

        };
    }

    public static CalculateElement createAndElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("And",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.andTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() & src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() & ((ConstValue)src2).getValue();
            }

        };
    }

    public static CalculateElement createOrElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Or",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.orTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() | src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() | ((ConstValue)src2).getValue();
            }

        };
    }

    public static CalculateElement createXorElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Xor",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.xorTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() ^ src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() ^ ((ConstValue)src2).getValue();
            }
        };
    }

    public static CalculateElement createMulElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Mul",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.mulTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() * src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() * ((ConstValue)src2).getValue();
            }
        };
    }

    public static CalculateElement createDivElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Div",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.divTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {

            @Override
            public int Hash() {
                return src1.GetOperandHash() / src2.GetOperandHash();
            }

            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() / ((ConstValue)src2).getValue();
            }
        };
    }

    public static CalculateElement createModElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Mod",dst,src1,src2,(Dst,Src1,Src2,assembly)-> CalTemplate.modTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() % src2.GetOperandHash();
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() % ((ConstValue)src2).getValue();
            }
        };
    }

    public static CalculateElement createSeqElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Seq",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SeqTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() * 2 - src2.GetOperandHash() * 2;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() == ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set equal

    public static CalculateElement createSneElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Sne",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SneTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            @Override
            public int Hash() {
                return src1.GetOperandHash() * 3 - src2.GetOperandHash() * 3;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }


            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() != ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set not equal

    public static CalculateElement createSgeElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Sge",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SgeTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            public int Hash() {
                return src1.GetOperandHash() * 4 - src2.GetOperandHash() * 4;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() >= ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set greater equal

    public static CalculateElement createSgtElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Sgt",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SgtTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            public int Hash() {
                return src1.GetOperandHash() * 5 - src2.GetOperandHash() * 5;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() > ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set greater than

    public static CalculateElement createSleElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Sle",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SleTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            public int Hash() {
                return src1.GetOperandHash() * 6 - src2.GetOperandHash() * 6;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() <= ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set less equal

    public static CalculateElement createSltElement(VariableOperand dst,PrimaryOperand src1,PrimaryOperand src2) {
        return new CalculateElement("Slt",dst,src1,src2,(Dst,Src1,Src2,assembly)-> BranchTemplate.SltTemplate(Dst,Src1,Src2,assembly,assembly.getObjectCode())) {
            public int Hash() {
                return src1.GetOperandHash() * 7 - src2.GetOperandHash() * 7;
            }
            @Override
            public void setAddress(int address) {
                this.dst.setOffset(address);
            }

            @Override
            public int getSpace() {
                return this.dst.getSpace();
            }

            @Override
            public int Calculate() {
                if (!(src1 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                if (!(src2 instanceof ConstValue)) {
                    ErrorMessage.handleSelfCheckError(this.getClass());
                }
                return ((ConstValue)src1).getValue() < ((ConstValue)src2).getValue() ? 1 : 0;
            }
        };
    } // set less than

    @Override
    public String toString() {
        return this.operatorName + " " + dst.toString() + " " + src1.toString() + " " + src2.toString();
    }

    public VariableOperand getDst() {
        return dst;
    }

    public void setDst(VariableOperand dst) {
        this.dst = dst;
    }

    public void setSrc1(PrimaryOperand src1) {
        this.src1 = src1;
    }

    public void setSrc2(PrimaryOperand src2) {
        this.src2 = src2;
    }

    @Override
    public ArrayList<VariableOperand> getDefineVariable() {
        ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
        DefineVariable.add(dst);
        return DefineVariable;
    }

    @Override
    public ArrayList<PrimaryOperand> getDefine() {
        ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
        DefineVariable.add(dst);
        return DefineVariable;
    }

    @Override
    public ArrayList<PrimaryOperand> getUsed() {
        ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
        UsedVariable.add(src1);
        UsedVariable.add(src2);
        return UsedVariable;
    }

    @Override
    public void ReplaceUseVariable(PrimaryOperand origin, PrimaryOperand target) {
        if (src1 == origin) {
            src1 = target;
        }
        if (src2 == origin) {
            src2 = target;
        }
    }

    @Override
    public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
        if (dst == origin) {
            dst = (VariableOperand) target;
        }
    }

    @Override
    public ArrayList<VariableOperand> getUsedVariable() {
        ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
        if (src1 instanceof VariableOperand src_1) {
            UsedVariable.add(src_1);
        }
        if (src2 instanceof VariableOperand src_2) {
            UsedVariable.add(src_2);
        }
        return UsedVariable;
    }
}
