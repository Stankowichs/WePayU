package br.ufal.ic.p2.wepayu.models.metodospagamento;

import java.io.Serializable;

public abstract class MetodoPagamento implements Serializable {
    public abstract String getTipo();
}
