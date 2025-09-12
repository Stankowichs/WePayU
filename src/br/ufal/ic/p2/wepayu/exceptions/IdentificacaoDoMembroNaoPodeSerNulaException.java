package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoDoMembroNaoPodeSerNulaException extends Exception{
    public IdentificacaoDoMembroNaoPodeSerNulaException(){
        super("Identificacao do membro nao pode ser nula.");
    }
}
