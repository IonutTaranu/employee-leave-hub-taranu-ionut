package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.leavehub.model.Attachment;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByLeaveRequestIdOrderByUploadedAtAsc(Long leaveRequestId);
    long countByLeaveRequestId(Long leaveRequestId);
}
