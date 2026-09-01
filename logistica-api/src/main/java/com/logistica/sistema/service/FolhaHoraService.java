package com.logistica.sistema.service;

import com.logistica.sistema.exception.RecursoNaoEncontradoException;
import com.logistica.sistema.model.RegistroPonto;
import com.logistica.sistema.model.Usuario;
import com.logistica.sistema.repository.RegistroPontoRepository;
import com.logistica.sistema.repository.UsuarioRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Gera a folha de hora em PDF seguindo os campos obrigatorios do
 * padrao definido: identificacao da empresa, identificacao do
 * colaborador, periodo, marcacoes de horario, espaco para excecoes
 * e campo de assinatura.
 *
 * IMPORTANTE: o calculo automatico de horas extras/atrasos/faltas
 * exige uma jornada padrao configurada (ex: 8h/dia, tolerancia,
 * calendario de feriados), que ainda nao foi definida no sistema.
 * Por isso, essa coluna sai em branco no PDF, para preenchimento
 * manual - ajustar quando a jornada padrao da empresa for definida.
 */
@Service
@RequiredArgsConstructor
public class FolhaHoraService {

    private final RegistroPontoRepository registroPontoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.empresa.nome}")
    private String nomeEmpresa;

    @Value("${app.empresa.cnpj}")
    private String cnpjEmpresa;

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + usuarioId));

        List<RegistroPonto> registros = registroPontoRepository
                .findByUsuarioIdAndDataBetweenOrderByDataAsc(usuarioId, inicio, fim);

        try {
            Document documento = new Document(PageSize.A4, 36, 36, 54, 36);
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            PdfWriter.getInstance(documento, saida);
            documento.open();

            adicionarCabecalho(documento, usuario, inicio, fim);
            adicionarTabelaMarcacoes(documento, registros);
            adicionarAssinatura(documento, usuario);

            documento.close();
            return saida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar o PDF da folha de hora.", e);
        }
    }

    private void adicionarCabecalho(Document documento, Usuario usuario,
                                     LocalDate inicio, LocalDate fim) throws DocumentException {
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titulo = new Paragraph("Folha de Ponto", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(12);
        documento.add(titulo);

        documento.add(new Paragraph("Empresa: " + nomeEmpresa, fonteNormal));
        documento.add(new Paragraph("CNPJ: " + cnpjEmpresa, fonteNormal));
        documento.add(new Paragraph(" ", fonteNormal));

        documento.add(new Paragraph("Colaborador: " + usuario.getNome(), fonteNormal));
        documento.add(new Paragraph("CPF: " + formatarCpf(usuario.getCpf()), fonteNormal));
        documento.add(new Paragraph("Matrícula: " + usuario.getMatricula(), fonteNormal));
        if (usuario.getCargo() != null) {
            documento.add(new Paragraph("Cargo: " + usuario.getCargo(), fonteNormal));
        }

        String periodo = capitalizar(inicio.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")))
                + "/" + inicio.getYear()
                + "  (" + inicio.format(FMT_DATA) + " a " + fim.format(FMT_DATA) + ")";
        documento.add(new Paragraph("Período de apuração: " + periodo, fonteNormal));
        documento.add(new Paragraph(" ", fonteNormal));
    }

    private void adicionarTabelaMarcacoes(Document documento, List<RegistroPonto> registros)
            throws DocumentException {
        Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font fonteCelula = FontFactory.getFont(FontFactory.HELVETICA, 8);

        PdfPTable tabela = new PdfPTable(7);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{12, 11, 13, 13, 11, 11, 29});

        for (String coluna : new String[]{
                "Data", "Entrada", "Saída Intervalo", "Volta Intervalo",
                "Saída", "Horas", "Exceções (extras/atrasos/faltas)"}) {
            PdfPCell celula = new PdfPCell(new Phrase(coluna, fonteCabecalho));
            celula.setBackgroundColor(new Color(230, 230, 230));
            celula.setPadding(4);
            tabela.addCell(celula);
        }

        for (RegistroPonto r : registros) {
            tabela.addCell(celula(r.getData().format(FMT_DATA), fonteCelula));
            tabela.addCell(celula(formatarHora(r.getHoraEntrada()), fonteCelula));
            tabela.addCell(celula(formatarHora(r.getHoraSaidaIntervalo()), fonteCelula));
            tabela.addCell(celula(formatarHora(r.getHoraVoltaIntervalo()), fonteCelula));
            tabela.addCell(celula(formatarHora(r.getHoraSaida()), fonteCelula));
            tabela.addCell(celula(calcularHorasTrabalhadas(r), fonteCelula));
            // coluna de excecoes fica em branco - ver nota da classe
            tabela.addCell(celula("", fonteCelula));
        }

        documento.add(tabela);
        documento.add(new Paragraph(" "));
    }

    private void adicionarAssinatura(Document documento, Usuario usuario) throws DocumentException {
        Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph espaco = new Paragraph(" \n \n");
        documento.add(espaco);

        Paragraph linhaAssinatura = new Paragraph("________________________________________", fonteNormal);
        linhaAssinatura.setSpacingBefore(24);
        documento.add(linhaAssinatura);
        documento.add(new Paragraph(usuario.getNome() + " - Assinatura do colaborador", fonteNormal));
    }

    private PdfPCell celula(String texto, Font fonte) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setPadding(4);
        return celula;
    }

    private String formatarHora(LocalTime hora) {
        return hora != null ? hora.format(FMT_HORA) : "-";
    }

    private String calcularHorasTrabalhadas(RegistroPonto r) {
        if (r.getHoraEntrada() == null || r.getHoraSaida() == null) {
            return "-";
        }
        Duration total = Duration.between(r.getHoraEntrada(), r.getHoraSaida());
        if (r.getHoraSaidaIntervalo() != null && r.getHoraVoltaIntervalo() != null) {
            total = total.minus(Duration.between(r.getHoraSaidaIntervalo(), r.getHoraVoltaIntervalo()));
        }
        long horas = total.toHours();
        long minutos = total.toMinutesPart();
        return String.format("%02d:%02d", horas, minutos);
    }

    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
