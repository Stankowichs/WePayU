package services;

import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.models.empregado.*;
import br.ufal.ic.p2.wepayu.models.metodospagamento.*;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FolhaPagamentoService {
    private Map<String, String> folhasProcessadas = new HashMap<>();
    private Map<String, Double> totaisProcessados = new HashMap<>();
    private final EmpregadoService empregadoService;
    private UndoRedoService undoRedoService;
    private final AgendaRepository agendaRepository;

    public FolhaPagamentoService(EmpregadoService empregadoService) {
        this.empregadoService = empregadoService;
        this.agendaRepository = empregadoService.getAgendaRepository();
    }

// define serviço de undo redo para registrar processamento
    public void setUndoRedoService(UndoRedoService s) {
        this.undoRedoService = s;
    }

    Map<String, String> getFolhasProcessadas() {
        return folhasProcessadas;
    }
    Map<String, Double> getTotaisProcessados() {
        return totaisProcessados;
    }
    void setFolhasProcessadas(Map<String, String> m) {
        folhasProcessadas = m;
    }
    void setTotaisProcessados(Map<String, Double> m) {
        totaisProcessados = m;
    }
    public void clear() { folhasProcessadas.clear();
        totaisProcessados.clear();
    }

// gera folha e grava arquivo para a data informada
    public void rodaFolha(String data, String saida) throws Exception {
        LocalDate d = empregadoService.parseData(data, new DataInvalidaException());
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        String key = d.toString();
        FolhaResult res;
        if (folhasProcessadas.containsKey(key)) {
            res = new FolhaResult();
            res.texto = folhasProcessadas.get(key);
            res.totalBruto = totaisProcessados.get(key);
        } else {
            res = gerarFolha(d, true);
            folhasProcessadas.put(key, res.texto);
            totaisProcessados.put(key, res.totalBruto);
        }
        try (PrintWriter pw = new PrintWriter(new File(saida))) {
            pw.print(res.texto);
        }
        undoRedoService.pushUndo(estado);
    }

// retorna total bruto da folha na data desejada
    public String totalFolha(String data) throws Exception {
        LocalDate d = empregadoService.parseData(data, new DataInvalidaException());
        String key = d.toString();
        if (totaisProcessados.containsKey(key)) {
            return empregadoService.format(totaisProcessados.get(key));
        }
        FolhaResult res = gerarFolha(d, false);
        return empregadoService.format(res.totalBruto);
    }

// efetua cálculo detalhado da folha de pagamento
    private FolhaResult gerarFolha(LocalDate data, boolean efetiva) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("FOLHA DE PAGAMENTO DO DIA ").append(data).append("\n");
        sb.append("====================================\n\n");

        sb.append(repeat('=', 127)).append("\n");
        sb.append(repeat('=', 21)).append(" HORISTAS ").append(repeat('=', 96)).append("\n");
        sb.append(repeat('=', 127)).append("\n");
        sb.append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo\n");
        sb.append(repeat('=', 36)).append(" ")
                .append(repeat('=', 5)).append(" ")
                .append(repeat('=', 5)).append(" ")
                .append(repeat('=', 13)).append(" ")
                .append(repeat('=', 9)).append(" ")
                .append(repeat('=', 15)).append(" ")
                .append(repeat('=', 38)).append("\n");

        double totalHoras = 0, totalExtras = 0, totalBrutoH = 0, totalDescH = 0, totalLiqH = 0;
        List<Horista> horistas = new ArrayList<>();
        for (Empregado e : empregadoService.getEmpregados().values()) {
            if (e instanceof Horista) {
                horistas.add((Horista) e);
            }
        }
        horistas.sort(Comparator.comparing(Empregado::getNome));
        for (Horista h : horistas) {
            LocalDate last = getUltimoPagamento(h);
            AgendaPagamento agenda = agendaRepository.getAgenda(h.getAgendaPagamento());
            if (!agenda.isPayday(data, last)) {
                continue;
            }
            LocalDate anchor = agenda.anchor(data, last);
            double horasNormais = 0, horasExtras = 0;
            for (CartaoDePonto c : h.getCartoesPonto()) {
                LocalDate cd = empregadoService.parseData(c.getData(), new DataInvalidaException());
                if (cd.isAfter(anchor) && !cd.isAfter(data)) {
                    double htot = c.getHoras();
                    horasNormais += Math.min(8.0, htot);
                    if (htot > 8.0) horasExtras += htot - 8.0;
                }
            }
            double bruto = horasNormais * h.getSalario() + horasExtras * h.getSalario() * 1.5;
            bruto = round2(bruto);
            double descontos = 0;
            if (bruto > 0) {
                descontos = calcularDescontos(h, anchor, data, efetiva);
                if (descontos > bruto) descontos = bruto;
                descontos = round2(descontos);
            }
            double liquido = round2(bruto - descontos + 1e-9);

            sb.append(String.format(Locale.ROOT,
                    "%-36s %5s %5s %13s %9s %15s %s\n",
                    h.getNome(),
                    empregadoService.formatHoras(horasNormais),
                    empregadoService.formatHoras(horasExtras),
                    empregadoService.format(bruto),
                    empregadoService.format(descontos),
                    empregadoService.format(liquido),
                    metodoPagamentoStr(h)));

            totalHoras += horasNormais;
            totalExtras += horasExtras;
            totalBrutoH += bruto;
            totalDescH += descontos;
            totalLiqH += liquido;

            if (efetiva && bruto > 0) {
                h.setDataUltimoPagamento(formatDate(data));
            }
        }

        sb.append("\n");
        sb.append(String.format(Locale.ROOT,
                "%-36s %5s %5s %13s %9s %15s\n",
                "TOTAL HORISTAS",
                empregadoService.formatHoras(totalHoras),
                empregadoService.formatHoras(totalExtras),
                empregadoService.format(totalBrutoH),
                empregadoService.format(totalDescH),
                empregadoService.format(totalLiqH)));
        sb.append("\n");

        sb.append(repeat('=', 127)).append("\n");
        sb.append(repeat('=', 21)).append(" ASSALARIADOS ").append(repeat('=', 92)).append("\n");
        sb.append(repeat('=', 127)).append("\n");
        sb.append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo\n");
        sb.append(repeat('=', 48)).append(" ")
                .append(repeat('=', 13)).append(" ")
                .append(repeat('=', 9)).append(" ")
                .append(repeat('=', 15)).append(" ")
                .append(repeat('=', 38)).append("\n");

        double totalBrutoA = 0, totalDescA = 0, totalLiqA = 0;
        List<Assalariado> assas = new ArrayList<>();
        for (Empregado e : empregadoService.getEmpregados().values()) {
            if (e instanceof Assalariado && !(e instanceof Comissionado)) {
                assas.add((Assalariado) e);
            }
        }
        assas.sort(Comparator.comparing(Empregado::getNome));
        for (Assalariado a : assas) {
            LocalDate last = getUltimoPagamento(a);
            AgendaPagamento agenda = agendaRepository.getAgenda(a.getAgendaPagamento());
            if (!agenda.isPayday(data, last)) {
                continue;
            }
            LocalDate anchor = agenda.anchor(data, last);
            double bruto = round2(calcularValorFixo(a.getSalario(), agenda));
            double descontos = 0;
            if (bruto > 0) {
                descontos = calcularDescontos(a, anchor, data, efetiva);
                if (descontos > bruto) descontos = bruto;
                descontos = round2(descontos);
            }
            double liquido = round2(bruto - descontos + 1e-9);

            sb.append(String.format(Locale.ROOT,
                    "%-48s %13s %9s %15s %s\n",
                    a.getNome(),
                    empregadoService.format(bruto),
                    empregadoService.format(descontos),
                    empregadoService.format(liquido),
                    metodoPagamentoStr(a)));

            totalBrutoA += bruto;
            totalDescA += descontos;
            totalLiqA += liquido;

            if (efetiva && bruto > 0) {
                a.setDataUltimoPagamento(formatDate(data));
            }
        }

        sb.append("\n");
        sb.append(String.format(Locale.ROOT,
                "%-48s %13s %9s %15s\n",
                "TOTAL ASSALARIADOS",
                empregadoService.format(totalBrutoA),
                empregadoService.format(totalDescA),
                empregadoService.format(totalLiqA)));
        sb.append("\n");

        sb.append(repeat('=', 127)).append("\n");
        sb.append(repeat('=', 21)).append(" COMISSIONADOS ").append(repeat('=', 91)).append("\n");
        sb.append(repeat('=', 127)).append("\n");
        sb.append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo\n");
        sb.append(repeat('=', 21)).append(" ")
                .append(repeat('=', 8)).append(" ")
                .append(repeat('=', 8)).append(" ")
                .append(repeat('=', 8)).append(" ")
                .append(repeat('=', 13)).append(" ")
                .append(repeat('=', 9)).append(" ")
                .append(repeat('=', 15)).append(" ")
                .append(repeat('=', 38)).append("\n");

        double totalFixo = 0, totalVendas = 0, totalComissao = 0, totalBrutoC = 0, totalDescC = 0, totalLiqC = 0;
        List<Comissionado> coms = new ArrayList<>();
        for (Empregado e : empregadoService.getEmpregados().values()) {
            if (e instanceof Comissionado) {
                coms.add((Comissionado) e);
            }
        }
        coms.sort(Comparator.comparing(Empregado::getNome));
        for (Comissionado c : coms) {
            LocalDate last = getUltimoPagamento(c);
            AgendaPagamento agenda = agendaRepository.getAgenda(c.getAgendaPagamento());
            if (!agenda.isPayday(data, last)) {
                continue;
            }
            LocalDate anchor = agenda.anchor(data, last);
            double vendas = 0;
            for (ResultadoDeVenda v : c.getVendas()) {
                LocalDate vd = empregadoService.parseData(v.getData(), new DataInvalidaException());
                if (vd.isAfter(anchor) && !vd.isAfter(data)) {
                    vendas += v.getValor();
                }
            }
            double comis = round2(vendas * c.getComissao());
            double fixo = round2(calcularValorFixo(c.getSalario(), agenda));
            double bruto = round2(fixo + comis + 1e-9);
            double descontos = 0;
            if (bruto > 0) {
                descontos = calcularDescontos(c, anchor, data, efetiva);
                if (descontos > bruto) descontos = bruto;
                descontos = round2(descontos);
            }
            double liquido = round2(bruto - descontos + 1e-9);

            sb.append(String.format(Locale.ROOT,
                    "%-21s %8s %8s %8s %13s %9s %15s %s\n",
                    c.getNome(),
                    empregadoService.format(fixo),
                    empregadoService.format(vendas),
                    empregadoService.format(comis),
                    empregadoService.format(bruto),
                    empregadoService.format(descontos),
                    empregadoService.format(liquido),
                    metodoPagamentoStr(c)));

            totalFixo += fixo;
            totalVendas += vendas;
            totalComissao += comis;
            totalBrutoC += bruto;
            totalDescC += descontos;
            totalLiqC += liquido;

            if (efetiva && bruto > 0) {
                c.setDataUltimoPagamento(formatDate(data));
            }
        }

        sb.append("\n");
        sb.append(String.format(Locale.ROOT,
                "%-21s %8s %8s %8s %13s %9s %15s\n",
                "TOTAL COMISSIONADOS",
                empregadoService.format(totalFixo),
                empregadoService.format(totalVendas),
                empregadoService.format(totalComissao),
                empregadoService.format(totalBrutoC),
                empregadoService.format(totalDescC),
                empregadoService.format(totalLiqC)));
        sb.append("\n");

        double totalBruto = totalBrutoH + totalBrutoA + totalBrutoC;
        sb.append("TOTAL FOLHA: ")
                .append(empregadoService.format(totalBruto))
                .append("\n");

        FolhaResult fr = new FolhaResult();
        fr.texto = sb.toString();
        fr.totalBruto = totalBruto;
        return fr;
    }

