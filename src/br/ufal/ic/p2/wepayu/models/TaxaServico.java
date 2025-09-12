package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

public class TaxaServico implements Serializable {
    private String data;
    private double valor;
    private boolean deduzida;

    public TaxaServico() {
    }

    public TaxaServico(String data, double valor) {
        this.data = data;
        this.valor = valor;
        this.deduzida = false;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isDeduzida() {
        return deduzida;
    }

    public void setDeduzida(boolean deduzida) {
        this.deduzida = deduzida;
    }
}
