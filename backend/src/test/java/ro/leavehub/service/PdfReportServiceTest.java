package ro.leavehub.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.repository.LeaveRequestRepository;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PdfReportServiceTest {

    @Autowired
    PdfReportService pdfReportService;

    @Autowired
    LeaveRequestRepository requestRepository;

    @Test
    @Transactional
    void generatesAValidPdfHeader() {
        var request = requestRepository.findAll().getFirst();
        var content = pdfReportService.leaveRequest(request);
        assertThat(content).hasSizeGreaterThan(10_000);
        assertThat(new String(content, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
