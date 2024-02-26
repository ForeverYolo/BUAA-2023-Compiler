package Optimize.BackOpt;

import ObjectCode.Instruction.PrimaryInstruction;
import ObjectCode.Template.Register;
import Tools.Combination;

import java.util.ArrayList;

public class MulOptimizeValue {
    protected int price;
    protected int value;
    protected ArrayList<Combination<Boolean,Integer>> BitItems;

    public MulOptimizeValue(int... values) {
        BitItems = new ArrayList<>();
        for(int SignBit : values) {
            if (SignBit > 0) {
                value += 1 << (SignBit & Integer.MAX_VALUE);
                BitItems.add(new Combination<>(true,SignBit & Integer.MAX_VALUE));
            } else {
                value -= 1 << (SignBit & Integer.MAX_VALUE);
                BitItems.add(new Combination<>(false,SignBit & Integer.MAX_VALUE));
            }
        }
        //取反需要加1个周期，加自己的话不需要sll，少一个周期
        price = BitItems.get(0).getKey() || BitItems.get(0).getValue() == 0 ? 1 : 2;
        for (int i = 1; i < BitItems.size(); i++) {
            price += BitItems.get(i).getValue() == 0 ? 1 : 2;
        }
    }

    public boolean CheckBetter() {
        //加载数+乘法常规是2+4
        int base = 6;
        //lui和ori可以处理的Value为1+4
        if ((value & 0xffff) == 0 || (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE - Short.MIN_VALUE)) {
            base--;
        }
        return price < base;
    }

    public void MulTemplate(int dst, int src, ArrayList<PrimaryInstruction> objectCode) {
        if (BitItems.size() == 1) {
            objectCode.add(PrimaryInstruction.createSll(dst, src, BitItems.get(0).getValue()));
            if (!BitItems.get(0).getKey()) {
                objectCode.add(PrimaryInstruction.createSubu(dst, Register.zero.ordinal(), dst));
            }
        } else {
            //首尾特殊处理，各自省出一条指令。
            objectCode.add(PrimaryInstruction.createSll(Register.a0.ordinal(), src, BitItems.get(0).getValue()));
            if (!BitItems.get(0).getKey()) {
                objectCode.add(PrimaryInstruction.createSubu(Register.a0.ordinal(), Register.zero.ordinal(), Register.a0.ordinal()));
            }
            for (int i = 1; i < BitItems.size() - 1; i++) {
                if (BitItems.get(i).getValue() == 0) {
                    if (BitItems.get(i).getKey()) {
                        objectCode.add(PrimaryInstruction.createAddu(Register.a0.ordinal(), Register.a0.ordinal(), src));
                    } else {
                        objectCode.add(PrimaryInstruction.createSubu(Register.a0.ordinal(), Register.a0.ordinal(), src));
                    }
                } else {
                    objectCode.add(PrimaryInstruction.createSll(Register.v0.ordinal(), src, BitItems.get(i).getValue()));
                    if (BitItems.get(i).getKey()) {
                        objectCode.add(PrimaryInstruction.createAddu(Register.a0.ordinal(), Register.a0.ordinal(), Register.v0.ordinal()));
                    } else {
                        objectCode.add(PrimaryInstruction.createSubu(Register.a0.ordinal(), Register.a0.ordinal(), Register.v0.ordinal()));
                    }
                }
            }
            if (BitItems.get(BitItems.size() - 1).getValue() == 0) {
                if (BitItems.get(BitItems.size() - 1).getKey()) {
                    objectCode.add(PrimaryInstruction.createAddu(dst, Register.a0.ordinal(), src));
                } else {
                    objectCode.add(PrimaryInstruction.createSubu(dst, Register.a0.ordinal(), src));
                }
            } else {
                objectCode.add(PrimaryInstruction.createSll(Register.v0.ordinal(), src, BitItems.get(BitItems.size() - 1).getValue()));
                if (BitItems.get(BitItems.size() - 1).getKey()) {
                    objectCode.add(PrimaryInstruction.createAddu(dst, Register.a0.ordinal(), Register.v0.ordinal()));
                } else {
                    objectCode.add(PrimaryInstruction.createSubu(dst, Register.a0.ordinal(), Register.v0.ordinal()));
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("src * ").append(value).append(" = ");
        for (int i = 0; i < BitItems.size(); i++) {
            if (BitItems.get(i).getKey()) {
                if (i != 0) {
                    sb.append(" + ");
                }
                if (BitItems.get(i).getValue() == 0) {
                    sb.append("src ").append(BitItems.get(i).getValue());
                } else {
                    sb.append("(src << ").append(BitItems.get(i).getValue()).append(")");
                }
            } else {
                if (i != 0) {
                    sb.append(" - ");
                }
                if (BitItems.get(i).getValue() == 0) {
                    sb.append("src ").append(BitItems.get(i).getValue());
                } else {
                    sb.append("(src << ").append(BitItems.get(i).getValue()).append(")");
                }
            }
        }
        return sb.toString();
    }

    public int getValue() {
        return value;
    }

    public int getPrice() {
        return price;
    }
}
