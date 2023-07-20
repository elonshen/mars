package com.elon.demo.inspection;

import com.elon.demo.inspection.model.InspectionPointRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionPointRecordRepository extends JpaRepository<InspectionPointRecord, Long> {
}