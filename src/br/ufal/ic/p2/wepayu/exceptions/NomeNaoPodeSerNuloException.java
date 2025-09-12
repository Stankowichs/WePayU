package br.ufal.ic.p2.wepayu.exceptions;

public class NomeNaoPodeSerNuloException extends Exception{
    public NomeNaoPodeSerNuloException(){
        super("Nome nao pode ser nulo.");
    }
}
