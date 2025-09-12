package br.ufal.ic.p2.wepayu.models.empregado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SistemaFolha implements Serializable {

    private List<Empregado> empregados = new ArrayList<>();

    public void adicionarEmpregado(Empregado e) {
        empregados.add(e);
    }

    public List<Empregado> getEmpregados() {
        return empregados;
    }
}
