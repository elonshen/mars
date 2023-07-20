package com.elon.demo.inspection;

import com.elon.demo.inspection.model.InspectionPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionPointRepository extends JpaRepository<InspectionPoint, Long> {

    List<InspectionPoint> findByNameLikeOrderByCreatedAtDesc(String name);

    List<InspectionPoint> findByOrderByCreatedAtDesc();

}