package services;

import br.ufal.ic.p2.wepayu.exceptions.AgendaDePagamentoNaoEstaDisponivelException;
import br.ufal.ic.p2.wepayu.exceptions.AgendaDePagamentosJaExisteException;
import br.ufal.ic.p2.wepayu.exceptions.DescricaoDeAgendaInvalidaException;
import br.ufal.ic.p2.wepayu.models.empregado.Comissionado;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;
import br.ufal.ic.p2.wepayu.models.empregado.Horista;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AgendaRepository {
    private final Map<String, AgendaPagamento> agendas = new HashMap<>();
    private final Set<String> padroes = new HashSet<>();
    private final Set<String> personalizadas = new LinkedHashSet<>();

    public AgendaRepository() {
        reset();
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

    public void criarAgenda(String descricao) throws AgendaDePagamentosJaExisteException,
            DescricaoDeAgendaInvalidaException {
        if (descricao == null) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        String chave = normalize(descricao);
        if (chave.isEmpty()) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        if (agendas.containsKey(chave)) {
            throw new AgendaDePagamentosJaExisteException();
        }
        AgendaPagamento agenda = criarAgendaInterna(chave);
        agendas.put(chave, agenda);
        if (!padroes.contains(chave)) {
            personalizadas.add(chave);
        }
    }

    public void registrarExistente(String descricao) {
        if (descricao == null) {
            return;
        }
        String chave = normalize(descricao);
        if (chave.isEmpty()) {
            return;
        }
        if (agendas.containsKey(chave)) {
            if (!padroes.contains(chave)) {
                personalizadas.add(chave);
            }
            return;
        }
        try {
            AgendaPagamento agenda = criarAgendaInterna(chave);
            agendas.put(chave, agenda);
            if (!padroes.contains(chave)) {
                personalizadas.add(chave);
            }
        } catch (DescricaoDeAgendaInvalidaException ignored) {
        }
    }

    public void registrarPersonalizadas(Collection<String> descricoes) {
        if (descricoes == null) {
            return;
        }
        for (String descricao : descricoes) {
            if (descricao == null) {
                continue;
            }
            String chave = normalize(descricao);
            if (chave.isEmpty()) {
                continue;
            }
            if (!agendas.containsKey(chave)) {
                try {
                    AgendaPagamento agenda = criarAgendaInterna(chave);
                    agendas.put(chave, agenda);
                } catch (DescricaoDeAgendaInvalidaException ex) {
                    continue;
                }
            }
            if (!padroes.contains(chave)) {
                personalizadas.add(chave);
            }
        }
    }

    public Set<String> getDescricoesPersonalizadas() {
        return new LinkedHashSet<>(personalizadas);
    }

    public void reset() {
        agendas.clear();
        padroes.clear();
        personalizadas.clear();
        registrarPadroes();
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
        String principal = normalize(descricaoPrincipal);
        agendas.put(principal, agenda);
        padroes.add(principal);
        for (String alias : aliases) {
            String chave = normalize(alias);
            agendas.put(chave, agenda);
            padroes.add(chave);
        }
    }

    private void registrarMensalUltimo(String descricao) {
        AgendaPagamento agenda = AgendaPagamento.mensalUltimoDiaUtil(descricao);
        String chave = normalize(descricao);
        agendas.put(chave, agenda);
        padroes.add(chave);
    }

    private AgendaPagamento criarAgendaInterna(String descricao) throws DescricaoDeAgendaInvalidaException {
        String[] partes = descricao.split(" ");
        if (partes.length == 0) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        if ("mensal".equals(partes[0])) {
            return criarAgendaMensal(descricao, partes);
        }
        if ("semanal".equals(partes[0])) {
            return criarAgendaSemanal(descricao, partes);
        }
        throw new DescricaoDeAgendaInvalidaException();
    }

    private AgendaPagamento criarAgendaMensal(String descricao, String[] partes)
            throws DescricaoDeAgendaInvalidaException {
        if (partes.length != 2) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        if ("$".equals(partes[1])) {
            return AgendaPagamento.mensalUltimoDiaUtil(descricao);
        }
        try {
            int dia = Integer.parseInt(partes[1]);
            if (dia < 1 || dia > 28) {
                throw new DescricaoDeAgendaInvalidaException();
            }
            return AgendaPagamento.mensalDia(descricao, dia);
        } catch (NumberFormatException ex) {
            throw new DescricaoDeAgendaInvalidaException();
        }
    }

    private AgendaPagamento criarAgendaSemanal(String descricao, String[] partes)
            throws DescricaoDeAgendaInvalidaException {
        if (partes.length < 2 || partes.length > 3) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        int frequencia = 1;
        String diaToken;
        if (partes.length == 2) {
            diaToken = partes[1];
        } else {
            try {
                frequencia = Integer.parseInt(partes[1]);
            } catch (NumberFormatException ex) {
                throw new DescricaoDeAgendaInvalidaException();
            }
            if (frequencia < 1 || frequencia > 52) {
                throw new DescricaoDeAgendaInvalidaException();
            }
            diaToken = partes[2];
        }
        DayOfWeek dia = parseDiaSemana(diaToken);
        return AgendaPagamento.semanal(descricao, frequencia, dia);
    }

    private DayOfWeek parseDiaSemana(String token) throws DescricaoDeAgendaInvalidaException {
        switch (token) {
            case "1":
            case "segunda":
            case "segunda-feira":
                return DayOfWeek.MONDAY;
            case "2":
            case "terca":
            case "terca-feira":
                return DayOfWeek.TUESDAY;
            case "3":
            case "quarta":
            case "quarta-feira":
                return DayOfWeek.WEDNESDAY;
            case "4":
            case "quinta":
            case "quinta-feira":
                return DayOfWeek.THURSDAY;
            case "5":
            case "sexta":
            case "sexta-feira":
                return DayOfWeek.FRIDAY;
            case "6":
            case "sabado":
                return DayOfWeek.SATURDAY;
            case "7":
            case "domingo":
                return DayOfWeek.SUNDAY;
            default:
                throw new DescricaoDeAgendaInvalidaException();
        }
    }

    private String normalize(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcentos.trim().toLowerCase(new Locale("pt", "BR")).replaceAll("\\s+", " ");
    }
}
