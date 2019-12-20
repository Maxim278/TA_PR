package com.company.AST;

import com.company.Token;

public class NumberNode extends ExprNode {
    public final Token number;

    public NumberNode(Token number) {
        this.number = number;
    }

    public String getDecimal() {
        return number.text;
    }

    @Override
    public String toString() {
        return number.text;
    }
}
