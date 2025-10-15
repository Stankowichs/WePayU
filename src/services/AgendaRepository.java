package services;

import br.ufal.ic.p2.wepayu.exceptions.AgendaDePagamentoNaoEstaDisponivelException;
import br.ufal.ic.p2.wepayu.models.empregado.Comissionado;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;
import br.ufal.ic.p2.wepayu.models.empregado.Horista;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AgendaRepository {
    private final Map<String, AgendaPagamento> agendas = new HashMap<>();

    public AgendaRepository() {
        registrarPadroes();
    }

    public AgendaPagamento getAgenda(String descricao) throws AgendaDePagamentoNaoEstaDisponivelException {
        if (descricao == null) {
            throw new AgendaDePagamentoNaoEstaDisponivelException();
        }
        AgendaPagamento agenda = agendas.get(normalize(descricao));
        if (agenda == null) {
            throw new AgendaDePagamentoNaoEstaDisponivelException();
        }
        return agenda;
    }

    public boolean existeAgenda(String descricao) {
        if (descricao == null) {
            return false;
        }
        return agendas.containsKey(normalize(descricao));
    }

    public String getDescricaoCanonica(String descricao) throws AgendaDePagamentoNaoEstaDisponivelException {
        return getAgenda(descricao).getDescricao();
    }

    public String getAgendaPadrao(Empregado e) {
        if (e instanceof Horista) {
            return "semanal 5";
        }
        if (e instanceof Comissionado) {
            return "semanal 2 5";
        }
        return "mensal $";
    }

    private void registrarPadroes() {
        registrarSemanal(1, DayOfWeek.FRIDAY, "semanal 5", "semanal 1 sexta", "semanal sexta", "semanal 1 5");
        registrarSemanal(2, DayOfWeek.FRIDAY, "semanal 2 5", "semanal 2 sexta");
        registrarMensalUltimo("mensal $");
    }

    private void registrarSemanal(int frequencia, DayOfWeek dia, String descricaoPrincipal, String... aliases) {
        AgendaPagamento agenda = AgendaPagamento.semanal(descricaoPrincipal, frequencia, dia);
        agendas.put(normalize(descricaoPrincipal), agenda);
        for (String alias : aliases) {
            agendas.put(normalize(alias), agenda);
        }
    }

    private void registrarMensalUltimo(String descricao) {
        AgendaPagamento agenda = AgendaPagamento.mensalUltimoDiaUtil(descricao);
        agendas.put(normalize(descricao), agenda);
    }

    private String normalize(String valor) {
        return valor.trim().toLowerCase(new Locale("pt", "BR")).replaceAll("\\s+", " ");
    }
}
