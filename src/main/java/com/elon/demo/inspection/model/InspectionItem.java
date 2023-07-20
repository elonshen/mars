package com.elon.demo.inspection.model;

import com.elon.demo.user.model.User;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection_item")
@EntityListeners(AuditingEntityListener.class)
public class InspectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inspection_point_id", nullable = false)
    private InspectionPoint inspectionPoint;

    @ManyToMany
    @JoinTable(name = "inspection_items_handlers",
            joinColumns = @JoinColumn(name = "inspection_item_id"),
            inverseJoinColumns = @JoinColumn(name = "handler_id"))
    private List<User> handlers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "inspection_items_reporters",
            joinColumns = @JoinColumn(name = "inspection_item_id"),
            inverseJoinColumns = @JoinColumn(name = "reporter_id"))
    private List<User> reporters = new ArrayList<>();

    private InspectionItem(Long id, String name, InspectionPoint inspectionPoint, List<User> handlers, List<User> reporters) {
        this.id = id;
        this.name = name;
        this.inspectionPoint = inspectionPoint;
        this.handlers = handlers;
        this.reporters = reporters;
    }

    public InspectionItem() {
    }

    public static InspectionItem ofNew(String name, InspectionPoint inspectionPoint, List<User> handlers, List<User> reporters) {
        return new InspectionItem(null, name, inspectionPoint, handlers, reporters);
    }

    public List<User> getReporters() {
        return reporters;
    }

    public void setReporters(List<User> reporters) {
        this.reporters = reporters;
    }

    public List<User> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<User> handlers) {
        this.handlers = handlers;
    }

    public InspectionPoint getInspectionPoint() {
        return inspectionPoint;
    }

    public void setInspectionPoint(InspectionPoint inspectionPoint) {
        this.inspectionPoint = inspectionPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}