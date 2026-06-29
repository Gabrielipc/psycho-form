package com.uam.psychoform.reporting.service;

import com.uam.psychoform.academic.repository.ParticipanteRepository;
import com.uam.psychoform.assessment.repository.SesionAplicacionRepository;
import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.reporting.repository.ReporteGeneradoRepository;
import com.uam.psychoform.reporting.model.FormatoReporte;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportDocumentService {
    private final ParticipanteRepository participantes;
    private final SesionAplicacionRepository sesiones;
    private final AuditoriaRepository auditoria;
    private final ReporteGeneradoRepository reportes;
    private final ResultQueryService results;

    public ReportDocumentService(ParticipanteRepository participantes, SesionAplicacionRepository sesiones,
            AuditoriaRepository auditoria, ReporteGeneradoRepository reportes, ResultQueryService results) {
        this.participantes = participantes;
        this.sesiones = sesiones;
        this.auditoria = auditoria;
        this.reportes = reportes;
        this.results = results;
    }

    public Document export(String type, FormatoReporte format, Long attemptId, Long sessionId) {
        List<Map<String, Object>> rows = switch (type) {
            case "participants" -> participantes.findAll().stream().map(p -> row(
                    "id", p.getId(), "codigo", p.getCodigoParticipante(), "nombres", p.getNombres(),
                    "apellidos", p.getApellidos(), "estado", p.getEstado())).toList();
            case "sessions" -> sesiones.findAll().stream().map(s -> row(
                    "id", s.getId(), "codigo", s.getCodigoSesion(), "nombre", s.getNombreSesion(),
                    "estado", s.getEstado(), "inicio", s.getInicioProgramado(), "fin", s.getFinProgramado())).toList();
            case "audit" -> auditoria.findAllByOrderByCreadoEnDesc().stream().map(a -> row(
                    "id", a.getId(), "accion", a.getAccion(), "entidad", a.getEntidad(),
                    "entidadId", a.getEntidadId(), "fecha", a.getCreadoEn())).toList();
            case "registeredReports" -> reportes.findAll().stream().map(r -> row(
                    "id", r.getId(), "tipo", r.getTipoReporte(), "formato", r.getFormato(),
                    "ruta", r.getRutaAlmacenamiento(), "fecha", r.getGeneradoEn())).toList();
            case "individualResult" -> individualRows(attemptId);
            case "sessionSummary" -> sessionRows(sessionId);
            default -> throw new IllegalArgumentException("Tipo de reporte no soportado: " + type);
        };
        byte[] bytes = switch (format) {
            case CSV -> csv(rows).getBytes(StandardCharsets.UTF_8);
            case PDF -> pdf(type, rows);
            case XLSX -> xlsx(rows);
        };
        String ext = format.name().toLowerCase(Locale.ROOT).replace("xlsx", "xlsx");
        String mime = switch (format) {
            case CSV -> "text/csv";
            case PDF -> "application/pdf";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };
        return new Document(type + "." + ext, mime, bytes);
    }

    private List<Map<String, Object>> individualRows(Long attemptId) {
        if (attemptId == null) throw new IllegalArgumentException("attemptId requerido");
        var result = results.getAttemptResult(attemptId);
        return result.dimensions().stream().map(d -> row(
                "attemptId", result.attemptId(), "resultId", result.resultId(), "estado", result.status(),
                "puntajeTotal", result.totalScore(), "dimension", d.name(), "puntaje", d.directScore(),
                "percentil", d.percentile(), "categoria", d.category(), "interpretacion", d.interpretation()))
                .toList();
    }

    private List<Map<String, Object>> sessionRows(Long sessionId) {
        if (sessionId == null) throw new IllegalArgumentException("sessionId requerido");
        var summary = results.getSessionSummary(sessionId);
        return List.of(row("sessionId", summary.sessionId(), "assigned", summary.assignedCount(),
                "started", summary.startedCount(), "completed", summary.completedCount(), "scored", summary.scoredCount()));
    }

    private static Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }

    private static String csv(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return "";
        List<String> headers = new ArrayList<>(rows.getFirst().keySet());
        StringBuilder out = new StringBuilder(String.join(",", headers)).append('\n');
        for (Map<String, Object> row : rows) {
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) out.append(',');
                out.append(escape(row.get(headers.get(i))));
            }
            out.append('\n');
        }
        return out.toString();
    }

    private static String escape(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return text.matches(".*[\",\\n\\r].*") ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }

    private static byte[] pdf(String title, List<Map<String, Object>> rows) {
        StringBuilder text = new StringBuilder("BFA Digital - ").append(title).append("\\n\\n");
        rows.forEach(row -> text.append(row).append("\\n"));
        String stream = "BT /F1 10 Tf 40 760 Td (" + pdfSafe(text.toString()) + ") Tj ET";
        String body = "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n"
                + "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "5 0 obj << /Length " + stream.length() + " >> stream\n" + stream + "\nendstream endobj\n";
        String pdf = "%PDF-1.4\n" + body + "trailer << /Root 1 0 R >>\n%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private static String pdfSafe(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\n", " ");
    }

    private static byte[] xlsx(List<Map<String, Object>> rows) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(baos)) {
                put(zip, "[Content_Types].xml", "<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
                put(zip, "_rels/.rels", "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
                put(zip, "xl/workbook.xml", "<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Reporte\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
                put(zip, "xl/_rels/workbook.xml.rels", "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
                put(zip, "xl/worksheets/sheet1.xml", sheetXml(rows));
            }
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String sheetXml(List<Map<String, Object>> rows) {
        List<String> headers = rows.isEmpty() ? List.of("sin_datos") : new ArrayList<>(rows.getFirst().keySet());
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        appendRow(xml, 1, headers.stream().map(h -> (Object) h).toList());
        int r = 2;
        for (Map<String, Object> row : rows) appendRow(xml, r++, headers.stream().map(row::get).toList());
        return xml.append("</sheetData></worksheet>").toString();
    }

    private static void appendRow(StringBuilder xml, int rowNum, List<Object> values) {
        xml.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < values.size(); i++) xml.append("<c t=\"inlineStr\"><is><t>")
                .append(xmlSafe(values.get(i))).append("</t></is></c>");
        xml.append("</row>");
    }

    private static String xmlSafe(Object value) {
        return (value == null ? "" : String.valueOf(value)).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void put(ZipOutputStream zip, String name, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    public record Document(String fileName, String mimeType, byte[] bytes) {
    }
}
