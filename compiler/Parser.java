package compiler;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static compiler.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException{};

    private final List<Token> tokens;
    private int current=0;

    Parser(List<Token> tokens){
        this.tokens=tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statemments=new ArrayList<>();
        while(!isAtEnd()){
            statemments.add(declaration());
        }
        return statemments;
    }

    //Grammar rules starts here

    //1st rule
    //Grammar for expression is expression -> equality
    private Expr expression(){
        return assignment();
    }

    private Stmt statement(){

        if(match(FOR))
            return forStatement();

        if(match(IF))
                return ifStatement();

        if(match(PRINT))
            return printStatemtent();

        if(match(RETURN))
            return returnStatement();

        if(match(WHILE))
            return whileStatement();

        if(match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expect '(' afte 'for'.");

        //Initializer Block
        Stmt initializer;
        //for empty initializer i.e. no initializer is present
        if(match(SEMICOLON))
            initializer=null;
        //initializer is defined using a variable like var i=0
        else if(match(VAR))
            initializer=varDeclaration();
        //Iniitializer is defined but only initialized like i=0
        else
            initializer=expressionStatement();

        //Condition Block
        Expr condition=null;
        if(!check(SEMICOLON))
            condition=expression();

        consume(SEMICOLON, "Expect ';' after loop condition.");

        //Increment Block
        Expr increment=null;
        if(!check(SEMICOLON))
            increment=expression();

        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body=statement();

        if(increment!=null){
            body=new Stmt.Block(
                Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
                )
            );
        }

        if(condition==null)
            condition=new Expr.Literal(true);

        body=new Stmt.While(condition, body);

        if(initializer!=null){
            body=new Stmt.Block(
                Arrays.asList(
                    initializer,
                    body
                )
            );
        }

        return body;

    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN,"Expect '(' after 'if'.");
        Expr condition=expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");

        Stmt thenBranch=statement();
        Stmt elseBranch=null;
        if(match(ELSE))
            elseBranch=statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatemtent(){
        Expr value=expression();
        consume(SEMICOLON,"Expect ';' after value." );
        return new Stmt.Print(value);
    }

    private Stmt returnStatement(){
        Token keyword=previous();
        Expr value=null;
        if(!check(SEMICOLON))
            value=expression();

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt varDeclaration(){
        Token name=consume(IDENTIFIER, "Expect variable name.");

        Expr initializer=null;
        if(match(EQUAL))
            initializer=expression();

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition=expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body=statement();

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement(){
        Expr value=expression();
        consume(SEMICOLON,"Expect ';' after expression");
        return new Stmt.Expression(value);
    }

    private Stmt.Function function(String kind){
        Token name=consume(IDENTIFIER, "Expect "+kind+" name.");
        consume(LEFT_PAREN, "Epect '(' after kind.");

        List<Token> parameters=new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do {
                if(parameters.size()>=255)
                    error(peek(), "Can't have more than 255 parameters.");

                parameters.add(
                    consume(IDENTIFIER, "Expect parameter name.")
                );
            } while (match(COMMA));

        }

        consume(RIGHT_PAREN, "Expect ')' after parameter.");

        consume(LEFT_BRACE,"Expect '{' before "+kind+" body.");
        List<Stmt> body=block();
        return new Stmt.Function(name, parameters, body);
        
    }

    private List<Stmt> block(){
        List<Stmt> statements=new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after Block.");
        return statements;
    }

    private Expr assignment(){
        Expr expr=equality();

        if(match(EQUAL)){
            Token equals=previous();
            Expr value=assignment();

            if(expr instanceof Expr.Variable){
                Token name=((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            else if(expr instanceof Expr.Get){
                Expr.Get get=(Expr.Get)expr;
                return new Expr.Set(get, equals, value);
            }

            error(equals, "Invalid assignment target.");
        }
        
        return expr;
    } 

    private Expr or(){
        Expr expr=and();

        while(match(OR)){
            Token operator=previous();
            Expr right=and();

            expr=new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and(){
        Expr expr=equality();

        while(match(AND)){
            Token opearator=previous();
            Expr right=equality();
            expr=new Expr.Logical(expr, opearator, right);
        }

        return expr;
    }

    //2nd Rule
    //Grammar for equality is equality-> comparison(("==" || "!=")comparison)*
    private Expr equality(){
        Expr expr=comparison();

        while(match(BANG_EQUAL,EQUAL_EQUAL)){
            Token operator=previous();
            Expr right=comparison();

            expr=new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //3rd Rule
    //Grammar for comparison is comparison -> term((">" || ">=" || "<" || "<=")term)*
    private Expr comparison(){
        Expr expr=term();

        while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
            Token operator=previous();
            Expr right=term();

            expr=new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    //4th Rule
    //Grammar for term is term-> factor(("-" || "+")factor)*
    private Expr term(){
        Expr expr=factor();

        while(match(MINUS, PLUS)){
            Token operator= previous();
            Expr right=factor();

            expr=new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //5th Rule
    //Grammar for factor is factor-> unary(("/"|| "*")unary)*
    private Expr factor(){
        Expr expr=unary();

        while(match(SLASH,STAR)){
            Token operator = previous();
            Expr right=unary();

            expr=new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //6th Rule
    //Grammar for unary is unary->  ("!" || "-")unary 
    //                              || primary
    private Expr unary(){
        if(match(BANG,MINUS)){
            Token operator=previous();
            Expr right=unary();

            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call(){
        Expr expr=primary();
        while(true){
            if(match(LEFT_PAREN))
                expr=finishCall(expr);
            else if(match(DOT)){
                Token name=consume(IDENTIFIER, "Expect property name after '.'.");
                expr=new Expr.Get(expr, name);
            }
            else
                break;
        }

        return expr;
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments=new ArrayList<>();

        if(!check(RIGHT_PAREN)){
            do {
                if(arguments.size()>=255)
                    error(peek(), "Can't have more than 255 arguments.");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren=consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    //7th Rule
    //Grammar for primary is primary-> NUMBER | STRING | "true" | "false" | "nil"
    //                                  | "(" expression ")" ;
    private Expr primary(){
        if(match(FALSE))
            return new Expr.Literal(false);

        if(match(TRUE))
            return new Expr.Literal(true);

        if(match(NIL))
            return new Expr.Literal(null);

        if(match(NUMBER,STRINGS)){
            return new Expr.Literal(previous().literal);
        }

        if(match(THIS))
            return new Expr.This(previous());

        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(LEFT_PAREN)){
            Expr expr=expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression");
    }

    

    //helper functions

    private boolean match(TokenType...types){
        for(TokenType type:types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type){
        if(isAtEnd())
            return false;

        return peek().type==type;
    }

    private Token advance(){
        if(!isAtEnd())
            current++;
    
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type==EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private Token consume(TokenType type, String message){
        if(check(type))
            return advance();

        throw error(peek(),message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private Stmt declaration(){
        try {
            if(match(CLASS))
                return classDeclaration();

            if(match(FUN))
                return function("function");

            if(match(VAR))
                return varDeclaration();

            return statement();

        } catch ( ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration(){
        Token name=consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods=new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods);
    }

    private void synchronize() {
    advance();

    while (!isAtEnd()) {
        if (previous().type == SEMICOLON) return;

        switch (peek().type) {
            case CLASS:
            case FUN:
            case VAR:
            case FOR:
            case IF:
            case WHILE:
            case PRINT:
            case RETURN:
                return;
            
            default:
                //do nothing
                break;
        }

        advance();
        }
    }

}


