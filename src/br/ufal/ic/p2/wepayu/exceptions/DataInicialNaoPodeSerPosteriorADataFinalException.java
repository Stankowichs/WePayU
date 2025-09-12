package br.ufal.ic.p2.wepayu.exceptions;

public class DataInicialNaoPodeSerPosteriorADataFinalException extends Exception{
    public DataInicialNaoPodeSerPosteriorADataFinalException(){
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
