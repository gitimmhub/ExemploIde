package gals;
public class Semantico implements Constants
{
    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println("Ação semântica: #" + action + " - " + token.getLexeme());
        switch (action) {
            case 1:
                
                break;

            default:
                throw new SemanticError("Ação semântica não definida: #" + action);
        }
    }
}
                // Ação semântica correspondente