package com.company;

import java.util.regex.Pattern;

public enum TokenType {
    NUMBER("[0-9][0-9a-f]*"),
    ASSIGN(":="),
    WHILE("while"),
    DONE("done"),
    DO("do"),
    DEC("\\-\\-"),
    INC("\\+\\+"),
    SEMICOLON(";"),
    LESS("<"),
    EQ("="),
    MORE(">"),
    PRINT("print"),
    SPACE("[ \t\r\n]+"),
    ID("[a-z_][a-z_0-9]*");

    final Pattern pattern;

    TokenType(String regexp) {
        pattern = Pattern.compile(regexp);
    }
}
