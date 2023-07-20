package com.elon.demo.inspection.model;

import com.elon.demo.user.model.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection_point_record")
@EntityListeners(AuditingEntityListener.class)
public class InspectionPointRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @CreatedBy
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "inspectionPointRecord", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<InspectionItemRecord> inspectionItemRecords = new ArrayList<>();

    public InspectionPointRecord() {
    }

    public List<InspectionItemRecord> getInspectionItemRecords() {
        return inspectionItemRecords;
    }

    public void setInspectionItemRecords(List<InspectionItemRecord> inspectionItemRecords) {
        this.inspectionItemRecords = inspectionItemRecords;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}