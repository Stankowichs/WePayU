package br.ufal.ic.p2.wepayu.exceptions;

public class TaxaSindicalDeveSerNaoNegativaException extends Exception{
    public TaxaSindicalDeveSerNaoNegativaException(){
        super("Taxa sindical deve ser nao-negativa.");
    }
}
