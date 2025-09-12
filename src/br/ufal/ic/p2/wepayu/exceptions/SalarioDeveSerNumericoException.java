package br.ufal.ic.p2.wepayu.exceptions;

public class SalarioDeveSerNumericoException extends Exception{
    public SalarioDeveSerNumericoException(){
        super("Salario deve ser numerico.");
    }
}
