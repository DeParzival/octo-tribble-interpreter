package compiler;


class Interpreter implements Expr.Visitor<Object>{
    
    //simply returns the value stored inside the literal node
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }
    
    //Grouping exprssion simply means it is inside parantheses
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right=evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator,right);
                return -(double)right;
            
            case BANG:
                return !isTruthy(right);

            default:
                return null;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object left=evaluate(expr.left);
        Object right=evaluate(expr.right);

        switch(expr.operator.type){

            //Arithmetic operators
            case MINUS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left-(double)right;

            case PLUS:
                if(left instanceof Double && right instanceof Double)
                    return (double)left+(double)right;
                if(left instanceof String && right instanceof String)
                    return (String)left+(String)right;

                throw new RuntimeError(expr.operator, "Operands must be two numbers or Strings.");

            case SLASH:
                checkNumberOperands(expr.operator,left,right);
                return (double)left/(double)right;

            case STAR:
                checkNumberOperands(expr.operator,left,right);
                return (double)left*(double)right;

            //Comparison Operators
            case GREATER:
                checkNumberOperands(expr.operator,left,right);
                return (double)left>(double)right;
            
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left>=(double)right;

            case LESS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left<(double)right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left<=(double)right;

            //Equality Operators
            case BANG_EQUAL:
                return !isEqual(left,right);

            case EQUAL_EQUAL:
                return isEqual(left,right);

            default:
                return null;
        }
    }

    void interpret(Expr expression){
        try{
            Object value=evaluate(expression);
            System.out.println(stringify(value));
        }
        catch(RuntimeError error){
            Lox.runtimeError(error);
        }
    }


    //Helper functions

    //this is used simply to return what is inside the literal node
    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private boolean isTruthy(Object object){
        if(object==null)
            return false;
        if(object instanceof Boolean)
            return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b){
        if(a==null && b==null)
            return true;
        if(a==null)
            return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must me numbers.");
    }

    private String stringify(Object object){
        if(object==null)
            return "nill";

        if(object instanceof Double){
            String text=object.toString();
            if(text.endsWith(".0")){
                text=text.substring(0,text.length()-2);
            }
            return text;
        }

        return object.toString();
    }
}