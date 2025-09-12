package br.ufal.ic.p2.wepayu.exceptions;

public class NaoHaComandoARefazerException extends Exception {
    public NaoHaComandoARefazerException() {
        super("Nao ha comando a refazer.");
    }
}
