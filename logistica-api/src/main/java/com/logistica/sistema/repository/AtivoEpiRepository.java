package com.logistica.sistema.repository;

import com.logistica.sistema.model.AtivoEpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtivoEpiRepository extends JpaRepository<AtivoEpi, Long> {

    List<AtivoEpi> findByAtivoIdIn(List<Long> ids);
}
