package com.led.broker.repository;

import com.led.broker.model.Dispositivo;
import com.led.broker.model.constantes.Comando;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface DispositivoRepository extends MongoRepository<Dispositivo, Long> {



    @Query("{ 'ativo': ?0 }")
    List<Dispositivo> findAllByAtivo(boolean ativo);
    @Query("{ 'ativo': ?0, 'ignorarAgenda': ?1, 'comando': ?2 }")
    List<Dispositivo> findAllByAtivoIgnorarAgendaOnline(boolean ativo, boolean ignorarAgenda, Comando comando);
    @Query("{ 'ativo': ?0 }")
    Page<Dispositivo> findAllByAtivo(boolean ativo, Pageable pageable);
    @Query("{ 'ativo': ?0 }")
    Page<Dispositivo> findAllByInativo(boolean ativo, Pageable pageable);
    @Query("{ 'ativo' : true, 'ultimaAtualizacao' : { $lt: ?0 }}")
    List<Dispositivo> findAllAtivosComUltimaAtualizacaoAntes(Date dataLimite);
    @Query("{ 'ativo' : true, 'ultimaAtualizacao' : { $lt: ?0 } }")
    Page<Dispositivo> findAllAtivosComUltimaAtualizacaoAntes(Date dataLimite, Pageable pageable);
    @Query("{ 'ativo' : true, 'ultimaAtualizacao' : { $lt: ?0 }, 'status' : 'Online'}")
    List<Dispositivo> findAllAtivosComUltimaAtualizacaoAntesQueEstavaoOnline(Date dataLimite);

}
