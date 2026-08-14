package ro.leavehub.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ro.leavehub.api.ApiDtos.ReportSummaryDto;
import ro.leavehub.service.ReportService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/reports/summary")
    public ReportSummaryDto summary() {
        return service.summary();
    }

    @GetMapping(value = "/leave-requests/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> requestPdf(@PathVariable Long id) {
        return pdf(service.requestPdf(id), "cerere-concediu-" + id + ".pdf");
    }

    @GetMapping(value = "/reports/pending.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pendingPdf() {
        return pdf(service.pendingPdf(), "cereri-in-asteptare.pdf");
    }

    @GetMapping(value = "/reports/balances.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> balancesPdf() {
        return pdf(service.balancesPdf(), "situatia-soldurilor.pdf");
    }

    private ResponseEntity<byte[]> pdf(byte[] content, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .body(content);
    }
}
