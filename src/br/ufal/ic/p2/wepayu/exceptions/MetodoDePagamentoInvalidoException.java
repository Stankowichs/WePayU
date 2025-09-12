package br.ufal.ic.p2.wepayu.exceptions;

public class MetodoDePagamentoInvalidoException extends Exception{
    public MetodoDePagamentoInvalidoException(){
        super("Metodo de pagamento invalido.");
    }
}
