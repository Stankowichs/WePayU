package br.ufal.ic.p2.wepayu.exceptions;

public class ValorDeveSerPositivoException extends Exception{
    public ValorDeveSerPositivoException(){
        super("Valor deve ser positivo.");
    }
}
