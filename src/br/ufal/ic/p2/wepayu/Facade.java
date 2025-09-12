package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;

import services.EmpregadoService;
import services.FolhaPagamentoService;
import services.UndoRedoService;
import services.LoadSaveService;

import java.io.File;
import java.util.Map;

public class Facade {
    private final EmpregadoService empregadoService;
    private final FolhaPagamentoService folhaPagamentoService;
    private final UndoRedoService undoRedoService;
    private final LoadSaveService loadSaveService;
    private boolean encerrado = false;

    public Facade() {
        loadSaveService = new LoadSaveService();
        Map<String, Empregado> emps = loadSaveService.load();
        empregadoService = new EmpregadoService(emps);
        folhaPagamentoService = new FolhaPagamentoService(empregadoService);
        undoRedoService = new UndoRedoService(empregadoService, folhaPagamentoService);
        empregadoService.setUndoRedoService(undoRedoService);
        folhaPagamentoService.setUndoRedoService(undoRedoService);
    }

    public void zerarSistema() {
        UndoRedoService.Estado estado = undoRedoService.snapshot();
        empregadoService.clear();
        folhaPagamentoService.clear();
        File f = new File("empregados.xml");
        if (f.exists()) {
            f.delete();
        }
        undoRedoService.pushUndo(estado);
    }

// sem comissão
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return empregadoService.criarEmpregado(nome, endereco, tipo, salario);
    }

// com comissão
    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        return empregadoService.criarEmpregado(nome, endereco, tipo, salario, comissao);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        return empregadoService.getAtributoEmpregado(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return empregadoService.getEmpregadoPorNome(nome, indice);
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        empregadoService.alteraEmpregado(emp, atributo, valor);
    }

// altera sindicalização
    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        empregadoService.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
    }

// altera tipo ou detalhes do empregado
    public void alteraEmpregado(String emp, String atributo, String valor, String extra) throws Exception {
        empregadoService.alteraEmpregado(emp, atributo, valor, extra);
    }

// altera método de pagamento para banco
    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        empregadoService.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
    }

    public void removerEmpregado(String emp) throws Exception {
        empregadoService.removerEmpregado(emp);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        empregadoService.lancaCartao(emp, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return empregadoService.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return empregadoService.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

// lança venda de empregado comissionado
    public void lancaVenda(String emp, String data, String valor) throws Exception {
        empregadoService.lancaVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return empregadoService.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        empregadoService.lancaTaxaServico(membro, data, valor);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return empregadoService.getTaxasServico(emp, dataInicial, dataFinal);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        folhaPagamentoService.rodaFolha(data, saida);
    }

    public String totalFolha(String data) throws Exception {
        return folhaPagamentoService.totalFolha(data);
    }

    public void undo() throws Exception {
        if (encerrado) {
            throw new NaoPodeDarComandosDepoisDeEncerrarSistemaException();
        }
        undoRedoService.undo();
    }

    public void redo() throws Exception {
        if (encerrado) {
            throw new NaoPodeDarComandosDepoisDeEncerrarSistemaException();
        }
        undoRedoService.redo();
    }

    public String getNumeroDeEmpregados() {
        return String.valueOf(empregadoService.getEmpregados().size());
    }

    public void encerrarSistema() throws Exception {
        loadSaveService.save(empregadoService.getEmpregados());
        encerrado = true;
        undoRedoService.clear();
    }
}
