package es.iesquevedo.servicios;

import es.iesquevedo.modelo.Alquiler;
import es.iesquevedo.modelo.Pelicula;
import es.iesquevedo.modelo.Socio;

import java.util.List;

public interface AlquilerService {
    Alquiler alquilar(Socio socio, Pelicula pelicula);

    void devolver(Long alquilerId);

    List<Alquiler> listarAlquileres();

    List<Alquiler> listarPorSocio(String dni);
}
