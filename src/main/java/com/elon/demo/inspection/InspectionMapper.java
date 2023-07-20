package com.elon.demo.inspection;

import com.elon.demo.inspection.model.InspectionItem;
import com.elon.demo.inspection.model.InspectionItemRecord;
import com.elon.demo.inspection.model.InspectionPoint;
import com.elon.demo.inspection.model.InspectionPointRecord;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InspectionMapper {
    InspectionPointVO toInspectionPointVO(InspectionPoint inspectionPoint);

    List<InspectionPointVO> toInspectionPointVOs(List<InspectionPoint> inspectionPoints);

    List<InspectionItemVO> toInspectionItemVOs(List<InspectionItem> inspectionItems);

    List<InspectionPointRecordVO> toInspectionPointRecordVOs(List<InspectionPointRecord> inspectionPointRecords);

    default PageImpl<InspectionPointRecordVO> toInspectionPointRecordVOPage(List<InspectionPointRecord> content, Pageable pageable, long total) {
        return new PageImpl<>(toInspectionPointRecordVOs(content), pageable, total);
    }

    List<InspectionItemRecordVO> toInspectionItemRecordVOs(List<InspectionItemRecord> inspectionItemRecords);
}
