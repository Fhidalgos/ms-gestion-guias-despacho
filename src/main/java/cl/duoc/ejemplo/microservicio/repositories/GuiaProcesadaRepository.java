package cl.duoc.ejemplo.microservicio.repositories;

import cl.duoc.ejemplo.microservicio.entities.GuiaProcesada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuiaProcesadaRepository
        extends JpaRepository<GuiaProcesada, Long> {

    /**
     * Permite buscar un registro utilizando
     * el identificador original de la guía.
     */
    Optional<GuiaProcesada> findByIdGuia(String idGuia);
}