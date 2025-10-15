package services;

import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.models.empregado.*;
import br.ufal.ic.p2.wepayu.models.metodospagamento.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EmpregadoService {
    private Map<String, Empregado> empregados;
    private UndoRedoService undoRedoService;
    private final AgendaRepository agendaRepository;

    public EmpregadoService(Map<String, Empregado> empregados, Collection<String> agendasPersonalizadas) {
        this.empregados = empregados;
        this.agendaRepository = new AgendaRepository();
        agendaRepository.registrarPersonalizadas(agendasPersonalizadas);
        registrarAgendasExistentes();
        for (Empregado e : this.empregados.values()) {
            atribuirAgendaSeNecessario(e);
        }
    }

    public void setUndoRedoService(UndoRedoService s) {
        this.undoRedoService = s;
    }

    public Map<String, Empregado> getEmpregados() {
        return empregados;
    }

    public AgendaRepository getAgendaRepository() {
        return agendaRepository;
    }

    public Set<String> getAgendasPersonalizadas() {
        return agendaRepository.getDescricoesPersonalizadas();
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        agendaRepository.criarAgenda(descricao);
    }

    void setEmpregados(Map<String, Empregado> novos, Collection<String> agendasPersonalizadas) {

        this.empregados = novos;
        agendaRepository.reset();
        agendaRepository.registrarPersonalizadas(agendasPersonalizadas);
        registrarAgendasExistentes();
        for (Empregado e : this.empregados.values()) {
            atribuirAgendaSeNecessario(e);
        }
    }

    public void clear() {
        empregados.clear();
        agendaRepository.reset();
    }

// sem comissão
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return criarEmpregado(nome, endereco, tipo, salario, null);
    }

// cria empregado podendo definir comissão para comissionados
    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        validarTexto(nome, new NomeNaoPodeSerNuloException());
        validarTexto(endereco, new EnderecoNaoPodeSerNuloException());

        if (tipo == null || tipo.isEmpty() ||
                !("horista".equals(tipo) || "assalariado".equals(tipo) || "comissionado".equals(tipo))) {
            throw new TipoInvalidoException();
        }
        if (!"comissionado".equals(tipo) && comissao != null) {
            throw new TipoNaoAplicavelException();
        }
        if ("comissionado".equals(tipo) && comissao == null) {
            throw new TipoNaoAplicavelException();
        }

        double sal = parseValor(salario, new SalarioNaoPodeSerNuloException(),
                new SalarioDeveSerNumericoException(), new SalarioDeveSerNaoNegativoException());
        Double com = null;
        if ("comissionado".equals(tipo)) {
            com = parseValor(comissao, new ComissaoNaoPodeSerNulaException(),
                    new ComissaoDeveSerNumericaException(), new ComissaoDeveSerNaoNegativaException());
        }

        String id = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        while (empregados.containsKey(id)) {
            id = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        }

        Empregado e;
        switch (tipo) {
            case "horista":
                e = new Horista(id, nome, endereco, sal);
                break;
            case "assalariado":
                e = new Assalariado(id, nome, endereco, sal);
                break;
            default:
                e = new Comissionado(id, nome, endereco, sal, com);
                break;
        }
        e.setMetodoPagamento(new EmMaos());
        e.setDataUltimoPagamento("31/12/2004");
        atribuirAgendaPadrao(e);
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        empregados.put(id, e);
        undoRedoService.pushUndo(estado);
        return id;
    }

