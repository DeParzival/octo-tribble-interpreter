package compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static compiler.TokenType.*;


public class Lox{
    static boolean hadError=false;
    public static void main(String[] args)  throws IOException{
        if(args.length>1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if(args.length==1){
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException{
        //Read the entire file located at 'path' into a byte array
        //This loads the full file into memory at once
        byte[] bytes=Files.readAllBytes(Paths.get(path));

        //Convert the byte array into string using the system's default charset
        run(new String(bytes,Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input= new InputStreamReader(System.in);
        BufferedReader reader=new BufferedReader(input);

        for(;;){
            System.out.print("> ");
            String line=reader.readLine();
            if(line==null)
                break;
            run(line);
            hadError=false;
        }
    }

    private static void run(String source){
        Scanner scanner=new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.

        if (hadError) return;
            System.out.println(new AstPrinter().print(expression));
    }

    //Error Handling
    static void error(int line, String message){
        report(line,"",message);
    }

    static void error(Token token, String message){
        if(token.type==EOF){
            report(token.line, "at line", message);
        }
        else{
            report(token.line, " at '"+ token.lexeme+"'", message);
        }
    }

    private static void report(int line, String where, String message){
        System.err.println("[line" + line + "] Error" + where +": " + message);
        hadError=true;
    }

}
