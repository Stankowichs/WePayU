package br.ufal.ic.p2.wepayu.exceptions;

public class NaoHaComandoADesfazerException extends Exception {
    public NaoHaComandoADesfazerException() {
        super("Nao ha comando a desfazer.");
    }
}
