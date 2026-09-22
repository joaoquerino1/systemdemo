package com.logistica.sistema.model;

/**
 * As 7 etapas fixas do cronograma de producao, na ordem em que
 * aparecem no formulario impresso. A ordem do enum e usada apenas
 * para exibicao - o fluxo de execucao e livre (ver plano, secao 2);
 * a unica regra de sequencia (CONFERENCIA por ultimo) fica no service.
 */
public enum EtapaProducao {

    SEPARACAO("Separação"),
    CORTE("Corte"),
    USINAGEM("Usinagem"),
    PREPARACAO("Preparação"),
    MONTAGEM("Montagem"),
    VIDROS("Vidros"),
    CONFERENCIA("Conferência");

    private final String label;

    EtapaProducao(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Etapa seguinte na ordem de exibicao (null se for a ultima). */
    public EtapaProducao proxima() {
        EtapaProducao[] todas = values();
        int indice = ordinal();
        return indice + 1 < todas.length ? todas[indice + 1] : null;
    }
}
