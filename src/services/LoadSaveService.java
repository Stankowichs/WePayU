package services;

import br.ufal.ic.p2.wepayu.exceptions.FalhaAoSalvarXmlException;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LoadSaveService {
    private static final String FILE = "empregados.xml";

    // recupera empregados salvos em disco
    public Result load() {
        File f = new File(FILE);
        Map<String, Empregado> empregados = new LinkedHashMap<>();
        Set<String> agendas = new LinkedHashSet<>();
        if (!f.exists()) return new Result(empregados, agendas);
        try (XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)))) {
            Object obj = dec.readObject();
            if (obj instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) obj;
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    if (e.getKey() != null && e.getValue() instanceof Empregado) {
                        empregados.put(String.valueOf(e.getKey()), (Empregado) e.getValue());
                    }
                }
            } else if (obj instanceof PersistedData) {
                PersistedData data = (PersistedData) obj;
                Map<String, Empregado> mapa = data.getEmpregados();
                if (mapa != null) {
                    for (Map.Entry<String, Empregado> e : mapa.entrySet()) {
                        if (e.getKey() != null && e.getValue() != null) {
                            empregados.put(e.getKey(), e.getValue());
                        }
                    }
                }
                agendas.addAll(data.getAgendas());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            f.delete();
            empregados.clear();
            agendas.clear();
        }
        return new Result(empregados, agendas);
    }

    // salva empregados no arquivo xml
    public void save(Map<String, Empregado> empregados, Set<String> agendas) throws Exception {
        try (XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(FILE)))) {
            PersistedData data = new PersistedData();
            data.setEmpregados(new LinkedHashMap<>(empregados));
            data.setAgendas(new ArrayList<>(agendas));
            enc.writeObject(data);
            enc.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FalhaAoSalvarXmlException();
        }
    }

    public static class Result {
        private final Map<String, Empregado> empregados;
        private final Set<String> agendasPersonalizadas;

        public Result(Map<String, Empregado> empregados, Set<String> agendasPersonalizadas) {
            this.empregados = empregados;
            this.agendasPersonalizadas = agendasPersonalizadas;
        }

        public Map<String, Empregado> getEmpregados() {
            return empregados;
        }

        public Set<String> getAgendasPersonalizadas() {
            return agendasPersonalizadas;
        }
    }
}
