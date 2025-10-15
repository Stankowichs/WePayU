package br.ufal.ic.p2.wepayu.exceptions;

public class AgendaDePagamentoNaoEstaDisponivelException extends Exception {
    public AgendaDePagamentoNaoEstaDisponivelException() {
        super("Agenda de pagamento nao esta disponivel");
    }
}