package es.iesquevedo.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.iesquevedo.modelo.Socio;
import es.iesquevedo.util.LocalDateAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonSocioDaoImpl implements JsonSocioDao {
    private final File file;
    private final Gson gson;
    private List<Socio> store = new ArrayList<>();

    public JsonSocioDaoImpl(String filePath) {
        this.file = new File(filePath);
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
        load();
    }

    @Override
    public void load() {
        try {
            if (!file.exists()) return;
            Type listType = new TypeToken<List<Socio>>() {}.getType();
            try (FileReader fr = new FileReader(file)) {
                List<Socio> list = gson.fromJson(fr, listType);
                if (list != null) store = list;
            }
        } catch (Exception e) {
            try {
                File backup = new File(file.getAbsolutePath() + ".corrupt.bak");
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                // ignore
            }
            store = new ArrayList<>();
            try { persist(); } catch (Exception ex) { /* ignore */ }
        }
    }

    @Override
    public void persist() {
        try (FileWriter fw = new FileWriter(file)) {
            gson.toJson(store, fw);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando socios en " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public Socio save(Socio socio) {
        store = store.stream().filter(s -> !s.getDni().equals(socio.getDni())).collect(Collectors.toList());
        store.add(socio);
        persist();
        return socio;
    }

    @Override
    public Optional<Socio> findByDni(String dni) {
        return store.stream().filter(s -> s.getDni().equals(dni)).findFirst();
    }

    @Override
    public List<Socio> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public boolean deleteByDni(String dni) {
        int before = store.size();
        store = store.stream().filter(s -> !s.getDni().equals(dni)).collect(Collectors.toList());
        boolean changed = store.size() != before;
        if (changed) persist();
        return changed;
    }
}
