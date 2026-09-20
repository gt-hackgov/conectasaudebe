package br.com.conectasaude.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("CPF ou senha inválidos");
    }
}
