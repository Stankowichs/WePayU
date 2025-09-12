package br.ufal.ic.p2.wepayu.models.empregado;

import br.ufal.ic.p2.wepayu.models.MembroSindicato;
import br.ufal.ic.p2.wepayu.models.metodospagamento.MetodoPagamento;

import java.io.Serializable;

public abstract class Empregado implements Serializable {

    private String id;
    private String nome;
    private String endereco;
    private MetodoPagamento metodoPagamento;
    private MembroSindicato sindicato;
    private String dataUltimoPagamento;

    public Empregado() {
    }

    public Empregado(String id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public MembroSindicato getSindicato() {
        return sindicato;
    }

    public void setSindicato(MembroSindicato sindicato) {
        this.sindicato = sindicato;
    }

    public String getDataUltimoPagamento() {
        return dataUltimoPagamento;
    }

    public void setDataUltimoPagamento(String dataUltimoPagamento) {
        this.dataUltimoPagamento = dataUltimoPagamento;
    }

    public abstract String getTipo();

    public abstract double getSalario();

    public abstract void setSalario(double valor);

    public Double getComissao() {
        return null;
    }

    public void setComissao(Double valor) {
// por padrão não faz nada; subclasses podem sobrescrever
    }
}
