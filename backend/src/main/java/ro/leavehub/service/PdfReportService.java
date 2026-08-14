package ro.leavehub.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ro.leavehub.model.Employee;
import ro.leavehub.model.LeaveRequest;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private static final Color NAVY = new Color(17, 39, 57);
    private static final Color CYAN = new Color(0, 151, 178);
    private static final Color LIGHT = new Color(239, 246, 248);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] leaveRequest(LeaveRequest request) {
        return render(document -> {
            addHeader(document, "Cerere de concediu", "Document generat automat");
            document.add(spacer(12));
            document.add(infoTable(List.of(
                    row("Numar cerere", "#" + request.getId()),
                    row("Angajat", request.getEmployee().getName()),
                    row("Email", request.getEmployee().getEmail()),
                    row("Departament", request.getEmployee().getDepartment().getName()),
                    row("Tip concediu", request.getLeaveType().getName() + " (" + request.getLeaveType().getCode() + ")"),
                    row("Perioada", DATE.format(request.getStartDate()) + " - " + DATE.format(request.getEndDate())),
                    row("Zile lucratoare", String.valueOf(request.getWorkingDays())),
                    row("Status", request.getStatus().name()),
                    row("Motiv", request.getReason() == null ? "-" : request.getReason())
            )));
            document.add(spacer(22));
            document.add(paragraph("Declar pe propria raspundere ca informatiile completate sunt corecte.", 10, Color.DARK_GRAY));
            document.add(spacer(36));
            var signatures = new PdfPTable(new float[]{1, 1});
            signatures.setWidthPercentage(100);
            signatures.addCell(signature("Semnatura angajat"));
            signatures.addCell(signature("Aprobare responsabil"));
            document.add(signatures);
            addFooter(document);
        });
    }

    public byte[] pendingRequests(List<LeaveRequest> requests) {
        return render(document -> {
            addHeader(document, "Raport cereri in asteptare", "Employee Leave Hub");
            document.add(spacer(12));
            var table = new PdfPTable(new float[]{0.7f, 1.5f, 1.2f, 1.1f, 1.2f, 0.7f});
            table.setWidthPercentage(100);
            List.of("ID", "Angajat", "Departament", "Tip", "Perioada", "Zile")
                    .forEach(value -> table.addCell(headerCell(value)));
            for (var request : requests) {
                table.addCell(bodyCell("#" + request.getId()));
                table.addCell(bodyCell(request.getEmployee().getName()));
                table.addCell(bodyCell(request.getEmployee().getDepartment().getName()));
                table.addCell(bodyCell(request.getLeaveType().getCode()));
                table.addCell(bodyCell(DATE.format(request.getStartDate()) + " - " + DATE.format(request.getEndDate())));
                table.addCell(bodyCell(String.valueOf(request.getWorkingDays())));
            }
            if (requests.isEmpty()) {
                var empty = bodyCell("Nu exista cereri in asteptare.");
                empty.setColspan(6);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
            }
            document.add(table);
            addFooter(document);
        });
    }

    public byte[] leaveBalances(List<Employee> employees) {
        return render(document -> {
            addHeader(document, "Situatia soldurilor de concediu", "Employee Leave Hub");
            document.add(spacer(12));
            var table = new PdfPTable(new float[]{1.7f, 1.3f, 0.8f, 0.8f, 0.8f});
            table.setWidthPercentage(100);
            List.of("Angajat", "Departament", "Anual", "Disponibil", "Consumate")
                    .forEach(value -> table.addCell(headerCell(value)));
            for (var employee : employees) {
                table.addCell(bodyCell(employee.getName()));
                table.addCell(bodyCell(employee.getDepartment().getName()));
                table.addCell(bodyCell(String.valueOf(employee.getAnnualLeaveDays())));
                table.addCell(bodyCell(String.valueOf(employee.getAvailableLeaveDays())));
                table.addCell(bodyCell(String.valueOf(employee.getAnnualLeaveDays() - employee.getAvailableLeaveDays())));
            }
            document.add(table);
            addFooter(document);
        });
    }

    private byte[] render(DocumentWriter writer) {
        try (var output = new ByteArrayOutputStream()) {
            var document = new Document(PageSize.A4, 42, 42, 44, 44);
            PdfWriter.getInstance(document, output);
            document.open();
            writer.write(document);
            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Documentul PDF nu a putut fi generat.");
        }
    }

    private void addHeader(Document document, String title, String subtitle) throws DocumentException, IOException {
        var table = new PdfPTable(new float[]{3.5f, 1f});
        table.setWidthPercentage(100);
        var left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setBackgroundColor(NAVY);
        left.setPadding(18);
        left.addElement(paragraph("EMPLOYEE LEAVE HUB", 10, Color.WHITE, true));
        left.addElement(paragraph(title, 22, Color.WHITE, true));
        left.addElement(paragraph(subtitle, 9, new Color(198, 230, 235)));
        table.addCell(left);
        var accent = new PdfPCell();
        accent.setBorder(Rectangle.NO_BORDER);
        accent.setBackgroundColor(CYAN);
        accent.setPadding(18);
        var mark = paragraph("ELH", 24, Color.WHITE, true);
        mark.setAlignment(Element.ALIGN_CENTER);
        accent.addElement(mark);
        table.addCell(accent);
        document.add(table);
    }

    private PdfPTable infoTable(List<String[]> rows) throws IOException, DocumentException {
        var table = new PdfPTable(new float[]{1.3f, 2.7f});
        table.setWidthPercentage(100);
        for (var row : rows) {
            var label = new PdfPCell(paragraph(row[0], 9, NAVY, true));
            label.setPadding(9);
            label.setBackgroundColor(LIGHT);
            label.setBorderColor(Color.WHITE);
            table.addCell(label);
            var value = new PdfPCell(paragraph(row[1], 9, Color.DARK_GRAY));
            value.setPadding(9);
            value.setBorderColor(new Color(224, 231, 234));
            table.addCell(value);
        }
        return table;
    }

    private PdfPCell headerCell(String value) {
        try {
            var cell = new PdfPCell(paragraph(value, 8, Color.WHITE, true));
            cell.setBackgroundColor(NAVY);
            cell.setPadding(7);
            cell.setBorderColor(Color.WHITE);
            return cell;
        } catch (IOException | DocumentException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private PdfPCell bodyCell(String value) {
        try {
            var cell = new PdfPCell(paragraph(value, 8, Color.DARK_GRAY));
            cell.setPadding(7);
            cell.setBorderColor(new Color(224, 231, 234));
            return cell;
        } catch (IOException | DocumentException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private PdfPCell signature(String label) throws IOException, DocumentException {
        var cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingRight(30);
        cell.addElement(paragraph("____________________________", 10, Color.GRAY));
        cell.addElement(paragraph(label, 9, Color.DARK_GRAY));
        return cell;
    }

    private void addFooter(Document document) throws DocumentException, IOException {
        document.add(spacer(28));
        var line = new PdfPTable(1);
        line.setWidthPercentage(100);
        var cell = new PdfPCell(paragraph(
                "Generat de Employee Leave Hub | Document pentru uz intern", 8, Color.GRAY));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(new Color(210, 220, 224));
        cell.setPaddingTop(8);
        line.addCell(cell);
        document.add(line);
    }

    private Paragraph paragraph(String text, float size, Color color) throws IOException, DocumentException {
        return paragraph(text, size, color, false);
    }

    private Paragraph paragraph(String text, float size, Color color, boolean bold) throws IOException, DocumentException {
        return new Paragraph(text, font(size, color, bold));
    }

    private Font font(float size, Color color, boolean bold) throws IOException, DocumentException {
        var resource = new ClassPathResource(bold ? "fonts/NotoSans-Bold.ttf" : "fonts/NotoSans-Regular.ttf");
        byte[] bytes;
        try (var stream = resource.getInputStream()) {
            bytes = stream.readAllBytes();
        }
        var baseFont = BaseFont.createFont(
                bold ? "NotoSans-Bold.ttf" : "NotoSans-Regular.ttf",
                BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        return new Font(baseFont, size, Font.NORMAL, color);
    }

    private Paragraph spacer(float size) {
        return new Paragraph(" ", new Font(Font.HELVETICA, size));
    }

    private String[] row(String label, String value) {
        return new String[]{label, value};
    }

    @FunctionalInterface
    private interface DocumentWriter {
        void write(Document document) throws DocumentException, IOException;
    }
}
