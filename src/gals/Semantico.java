package gals;

import java.util.Stack;

public class Semantico implements Constants
{
    Stack<Integer> stack = new Stack();
    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action) {
            case 1:
                // Ação para verificar se o token é um identificador
                if (token.getLexeme().equals(Constants.t_ID)) {
                    System.out.println("Esperado um identificador, encontrado: " + token.getLexeme());
                    throw new SemanticError("Esperado um identificador, encontrado: " + token.getLexeme());
                }
                break;
            
        }
        System.out.println("Ação #"+action+", Token: "+token);
    }	
}
