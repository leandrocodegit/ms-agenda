package com.led.broker.model;

import com.led.broker.model.constantes.Efeito;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Parametro {

    private int pino;
    private Efeito efeito;
    private int[] cor;
    private List<String> corHexa;
    private int[] correcao;
    private Configuracao configuracao;
}
