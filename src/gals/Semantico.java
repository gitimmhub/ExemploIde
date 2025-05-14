package gals;

import java.util.HashMap;
import java.util.Map;

import compil.Pilha;
import compil.Simbolo;

public class Semantico implements Constants {
    private Map<String, Simbolo> symbolTable = new HashMap<>();
    private String tipoAtual = null;
    private boolean isVetor = false;
    private Integer escopoAtual = 0;
    private String idAtual = null;
    private Pilha pilhaEscopo = new Pilha();
    private boolean inicializarAgora = false;
    private boolean isFuncaoDeclarando = false; // <-- Adicione esta linha

    public compil.TabelaSimbolos getTabelaSimbolos() {
        compil.TabelaSimbolos tabela = new compil.TabelaSimbolos();
        for (Simbolo simbolo : symbolTable.values()) {
            tabela.addSimbolo(simbolo);
        }
        return tabela;
    }

    public void executeAction(int action, Token token) throws SemanticError {
        System.err.println("Action: " + action + " - " + token.getLexeme());
        switch (action) {

            case 1:
                if (token.getId() != Constants.t_ID)
                    throw new SemanticError("Esperado um identificador, encontrado: " + token.getLexeme());

                idAtual = token.getLexeme();

                Simbolo simboloUsoUse = buscarSimbolo(token.getLexeme());
                if (simboloUsoUse != null) {
                    simboloUsoUse.setFlagUsada(true);
                } else {
                    if (tipoAtual == null) {
                        throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                    } else if (isFuncaoDeclarando) {
                        String chaveFunc = idAtual + "#" + escopoAtual;
                        if (symbolTable.containsKey(chaveFunc)) {
                            throw new SemanticError("Função já declarada neste contexto: " + idAtual);
                        }
                        Simbolo simboloFunc = new Simbolo(
                                tipoAtual,
                                idAtual,
                                escopoAtual,
                                false,
                                true,
                                false,
                                true,
                                false);
                        symbolTable.put(chaveFunc, simboloFunc);
                        idAtual = null;
                        tipoAtual = null;
                        isFuncaoDeclarando = false;
                    }
                }
                System.err.println("DEBUG CASE 1: idAtual=" + idAtual + ", escopoAtual=" + escopoAtual);
                break;

            case 2:
                System.err.println("DEBUG CASE 2: idAtual=" + idAtual + ", escopoAtual=" + escopoAtual);
                if (idAtual == null)
                    break;

                String chave = idAtual + "#" + escopoAtual;

                if (symbolTable.containsKey(chave)) {
                    throw new SemanticError("Variável já declarada neste escopo: " + idAtual);
                }

                Simbolo simbolo = new Simbolo(
                        tipoAtual,
                        idAtual,
                        escopoAtual,
                        isVetor,
                        false,
                        false,
                        inicializarAgora,
                        false);

                symbolTable.put(chave, simbolo);

                idAtual = null;
                inicializarAgora = false;
                tipoAtual = null; // Limpe aqui, após declarar a variável
                break;

            case 3:
                tipoAtual = token.getLexeme();
                break;

            case 4:
                isVetor = true;
                break;

            case 5:
                break;

            case 6:
                break;

            case 7:
                if (token.getId() == Constants.t_ID) {
                    String chaveUso = token.getLexeme() + "#" + escopoAtual;
                    Simbolo simboloUso = symbolTable.get(chaveUso);
                    if (simboloUso != null) {
                        simboloUso.setFlagUsada(true);
                        if (!Boolean.TRUE.equals(simboloUso.getFlagInicializada())) {
                            throw new SemanticError(
                                    "Variável '" + simboloUso.getId() + "' usada sem estar inicializada.");
                        }
                    } else {
                        throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                    }
                }
                break;

            case 8:
                break;

            case 9:
                inicializarAgora = true;
                break;

            case 10:
                break;

            case 11:
                break;

            case 12:
                break;

            case 13:
                break;

            case 14:
                break;

            case 15:
                break;

            case 16:
                break;

            case 17:
                break;

            case 18:
                break;

            case 19:
                break;

            case 20:
                break;

            case 21:
                if (token.getId() == Constants.t_ID) {
                    String chaveAttr = token.getLexeme() + "#" + escopoAtual;
                    Simbolo simboloAttr = buscarSimbolo(token.getLexeme());
                    if (simboloAttr != null) {
                        simboloAttr.setFlagInicializada(true);
                        simboloAttr.setFlagUsada(true);
                        symbolTable.put(chaveAttr, simboloAttr);
                    }
                }
                break;

            case 22:
                break;

            case 23:
                isFuncaoDeclarando = true;
                tipoAtual = token.getLexeme();
                break;

            case 24:
                break;

            case 25:
                break;

            case 26:
                break;

            case 27:
                break;

            case 29:
                break;

            case 30:
                break;

            case 31:
                break;

            case 32:
                break;

            case 33:
                break;

            case 34:
                break;

            case 35:
                break;

            case 36:
                break;

            case 37:
                break;

            case 38:
                break;

            case 39:
                break;

            case 40:
                break;

            case 41:
                break;

            case 42:
                if (token != null && token.getId() == Constants.t_ID) {
                    String chaveAttr = token.getLexeme() + "#" + escopoAtual;
                    Simbolo simboloAttr = symbolTable.get(chaveAttr);
                    if (simboloAttr != null) {
                        simboloAttr.setFlagInicializada(true);
                        simboloAttr.setFlagUsada(true);
                    }
                }
                break;

            case 43:
                pilhaEscopo.push(escopoAtual + 1);
                escopoAtual = pilhaEscopo.topo();
                System.err.println("Novo escopo: " + escopoAtual);
                break;

            case 44:
                if (!pilhaEscopo.isEmpty()) {
                    pilhaEscopo.pop();
                    if (!pilhaEscopo.isEmpty()) {
                        escopoAtual = pilhaEscopo.topo();
                    } else {
                        escopoAtual = 0;
                    }
                }
                System.err.println("Escopo após sair do bloco: " + escopoAtual);
                break;

            case 45:
                isVetor = true;
                break;

            case 46:
                isVetor = true;
                break;

            default:
                break;
        }
    }

    public String avisarNaoUsados() {
        StringBuilder avisos = new StringBuilder();
        for (Simbolo simbolo : symbolTable.values()) {
            if (!Boolean.TRUE.equals(simbolo.getFlagUsada())
                    && !Boolean.TRUE.equals(simbolo.getFlagFuncao())
                    && !Boolean.TRUE.equals(simbolo.getFlagParametro())) {
                avisos.append("Aviso: O identificador '")
                        .append(simbolo.getId())
                        .append("' declarado no escopo ")
                        .append(simbolo.getEscopo())
                        .append(" não foi usado.\n");
            }
        }

        System.out.println("Tabela de símbolos:");
        for (Simbolo simbolo : symbolTable.values()) {
            System.out.println(simbolo.getId() + " escopo=" + simbolo.getEscopo() + " usada=" + simbolo.getFlagUsada());
        }
        return avisos.toString();
    }

    private Simbolo buscarSimbolo(String id) {
        for (int i = pilhaEscopo.tamanho() - 1; i >= 0; i--) {
            int escopo = pilhaEscopo.encontrarInt(i);
            String chave = id + "#" + escopo;
            Simbolo simbolo = symbolTable.get(chave);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }
}
