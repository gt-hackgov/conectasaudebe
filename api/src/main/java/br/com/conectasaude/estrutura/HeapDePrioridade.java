package br.com.conectasaude.estrutura;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Heap binário de mínimo (árvore binária completa guardada em um array).
 *
 * Por que uma árvore? Numa fila de atendimento por risco, a operação mais frequente é
 * "quem é o próximo?" e "chegou um paciente novo". Com uma lista ordenada, cada chegada
 * custaria O(n) (deslocar elementos) ou O(n log n) (reordenar tudo). No heap:
 *
 *   inserir ............ O(log n)  (o novo item "sobe" pela altura da árvore)
 *   espiar (próximo) ... O(1)      (o mais prioritário está sempre na raiz, índice 0)
 *   remover (chamar) ... O(log n)  (o último vai para a raiz e "desce")
 *   emOrdem ............ O(n log n) (cópia + n remoções; usado só para exibir a fila)
 *   espaço ............. O(n)
 *
 * Mapeamento árvore → array: para o nó no índice i,
 *   pai = (i - 1) / 2, filho esquerdo = 2i + 1, filho direito = 2i + 2.
 *
 * O "menor" segundo o Comparator é o mais prioritário.
 * Não é thread-safe: quem usa (FilaAtendimentoService) sincroniza o acesso.
 */
public class HeapDePrioridade<T> {

    private final List<T> itens;
    private final Comparator<? super T> comparador;

    public HeapDePrioridade(Comparator<? super T> comparador) {
        this(comparador, new ArrayList<>());
    }

    private HeapDePrioridade(Comparator<? super T> comparador, List<T> itens) {
        this.comparador = comparador;
        this.itens = itens;
    }

    public void inserir(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item não pode ser nulo");
        }
        itens.add(item);
        subir(itens.size() - 1);
    }

    /** Retorna o mais prioritário sem remover, ou null se estiver vazio. O(1). */
    public T espiar() {
        return itens.isEmpty() ? null : itens.get(0);
    }

    /** Remove e retorna o mais prioritário. O(log n). */
    public T remover() {
        if (itens.isEmpty()) {
            throw new NoSuchElementException("Fila vazia");
        }
        T raiz = itens.get(0);
        T ultimo = itens.remove(itens.size() - 1);
        if (!itens.isEmpty()) {
            itens.set(0, ultimo);
            descer(0);
        }
        return raiz;
    }

    /** Remove o primeiro item que satisfaz o critério. O(n) para achar + O(log n) para reajustar. */
    public boolean removerSe(java.util.function.Predicate<T> criterio) {
        for (int i = 0; i < itens.size(); i++) {
            if (criterio.test(itens.get(i))) {
                int ultimoIndice = itens.size() - 1;
                trocar(i, ultimoIndice);
                itens.remove(ultimoIndice);
                if (i < itens.size()) {
                    subir(i);
                    descer(i);
                }
                return true;
            }
        }
        return false;
    }

    public int tamanho() {
        return itens.size();
    }

    public boolean vazio() {
        return itens.isEmpty();
    }

    /** Lista todos em ordem de prioridade, sem alterar o heap original. O(n log n). */
    public List<T> emOrdem() {
        HeapDePrioridade<T> copia = new HeapDePrioridade<>(comparador, new ArrayList<>(itens));
        List<T> ordenados = new ArrayList<>(itens.size());
        while (!copia.vazio()) {
            ordenados.add(copia.remover());
        }
        return ordenados;
    }

    private void subir(int indice) {
        while (indice > 0) {
            int pai = (indice - 1) / 2;
            if (comparador.compare(itens.get(indice), itens.get(pai)) >= 0) {
                return;
            }
            trocar(indice, pai);
            indice = pai;
        }
    }

    private void descer(int indice) {
        int tamanho = itens.size();
        while (true) {
            int esquerdo = 2 * indice + 1;
            int direito = esquerdo + 1;
            int menor = indice;

            if (esquerdo < tamanho && comparador.compare(itens.get(esquerdo), itens.get(menor)) < 0) {
                menor = esquerdo;
            }
            if (direito < tamanho && comparador.compare(itens.get(direito), itens.get(menor)) < 0) {
                menor = direito;
            }
            if (menor == indice) {
                return;
            }
            trocar(indice, menor);
            indice = menor;
        }
    }

    private void trocar(int a, int b) {
        T temp = itens.get(a);
        itens.set(a, itens.get(b));
        itens.set(b, temp);
    }
}