// calcula descontos de sindicato para o período
    private double calcularDescontos(Empregado e, LocalDate last, LocalDate atual, boolean efetiva) throws Exception {
        double total = 0.0;
        if (e.getSindicato() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(last, atual);
            total += dias * e.getSindicato().getTaxaSindical();
            for (TaxaServico t : e.getSindicato().getTaxasServico()) {
                LocalDate dt = empregadoService.parseData(t.getData(), new DataInvalidaException());
                if (!t.isDeduzida() && dt.isAfter(last) && !dt.isAfter(atual)) {
                    total += t.getValor();
                    if (efetiva) {
                        t.setDeduzida(true);
                    }
                }
            }
        }
        return total;
    }

// obtém última data de pagamento do empregado
    private LocalDate getUltimoPagamento(Empregado e) throws Exception {
        String d = e.getDataUltimoPagamento();
        if (d == null || d.trim().isEmpty()) {
            return LocalDate.of(2004, 12, 31);
        }
        return empregadoService.parseData(d, new DataInvalidaException());
    }

// descreve método de pagamento para impressão
    private String metodoPagamentoStr(Empregado e) {
        MetodoPagamento m = e.getMetodoPagamento();
        if (m instanceof Banco) {
            Banco b = (Banco) m;
            return String.format("%s, Ag. %s CC %s", b.getBanco(), b.getAgencia(), b.getContaCorrente());
        } else if (m instanceof Correios) {
            return "Correios, " + e.getEndereco();
        }
        return "Em maos";
    }

    private double calcularValorFixo(double salarioMensal, AgendaPagamento agenda) {
        if (agenda.getTipo() == AgendaPagamento.Tipo.MENSAL) {
            return salarioMensal;
        }
        int frequencia = Math.max(agenda.getFrequenciaSemanas(), 1);
        double periodosPorAno = 52.0 / frequencia;
        return salarioMensal * 12.0 / periodosPorAno;
    }

    private String formatDate(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("d/M/uuuu"));
    }

    private String repeat(char c, int n) {
        char[] arr = new char[n];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    private double round2(double v) {
        return Math.floor(v * 100.0) / 100.0;
    }

    private static class FolhaResult {
        String texto;
        double totalBruto;
    }
}
