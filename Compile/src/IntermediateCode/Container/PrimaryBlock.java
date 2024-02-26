package IntermediateCode.Container;

import IntermediateCode.Elements.PrimaryElement;

import java.util.ArrayList;

public abstract  class PrimaryBlock {
    public ArrayList<PrimaryElement> IntermediateExpression;

    public PrimaryBlock() {
        IntermediateExpression = new ArrayList<>();
    }

    public void AddIntermediateExpression(PrimaryElement element) {
        IntermediateExpression.add(element);
    }

    public ArrayList<PrimaryElement> getIntermediateExpression() {
        return IntermediateExpression;
    }
}
