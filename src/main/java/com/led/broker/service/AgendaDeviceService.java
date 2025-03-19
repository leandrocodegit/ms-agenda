package com.led.broker.service;

import com.google.gson.Gson;
import com.led.broker.model.Agenda;
import com.led.broker.model.Dispositivo;
import com.led.broker.model.Log;
import com.led.broker.model.constantes.Comando;
import com.led.broker.model.constantes.ModoOperacao;
import com.led.broker.model.constantes.StatusConexao;
import com.led.broker.model.constantes.Topico;
import com.led.broker.repository.AgendaRepository;
import com.led.broker.repository.DispositivoRepository;
import com.led.broker.repository.LogRepository;
import com.led.broker.repository.OperacaoRepository;
import com.led.broker.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.MonoSink;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgendaDeviceService {

    private final AgendaRepository agendaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final OperacaoRepository operacaoRepository;
    private final ComandoService comandoService;
    private final LogRepository logRepository;
    public static Map<String, MonoSink<String>> streams = new HashMap<>();

    public List<Agenda> listaTodosAgendasPrevistaHoje() {
        LocalDate data = LocalDateTime.now().toLocalDate();
        return agendaRepository.findAgendasByDataDentroDoIntervalo(data);
    }

    public Agenda buscarAgendaDipositivoPrevistaHoje(String mac) {
        List<Agenda> agendaList = agendaRepository.findFirstByDataAndDispositivo(LocalDate.now(), LocalDate.now(), mac, UUID.randomUUID());
        if (!agendaList.isEmpty()) {
            System.out.println("Com agenda");
            return agendaList.get(0);
        }
        System.out.println("Sem agenda");
        return null;
    }

    public boolean possuiAgendaDipositivoPrevistaHoje(Agenda agenda, String mac) {
        return !agendaRepository.findFirstByDataAndDispositivo(agenda.getInicio(), agenda.getTermino(), mac, agenda.getId()).isEmpty();
    }

    public void atualizarDataExecucao(Agenda agenda) {
        agenda.setExecucao(LocalDateTime.now().toLocalDate());
        agendaRepository.save(agenda);
    }

    public void atualizarOperacaoDispositivo(Agenda agenda, Dispositivo dispositivo) {
        dispositivo.getOperacao().setModoOperacao(ModoOperacao.AGENDA);
        dispositivo.getOperacao().setAgenda(agenda);
        operacaoRepository.save(dispositivo.getOperacao());
    }

    public void enviarComando(Agenda agenda) {

        if (agenda.getCor() != null) {
            List<Dispositivo> dispositivos = Collections.EMPTY_LIST;

            if (agenda.isTodos()) {
                dispositivos = dispositivoRepository.findAllByAtivoIgnorarAgendaOnline(true, false, Comando.ONLINE);
            } else {
                dispositivos = dispositivoRepository.findAllById(agenda.getDispositivos());
            }

            if (!dispositivos.isEmpty()) {
                dispositivos.forEach(device -> {

                    if (device.isAtivo() && device.getConexao().getStatus().equals(StatusConexao.Online)) {
                        if (Boolean.FALSE.equals(device.isIgnorarAgenda()) && !TimeUtil.isTime(device)) {
                            device.setCor(agenda.getCor());
                            comandoService.sincronizar(device.getId());
                            atualizarOperacaoDispositivo(agenda, device);
                        }
                    }
                });
                logRepository.save(Log.builder()
                        .data(LocalDateTime.now())
                        .usuario(Comando.SISTEMA.value())
                        .mensagem("Tarefa agenda executada")
                        .cor(agenda.getCor())
                        .comando(Comando.SISTEMA)
                        .descricao("Tarefa agenda executada")
                        .mac(agenda.getDispositivos().toString())
                        .build());
            }
        }
    }

}
