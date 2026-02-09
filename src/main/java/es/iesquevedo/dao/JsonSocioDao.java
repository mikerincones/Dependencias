package es.iesquevedo.dao;

import es.iesquevedo.modelo.Socio;

import java.util.List;
import java.util.Optional;

public interface JsonSocioDao {
    void load();

    void persist();

    Socio save(Socio socio);

    Optional<Socio> findByDni(String dni);

    List<Socio> findAll();

    boolean deleteByDni(String dni);
}
