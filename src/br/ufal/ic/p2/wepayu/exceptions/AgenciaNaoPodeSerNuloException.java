package br.ufal.ic.p2.wepayu.exceptions;

public class AgenciaNaoPodeSerNuloException extends Exception{
    public AgenciaNaoPodeSerNuloException(){
        super("Agencia nao pode ser nulo.");
    }
}
