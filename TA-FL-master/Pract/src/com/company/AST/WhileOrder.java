package com.company.AST;

import java.util.ArrayList;
import java.util.List;

public class WhileOrder extends ExprNode {
    List<ExprNode> nodeList;

    public WhileOrder() {
        this.nodeList = new ArrayList<>();
    }

    public void addNode(ExprNode node) {
        this.nodeList.add(node);
    }

    public List<ExprNode> getNodeList() {
        return nodeList;
    }
}
