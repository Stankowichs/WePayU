package br.ufal.ic.p2.wepayu.exceptions;

public class NaoPodeDarComandosDepoisDeEncerrarSistemaException extends Exception {
    public NaoPodeDarComandosDepoisDeEncerrarSistemaException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }
}
