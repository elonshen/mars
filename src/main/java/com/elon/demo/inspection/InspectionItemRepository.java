package com.elon.demo.inspection;

import com.elon.demo.inspection.model.InspectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, Long> {
    List<InspectionItem> findByInspectionPoint_Id(Long id);

}