package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Lexer {

    private final String src;
    private int pos = 0;
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String src) {
        this.src = src;
    }

    private boolean nextToken() {
        if (pos >= src.length())
            return false;
        for (TokenType tt : TokenType.values()) {
            Matcher m = tt.pattern.matcher(src);
            m.region(pos, src.length());
            if (m.lookingAt()) {
                Token t = new Token(tt, m.group(), pos);
                tokens.add(t);
                pos = m.end();
                return true;
            }
        }
        throw new RuntimeException("Неожиданный символ " + src.charAt(pos) + " в позиции " + pos);
    }

    public List<Token> lex() {
        while (nextToken()) {
            // do nothing
        }
        return tokens;
    }

    public static void main(String[] args) {
        String text = "while x>03 do" +
                "print x;" +
                "x--;"
                + "while 0beef = 0dead do print x; done;" +
                "done;" +
                "x := 44;" +
                "print x;";
        Lexer l = new Lexer(text);
        List<Token> tokens = l.lex();
        for (Token t : tokens)
            System.out.print(t.type + " " + t.text + "\n");
    }
}
