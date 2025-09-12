package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MembroSindicato implements Serializable {

    private String idMembro;
    private double taxaSindical;
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public MembroSindicato() {
    }

    public MembroSindicato(String idMembro, double taxaSindical) {
        this.idMembro = idMembro;
        this.taxaSindical = taxaSindical;
    }

    public String getIdMembro() {
        return idMembro;
    }

    public void setIdMembro(String idMembro) {
        this.idMembro = idMembro;
    }

    public double getTaxaSindical() {
        return taxaSindical;
    }

    public void setTaxaSindical(double taxaSindical) {
        this.taxaSindical = taxaSindical;
    }

    public List<TaxaServico> getTaxasServico() {
        return taxasServico;
    }

    public void setTaxasServico(List<TaxaServico> taxasServico) {
        this.taxasServico = taxasServico;
    }
}
