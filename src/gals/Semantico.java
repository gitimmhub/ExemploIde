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
                System.err.println("DEBUG: tipoAtual=" + tipoAtual + ", token=" + token.getLexeme());
                if (token.getId() != Constants.t_ID)
                    throw new SemanticError("Esperado um identificador, encontrado: " + token.getLexeme());

                Simbolo simboloUsoUse = buscarSimbolo(token.getLexeme());
                if (simboloUsoUse != null) {
                    simboloUsoUse.setFlagUsada(true);
                } else {
                    // Se não existe, verifique se é declaração ou uso indevido
                    if (tipoAtual == null) { // Não está em contexto de declaração
                        throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                    }
                    // Se está em contexto de declaração, apenas salva para o case 2 ou 23
                    idAtual = token.getLexeme();
                }
                break;

            case 2:
                if (idAtual == null)
                    break;

                // Não criar símbolo de variável se estamos declarando uma função (escopo global
                // e será função)
                if (escopoAtual == 0 && tipoAtual != null) {
                    // O símbolo será criado no case 23 como função
                    // NÃO limpe idAtual nem tipoAtual aqui!
                    break;
                }

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

                // Só limpe aqui se for variável!
                idAtual = null;
                inicializarAgora = false;
                break;

            case 3:
                // Captura o tipo da variável (ex: int, float, char, string, etc)
                tipoAtual = token.getLexeme();
                break;

            case 4:
                // Marca que a próxima variável a ser declarada é um vetor
                isVetor = true;
                break;

            case 5:
                // Fim da declaração de variável (pode ser usado para limpar contexto)
                // TODO: finalizar declaração
                break;

            case 6:
                // Fim de linha (ponto e vírgula)
                // Pode ser usado para validações finais
                break;

            case 7:
                // Uso de variável em expressão
                if (token.getId() == Constants.t_ID) {
                    Simbolo simboloUso = buscarSimbolo(token.getLexeme());
                    if (simboloUso != null) {
                        simboloUso.setFlagUsada(true);
                        // Verifica se está inicializada
                        if (!Boolean.TRUE.equals(simboloUso.getFlagInicializada())) {
                            throw new SemanticError(
                                    "Variável '" + simboloUso.getId() + "' usada sem estar inicializada.");
                        }
                    } else {
                        // Variável não declarada
                        throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                    }
                }
                break;

            case 8:
                // Continuação de lista de variáveis
                break;

            case 9:
                inicializarAgora = true;
                break;

            case 10:
                // Operador relacional em expressão
                break;

            case 11:
                // Operador bitwise em expressão
                break;

            case 12:
                // Operador aritmético de baixa prioridade
                break;

            case 13:
                // Operador aritmético de alta prioridade
                break;

            case 14:
                // Operador de adição (ponto)
                break;

            case 15:
                // Operador de negação
                break;

            case 16:
                // Uso de vetor (ex: a[2])
                break;

            case 17:
                // Entrada (ex: leitura de variável)
                break;

            case 18:
                // Saída (ex: comando de print)
                break;

            case 19:
                // Tipagem de saída (ex: << "texto" << var)
                break;

            case 20:
                // Continuação de tipagem de saída
                break;

            case 21:
                // Atribuição (ex: a = 2;)
                // Marcar variável como usada e inicializada
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
                // Comando (pode ser usado para validações gerais)
                break;

            case 23:
                if (tipoAtual == null && token != null) {
                    tipoAtual = token.getLexeme();
                }
                if (tipoAtual != null && idAtual != null) {
                    String chaveFunc = idAtual + "#0";
                    if (symbolTable.containsKey(chaveFunc)) {
                        throw new SemanticError("Função já declarada: " + idAtual);
                    }
                    Simbolo simboloFunc = new Simbolo(
                            tipoAtual,
                            idAtual,
                            0,
                            false,
                            true,
                            false,
                            true,
                            true);
                    symbolTable.put(chaveFunc, simboloFunc);
                    // Limpe o contexto aqui!
                    idAtual = null;
                    tipoAtual = null;
                }
                break;

            case 24:
                // Parâmetro de função
                // TODO: adicionar parâmetro na tabela de símbolos
                break;

            case 25:
                // Fim de bloco (pode ser usado para controle de escopo)
                break;

            case 26:
                // Lista de instruções em bloco
                break;

            case 27:
                // Comando simples em bloco
                break;

            case 29:
                // Retorno de função
                break;

            case 30:
                // Início de if
                break;

            case 31:
                // Auxiliar de if
                break;

            case 32:
                // Else if
                break;

            case 33:
                // Else if sequência
                break;

            case 34:
                // Else
                break;

            case 35:
                // For
                break;

            case 36:
                // Auxiliar de while/do-while/for
                break;

            case 37:
                // Declaração simples dentro de for/while
                break;

            case 38:
                // While
                break;

            case 39:
                // Do-while
                break;

            case 40:
                // Invocação de função
                break;

            case 41:
                // Parâmetro de invocação de função
                break;

            case 42:
                // Atribuição: marca o identificador à esquerda como inicializada
                if (idAtual != null) {
                    Simbolo simboloAttr = buscarSimbolo(idAtual);
                    if (simboloAttr != null) {
                        simboloAttr.setFlagInicializada(true);
                    }
                }
                break;

            case 43:
                // Aumenta o escopo atual (novo bloco)
                pilhaEscopo.push(escopoAtual + 1);
                escopoAtual = pilhaEscopo.topo();
                System.err.println("Novo escopo: " + escopoAtual);
                break;

            case 44:
                // Diminui o escopo atual (fim de bloco)
                if (!pilhaEscopo.isEmpty()) {
                    pilhaEscopo.pop();
                    if (!pilhaEscopo.isEmpty()) {
                        escopoAtual = pilhaEscopo.topo();
                    } else {
                        escopoAtual = 0; // volta para o global se pilha vazia
                    }
                }
                System.err.println("Escopo após sair do bloco: " + escopoAtual);
                break;
            case 45:
                // Marca que a próxima variável a ser declarada é um vetor
                isVetor = true;
                break;
            case 46:
                // Marca que a próxima variável a ser declarada é um vetor
                isVetor = true;
                break;

            default:
                // Caso não tratado
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
        return avisos.toString();
    }

    private Simbolo buscarSimbolo(String id) {
        // Percorre a pilha de escopos do topo para a base
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
