package br.ufal.ic.p2.wepayu.exceptions;

public class NaoHaEmpregadoComEsseNomeException extends Exception{
    public NaoHaEmpregadoComEsseNomeException(){
        super("Nao ha empregado com esse nome.");
    }
}
