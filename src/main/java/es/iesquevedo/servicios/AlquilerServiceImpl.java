package es.iesquevedo.servicios;

import es.iesquevedo.dao.*;
import es.iesquevedo.modelo.Alquiler;
import es.iesquevedo.modelo.Pelicula;
import es.iesquevedo.modelo.Socio;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AlquilerServiceImpl implements AlquilerService {
    private final JsonAlquilerDao alquilerDao;
    private final JsonPeliculaDao peliculaDao;
    private final JsonSocioDao socioDao;
    private final int limitePorSocio;

    public AlquilerServiceImpl(JsonPeliculaDao jsonP, JsonSocioDao jsonS, JsonAlquilerDao jsonA) {
        // Acoplamiento directo: se crean las implementaciones concretas aquí.
        String base = System.getProperty("user.dir");
        this.peliculaDao = jsonP;
        this.socioDao = jsonS;
        this.alquilerDao = jsonA;
        this.limitePorSocio = 3;
    }

    // Constructor alternativo para pruebas que permite pasar rutas de fichero
    public AlquilerServiceImpl(String basePath) {
        this.peliculaDao = new JsonPeliculaDaoImpl(basePath + "/peliculas.json");
        this.socioDao = new JsonSocioDaoImpl(basePath + "/socios.json");
        this.alquilerDao = new JsonAlquilerDaoImpl(basePath + "/alquileres.json");
        this.limitePorSocio = 3;
    }

    @Override
    public Alquiler alquilar(Socio socio, Pelicula pelicula) {
        if (socio == null) throw new RuntimeException("Socio nulo");
        if (pelicula == null) throw new RuntimeException("Pelicula nula");

        // asegurar que socio existe
        if (socioDao.findByDni(socio.getDni()).isEmpty()) {
            socioDao.save(socio);
        }

        // comprobar limite de socio
        List<Alquiler> porSocio = alquilerDao.findBySocio(socio.getDni()).stream().filter(a -> !a.isDevuelto()).collect(Collectors.toList());
        if (porSocio.size() >= limitePorSocio) {
            throw new RuntimeException("El socio ha alcanzado el límite de alquileres activos: " + limitePorSocio);
        }

        // buscar un ejemplar disponible de la pelicula
        var optEjemplar = peliculaDao.findAvailableByTitulo(pelicula.getTitulo());
        if (optEjemplar.isEmpty()) {
            throw new RuntimeException("No hay ejemplares disponibles para: " + pelicula.getTitulo());
        }
        Pelicula ejemplar = optEjemplar.get();

        // comprobar que el socio no tiene ya ese ejemplar activo
        boolean yaTieneMismoEjemplar = porSocio.stream().anyMatch(a -> a.getPelicula() != null && a.getPelicula().getId() != null && a.getPelicula().getId().equals(ejemplar.getId()));
        if (yaTieneMismoEjemplar) {
            throw new RuntimeException("El socio ya tiene alquilado ese ejemplar (id=" + ejemplar.getId() + ")");
        }

        // marcar no disponible
        ejemplar.setDisponible(false);
        peliculaDao.save(ejemplar);

        // crear alquiler apuntando al ejemplar
        Alquiler alquiler = new Alquiler(socio, ejemplar, LocalDate.now());
        return alquilerDao.save(alquiler);
    }

    @Override
    public void devolver(Long alquilerId) {
        var opt = alquilerDao.findById(alquilerId);
        if (opt.isEmpty()) throw new RuntimeException("Alquiler no encontrado: " + alquilerId);
        Alquiler a = opt.get();
        if (a.isDevuelto()) throw new RuntimeException("Alquiler ya devuelto: " + alquilerId);
        a.setFechaDevolucion(LocalDate.now());
        alquilerDao.save(a);

        // marcar ejemplar disponible otra vez
        Pelicula ejemplar = a.getPelicula();
        if (ejemplar != null && ejemplar.getId() != null) {
            ejemplar.setDisponible(true);
            peliculaDao.save(ejemplar);
        }
    }

    @Override
    public List<Alquiler> listarAlquileres() {
        return alquilerDao.findAll();
    }

    @Override
    public List<Alquiler> listarPorSocio(String dni) {
        return alquilerDao.findBySocio(dni);
    }
}
