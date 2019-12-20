package com.company;

import com.company.AST.*;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;
    static Map<String, String> scope = new TreeMap<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private void error(String message) {
        if (pos < tokens.size()) {
            Token t = tokens.get(pos);
            throw new RuntimeException(message + " в позиции " + t.pos);
        } else {
            throw new RuntimeException(message + " в конце файла");
        }
    }

    private Token match(TokenType... expected) {
        if (pos < tokens.size()) {//проход по всему списку токенов
            Token curr = tokens.get(pos);
            if (Arrays.asList(expected).contains(curr.type)) {//если этот элемент соответствует переданному типу, то передать токен
                pos++;
                return curr;
            }
        }
        return null;
    }

    private Token require(TokenType... expected) {
        Token t = match(expected); //поиск токена
        if (t == null)
            error("Ожидается " + Arrays.toString(expected));
        return t;
    }

    private ExprNode parseElem() {
        Token num = match(TokenType.NUMBER);
        if (num != null)
            return new NumberNode(num);
        Token id = match(TokenType.ID);
        if (id != null)
            return new VarNode(id);
        error("Ожидается число или переменная");
        return null;
    }

    public ExprNode parseUnoOp() {
        Token t;
        while ((t = match(
                TokenType.ID,
                TokenType.NUMBER,
                TokenType.INC,
                TokenType.DEC,
                TokenType.PRINT)) != null) {
            if (t.type == TokenType.NUMBER || t.type == TokenType.ID) {
                pos--;
                ExprNode e = parseElem();
                if ((t = match(TokenType.INC, TokenType.DEC)) != null) {
                    e = new UnoOpNode(t, e);
                    require(TokenType.SEMICOLON);
                    return e;
                }
            } else {
                ExprNode e = new UnoOpNode(t, parseElem());
                require(TokenType.SEMICOLON);
                return e;
            }

        }
        throw new IllegalStateException();
    }

    public ExprNode parseCond() { // только для условий
        ExprNode e1 = parseElem();
        Token op;
        if ((op = match(TokenType.LESS, TokenType.EQ, TokenType.MORE)) != null) {
            ExprNode e2 = parseElem();
            e1 = new BinOpNode(op, e1, e2);
        }
        return e1;
    }

    public ExprNode parseAssign() { // если встретим переменную и присвоение, то вернем соответствующий узел
                                    // в противном случае не вернем ничего
        if (match(TokenType.ID) == null) {
            return null;
        }
        pos--;
        ExprNode e1 = parseElem();
        Token op;
        if ((op = match(TokenType.ASSIGN)) != null) {
            ExprNode e2 = parseElem();
            e1 = new BinOpNode(op, e1, e2);
            require(TokenType.SEMICOLON);
            return e1;
        }
        pos--;
        return null;
    }

    public ExprNode parseWhile() {// если встретился цикл, то считываем условие
                                    //условие помещается в бин узел
                                    //так же добавляем проверку на вложенные циклы (рекурсия)
                                    //в теле цикла либо бинарный либо одиночный узел (проверяем)
        if (match(TokenType.WHILE) != null) {
            ExprNode condition = parseCond();
            require(TokenType.DO);
            WhileOrder statements = new WhileOrder();
            while (match(TokenType.DONE) == null) {
                statements.addNode(parseWhile());
            }
            require(TokenType.SEMICOLON);
            return new WhileNode(condition, statements);
        } else {
            ExprNode node = parseAssign();
            if (node != null) {
                return node;
            }
            return parseUnoOp();
        }
    }

    public ExprNode parseExpression() {//создаем список узлов
                                        //из списка токенов
        WhileOrder wo = new WhileOrder();
        while (pos < tokens.size()) {
            wo.addNode(parseWhile());
        }
        return wo;
    }

    public String eval(ExprNode e) {
        if (e instanceof NumberNode) {
            NumberNode num = (NumberNode) e;
            return num.getDecimal();
        } else if (e instanceof VarNode) {
            VarNode var = (VarNode) e;
            if (scope.containsKey(var.id.text)) {
                return scope.get(var.id.text);
            } else {
                System.out.println("Введите значение переменной(х16) " + var.id.text + ":");
                String line = new Scanner(System.in).nextLine();
                scope.put(var.id.text, line);
                return line;
            }
        } else if (e instanceof UnoOpNode) {
            UnoOpNode uOp = (UnoOpNode) e;
            String value;
            switch (uOp.operator.type) {
                case PRINT:
                    System.out.println(eval(uOp.operand));
                    return "";
                case INC:
                    if (uOp.operand instanceof VarNode) {
                        value = scope.get(((VarNode) uOp.operand).id.text);
                        value = dexToHex(value,TokenType.INC);
                        VarNode var = (VarNode) uOp.operand;
                        scope.put(var.id.text, value);
                    } else if (uOp.operand instanceof NumberNode) {
                        value = eval(uOp.operand);
                        value = dexToHex(value,TokenType.INC);
                    } else {
                        throw new IllegalStateException();
                    }
                    return value;
                case DEC:
                    if (uOp.operand instanceof VarNode) {
                        value = scope.get(((VarNode) uOp.operand).id.text);
                        value = dexToHex(value,TokenType.DEC);
                        VarNode var = (VarNode) uOp.operand;
                        scope.put(var.id.text, value);
                    } else if (uOp.operand instanceof NumberNode) {
                        value = eval(uOp.operand);
                        value = dexToHex(value,TokenType.DEC);
                    } else {
                        throw new IllegalStateException();
                    }
                    return value;
                default:
                    return "";
            }
        } else if (e instanceof BinOpNode) {
            BinOpNode bOp = (BinOpNode) e;

            if (bOp.op.type == TokenType.ASSIGN) {
                if (bOp.left instanceof VarNode) {
                    String key = ((VarNode) bOp.left).id.text;
                    String value;
                    if (bOp.right instanceof NumberNode) {
                        value = ((NumberNode) bOp.right).number.text;
                        scope.put(key, value);
                        return "";
                    } else if (bOp.right instanceof VarNode) {
                        String refKey = ((VarNode) bOp.right).id.text;
                        value = scope.get(refKey);
                        scope.put(key, value);
                        return "";
                    }
                }
                throw new IllegalStateException();
            }

            String l = eval(bOp.left);
            String r = eval(bOp.right);
            switch (bOp.op.type) {
                case LESS:
                    return Integer.parseInt(l,16) < Integer.parseInt(r,16) ? "True" : "False";
                case EQ:
                    return Integer.parseInt(l,16) == Integer.parseInt(r,16) ? "True" : "False";
                case MORE:
                    return Integer.parseInt(l,16) > Integer.parseInt(r,16) ? "True" : "False";
                default:
                    break;
            }
        } else if (e instanceof WhileOrder) {
            WhileOrder wo = (WhileOrder) e;
            for (ExprNode node : wo.getNodeList()) {
                eval(node);
            }
            return "";
        } else if (e instanceof WhileNode) {
            WhileNode node = (WhileNode) e;
            while (eval(node.condition).equals("True")) {
                eval(node.innerExpr);
            }
            return "";
        }
        throw new IllegalStateException();
    }

    public String dexToHex(String res, TokenType tokenType)
    {
        switch(tokenType)
        {
            case INC: {
                int dex = Integer.parseInt(res, 16);  //конвертация из 10-й в 16-ю сс
                dex++;
                res = Integer.toString(dex, 16);
                return res;
            }
            case DEC: {
                int dex = Integer.parseInt(res, 16);
                dex--;
                res = Integer.toString(dex, 16);
                return res;
            }
        }
        return "";
    }

    public static void main(String[] args) {
        String text =
                "while x>03 do" +
                        "print x;" +
                        "x--;" +
                "done;" +
                "x := 44;" +
                "print x;" +
                "while y < 0d do" +
                        "y++;" +
                        "while 1 > 2 do" +
                            "print 777;" +
                        "done;" +
                        "print y;" +
                "done;";

        Lexer l = new Lexer(text);
        List<Token> tokens = l.lex();
        tokens.removeIf(t -> t.type == TokenType.SPACE);

        Parser p = new Parser(tokens);
        ExprNode node = p.parseExpression();

        p.eval(node);
    }
}
