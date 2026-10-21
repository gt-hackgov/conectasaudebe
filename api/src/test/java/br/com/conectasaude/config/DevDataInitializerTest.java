package br.com.conectasaude.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DevDataInitializerTest {

    @Test
    void corrigeNomeComAcentoDesconfigurado() {
        assertEquals("João", DevDataInitializer.corrigirAcentuacao("JoÃ£o"));
        assertEquals("Conceição", DevDataInitializer.corrigirAcentuacao("ConceiÃ§Ã£o"));
    }

    @Test
    void mantemNomeQueJaEstaCorreto() {
        assertEquals("João", DevDataInitializer.corrigirAcentuacao("João"));
        assertEquals("Fernanda", DevDataInitializer.corrigirAcentuacao("Fernanda"));
        assertEquals("", DevDataInitializer.corrigirAcentuacao(""));
    }
}
