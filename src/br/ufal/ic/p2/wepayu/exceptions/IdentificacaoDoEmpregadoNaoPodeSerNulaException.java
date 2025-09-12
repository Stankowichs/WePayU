package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoDoEmpregadoNaoPodeSerNulaException extends Exception{
    public IdentificacaoDoEmpregadoNaoPodeSerNulaException(){
        super("Identificacao do empregado nao pode ser nula.");
    }
}
