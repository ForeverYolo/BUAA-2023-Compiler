package IntermediateCode.Operands;

import javax.swing.text.html.HTML;

public class TagOperand extends PrimaryOperand {

    public TagOperand(String name) {
        super(name);
    }

    @Override
    public int getSpace() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TagOperand)) {
            return false;
        } else {
            return ((TagOperand)obj).OperandName.equals(this.OperandName);
        }
    }
}
