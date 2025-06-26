package gals;

import compil.Pilha;
import compil.Simbolo;
import compil.blipSim;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Semantico implements Constants {
    private Map<String, Simbolo> symbolTable = new HashMap<>();
    private String tipoAtual = null;
    private boolean isVetor = false;
    private int tamanhoVetor = 0;
    private Integer escopoAtual = 0;
    private String idAtual = null;
    private String idAtribuicao = null;
    private Integer valorAtualInt = null;
    private Double valorAtualDouble = null;
    private String valorAtualString = null;
    private String operacaoSimbolo = null;
    private int escopoMax = 0;
    private Pilha pilhaEscopo = new Pilha();
    private boolean inicializarAgora = false;
    private boolean isFuncaoDeclarando = false;
    private boolean aguardandoOperador = false;

    private Stack<String> labelStack = new Stack<>();
    private int labelCounter = 0;
    private String oprel = null;

    private blipSim geradorAssembly = new blipSim();

    private String ultimoOperando = null;
    private int ultimoOperandoTipo = -1;
    private String operadorAtual = null;

    private int tempCounter = 0;

    private String temp_esq = null;
    private String temp_dir = null;

    private String newTemp() {
        tempCounter++;
        return "temp" + tempCounter;
    }

    private String newRotulo() {
        labelCounter++;
        return "R" + labelCounter;
    }

    private void pushRotulo(String label) {
        labelStack.push(label);
    }

    private String popRotulo() {
        if (!labelStack.isEmpty()) {
            return labelStack.pop();
        }
        return null;
    }

    public compil.TabelaSimbolos getTabelaSimbolos() {
        compil.TabelaSimbolos tabela = new compil.TabelaSimbolos();
        for (Simbolo simbolo : symbolTable.values()) {
            tabela.addSimbolo(simbolo);
        }
        return tabela;
    }

    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println("Executando ação: " + action + " com token: " + (token != null ? token.getLexeme() : "null"));
        switch (action) {

            case 1:
                if (token.getId() != Constants.t_ID)
                    throw new SemanticError("Esperado um identificador, encontrado: " + token.getLexeme());

                idAtual = token.getLexeme();

                String chaveUsoUse = token.getLexeme() + "#" + escopoAtual;
                Simbolo simboloUsoUse = symbolTable.get(chaveUsoUse);
                if (simboloUsoUse != null) {
                    simboloUsoUse.setFlagUsada(true);

                    // Só gera LD se for identificador válido
                    if (token.getId() == Constants.t_ID) {
                        geradorAssembly.gerarInstrucao("LD", token.getLexeme());
                    }
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
                // Atualiza o idAtual com o nome do identificador recebido
                if (token != null && token.getLexeme() != null && !token.getLexeme().isEmpty()) {
                    idAtual = token.getLexeme();
                } else {
                    throw new SemanticError("Identificador não pode ser nulo ou vazio.");
                }

                String chave = idAtual + "#" + escopoAtual;
                idAtribuicao = idAtual; // Salva o identificador da variável a ser atribuída

                // Verifique se o nome já foi usado como parâmetro
                if (symbolTable.containsKey(chave)) {
                    throw new SemanticError("Variável já declarada neste escopo: " + idAtual);
                }

                // Cria o símbolo e marca como inicializada se necessário
                Simbolo simbolo = new Simbolo(
                        tipoAtual,
                        idAtual,
                        escopoAtual,
                        isVetor,
                        false,
                        false,
                        inicializarAgora,
                        false);

                // Se houver inicialização, marque como inicializada e gere o assembly correto
                if (inicializarAgora) {
                    simbolo.setFlagInicializada(true);
                    simbolo.setFlagUsada(true);
                    // Gera o código de atribuição
                    if (token != null) {
                        if (token.getId() == Constants.t_INTEIRO) {
                            geradorAssembly.gerarInstrucao("LDI", token.getLexeme());
                        } else if (token.getId() == Constants.t_DECIMAL || token.getId() == Constants.t_FLOAT) {
                            Double valorDouble = Double.parseDouble(token.getLexeme().replace(',', '.'));
                            int bits = Float.floatToIntBits(valorDouble.floatValue());
                            String valorBinario = Integer.toBinaryString(bits);
                            geradorAssembly.gerarInstrucao("LDI", valorBinario);
                        } else if (token.getLexeme().startsWith("\"") && token.getLexeme().endsWith("\"")) {
                            geradorAssembly.gerarInstrucao("LDI", token.getLexeme());
                        } else if (token.getId() == Constants.t_ID) {
                            geradorAssembly.gerarInstrucao("LD", token.getLexeme());
                        }
                    }

                }

                symbolTable.put(chave, simbolo);

                // Gera a declaração no .data
                geradorAssembly.gerarDataSection(getTabelaSimbolos().getListSimbolos());


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
                if (token == null || token.getId() == Constants.t_INTEIRO) {
                    valorAtualInt = token.getLexeme() != null ? Integer.parseInt(token.getLexeme()) : 1;
                }
                try {
                    tamanhoVetor = Integer.parseInt(token.getLexeme());  // ex: "10" → 10
                } catch (NumberFormatException e) {
                    tamanhoVetor = 1; // padrão
                }
                break;

            case 6:
                break;

            case 7:
                if (token.getId() == Constants.t_ID) {
                    ultimoOperando = token.getLexeme();
                    ultimoOperandoTipo = Constants.t_ID;
                    Simbolo simboloUso = buscarSimbolo(token.getLexeme());

                    if (inicializarAgora) {
                        if (simboloUso != null) {
                            simboloUso.setFlagInicializada(true);
                            // geradorAssembly.gerarInstrucao("LD", "$in_port");
                            // geradorAssembly.gerarInstrucao("STO", simboloUso.getId());
                        } else {
                            throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                        }
                    }
                    if (simboloUso != null) {
                        simboloUso.setFlagUsada(true);
                        if (!Boolean.TRUE.equals(simboloUso.getFlagInicializada())) {
                            throw new SemanticError(
                                    "Variável '" + simboloUso.getId() + "' usada sem estar inicializada.");
                        }
                        // Só gera LD se for o primeiro operando da expressão
                        if (token.getId() == Constants.t_ID && operadorAtual == null) {
                            geradorAssembly.gerarInstrucao("LD", simboloUso.getId());
                        }
                    } else {
                        throw new SemanticError("Variável '" + token.getLexeme() + "' usada sem declaração.");
                    }
                }
                geradorAssembly.gerarInstrucao("STO", idAtual);

                idAtual = null; // Limpa após uso
                idAtribuicao = null; // Limpa após uso
                inicializarAgora = false;
                tipoAtual = null;
                isVetor = false;
                tamanhoVetor = 0;
                break;

            case 8:
                break;

            case 9:
                // if (idAtual != null) {
                //     System.out.println("Inicializando variável: " + idAtual);
                //     System.out.println("CHEGOU AQUI");
                //     geradorAssembly.gerarInstrucao("STO", idAtual);
                //     inicializarAgora = false;
                // }
                if (token != null) {
                    ultimoOperando = token.getLexeme();
                    ultimoOperandoTipo = token.getId();
                    String valor = token.getLexeme();
                    valorAtualString = valor;

                    if (token.getId() == Constants.t_INTEIRO) {
                        valorAtualInt = Integer.parseInt(valor);
                        geradorAssembly.gerarInstrucao("LDI", valorAtualInt.toString());
                    } else if (token.getId() == Constants.t_DECIMAL || token.getId() == Constants.t_FLOAT) {
                        valorAtualDouble = Double.parseDouble(valor.replace(',', '.'));
                        int bits = Float.floatToIntBits(valorAtualDouble.floatValue());
                        String valorBinario = Integer.toBinaryString(bits);
                        geradorAssembly.gerarInstrucao("LDI", valorBinario);
                    } else if (valor.startsWith("\"") && valor.endsWith("\"")) {
                        geradorAssembly.gerarInstrucao("LDI", valor);
                    }
                }
                inicializarAgora = true;
                break;

            case 10:
                if(token != null) {
                    oprel = token.getLexeme();
                    temp_esq = newTemp();
                    geradorAssembly.addData(temp_esq, "0"); // Inicializa o temporário com 0
                    geradorAssembly.gerarInstrucao("STO", temp_esq);
                    
                }
                break;

            case 11:
                break;

            case 12:
                // Aqui, token é o operador ("+" ou "-")
                if (token != null && ultimoOperando != null) {
                    String operador = token.getLexeme();
                    if (operador.equals("+")) {
                        operacaoSimbolo = "+";
                        if (ultimoOperandoTipo == Constants.t_INTEIRO) {
                            geradorAssembly.gerarInstrucao("ADDI", ultimoOperando);
                        } else if (ultimoOperandoTipo == Constants.t_ID) {
                            geradorAssembly.gerarInstrucao("ADD", ultimoOperando);
                        }
                    } else if (operador.equals("-")) {
                        operacaoSimbolo = "-";
                        if (ultimoOperandoTipo == Constants.t_INTEIRO) {
                            geradorAssembly.gerarInstrucao("SUBI", ultimoOperando);
                        } else if (ultimoOperandoTipo == Constants.t_ID) {
                            geradorAssembly.gerarInstrucao("SUB", ultimoOperando);
                        }
                    }
                    // Limpa após uso
                    ultimoOperando = null;
                    ultimoOperandoTipo = -1;
                }
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
                // Gera instrução para ler do dispositivo de entrada e armazenar na variável atual
                if (idAtual != null) {
                    geradorAssembly.gerarInstrucao("LD", "$in_port"); // Lê do dispositivo de entrada
                    geradorAssembly.gerarInstrucao("STO", idAtual);   // Armazena na variável
                    // Marca a variável como inicializada
                    Simbolo simboloEntrada = buscarSimbolo(idAtual);
                    if (simboloEntrada != null) {
                        simboloEntrada.setFlagInicializada(true);
                        simboloEntrada.setFlagUsada(true);
                    }
                }
                break;

            case 18:
                // Saída de dados: imprime string ou valor de variável
                if (token != null) {
                    String valor = token.getLexeme();
                    if(valorAtualString != null) {
                        valor = valorAtualString; // Usa o valor da string atual
                    } else if (valorAtualInt != null) {
                        geradorAssembly.gerarInstrucao("LDI", valorAtualInt.toString()); // Usa o valor inteiro atual
                        geradorAssembly.gerarInstrucao("STO", "$out_port");
                        valorAtualInt = null; // Limpa após uso
                    } else if (valorAtualDouble != null) {
                        valor = String.valueOf(valorAtualDouble); // Usa o valor decimal atual
                    }
                    // Se for string literal (entre aspas)
                    if (valor.startsWith("\"") && valor.endsWith("\"")) {
                        // Imprime string diretamente
                        geradorAssembly.gerarInstrucao("LDI", valor);
                        geradorAssembly.gerarInstrucao("STO", "$out_port");
                        valorAtualString = null; // Limpa após uso
                    } else if (token.getId() == Constants.t_ID) {
                        // Se for variável, carrega valor e imprime
                        Simbolo simboloSaida = buscarSimbolo(valor);
                        if (simboloSaida != null) {
                            geradorAssembly.gerarInstrucao("LD", simboloSaida.getId());
                            geradorAssembly.gerarInstrucao("STO", "$out_port");
                        } else {
                            throw new SemanticError("Variável '" + valor + "' usada sem declaração.");
                        }
                    }
                }
                break;

            case 19: //output

                if(token != null) {
                    if(idAtual != null) {
                        geradorAssembly.gerarInstrucao("LD", idAtual);
                        geradorAssembly.gerarInstrucao("STO", "$out_port");
                        idAtual = null; // Limpa após uso
                    }
                }

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

                        // Carrega valor já atribuído antes (via expressão)
                        geradorAssembly.gerarInstrucao("STO", simboloAttr.getId());
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
                if(token != null) {
                    if(oprel != null) {
                        String rotulo = popRotulo();
                        if (rotulo != null) {
                            geradorAssembly.gerarInstrucao("ROT", rotulo);
                        }
                    }
                }
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
                // Só gera LDI se for um literal numérico
                if (token != null && (token.getId() == Constants.t_INTEIRO || token.getId() == Constants.t_INTEIRO || token.getId() == Constants.t_DECIMAL)) {
                    geradorAssembly.gerarInstrucao("LDI", idAtual);
                    geradorAssembly.gerarInstrucao("STO", "$out_port");
                }
                break;

            case 42:
                if (token != null) {
                    System.out.println(idAtribuicao);
                    String chaveAttr = idAtribuicao + "#" + escopoAtual;
                    Simbolo simboloAttr = symbolTable.get(chaveAttr);
                    if (simboloAttr != null) {
                        simboloAttr.setFlagInicializada(true);
                        // simboloAttr.setFlagUsada(true);
                        // geradorAssembly.gerarInstrucao("LD", simboloAttr.getId());
                        // geradorAssembly.gerarInstrucao("STO", "$out_port");
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

            case 48:
                // Se for string literal (entre aspas)
                if (token != null) {
                    String valor = token.getLexeme();
                    valorAtualString = valor;
                }
                break;
            case 50:
                if (token != null) {
                    if(operacaoSimbolo.equals("+")) {
                        if(token.getId() == Constants.t_INTEIRO) {
                            geradorAssembly.gerarInstrucao("ADDI", token.getLexeme());
                            geradorAssembly.gerarInstrucao("STO", idAtual);
                        } else if (token.getId() == Constants.t_ID) {
                            geradorAssembly.gerarInstrucao("ADD", token.getLexeme());
                            geradorAssembly.gerarInstrucao("STO", idAtual);
                        }
                    }
                }
              break;

            case 61:
                break;
            
            case 62:
                if(token != null) {
                    temp_dir = newTemp();
                    geradorAssembly.addData(temp_dir, "0"); // Inicializa o temporário com 0
                    if(token.getId() == Constants.t_ID) {
                        geradorAssembly.gerarInstrucao("LD", token.getLexeme());
                    } else {
                        geradorAssembly.gerarInstrucao("LDI", token.getLexeme());
                    }
                    geradorAssembly.gerarInstrucao("STO", temp_dir);
                    geradorAssembly.gerarInstrucao("LD", temp_esq);
                    geradorAssembly.gerarInstrucao("SUB", temp_dir);
                }
                break;
            
            case 67:
            String rotIf = newRotulo();
            pushRotulo(rotIf);
               if (rotIf != null) {
                    if (oprel.equals("<")) { // if (A < B) then jump if (A >= B)
                        geradorAssembly.gerarInstrucao("BGE", rotIf);
                    } else if (oprel.equals(">")) { // if (A > B) then jump if (A <= B)
                        geradorAssembly.gerarInstrucao("BLE", rotIf);
                    } else if (oprel.equals("==")) { // if (A == B) then jump if (A != B)
                        geradorAssembly.gerarInstrucao("BNE", rotIf);
                    } else if (oprel.equals("!=")) { // if (A != B) then jump if (A == B)
                        geradorAssembly.gerarInstrucao("BEQ", rotIf);
                    } else if (oprel.equals("<=")) { // if (A <= B) then jump if (A > B)
                        geradorAssembly.gerarInstrucao("BGT", rotIf);
                    } else if (oprel.equals(">=")) { // if (A >= B) then jump if (A < B)
                        geradorAssembly.gerarInstrucao("BLT", rotIf);
                    }
                }
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