// retorna atributo solicitado do empregado, usado pelas histórias de usuário
    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        Empregado e = obterEmpregado(emp);
        switch (atributo) {
            case "nome":
                return e.getNome();
            case "endereco":
                return e.getEndereco();
            case "tipo":
                return e.getTipo();
            case "salario":
                return format(e.getSalario());
            case "comissao":
                if (!(e instanceof Comissionado)) {
                    throw new EmpregadoNaoEhComissionadoException();
                }
                return format(((Comissionado) e).getComissao());
            case "metodoPagamento":
                return e.getMetodoPagamento() == null ? "emMaos" : e.getMetodoPagamento().getTipo();
            case "banco":
                if (!(e.getMetodoPagamento() instanceof Banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                return ((Banco) e.getMetodoPagamento()).getBanco();
            case "agencia":
                if (!(e.getMetodoPagamento() instanceof Banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                return ((Banco) e.getMetodoPagamento()).getAgencia();
            case "contaCorrente":
                if (!(e.getMetodoPagamento() instanceof Banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                return ((Banco) e.getMetodoPagamento()).getContaCorrente();
            case "sindicalizado":
                return String.valueOf(e.getSindicato() != null);
            case "idSindicato":
                if (e.getSindicato() == null) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                return e.getSindicato().getIdMembro();
            case "taxaSindical":
                if (e.getSindicato() == null) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                return format(e.getSindicato().getTaxaSindical());
            case "agendaPagamento":
                atribuirAgendaSeNecessario(e);
                return e.getAgendaPagamento();
            default:
                throw new AtributoNaoExisteException();
        }
    }

// busca empregado por parte do nome considerando índice
    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> matches = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e.getNome().contains(nome)) {
                matches.add(e);
            }
        }
        if (indice <= 0 || indice > matches.size()) {
            throw new NaoHaEmpregadoComEsseNomeException();
        }
        return matches.get(indice - 1).getId();
    }

// altera atributo simples do empregado como nome ou endereço
    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        Empregado e = obterEmpregado(emp);
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        switch (atributo) {
            case "nome":
                validarTexto(valor, new NomeNaoPodeSerNuloException());
                e.setNome(valor);
                break;
            case "endereco":
                validarTexto(valor, new EnderecoNaoPodeSerNuloException());
                e.setEndereco(valor);
                break;
            case "tipo":
                if (valor == null || valor.isEmpty() ||
                        !("horista".equals(valor) || "assalariado".equals(valor) || "comissionado".equals(valor))) {
                    throw new TipoInvalidoException();
                }
                Empregado novo;
                switch (valor) {
                    case "horista":
                        novo = new Horista(e.getId(), e.getNome(), e.getEndereco(), e.getSalario());
                        break;
                    case "assalariado":
                        novo = new Assalariado(e.getId(), e.getNome(), e.getEndereco(), e.getSalario());
                        break;
                    default:
                        double comAtual = e.getComissao() == null ? 0.0 : e.getComissao();
                        novo = new Comissionado(e.getId(), e.getNome(), e.getEndereco(), e.getSalario(), comAtual);
                        break;
                }
                novo.setMetodoPagamento(e.getMetodoPagamento());
                novo.setSindicato(e.getSindicato());
                novo.setDataUltimoPagamento(e.getDataUltimoPagamento());
                novo.setAgendaPagamento(e.getAgendaPagamento());
                atribuirAgendaSeNecessario(novo);
                empregados.put(emp, novo);
                break;
            case "salario":
                double sal = parseValor(valor, new SalarioNaoPodeSerNuloException(),
                        new SalarioDeveSerNumericoException(), new SalarioDeveSerNaoNegativoException());
                e.setSalario(sal);
                break;
            case "comissao":
                if (!(e instanceof Comissionado)) {
                    throw new EmpregadoNaoEhComissionadoException();
                }
                double com = parseValor(valor, new ComissaoNaoPodeSerNulaException(),
                        new ComissaoDeveSerNumericaException(), new ComissaoDeveSerNaoNegativaException());
                ((Comissionado) e).setComissao(com);
                break;
            case "metodoPagamento":
                if (valor == null || valor.isEmpty()) {
                    throw new MetodoDePagamentoInvalidoException();
                }
                if (valor.equals("banco")) {
                    throw new BancoNaoPodeSerNuloException();
                }
                if (valor.equals("emMaos")) {
                    e.setMetodoPagamento(new EmMaos());
                } else if (valor.equals("correios")) {
                    e.setMetodoPagamento(new Correios());
                } else {
                    throw new MetodoDePagamentoInvalidoException();
                }
                break;
            case "agendaPagamento":
                String novaAgenda = agendaRepository.getDescricaoCanonica(valor);
                e.setAgendaPagamento(novaAgenda);
                break;
            case "banco":
            case "agencia":
            case "contaCorrente":
                if (!(e.getMetodoPagamento() instanceof Banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                Banco b = (Banco) e.getMetodoPagamento();
                String banco = b.getBanco();
                String agencia = b.getAgencia();
                String conta = b.getContaCorrente();
                if ("banco".equals(atributo)) {
                    validarTexto(valor, new BancoNaoPodeSerNuloException());
                    banco = valor;
                } else if ("agencia".equals(atributo)) {
                    validarTexto(valor, new AgenciaNaoPodeSerNuloException());
                    agencia = valor;
                } else {
                    validarTexto(valor, new ContaCorrenteNaoPodeSerNuloException());
                    conta = valor;
                }
                e.setMetodoPagamento(new Banco(banco, agencia, conta));
                break;
            case "sindicalizado":
                if (valor == null || valor.isEmpty() ||
                        !("true".equals(valor) || "false".equals(valor))) {
                    throw new ValorDeveSerTrueOuFalseException();
                }
                if ("true".equals(valor)) {
                    if (e.getSindicato() != null) {
                        break;
                    }
                    e.setSindicato(new MembroSindicato(UUID.randomUUID().toString(), 0.0));
                } else {
                    e.setSindicato(null);
                }
                break;
            case "idSindicato":
                if (e.getSindicato() == null) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                validarTexto(valor, new IdentificacaoDoMembroNaoPodeSerNulaException());
                e.getSindicato().setIdMembro(valor);
                break;
            case "taxaSindical":
                if (e.getSindicato() == null) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                double taxa = parseValor(valor, new TaxaSindicalDeveSerNumericaException(),
                        new TaxaSindicalDeveSerNumericaException(), new TaxaSindicalDeveSerNaoNegativaException());
                e.getSindicato().setTaxaSindical(taxa);
                break;
            default:
                throw new AtributoNaoExisteException();
        }
        undoRedoService.pushUndo(estado);
    }

// altera informações de sindicalização do empregado
    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (!"sindicalizado".equals(atributo)) {
            throw new AtributoNaoExisteException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        if (valor == null || valor.isEmpty() || !("true".equals(valor) || "false".equals(valor))) {
            throw new ValorDeveSerTrueOuFalseException();
        }
        if ("true".equals(valor)) {
            validarTexto(idSindicato, new IdentificacaoDoSindicatoNaoPodeSerNulaException());
            double taxa = parseValor(taxaSindical, new TaxaSindicalNaoPodeSerNulaException(),
                    new TaxaSindicalDeveSerNumericaException(), new TaxaSindicalDeveSerNaoNegativaException());
            for (Empregado empExist : empregados.values()) {
                if (empExist.getSindicato() != null && idSindicato.equals(empExist.getSindicato().getIdMembro()) && empExist != e) {
                    throw new HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException();
                }
            }
            e.setSindicato(new MembroSindicato(idSindicato, taxa));
        } else {
            e.setSindicato(null);
        }
        undoRedoService.pushUndo(estado);
    }

// altera tipo do empregado ou método de pagamento conforme parâmetros
    public void alteraEmpregado(String emp, String atributo, String valor, String extra) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (!"tipo".equals(atributo)) {
            throw new AtributoNaoExisteException();
        }
        if (valor == null || valor.isEmpty() ||
                !("horista".equals(valor) || "assalariado".equals(valor) || "comissionado".equals(valor))) {
            throw new TipoInvalidoException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        Empregado novo;
        switch (valor) {
            case "horista":
                double salH = parseValor(extra, new SalarioNaoPodeSerNuloException(),
                        new SalarioDeveSerNumericoException(), new SalarioDeveSerNaoNegativoException());
                novo = new Horista(e.getId(), e.getNome(), e.getEndereco(), salH);
                break;
            case "assalariado":
                double salA = parseValor(extra, new SalarioNaoPodeSerNuloException(),
                        new SalarioDeveSerNumericoException(), new SalarioDeveSerNaoNegativoException());
                novo = new Assalariado(e.getId(), e.getNome(), e.getEndereco(), salA);
                break;
            default:
                double com = parseValor(extra, new ComissaoNaoPodeSerNulaException(),
                        new ComissaoDeveSerNumericaException(), new ComissaoDeveSerNaoNegativaException());
                novo = new Comissionado(e.getId(), e.getNome(), e.getEndereco(), e.getSalario(), com);
                break;
        }
        novo.setMetodoPagamento(e.getMetodoPagamento());
        novo.setSindicato(e.getSindicato());
        novo.setDataUltimoPagamento(e.getDataUltimoPagamento());
        novo.setAgendaPagamento(e.getAgendaPagamento());
        atribuirAgendaSeNecessario(novo);
        empregados.put(emp, novo);
        undoRedoService.pushUndo(estado);
    }

// altera método de pagamento para depósito em banco
    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (!"metodoPagamento".equals(atributo) || !"banco".equals(valor1)) {
            throw new MetodoDePagamentoInvalidoException();
        }
        validarTexto(banco, new BancoNaoPodeSerNuloException());
        validarTexto(agencia, new AgenciaNaoPodeSerNuloException());
        validarTexto(contaCorrente, new ContaCorrenteNaoPodeSerNuloException());
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        e.setMetodoPagamento(new Banco(banco, agencia, contaCorrente));
        undoRedoService.pushUndo(estado);
    }

    public void removerEmpregado(String emp) throws Exception {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        if (empregados.remove(emp) == null) {
            throw new EmpregadoNaoExisteException();
        }
        undoRedoService.pushUndo(estado);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        Horista e = obterHorista(emp);
        parseData(data, new DataInvalidaException());
        double h = parseValor(horas, new HorasDevemSerPositivasException(),
                new HorasDevemSerPositivasException(), new HorasDevemSerPositivasException());
        if (h <= 0) {
            throw new HorasDevemSerPositivasException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        e.getCartoesPonto().add(new CartaoDePonto(data, h));
        undoRedoService.pushUndo(estado);
    }

// calcula horas normais trabalhadas no periodo
    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        Horista e = obterHorista(emp);
        LocalDate ini = parseData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = parseData(dataFinal, new DataFinalInvalidaException());
        if (fim.isBefore(ini)) {
            throw new DataInicialNaoPodeSerPosteriorADataFinalException();
        }
        double total = 0.0;
        for (CartaoDePonto lp : e.getCartoesPonto()) {
            LocalDate d = parseData(lp.getData(), new DataInvalidaException());
            if (!d.isBefore(ini) && d.isBefore(fim)) {
                total += Math.min(8.0, lp.getHoras());
            }
        }
        return formatHoras(total);
    }

// calcula horas extras trabalhadas no periodo
    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        Horista e = obterHorista(emp);
        LocalDate ini = parseData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = parseData(dataFinal, new DataFinalInvalidaException());
        if (fim.isBefore(ini)) {
            throw new DataInicialNaoPodeSerPosteriorADataFinalException();
        }
        double total = 0.0;
        for (CartaoDePonto lp : e.getCartoesPonto()) {
            LocalDate d = parseData(lp.getData(), new DataInvalidaException());
            if (!d.isBefore(ini) && d.isBefore(fim)) {
                if (lp.getHoras() > 8.0) {
                    total += lp.getHoras() - 8.0;
                }
            }
        }
        return formatHoras(total);
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        Comissionado e = obterComissionado(emp);
        parseData(data, new DataInvalidaException());
        double v = parseValor(valor, new ValorDeveSerPositivoException(),
                new ValorDeveSerPositivoException(), new ValorDeveSerPositivoException());
        if (v <= 0) {
            throw new ValorDeveSerPositivoException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        e.getVendas().add(new ResultadoDeVenda(data, v));
        undoRedoService.pushUndo(estado);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        Comissionado e = obterComissionado(emp);
        LocalDate ini = parseData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = parseData(dataFinal, new DataFinalInvalidaException());
        if (fim.isBefore(ini)) {
            throw new DataInicialNaoPodeSerPosteriorADataFinalException();
        }
        double total = 0.0;
        for (ResultadoDeVenda lv : e.getVendas()) {
            LocalDate d = parseData(lv.getData(), new DataInvalidaException());
            if (!d.isBefore(ini) && d.isBefore(fim)) {
                total += lv.getValor();
            }
        }
        return format(total);
    }

// registra taxa de serviço para membro do sindicato
    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        if (membro == null || membro.isEmpty()) {
            throw new IdentificacaoDoMembroNaoPodeSerNulaException();
        }
        Empregado e = null;
        for (Empregado emp : empregados.values()) {
            if (emp.getSindicato() != null && membro.equals(emp.getSindicato().getIdMembro())) {
                e = emp;
                break;
            }
        }
        if (e == null) {
            throw new MembroNaoExisteException();
        }
        parseData(data, new DataInvalidaException());
        double v = parseValor(valor, new ValorDeveSerPositivoException(),
                new ValorDeveSerPositivoException(), new ValorDeveSerPositivoException());
        if (v <= 0) {
            throw new ValorDeveSerPositivoException();
        }
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        e.getSindicato().getTaxasServico().add(new TaxaServico(data, v));
        undoRedoService.pushUndo(estado);
    }

// soma taxas de serviço de empregado sindicalizado
    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (e.getSindicato() == null) {
            throw new EmpregadoNaoEhSindicalizadoException();
        }
        LocalDate ini = parseData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = parseData(dataFinal, new DataFinalInvalidaException());
        if (fim.isBefore(ini)) {
            throw new DataInicialNaoPodeSerPosteriorADataFinalException();
        }
        double total = 0.0;
        for (TaxaServico lt : e.getSindicato().getTaxasServico()) {
            LocalDate d = parseData(lt.getData(), new DataInvalidaException());
            if (!d.isBefore(ini) && d.isBefore(fim)) {
                total += lt.getValor();
            }
        }
        return format(total);
    }

    private void registrarAgendasExistentes() {
        for (Empregado e : this.empregados.values()) {
            agendaRepository.registrarExistente(e.getAgendaPagamento());
        }
    }

    private void atribuirAgendaPadrao(Empregado e) {
        e.setAgendaPagamento(agendaRepository.getAgendaPadrao(e));
    }

    private void atribuirAgendaSeNecessario(Empregado e) {
        if (e.getAgendaPagamento() == null || e.getAgendaPagamento().trim().isEmpty()
                || !agendaRepository.existeAgenda(e.getAgendaPagamento())) {
            atribuirAgendaPadrao(e);
        }
    }

    private void validarTexto(String valor, Exception ex) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw ex;
        }
    }

    double parseValor(String valor, Exception msgNulo, Exception msgNumerico, Exception msgNegativo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw msgNulo;
        }
        String v = valor.replace(',', '.');
        double d;
        try {
            d = Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            throw msgNumerico;
        }
        if (d < 0) {
            throw msgNegativo;
        }
        return d;
    }

    String format(double valor) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(new Locale("pt", "BR"));
        df.setDecimalFormatSymbols(dfs);
        return df.format(valor);
    }

    String formatHoras(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##");
        java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(new Locale("pt", "BR"));
        df.setDecimalFormatSymbols(dfs);
        return df.format(valor);
    }

    Empregado obterEmpregado(String emp) throws Exception {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        }
        Empregado e = empregados.get(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException();
        }
        return e;
    }

    Horista obterHorista(String emp) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (!(e instanceof Horista)) {
            throw new EmpregadoNaoEhHoristaException();
        }
        return (Horista) e;
    }

    Comissionado obterComissionado(String emp) throws Exception {
        Empregado e = obterEmpregado(emp);
        if (!(e instanceof Comissionado)) {
            throw new EmpregadoNaoEhComissionadoException();
        }
        return (Comissionado) e;
    }

    LocalDate parseData(String data, Exception exception) throws Exception {
        if (data == null || data.trim().isEmpty()) {
            throw exception;
        }
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(data, f);
        } catch (Exception ex) {
            throw exception;
        }
    }

// gera cópia do map de empregados para uso no undo redo
    Map<String, Empregado> deepCopyEmpregados() {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(empregados);
            oos.flush();
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            return (Map<String, Empregado>) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
