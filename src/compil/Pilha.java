package compil;

import java.util.ArrayList;
import java.util.List;

public class Pilha {
    private final List<Integer> stack;

    public Pilha() {
        stack = new ArrayList<>();
    }

    public void push(int elemento) {
        stack.add(elemento);
    }

    public int topo() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("A pilha está vazia!");
        }
        return stack.get(stack.size() - 1);
    }

    public int pop() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("A pilha está vazia!");
        }
        return stack.remove(stack.size() - 1);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean encontrar(Integer dado) {
        // Busca do topo para a base
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (dado.equals(stack.get(i))) {
                return true;
            }
        }
        return false;
    }

    public int tamanho() {
        return stack.size();
    }
}