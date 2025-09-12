package br.ufal.ic.p2.wepayu.models.metodospagamento;

public class Correios extends MetodoPagamento {
    @Override
    public String getTipo() {
        return "correios";
    }
}
