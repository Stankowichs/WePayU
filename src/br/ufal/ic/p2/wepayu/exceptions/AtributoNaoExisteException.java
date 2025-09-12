package br.ufal.ic.p2.wepayu.exceptions;

public class AtributoNaoExisteException extends Exception{
    public AtributoNaoExisteException(){
        super("Atributo nao existe.");
    }
}
