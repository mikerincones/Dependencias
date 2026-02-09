package es.iesquevedo.servicios;

import es.iesquevedo.modelo.Alquiler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlquilerServiceImplTest {

    @Test
    void listarAlquileres() {
        AlquilerServiceImpl alquiler = new AlquilerServiceImpl("src/_test/resources/_test");
        List<Alquiler> alquileres = alquiler.listarAlquileres();
    }
}