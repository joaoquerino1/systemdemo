package com.logistica.sistema.repository;

import com.logistica.sistema.model.AtivoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtivoVeiculoRepository extends JpaRepository<AtivoVeiculo, Long> {

    Optional<AtivoVeiculo> findByPlaca(String placa);

    List<AtivoVeiculo> findByAtivoIdIn(List<Long> ids);
}
