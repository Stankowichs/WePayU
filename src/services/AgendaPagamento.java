package services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class AgendaPagamento {
    public enum Tipo { SEMANAL, MENSAL }

    private final Tipo tipo;
    private final String descricao;
    private final int frequenciaSemanas;
    private final DayOfWeek diaSemana;
    private final int diaMes;
    private final boolean ultimoDiaUtil;

    private AgendaPagamento(Tipo tipo, String descricao, int frequenciaSemanas,
                            DayOfWeek diaSemana, int diaMes, boolean ultimoDiaUtil) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.frequenciaSemanas = frequenciaSemanas;
        this.diaSemana = diaSemana;
        this.diaMes = diaMes;
        this.ultimoDiaUtil = ultimoDiaUtil;
    }

    public static AgendaPagamento semanal(String descricao, int frequenciaSemanas, DayOfWeek diaSemana) {
        return new AgendaPagamento(Tipo.SEMANAL, descricao, frequenciaSemanas, diaSemana, 0, false);
    }

    public static AgendaPagamento mensalDia(String descricao, int diaMes) {
        return new AgendaPagamento(Tipo.MENSAL, descricao, 0, null, diaMes, false);
    }

    public static AgendaPagamento mensalUltimoDiaUtil(String descricao) {
        return new AgendaPagamento(Tipo.MENSAL, descricao, 0, null, 0, true);
    }

    public boolean isPayday(LocalDate data, LocalDate ultimoPagamento) {
        if (ultimoPagamento != null && !data.isAfter(ultimoPagamento)) {
            return false;
        }
        if (tipo == Tipo.SEMANAL) {
            if (diaSemana == null || frequenciaSemanas <= 0) {
                return false;
            }
            if (data.getDayOfWeek() != diaSemana) {
                return false;
            }
            if (ultimoPagamento == null) {
                return true;
            }
            long dias = java.time.temporal.ChronoUnit.DAYS.between(ultimoPagamento, data);
            if (dias <= 0) {
                return false;
            }
            int offset = diaSemana.getValue() - ultimoPagamento.getDayOfWeek().getValue();
            if (offset <= 0) {
                offset += 7;
            }
            if (dias < offset) {
                return false;
            }
            long ocorrencias = 1 + (dias - offset) / 7;
            int freq = Math.max(frequenciaSemanas, 1);
            return ocorrencias % freq == 0;
        }
        return matchesMensal(data);
    }

    public LocalDate anchor(LocalDate data, LocalDate ultimoPagamento) {
        LocalDate esperado;
        if (tipo == Tipo.SEMANAL) {
            long intervalo = Math.max(1, frequenciaSemanas) * 7L;
            esperado = data.minusDays(intervalo);
        } else {
            esperado = anchorMensal(data);
        }
        if (ultimoPagamento != null && ultimoPagamento.isAfter(esperado)) {
            return ultimoPagamento;
        }
        return esperado;
    }

    private boolean matchesMensal(LocalDate data) {
        YearMonth mesAtual = YearMonth.from(data);
        LocalDate candidatoAtual = dataParaMes(mesAtual);
        if (data.equals(candidatoAtual)) {
            return true;
        }
        YearMonth proximoMes = mesAtual.plusMonths(1);
        LocalDate candidatoProximo = dataParaMes(proximoMes);
        return data.equals(candidatoProximo);
    }

    private LocalDate anchorMensal(LocalDate data) {
        YearMonth mesAtual = YearMonth.from(data);
        LocalDate candidatoAtual = dataParaMes(mesAtual);
        if (data.equals(candidatoAtual)) {
            YearMonth anterior = mesAtual.minusMonths(1);
            return dataParaMes(anterior);
        }
        YearMonth proximo = mesAtual.plusMonths(1);
        LocalDate candidatoProximo = dataParaMes(proximo);
        if (data.equals(candidatoProximo)) {
            return candidatoAtual;
        }
        return candidatoAtual;
    }

    private LocalDate dataParaMes(YearMonth mes) {
        LocalDate base;
        if (ultimoDiaUtil) {
            base = mes.atEndOfMonth();
            while (isFinalDeSemana(base)) {
                base = base.minusDays(1);
            }
            return base;
        }
        int dia = diaMes;
        if (dia <= 0) {
            dia = mes.lengthOfMonth();
        } else if (dia > mes.lengthOfMonth()) {
            dia = mes.lengthOfMonth();
        }
        base = mes.atDay(dia);
        return base;
    }

    private boolean isFinalDeSemana(LocalDate data) {
        DayOfWeek dow = data.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getFrequenciaSemanas() {
        return frequenciaSemanas;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public int getDiaMes() {
        return diaMes;
    }

    public boolean isUltimoDiaUtil() {
        return ultimoDiaUtil;
    }
}
