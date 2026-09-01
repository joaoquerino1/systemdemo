package com.logistica.sistema.repository;

import com.logistica.sistema.model.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    Optional<RegistroPonto> findByUsuarioIdAndData(Long usuarioId, LocalDate data);

    List<RegistroPonto> findByUsuarioIdAndDataBetweenOrderByDataAsc(
            Long usuarioId, LocalDate inicio, LocalDate fim);
}
