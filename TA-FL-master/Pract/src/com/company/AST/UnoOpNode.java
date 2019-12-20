package com.company.AST;

import com.company.Token;

public class UnoOpNode extends ExprNode {
    public final Token operator;
    public final ExprNode operand;

    public UnoOpNode(Token operator, ExprNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return "(" + operator.text + operand.toString() + ")";
    }
}
