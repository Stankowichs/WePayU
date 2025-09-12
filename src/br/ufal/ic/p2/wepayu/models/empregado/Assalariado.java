package br.ufal.ic.p2.wepayu.models.empregado;

public class Assalariado extends Empregado {

    private double salarioMensal;

    public Assalariado() {
        super();
    }

    public Assalariado(String id, String nome, String endereco, double salarioMensal) {
        super(id, nome, endereco);
        this.salarioMensal = salarioMensal;
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public double getSalario() {
        return salarioMensal;
    }

    @Override
    public void setSalario(double valor) {
        this.salarioMensal = valor;
    }
}
