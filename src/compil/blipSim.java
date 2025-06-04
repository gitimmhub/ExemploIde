package compil;

import java.util.List;

public class blipSim {
    
    private StringBuilder dataSection;
    private StringBuilder textSection;
    private StringBuilder codigo;

    public blipSim() {
        dataSection = new StringBuilder(".data\n");
        textSection = new StringBuilder(".text\n");
        codigo = new StringBuilder();
    }

    // Gera o .data com base na tabela de símbolos
    public void gerarDataSection(List<Simbolo> simbolos) {
        java.util.HashSet<String> idsAdicionados = new java.util.HashSet<>();
        for (Simbolo simbolo : simbolos) {
            if (!simbolo.getFlagFuncao() && idsAdicionados.add(simbolo.getId())) { // Ignora funções e repetidos
                dataSection.append(simbolo.getId()).append(" : 0\n");
            }
        }
    }

    // Gera código para entrada de dados
    public void gerarEntrada(String id) {
        textSection.append("LD $in_port\n");
        textSection.append("STO ").append(id).append("\n");
    }

    // Gera código para saída de dados
    public void gerarSaida(String id) {
        textSection.append("LD ").append(id).append("\n");
        textSection.append("STO $out_port\n");
    }

    // Gera código para atribuição
    public void gerarAtribuicao(String id, String valor) {
        textSection.append("STO ").append(id).append("\n");

        System.out.println("Gerando atribuição: " + id + " = " + valor);
        System.out.println(textSection.toString());
    }

    // Gera código para operações aritméticas (soma e subtração)
    public void gerarOperacao(String id1, String id2, String operador, String resultado) {
        textSection.append("LD ").append(id1).append("\n");
        if (operador.equals("+")) {
            textSection.append("ADD ").append(id2).append("\n");
        } else if (operador.equals("-")) {
            textSection.append("SUB ").append(id2).append("\n");
        }
        textSection.append("STO ").append(resultado).append("\n");
    }

    // Gera código para operações bit a bit
    public void gerarOperacaoBit(String id1, String id2, String operador, String resultado) {
        textSection.append("LD ").append(id1).append("\n");
        switch (operador) {
            case "AND":
                textSection.append("AND ").append(id2).append("\n");
                break;
            case "OR":
                textSection.append("OR ").append(id2).append("\n");
                break;
            case "SLL":
                textSection.append("SLL ").append(id2).append("\n");
                break;
            case "SRL":
                textSection.append("SRL ").append(id2).append("\n");
                break;
        }
        textSection.append("STO ").append(resultado).append("\n");
    }

    public void gerarInstrucao(String instrucao, String operando) {
        codigo.append(instrucao).append(" ").append(operando).append("\n");
    }

    // Gera o código completo
    public String gerarCodigoCompleto() {
        codigo.append("HLT 0\n");
        return dataSection.toString() + textSection.toString() + codigo.toString();
    }
}
