package br.ufal.ic.p2.wepayu.models.metodospagamento;

public class Banco extends MetodoPagamento {

    private String banco;
    private String agencia;
    private String contaCorrente;

    public Banco() {
    }

    public Banco(String banco, String agencia, String contaCorrente) {
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    @Override
    public String getTipo() {
        return "banco";
    }

    public String getBanco() {
        return banco;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getContaCorrente() {
        return contaCorrente;
    }
}
