package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static compiler.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens=new ArrayList<>();

    private int start=0;
    private int current=0;
    private int line=1;

    //Keywords
    private static final Map<String,TokenType> keywords;

    static {
        keywords=new HashMap<>();
        keywords.put("and",AND);
        keywords.put("class",CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
    

    Scanner(String source){
        this.source=source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            //Beginning of the next lexeme
            start=current;
            scanToken();
        }

        tokens.add(new Token(EOF,"",null,line));
        return tokens;
    }

    private void scanToken(){
        char c=advance();

        switch (c) {
            case '(':addToken(LEFT_PAREN); break;
            case ')':addToken(RIGHT_PAREN); break;
            case '{':addToken(LEFT_BRACE); break;
            case '}':addToken(RIGHT_BRACE); break;
            case ',':addToken(COMMA); break;
            case '.':addToken(DOT); break;
            case '-':addToken(MINUS);   break;
            case '+':addToken(PLUS);    break;
            case ';':addToken(SEMICOLON);   break;
            case '*':addToken(STAR);    break;

            case '!':
                addToken(match('=')?BANG_EQUAL:BANG);
                break;

            case '=':
                addToken(match('=')?EQUAL_EQUAL:EQUAL);
                break;

            case '<':
                addToken(match('=')?LESS_EQUAL:LESS);
                break;

            case '>':
                addToken(match('=')?GREATER_EQUAL:GREATER);
                break;

            case '/':
                if(match('/')){
                    while(peek()!='\n' && !isAtEnd())
                        advance();
                }
                else{
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;
        
            default:
                if(isDigit(c))
                    number();
                else if(isAlhpa(c))
                    identifier();
                else{
                    Lox.error(line, "Unexpected character.");
                }
                
                break;
        }
    }

    private boolean isAlhpa(char c){
        return (c>='a' && c<='z') || (c>='A' && c<='Z') || c=='_';
    }

    private void identifier(){
        /*
        1.Consume all valid identifier characters
        2.Extract the text
        3.Check if it matches a keyword
        4.Emit appropriate token type */

        while(isAphaNumberic(peek()))
            advance();

        String text=source.substring(start,current);
        TokenType type=keywords.get(text);

        if(type==null)
            type=IDENTIFIER;

        addToken(type);
    }

    private boolean isAphaNumberic(char c){
        return isAlhpa(c) || isDigit(c);
    }

    private boolean match(char expected){
        if(isAtEnd())
            return false;
        if(source.charAt(current)!=expected)
            return false;

        current++;
        return true;
    }

    private char peek(){
        if(isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private boolean isDigit(char c){
        return c>= '0' && c<= '9';
    }

    private void number(){
        /*
        1.Consume all whole-number digits
        2.Look for a fractional part:
        3.A . followed by a digit
        4.Consume fractional digits if present
        5.Convert text → Double
        6.Emit a NUMBER token
        */
       while(isDigit(peek()))
            advance();

       if(peek()=='.' && isDigit(peekNext())){
            advance(); //Consume the '.'

            while(isDigit(peek()))
                advance();
       }

       double value=Double.parseDouble(source.substring(start,current));
       addToken(NUMBER,value);
    }

    private void string(){
        /*  1.Read characters until " or end-of-file
            2.Increment line count if it sees \n
            3.If end-of-file occurs first → report unterminated string
            4.Extract the string contents
            5.Emit a STRING token with a literal value */

            while(peek()!='"' && !isAtEnd()){
                if(peek()=='\n')
                    line++;
                advance();
            }

            if(isAtEnd()){
                Lox.error(line, "Unterminated String");
                return;
            }
            
            //Consuming the closing "
            advance();

            //Now comes the trimming part, we eliminate the starting " and ending "
            String value=source.substring(start+1,current-1);
            addToken(STRINGS,value);
    }

    private char peekNext(){
        if(current+1>=source.length())
            return '\0';
        return source.charAt(current+1);
    }

    private boolean isAtEnd(){
        return current>=source.length();
    }

    private char advance(){
        current++;
        return source.charAt(current-1);
    }

    private void addToken(TokenType type){
        addToken(type,null);
    }

    private void addToken(TokenType type, Object literal){
        String text=source.substring(start,current);    
        tokens.add(new Token(type, text, literal, line));
    }
}