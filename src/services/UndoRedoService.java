package services;

import br.ufal.ic.p2.wepayu.exceptions.NaoHaComandoADesfazerException;
import br.ufal.ic.p2.wepayu.exceptions.NaoHaComandoARefazerException;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;

import java.io.*;
import java.util.*;

public class UndoRedoService {
    private final EmpregadoService empregadoService;
    private final FolhaPagamentoService folhaPagamentoService;
    private final Deque<Estado> undoStack = new ArrayDeque<>();
    private final Deque<Estado> redoStack = new ArrayDeque<>();

    public UndoRedoService(EmpregadoService emp, FolhaPagamentoService pay) {
        this.empregadoService = emp;
        this.folhaPagamentoService = pay;
    }

// gera uma cópia completa do estado atual para registrar no histórico
    public Estado snapshot() {
        Estado st = new Estado();
        st.empregados = empregadoService.deepCopyEmpregados();
        st.folhasProcessadas = new HashMap<>(folhaPagamentoService.getFolhasProcessadas());
        st.totaisProcessados = new HashMap<>(folhaPagamentoService.getTotaisProcessados());
        st.agendasPersonalizadas = new LinkedHashSet<>(empregadoService.getAgendasPersonalizadas());
        return st;
    }

// restaura um estado previamente salvo
    public void restore(Estado st) {
        empregadoService.setEmpregados(st.empregados, st.agendasPersonalizadas);
        folhaPagamentoService.setFolhasProcessadas(st.folhasProcessadas);
        folhaPagamentoService.setTotaisProcessados(st.totaisProcessados);
    }

    public void pushUndo(Estado st) {
        undoStack.push(st);
        redoStack.clear();
    }

// desfaz última operação registrando estado atual no redo
    public void undo() throws Exception {
        if (undoStack.isEmpty()) {
            throw new NaoHaComandoADesfazerException();
        }
        redoStack.push(snapshot());
        restore(undoStack.pop());
    }

// refaz operação previamente desfeita
    public void redo() throws Exception {
        if (redoStack.isEmpty()) {
            throw new NaoHaComandoARefazerException();
        }
        undoStack.push(snapshot());
        restore(redoStack.pop());
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

// guarda os dados necessários para restaurar o sistema
    public static class Estado implements Serializable {
        Map<String, Empregado> empregados;
        Map<String, String> folhasProcessadas;
        Map<String, Double> totaisProcessados;
        Set<String> agendasPersonalizadas;
    }
}
