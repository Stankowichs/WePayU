package br.ufal.ic.p2.wepayu.exceptions;

public class SalarioDeveSerNaoNegativoException extends Exception{
    public SalarioDeveSerNaoNegativoException(){
        super("Salario deve ser nao-negativo.");
    }
}
