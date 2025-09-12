package br.ufal.ic.p2.wepayu.models.empregado;

import br.ufal.ic.p2.wepayu.models.ResultadoDeVenda;

import java.util.ArrayList;
import java.util.List;

public class Comissionado extends Assalariado {

    private double taxaDeComissao;
    private List<ResultadoDeVenda> vendas = new ArrayList<>();

    public Comissionado() {
        super();
    }

    public Comissionado(String id, String nome, String endereco, double salarioMensal, double taxaDeComissao) {
        super(id, nome, endereco, salarioMensal);
        this.taxaDeComissao = taxaDeComissao;
    }

    @Override
    public String getTipo() {
        return "comissionado";
    }

    @Override
    public Double getComissao() {
        return taxaDeComissao;
    }

    @Override
    public void setComissao(Double valor) {
        this.taxaDeComissao = valor;
    }

    public List<ResultadoDeVenda> getVendas() {
        return vendas;
    }

    public void setVendas(List<ResultadoDeVenda> vendas) {
        this.vendas = vendas;
    }
}
