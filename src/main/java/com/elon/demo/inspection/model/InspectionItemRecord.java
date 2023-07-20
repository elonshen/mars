package com.elon.demo.inspection.model;

import com.elon.demo.user.model.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection_item_record")
@EntityListeners(AuditingEntityListener.class)
public class InspectionItemRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inspection_item_id", nullable = false)
    private InspectionItem inspectionItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inspection_point_record_id", nullable = false)
    private InspectionPointRecord inspectionPointRecord;

    @Column(name = "is_normal", nullable = false)
    private Boolean isNormal = true;

    @LastModifiedDate
    @Column(name = "last_processing_at")
    private LocalDateTime lastProcessingAt;

    @Column(name = "note")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @ManyToOne
    @JoinColumn(name = "handler_id")
    private User handler;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ElementCollection
    @Column(name = "photo_path")
    @CollectionTable(name = "inspection_item_record_photo_paths", joinColumns = @JoinColumn(name = "inspection_item_record_id"))
    private List<String> photoPaths = new ArrayList<>();

    @ElementCollection
    @Column(name = "handled_photo_path")
    @CollectionTable(name = "inspection_item_record_handled_photo_paths", joinColumns = @JoinColumn(name = "inspection_item_record_id"))
    private List<String> handledPhotoPaths = new ArrayList<>();

    public InspectionItemRecord() {
    }

    private InspectionItemRecord(Long id, InspectionItem inspectionItem, InspectionPointRecord inspectionPointRecord, Boolean isNormal, LocalDateTime lastProcessingAt, String note, ProcessingStatus processingStatus, User handler, User reporter, List<String> photoPaths) {
        this.id = id;
        this.inspectionItem = inspectionItem;
        this.inspectionPointRecord = inspectionPointRecord;
        this.isNormal = isNormal;
        this.lastProcessingAt = lastProcessingAt;
        this.note = note;
        this.processingStatus = processingStatus;
        this.handler = handler;
        this.reporter = reporter;
        this.photoPaths = photoPaths;
    }

    public static InspectionItemRecord ofNew(InspectionItem inspectionItem, InspectionPointRecord inspectionPointRecord, Boolean isNormal, String note, ProcessingStatus processingStatus, List<String> photoPaths) {
        return new InspectionItemRecord(null, inspectionItem, inspectionPointRecord, isNormal, null, note, processingStatus, null, null, photoPaths);
    }

    public List<String> getHandledPhotoPaths() {
        return handledPhotoPaths;
    }

    public void setHandledPhotoPaths(List<String> handledPhotoPaths) {
        this.handledPhotoPaths = handledPhotoPaths;
    }

    public List<String> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = photoPaths;
    }

    public InspectionItem getInspectionItem() {
        return inspectionItem;
    }

    public void setInspectionItem(InspectionItem inspectionItem) {
        this.inspectionItem = inspectionItem;
    }

    public Boolean getIsNormal() {
        return isNormal;
    }

    public void setIsNormal(Boolean isNormal) {
        this.isNormal = isNormal;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getHandler() {
        return handler;
    }

    public void setHandler(User handler) {
        this.handler = handler;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getLastProcessingAt() {
        return lastProcessingAt;
    }

    public void setLastProcessingAt(LocalDateTime lastProcessingAt) {
        this.lastProcessingAt = lastProcessingAt;
    }

    public InspectionPointRecord getInspectionPointRecord() {
        return inspectionPointRecord;
    }

    public void setInspectionPointRecord(InspectionPointRecord inspectionPointRecord) {
        this.inspectionPointRecord = inspectionPointRecord;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}