package com.interpreters.lox;

import java.util.List;

import com.interpreters.lox.Expr.Binary;

import static com.interpreters.lox.TokenType.*;

/*
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" ;
*/

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (!isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Expr expression() {
        // expression     → equality ;
        return equality();
    }

    private Expr equality() {
        // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        // term           → factor ( ( "-" | "+" ) factor )* ;
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        // factor         → unary ( ( "/" | "*" ) unary )* ;
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        // unary          → ( "!" | "-" ) unary
        //                  | primary ;
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            expr = new Expr.Unary(operator, expr);
        }
        return primary();
    }
    
    private Expr primary() {
        // primary        → NUMBER | STRING | "true" | "false" | "nil"
        //                  | "(" expression ")" ;
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) 
            return new Expr.Literal(previous().literal);

        if (match(LEFT_PAREN)) {
            Expr expr = expression();

            // [to do] change to consume function to deal with syntax error
            if (match(RIGHT_PAREN)) {
                return new Expr.Grouping(expr);
            }
        }

        return new Expr.Literal(null);
    }
    
}
