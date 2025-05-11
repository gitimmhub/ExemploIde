package compil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TabelaSimbolos {

    private List<Simbolo> listSimbolos = new ArrayList<Simbolo>();

    public List<Simbolo> getListSimbolos() {
        return listSimbolos;
    }

    public void setListSimbolos(List<Simbolo> listSimbolos) {
        this.listSimbolos = listSimbolos;
    }

    public Boolean addSimbolo(Simbolo simbolo) {
        if (listSimbolos.add(new Simbolo(
                simbolo.getTipo(),
                simbolo.getId(),
                simbolo.getEscopo(),
                simbolo.getFlagVetor(),
                simbolo.getFlagFuncao(),
                simbolo.getFlagParametro(),
                simbolo.getFlagInicializada(),
                simbolo.getFlagUsada()))) {
            return true;
        }
        return false;
    }

    // Verifica se a variavel pode ser declarada
    public Boolean podeDeclarar(Simbolo simbolo, Stack<Integer> pilhaEscopo) {
        // Se não for função
        if (!simbolo.getFlagFuncao()) {
            // Verifica se já existe uma variável com o mesmo id e escopo visível
            boolean existe = listSimbolos.stream()
                    .anyMatch(s -> s.getId().equals(simbolo.getId()) && // Mesmo identificador
                            pilhaEscopo.search(s.getEscopo()) != -1 && // Escopo visível na pilha
                            s.getEscopo().equals(simbolo.getEscopo()) && // Mesmo escopo
                            !s.getFlagVetor() // Não é vetor
                    );
            return !existe; // Só pode declarar se NÃO existe
        }

        // Se for função, verifica se já existe função com mesmo id
        boolean existeFuncao = listSimbolos.stream()
                .anyMatch(s -> s.getId().equals(simbolo.getId()) &&
                        s.getFlagFuncao());
        return !existeFuncao; // Só pode declarar se NÃO existe função igual
    }

    // Verifica se a variavel foi declarada
    public Boolean jaDeclarada(Simbolo simbolo, Stack<Integer> pilhaEscopo) {
        if (!simbolo.getFlagFuncao()) { // Não é função
            if (!simbolo.getFlagVetor()) { // Não é vetor
                // Verifica se existe variável (não vetor) com mesmo id e escopo visível
                return listSimbolos.stream()
                        .anyMatch(s -> s.getId().equals(simbolo.getId()) &&
                                pilhaEscopo.search(s.getEscopo()) != -1 &&
                                !s.getFlagVetor());
            } else { // É vetor
                // Verifica se existe vetor com mesmo id e escopo visível
                return listSimbolos.stream()
                        .anyMatch(s -> s.getId().equals(simbolo.getId()) &&
                                pilhaEscopo.search(s.getEscopo()) != -1 &&
                                s.getFlagVetor());
            }
        }

        // Se for função, verifica se já existe função com mesmo id
        return listSimbolos.stream()
                .anyMatch(s -> s.getId().equals(simbolo.getId()) &&
                        s.getFlagFuncao());
    }

    // Define se a variável foi usada
    // Retorna 0 se não inicializada, 1 se sucesso e -1 se não encontrada
    public Integer defineUsada(Simbolo simbolo, Stack<Integer> pilhaEscopo) {
        if (!simbolo.getFlagFuncao()) { // Não é função
            List<Simbolo> simbolos;

            if (!simbolo.getFlagVetor()) {
                // Busca variáveis (não vetor) visíveis no escopo
                simbolos = listSimbolos.stream()
                        .filter(s -> s.getId().equals(simbolo.getId()) &&
                                pilhaEscopo.search(s.getEscopo()) != -1 &&
                                !s.getFlagFuncao() &&
                                !s.getFlagVetor())
                        .toList();
            } else {
                // Busca vetores visíveis no escopo
                simbolos = listSimbolos.stream()
                        .filter(s -> s.getId().equals(simbolo.getId()) &&
                                pilhaEscopo.search(s.getEscopo()) != -1 &&
                                !s.getFlagFuncao() &&
                                s.getFlagVetor())
                        .toList();
            }

            // Se encontrou alguma variável/vetor
            if (!simbolos.isEmpty()) {
                // Marca a última (escopo mais interno) como usada
                Simbolo simboloUsado = simbolos.get(simbolos.size() - 1);
                simboloUsado.setFlagUsada(true);

                // Verifica se está inicializada
                if (!simboloUsado.getFlagInicializada()) {
                    return 0; // Não inicializada
                }
                return 1; // Sucesso
            }
            return -1; // Não encontrada
        }

        // Se for função, busca na lista
        List<Simbolo> simboloFromList = listSimbolos.stream()
                .filter(s -> s.getId().equals(simbolo.getId()) && s.getFlagFuncao())
                .toList();

        if (!simboloFromList.isEmpty()) {
            simboloFromList.get(0).setFlagUsada(true);
            return 1;
        }

        return -1; // Não encontrada
    }
    
    // Define se a variável foi inicializada
    public Boolean defineInicializada(String id, Boolean isVet) {
    List<Simbolo> simbolos;
    if (!isVet) {
        // Busca variáveis (não vetor) com o mesmo nome
        simbolos = listSimbolos.stream()
                .filter(s -> s.getId().equals(id) && !s.getFlagVetor())
                .toList();
    } else {
        // Busca vetores com o mesmo nome
        simbolos = listSimbolos.stream()
                .filter(s -> s.getId().equals(id) && s.getFlagVetor())
                .toList();
    }

    // Se encontrou alguma variável/vetor
    if (!simbolos.isEmpty()) {
        // Marca a última (escopo mais interno) como inicializada
        simbolos.get(simbolos.size() - 1).setFlagInicializada(true);
        return true;
    }

    return false;
}

    // Retorna o tipo da variável
    public int getTipoVariavel(String id, Boolean isFuncao, Boolean isVet, Stack<Integer> pilhaEscopo) {
    String tipo = null;
    List<Simbolo> simbolos;

    if (!isFuncao) {
        if (!isVet) {
            // Busca variáveis (não vetor) visíveis no escopo
            simbolos = listSimbolos.stream()
                .filter(s -> s.getId().equals(id) &&
                             pilhaEscopo.search(s.getEscopo()) != -1 &&
                             !s.getFlagFuncao() &&
                             !s.getFlagVetor())
                .toList();
        } else {
            // Busca vetores visíveis no escopo
            simbolos = listSimbolos.stream()
                .filter(s -> s.getId().equals(id) &&
                             pilhaEscopo.search(s.getEscopo()) != -1 &&
                             !s.getFlagFuncao() &&
                             s.getFlagVetor())
                .toList();
        }
        if (!simbolos.isEmpty()) {
            tipo = simbolos.get(simbolos.size() - 1).getTipo();
        }
    } else {
        // Busca funções com o mesmo id
        simbolos = listSimbolos.stream()
            .filter(s -> s.getId().equals(id) && s.getFlagFuncao())
            .toList();
        if (!simbolos.isEmpty()) {
            tipo = simbolos.get(0).getTipo();
        }
    }

    if (tipo == null) {
        return -1; // Não encontrado
    }

    return switch (tipo) {
        case "int" -> 0;
        case "float" -> 1;
        case "char" -> 2;
        case "string" -> 3;
        case "bool" -> 4;
        case "void" -> -1;
        default -> -1;
    };
}
}
