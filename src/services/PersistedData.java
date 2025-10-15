package services;

import br.ufal.ic.p2.wepayu.models.empregado.Empregado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersistedData implements Serializable {
    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private List<String> agendas = new ArrayList<>();

    public Map<String, Empregado> getEmpregados() {
        return empregados;
    }

    public void setEmpregados(Map<String, Empregado> empregados) {
        if (empregados == null) {
            this.empregados = new LinkedHashMap<>();
        } else {
            this.empregados = empregados;
        }
    }

    public List<String> getAgendas() {
        return agendas;
    }

    public void setAgendas(List<String> agendas) {
        if (agendas == null) {
            this.agendas = new ArrayList<>();
        } else {
            this.agendas = agendas;
        }
    }
}

