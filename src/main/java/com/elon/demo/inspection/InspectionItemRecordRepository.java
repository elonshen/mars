package com.elon.demo.inspection;

import com.elon.demo.inspection.model.InspectionItemRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionItemRecordRepository extends JpaRepository<InspectionItemRecord, Long> {

    List<InspectionItemRecord> findByIsNormalFalse();

}