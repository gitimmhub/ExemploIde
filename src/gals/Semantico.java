package gals;

import compil.Pilha;
import compil.Simbolo;
import compil.blipSim;

import java.util.HashMap;
import java.util.Map;

public class Semantico implements Constants {
    private Map<String, Simbolo> symbolTable = new HashMap<>();
    private String tipoAtual = null;
    private boolean isVetor = false;
    private int tamanhoVetor = 0;
    private Integer escopoAtual = 0;
    private String idAtual = null;
    private int escopoMax = 0;
    private Pilha pilhaEscopo = new Pilha();
    private boolean inicializarAgora = false;
    private boolean isFuncaoDeclarando = false; // <-- Adicione esta linha

    private blipSim geradorAssembly = new blipSim(); // Instância do GeradorAssembly

    public compil.TabelaSimbolos getTabelaSimbolos() {
        compil.TabelaSimbolos tabela = new compil.TabelaSimbolos();
        for (Simbolo simbolo : symbolTable.values()) {
            tabela.addSimbolo(simbolo);
        }
        return tabela;
    }

    public void executeAction(int action, Token token) throws SemanticError {
        switch (action) {

            case 1:
                if (token.getId() != Constants.t_ID)
                    throw new SemanticError("Esperado um identificador, encontrado: " + token.getLexeme());

                idAtual = token.getLexeme();

                String chaveUsoUse = token.getLexeme() + "#" + escopoAtual;
                Simbolo simboloUsoUse = symbolTable.get(chaveUsoUse);
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
                break;

            case 2: // Declaração de variável
                if (idAtual == null)
                    break;

                String chave = idAtual + "#" + escopoAtual;

                // Verifique se o nome já foi usado como parâmetro
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

                // Gera a declaração no .data
                geradorAssembly.gerarDataSection(getTabelaSimbolos().getListSimbolos());

                // Verifica se há inicialização
                if (inicializarAgora) {
                    String valor = token.getLexeme(); // Captura o valor do token
                    geradorAssembly.gerarAtribuicao(idAtual, valor); // Gera o código de atribuição
                }

                idAtual = null;
                inicializarAgora = false;
                tipoAtual = null;
                isVetor = false;
                break;
                
            case 3:
                tipoAtual = token.getLexeme();  // Armazena o tipo base
                isVetor = false;                // Inicialmente, assume que não é vetor
                break;

            case 4:
                tipoAtual += "[]";             // Marca como tipo vetor (ex: int[])
                isVetor = true;                // Marca a flag global
                break;

            case 5:
            /* 
                try {
                    tamanhoAtual = Integer.parseInt(token.getLexeme());  // ex: "10" → 10
                } catch (NumberFormatException e) {
                    System.err.println("Erro: tamanho do vetor inválido: " + token.getLexeme());
                    tamanhoAtual = -1;
                }
                break;
            */
            case 6:
                break;

            case 7:
                if (token.getId() == Constants.t_ID) {
                    Simbolo simboloUso = buscarSimbolo(token.getLexeme());

                    if (inicializarAgora) {
                        // Se for inicialização, marca como inicializada
                        if (simboloUso != null) {
                            simboloUso.setFlagInicializada(true);
                            geradorAssembly.gerarEntrada(simboloUso.getId());
                        } else {
                            throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                        }
                    }
                    if (simboloUso != null) {
                        simboloUso.setFlagUsada(true);

                        // Só lance erro se o contexto exigir variável inicializada
                        // Se quiser permitir uso mesmo sem inicializar, comente a linha abaixo:
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

           case 21: // Atribuição
            if (token.getId() == Constants.t_ID) {
                String chaveAttr = token.getLexeme() + "#" + escopoAtual;
                Simbolo simboloAttr = buscarSimbolo(token.getLexeme());
                if (simboloAttr != null) {
                    simboloAttr.setFlagInicializada(true);
                    simboloAttr.setFlagUsada(true);
                    symbolTable.put(chaveAttr, simboloAttr);

                    // Captura o valor atribuído a partir do próximo token ou contexto
                    String valor = token.getLexeme(); // Substitua por lógica para capturar o valor real
                    if (valor == null || valor.isEmpty()) {
                        throw new SemanticError("Valor inválido ou não encontrado para a atribuição.");
                    }

                    // Gera código para atribuição
                    geradorAssembly.gerarAtribuicao(simboloAttr.getId(), valor);
                } else {
                    throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
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
                // Cria símbolo para parâmetro
                if (idAtual != null && tipoAtual != null) {String chaveParam = idAtual + "#" + (escopoAtual + 1);
                    if (symbolTable.containsKey(chaveParam)) {
                        throw new SemanticError("Parâmetro já declarado neste escopo: " + idAtual);
                    }
                    Simbolo simboloParametro = new Simbolo(
                            tipoAtual,
                            idAtual,
                            (escopoAtual + 1),
                            false,
                            false,
                            true,
                            false,
                            false
                    );
                    symbolTable.put(chaveParam, simboloParametro);
                }
                idAtual = null;
                tipoAtual = null;
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
                        geradorAssembly.gerarSaida(simboloAttr.getId());
                    }
                }
                break;

            case 43:
                escopoMax = escopoMax +1;
                pilhaEscopo.push(escopoMax);
                escopoAtual = pilhaEscopo.topo();
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

    public String gerarCodigoAssembly() {
    // Gera o código completo combinando as seções .data e .text
    return geradorAssembly.gerarCodigoCompleto();
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
