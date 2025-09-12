package br.ufal.ic.p2.wepayu.models.empregado;

import br.ufal.ic.p2.wepayu.models.CartaoDePonto;

import java.util.ArrayList;
import java.util.List;

public class Horista extends Empregado {

    private double salarioPorHora;
    private List<CartaoDePonto> cartoesPonto = new ArrayList<>();

    public Horista() {
        super();
    }

    public Horista(String id, String nome, String endereco, double salarioPorHora) {
        super(id, nome, endereco);
        this.salarioPorHora = salarioPorHora;
    }

    @Override
    public String getTipo() {
        return "horista";
    }

    @Override
    public double getSalario() {
        return salarioPorHora;
    }

    @Override
    public void setSalario(double valor) {
        this.salarioPorHora = valor;
    }

    public List<CartaoDePonto> getCartoesPonto() {
        return cartoesPonto;
    }

    public void setCartoesPonto(List<CartaoDePonto> cartoesPonto) {
        this.cartoesPonto = cartoesPonto;
    }
}
