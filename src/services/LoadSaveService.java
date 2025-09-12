package services;

import br.ufal.ic.p2.wepayu.exceptions.FalhaAoSalvarXmlException;
import br.ufal.ic.p2.wepayu.models.empregado.Empregado;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoadSaveService {
    private static final String FILE = "empregados.xml";

    // recupera empregados salvos em disco
    public Map<String, Empregado> load() {
        File f = new File(FILE);
        Map<String, Empregado> empregados = new LinkedHashMap<>();
        if (!f.exists()) return empregados;
        try (XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)))) {
            Object obj = dec.readObject();
            if (obj instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) obj;
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    if (e.getKey() != null && e.getValue() instanceof Empregado) {
                        empregados.put(String.valueOf(e.getKey()), (Empregado) e.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            f.delete();
            empregados.clear();
        }
        return empregados;
    }

    // salva empregados no arquivo xml
    public void save(Map<String, Empregado> empregados) throws Exception {
        try (XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(FILE)))) {
            enc.writeObject(new LinkedHashMap<>(empregados));
            enc.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FalhaAoSalvarXmlException();
        }
    }
}
