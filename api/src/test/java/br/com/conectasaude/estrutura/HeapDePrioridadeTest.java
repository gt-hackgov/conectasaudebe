package br.com.conectasaude.estrutura;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeapDePrioridadeTest {

    @Test
    void removeSempreOMenor() {
        HeapDePrioridade<Integer> heap = new HeapDePrioridade<>(Comparator.naturalOrder());
        for (int n : new int[]{7, 3, 9, 1, 5, 1, 8}) {
            heap.inserir(n);
        }

        List<Integer> saida = new ArrayList<>();
        while (!heap.vazio()) {
            saida.add(heap.remover());
        }

        assertThat(saida).containsExactly(1, 1, 3, 5, 7, 8, 9);
    }

    @Test
    void emOrdemNaoAlteraOHeap() {
        HeapDePrioridade<Integer> heap = new HeapDePrioridade<>(Comparator.naturalOrder());
        heap.inserir(4);
        heap.inserir(2);
        heap.inserir(6);

        assertThat(heap.emOrdem()).containsExactly(2, 4, 6);
        assertThat(heap.tamanho()).isEqualTo(3);
        assertThat(heap.espiar()).isEqualTo(2);
    }

    @Test
    void equivaleAOrdenacaoParaEntradasAleatorias() {
        Random random = new Random(42);
        HeapDePrioridade<Integer> heap = new HeapDePrioridade<>(Comparator.naturalOrder());
        List<Integer> referencia = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            int n = random.nextInt(500);
            heap.inserir(n);
            referencia.add(n);
        }
        referencia.sort(Comparator.naturalOrder());

        assertThat(heap.emOrdem()).isEqualTo(referencia);
    }

    @Test
    void removerSeMantemAPropriedadeDoHeap() {
        HeapDePrioridade<Integer> heap = new HeapDePrioridade<>(Comparator.naturalOrder());
        for (int n : new int[]{10, 4, 15, 20, 0, 8}) {
            heap.inserir(n);
        }

        assertThat(heap.removerSe(n -> n == 4)).isTrue();
        assertThat(heap.removerSe(n -> n == 99)).isFalse();
        assertThat(heap.emOrdem()).containsExactly(0, 8, 10, 15, 20);
    }

    @Test
    void heapVazio() {
        HeapDePrioridade<Integer> heap = new HeapDePrioridade<>(Comparator.naturalOrder());
        assertThat(heap.espiar()).isNull();
        assertThatThrownBy(heap::remover).isInstanceOf(NoSuchElementException.class);
    }
}
