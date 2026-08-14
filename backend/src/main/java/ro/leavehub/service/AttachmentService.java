package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ro.leavehub.api.ApiDtos.AttachmentDto;
import ro.leavehub.model.*;
import ro.leavehub.repository.AttachmentRepository;
import ro.leavehub.repository.LeaveRequestRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final AttachmentRepository attachmentRepository;
    private final LeaveRequestRepository requestRepository;
    private final CurrentUserService currentUserService;
    private final ApiMapper mapper;

    @Value("${app.storage.location}")
    private String storageLocation;

    @Transactional
    public AttachmentDto upload(Long requestId, MultipartFile file) {
        var request = findRequest(requestId);
        var current = currentUserService.get();
        if (!Objects.equals(current.getId(), request.getEmployee().getId())) {
            throw ApiException.forbidden("Documentele pot fi incarcate doar de autorul cererii.");
        }
        if (request.getStatus() != LeaveStatus.DRAFT) {
            throw ApiException.badRequest("Documentele pot fi modificate doar cat timp cererea este DRAFT.");
        }
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Selectati un fisier valid.");
        }
        var contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw ApiException.badRequest("Sunt acceptate doar fisiere PDF, JPG si PNG.");
        }
        var originalName = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "document"));
        if (originalName.contains("..")) {
            throw ApiException.badRequest("Numele fisierului nu este valid.");
        }
        var extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase() : "";
        var storedName = UUID.randomUUID() + extension;
        var root = Path.of(storageLocation).toAbsolutePath().normalize();
        var target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw ApiException.badRequest("Calea fisierului nu este valida.");
        }
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Fisierul nu a putut fi salvat.");
        }
        var attachment = attachmentRepository.save(Attachment.builder()
                .leaveRequest(request)
                .fileName(originalName)
                .storedName(storedName)
                .contentType(contentType)
                .fileSize(file.getSize())
                .filePath(target.toString())
                .uploadedAt(LocalDateTime.now())
                .build());
        return mapper.attachment(attachment);
    }

    @Transactional(readOnly = true)
    public DownloadedAttachment download(Long id) {
        var attachment = find(id);
        assertCanView(attachment.getLeaveRequest(), currentUserService.get());
        var resource = new FileSystemResource(attachment.getFilePath());
        if (!resource.exists() || !resource.isReadable()) {
            throw ApiException.notFound("Fisierul nu mai exista pe disc.");
        }
        return new DownloadedAttachment(resource, attachment.getFileName(), attachment.getContentType());
    }

    @Transactional
    public void delete(Long id) {
        var attachment = find(id);
        var request = attachment.getLeaveRequest();
        var current = currentUserService.get();
        if (current.getRole() != Role.ADMIN && !Objects.equals(current.getId(), request.getEmployee().getId())) {
            throw ApiException.forbidden("Nu aveti dreptul sa stergeti acest document.");
        }
        if (request.getStatus() != LeaveStatus.DRAFT && current.getRole() != Role.ADMIN) {
            throw ApiException.badRequest("Documentele pot fi sterse doar dintr-o cerere DRAFT.");
        }
        deleteFile(attachment);
        attachmentRepository.delete(attachment);
    }

    @Transactional
    public void deleteFilesForRequest(Long requestId) {
        attachmentRepository.findAllByLeaveRequestIdOrderByUploadedAtAsc(requestId).forEach(this::deleteFile);
    }

    private void deleteFile(Attachment attachment) {
        try {
            Files.deleteIfExists(Path.of(attachment.getFilePath()));
        } catch (IOException ignored) {
            // The database record remains the source of truth; an orphan file can be cleaned administratively.
        }
    }

    private Attachment find(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Documentul nu exista."));
    }

    private LeaveRequest findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cererea nu exista."));
    }

    private void assertCanView(LeaveRequest request, Employee current) {
        if (current.getRole() == Role.ADMIN) {
            return;
        }
        if (current.getRole() == Role.MANAGER
                && Objects.equals(current.getDepartment().getId(), request.getEmployee().getDepartment().getId())) {
            return;
        }
        if (!Objects.equals(current.getId(), request.getEmployee().getId())) {
            throw ApiException.forbidden("Nu aveti acces la acest document.");
        }
    }

    public record DownloadedAttachment(FileSystemResource resource, String fileName, String contentType) {
    }
}
