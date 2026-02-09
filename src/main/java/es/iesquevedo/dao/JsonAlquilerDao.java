package es.iesquevedo.dao;

import es.iesquevedo.modelo.Alquiler;

import java.util.List;
import java.util.Optional;

public interface JsonAlquilerDao {
    void load();

    void persist();

    Alquiler save(Alquiler alquiler);

    Optional<Alquiler> findById(Long id);

    List<Alquiler> findAll();

    List<Alquiler> findBySocio(String dni);

    List<Alquiler> findActiveByPelicula(String titulo);

    boolean deleteById(Long id);
}
