package br.ufal.ic.p2.wepayu.exceptions;

public class EnderecoNaoPodeSerNuloException extends Exception{
    public EnderecoNaoPodeSerNuloException(){
        super("Endereco nao pode ser nulo.");
    }
}
