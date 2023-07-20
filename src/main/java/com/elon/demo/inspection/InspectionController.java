package com.elon.demo.inspection;

import com.elon.demo.inspection.model.*;
import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.imgscalr.Scalr;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "巡检相关接口")
public class InspectionController {

    private final InspectionPointRepository inspectionPointRepository;
    private final InspectionMapper inspectionMapper;
    private final UserRepository userRepository;
    private final InspectionItemRepository inspectionItemRepository;
    private final InspectionPointRecordRepository inspectionPointRecordRepository;
    private final InspectionItemRecordRepository inspectionItemRecordRepository;
    private final String inspectionPhotoPath;

    public InspectionController(InspectionPointRepository inspectionPointRepository, InspectionMapper inspectionMapper, UserRepository userRepository, InspectionItemRepository inspectionItemRepository, InspectionPointRecordRepository inspectionPointRecordRepository, InspectionItemRecordRepository inspectionItemRecordRepository, @Value("${mars.inspection.photo-path}") String uploadPath) {
        this.inspectionPointRepository = inspectionPointRepository;
        this.inspectionMapper = inspectionMapper;
        this.userRepository = userRepository;
        this.inspectionItemRepository = inspectionItemRepository;
        this.inspectionPointRecordRepository = inspectionPointRecordRepository;
        this.inspectionItemRecordRepository = inspectionItemRecordRepository;
        this.inspectionPhotoPath = uploadPath;
    }

    /**
     * 新增巡检点
     */
    @PostMapping("/inspection-points")
    public void createInspectionPoint(InspectionPointCreateRequest inspectionPointCreateRequest) {
        inspectionPointRepository.save(new InspectionPoint(inspectionPointCreateRequest.getName()));
    }

    /**
     * 查询巡检点
     *
     * @param name 查询名称
     */
    @GetMapping("/inspection-points")
    public List<InspectionPointVO> getInspectionPoints(@RequestParam(required = false) String name) {
        List<InspectionPoint> inspectionPoints;
        if (name != null) {
            inspectionPoints = inspectionPointRepository.findByNameLikeOrderByCreatedAtDesc(name);
        } else {
            inspectionPoints = inspectionPointRepository.findByOrderByCreatedAtDesc();
        }
        return inspectionMapper.toInspectionPointVOs(inspectionPoints);
    }

    /**
     * 修改巡检点
     *
     * @param id 巡检点id
     */
    @PutMapping("/inspection-points/{id}")
    public void updateInspectionPoint(@PathVariable Long id, @RequestBody InspectionPointCreateRequest inspectionPointCreateRequest) {
        InspectionPoint inspectionPoint = inspectionPointRepository.findById(id).orElseThrow(() -> new RuntimeException("巡检点不存在"));
        inspectionPoint.setName(inspectionPointCreateRequest.getName());
        inspectionPointRepository.save(inspectionPoint);
    }

    /**
     * 删除巡检点
     *
     * @param id 巡检点id
     */
    @DeleteMapping("/inspection-points/{id}")
    public void deleteInspectionPoint(@PathVariable Long id) {
        inspectionPointRepository.deleteById(id);
    }

    /**
     * 新增巡检项
     */
    @PostMapping("/inspection-items")
    public void createInspectionItem(@RequestBody InspectionItemCreateRequest inspectionItemCreateRequest) {
        InspectionPoint inspectionPoint = inspectionPointRepository.findById(inspectionItemCreateRequest.getInspectionPointId()).orElseThrow(() -> new RuntimeException("巡检点不存在"));
        List<User> handlers = userRepository.findAllById(inspectionItemCreateRequest.getHandlerIds());
        List<User> reporters = userRepository.findAllById(inspectionItemCreateRequest.getReporterIds());

        InspectionItem inspectionItem = InspectionItem.ofNew(inspectionItemCreateRequest.getName(), inspectionPoint, handlers, reporters);

        inspectionItemRepository.save(inspectionItem);
    }

    /**
     * 查询巡检项
     *
     * @param inspectionPointId 巡检点id（查询参数）
     */
    @GetMapping("/inspection-items")
    public List<InspectionItemVO> getInspectionItems(@RequestParam Long inspectionPointId) {
        List<InspectionItem> inspectionItems = inspectionItemRepository.findByInspectionPoint_Id(inspectionPointId);
        return inspectionMapper.toInspectionItemVOs(inspectionItems);
    }

    /**
     * 修改巡检项
     *
     * @param id 巡检项id
     */
    @PutMapping("/inspection-items/{id}")
    public void updateInspectionItem(@PathVariable Long id, @RequestBody InspectionItemUpdateRequest inspectionItemUpdateRequest) {
        InspectionItem inspectionItem = inspectionItemRepository.findById(id).orElseThrow(() -> new RuntimeException("巡检项不存在"));
        List<User> handlers = userRepository.findAllById(inspectionItemUpdateRequest.getHandlerIds());
        List<User> reporters = userRepository.findAllById(inspectionItemUpdateRequest.getReporterIds());
        inspectionItem.setName(inspectionItemUpdateRequest.getName());
        inspectionItem.setHandlers(handlers);
        inspectionItem.setReporters(reporters);
        inspectionItemRepository.save(inspectionItem);
    }

    /**
     * 删除巡检项
     */
    @DeleteMapping("/inspection-items/{id}")
    public void deleteInspectionItem(@PathVariable Long id) {
        inspectionItemRepository.deleteById(id);
    }

    /**
     * 提交巡检点记录
     */
    @PostMapping("/inspection-point-records")
    @Transactional
    public void createInspectionRecord(@RequestBody InspectionPointRecordCreateRequest inspectionPointRecordCreateRequest) {
        InspectionPointRecord inspectionPointRecord = new InspectionPointRecord();
        inspectionPointRecord = inspectionPointRecordRepository.save(inspectionPointRecord);

        List<InspectionItemRecord> inspectionItemRecords = new ArrayList<>();
        for (InspectionItemRecordCreateRequest inspectionItemRecordCreateRequest : inspectionPointRecordCreateRequest.getInspectionItemRecords()) {
            InspectionItem inspectionItem = inspectionItemRepository.findById(inspectionItemRecordCreateRequest.getInspectionItemId()).orElseThrow(() -> new RuntimeException("巡检项不存在"));
            InspectionItemRecord inspectionItemRecord = InspectionItemRecord.ofNew(
                    inspectionItem,
                    inspectionPointRecord,
                    inspectionItemRecordCreateRequest.getNormal(),
                    inspectionItemRecordCreateRequest.getNote(),
                    inspectionItemRecordCreateRequest.getNormal() ? ProcessingStatus.NO_PROCESSING : ProcessingStatus.PENDING,
                    inspectionItemRecordCreateRequest.getPhotoPaths());
            inspectionItemRecords.add(inspectionItemRecord);
        }
        inspectionItemRecordRepository.saveAll(inspectionItemRecords);
    }

    /**
     * 上传巡检项记录的图片
     * <p>
     * 图片访问方式：baseURL+ /inspection-item-records/photo-path + 图片路径, 例如："http://localhost:8080/inspection-item-records/photo-path/xxx.jpg"
     * </p>
     *
     * @return 图片路径，
     */
    @PostMapping(value = "/inspection-item-records/upload", consumes = {"multipart/form-data"})
    public String uploadPhoto(@RequestPart("file") MultipartFile source) throws IOException {
        String filePath = "/" + UUID.randomUUID() + ".jpg";
        Path imgPath = Paths.get(inspectionPhotoPath + filePath);
        try (
                InputStream inputStream = source.getInputStream();
                OutputStream outputStream = Files.newOutputStream(imgPath)
        ) {
            BufferedImage sourceImage = ImageIO.read(inputStream);
            BufferedImage destImage = Scalr.resize(sourceImage, Scalr.Method.AUTOMATIC, 1920);
            ImageIO.write(destImage, "jpg", outputStream);
        }
        return filePath;
    }

    /**
     * 查询巡检点记录
     * <p>
     * 分页（巡检记录多，需要分页，前端做下拉懒加载，默认按创建时间倒序）
     */
    @GetMapping("/inspection-point-records")
    public Page<InspectionPointRecordVO> getInspectionPointRecords(@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InspectionPointRecord> inspectionPointRecordPage = inspectionPointRecordRepository.findAll(pageable);
        return inspectionMapper.toInspectionPointRecordVOPage(inspectionPointRecordPage.getContent(), pageable, inspectionPointRecordPage.getTotalElements());
    }

    /**
     * 查询异常巡检项记录
     * <p>
     * 默认排序：处理状态为‘处理中’和‘确认中’的记录排前面，如果如果处理状态相同，则按创建时间倒叙排序
     */
    @GetMapping("/inspection-item-records/abnormal")
    public List<InspectionItemRecordVO> getAbnormalInspectionItemRecords() {
        List<InspectionItemRecord> inspectionItemRecords = inspectionItemRecordRepository.findByIsNormalFalse();
        // 排序：处理状态为‘处理中’和‘确认中’的记录拍前面，如果如果处理状态相同，则按创建时间倒叙排序
        inspectionItemRecords.sort((o1, o2) -> {
            if (o1.getProcessingStatus().equals(o2.getProcessingStatus())) {
                return o2.getInspectionPointRecord().getCreatedAt().compareTo(o1.getInspectionPointRecord().getCreatedAt());
            } else {
                if (o1.getProcessingStatus().equals(ProcessingStatus.PENDING) || o1.getProcessingStatus().equals(ProcessingStatus.CONFIRMING)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return inspectionMapper.toInspectionItemRecordVOs(inspectionItemRecords);
    }

    /**
     * 经办人处理/修改
     *
     * @param id 巡检项记录id
     */
    @PutMapping("/inspection-item-records/{id}/handle")
    public void handleInspectionItemRecord(@PathVariable Long id, @RequestBody InspectionItemRecordHandleRequest inspectionItemRecordHandleRequest, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("用户不存在"));
        InspectionItemRecord inspectionItemRecord = inspectionItemRecordRepository.findById(id).orElseThrow(() -> new RuntimeException("巡检项记录不存在"));
        inspectionItemRecord.setHandledPhotoPaths(inspectionItemRecordHandleRequest.getHandledPhotoPaths());
        inspectionItemRecord.setNote(inspectionItemRecordHandleRequest.getNote());
        inspectionItemRecord.setHandler(user);
        inspectionItemRecord.setProcessingStatus(ProcessingStatus.CONFIRMING);
        inspectionItemRecordRepository.save(inspectionItemRecord);
    }


    /**
     * 报告人确认
     *
     * @param id 巡检项记录id
     */
    @PutMapping("/inspection-item-records/{id}/confirm")
    public void confirmInspectionItemRecord(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("用户不存在"));
        InspectionItemRecord inspectionItemRecord = inspectionItemRecordRepository.findById(id).orElseThrow(() -> new RuntimeException("巡检项记录不存在"));
        inspectionItemRecord.setReporter(user);
        inspectionItemRecord.setProcessingStatus(ProcessingStatus.COMPLETED);
        inspectionItemRecordRepository.save(inspectionItemRecord);
    }
}

class InspectionPointCreateRequest {
    /**
     * 巡检点名称
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class InspectionPointVO {
    private Long id;
    /**
     * 巡检点名称
     */
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class InspectionItemCreateRequest {
    /**
     * 巡检项名称
     */
    private String name;
    /**
     * 巡检点ID
     */
    private Long InspectionPointId;

    /**
     * 经办人ID列表
     */
    private List<Long> handlerIds;
    /**
     * 报告人ID列表
     */
    private List<Long> reporterIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getInspectionPointId() {
        return InspectionPointId;
    }

    public void setInspectionPointId(Long inspectionPointId) {
        InspectionPointId = inspectionPointId;
    }

    public List<Long> getHandlerIds() {
        return handlerIds;
    }

    public void setHandlerIds(List<Long> handlerIds) {
        this.handlerIds = handlerIds;
    }

    public List<Long> getReporterIds() {
        return reporterIds;
    }

    public void setReporterIds(List<Long> reporterIds) {
        this.reporterIds = reporterIds;
    }
}

class InspectionItemVO {

    private Long id;
    /**
     * 巡检项名称
     */
    private String name;
    /**
     * 处理人列表
     */
    private List<UserVO> handlers;
    /**
     * 报告人列表
     */
    private List<UserVO> reporters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserVO> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<UserVO> handlers) {
        this.handlers = handlers;
    }

    public List<UserVO> getReporters() {
        return reporters;
    }

    public void setReporters(List<UserVO> reporters) {
        this.reporters = reporters;
    }
}

class InspectionItemUpdateRequest {
    /**
     * 巡检项名称
     */
    private String name;
    /**
     * 经办人ID列表
     */
    private List<Long> handlerIds;
    /**
     * 报告人ID列表
     */
    private List<Long> reporterIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getHandlerIds() {
        return handlerIds;
    }

    public void setHandlerIds(List<Long> handlerIds) {
        this.handlerIds = handlerIds;
    }

    public List<Long> getReporterIds() {
        return reporterIds;
    }

    public void setReporterIds(List<Long> reporterIds) {
        this.reporterIds = reporterIds;
    }
}

class InspectionPointRecordCreateRequest {
    /**
     * 巡检项纪录列表
     */
    private List<InspectionItemRecordCreateRequest> inspectionItemRecords;

    public List<InspectionItemRecordCreateRequest> getInspectionItemRecords() {
        return inspectionItemRecords;
    }

    public void setInspectionItemRecords(List<InspectionItemRecordCreateRequest> inspectionItemRecords) {
        this.inspectionItemRecords = inspectionItemRecords;
    }
}

class InspectionItemRecordCreateRequest {
    /**
     * 巡检项ID
     */
    private long inspectionItemId;
    /**
     * 是否正常，true：正常，false：异常
     */
    private Boolean isNormal;
    /**
     * 备注
     */
    private String note;
    /**
     * 异常照片路径列表
     */

    private List<String> photoPaths;

    public long getInspectionItemId() {
        return inspectionItemId;
    }

    public void setInspectionItemId(long inspectionItemId) {
        this.inspectionItemId = inspectionItemId;
    }

    public Boolean getNormal() {
        return isNormal;
    }

    public void setNormal(Boolean normal) {
        isNormal = normal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = photoPaths;
    }
}

class InspectionPointRecordVO {
    private Long id;
    /**
     * 创建人
     */
    private UserVO createdBy;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 巡检项纪录列表
     */
    private List<InspectionItemRecordVO> inspectionItemRecords;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserVO createdBy) {
        this.createdBy = createdBy;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<InspectionItemRecordVO> getInspectionItemRecords() {
        return inspectionItemRecords;
    }

    public void setInspectionItemRecords(List<InspectionItemRecordVO> inspectionItemRecords) {
        this.inspectionItemRecords = inspectionItemRecords;
    }
}

class InspectionItemRecordVO {
    private Long id;
    /**
     * 巡检项
     */
    private InspectionItemVO inspectionItem;
    /**
     * 是否正常，true：正常，false：异常
     */
    private Boolean isNormal;
    /**
     * 上一次处理时间
     */
    private LocalDateTime lastProcessingAt;
    /**
     * 备注
     */
    private String note;
    /**
     * 处理状态
     */
    private ProcessingStatus processingStatus;
    /**
     * 实际经办人
     */
    private UserVO handler;
    /**
     * 实际报告人
     */
    private UserVO reporter;
    /**
     * 异常照片路径列表
     */
    private List<String> photoPaths;
    /**
     * 处理完成后的照片路径列表
     */
    private List<String> handledPhotoPaths;

    public Boolean getNormal() {
        return isNormal;
    }

    public void setNormal(Boolean normal) {
        isNormal = normal;
    }

    public List<String> getHandledPhotoPaths() {
        return handledPhotoPaths;
    }

    public void setHandledPhotoPaths(List<String> handledPhotoPaths) {
        this.handledPhotoPaths = handledPhotoPaths;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InspectionItemVO getInspectionItem() {
        return inspectionItem;
    }

    public void setInspectionItem(InspectionItemVO inspectionItem) {
        this.inspectionItem = inspectionItem;
    }

    public Boolean getIsNormal() {
        return isNormal;
    }

    public void setIsNormal(Boolean normal) {
        isNormal = normal;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getLastProcessingAt() {
        return lastProcessingAt;
    }

    public void setLastProcessingAt(LocalDateTime lastProcessingAt) {
        this.lastProcessingAt = lastProcessingAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public UserVO getHandler() {
        return handler;
    }

    public void setHandler(UserVO handler) {
        this.handler = handler;
    }

    public UserVO getReporter() {
        return reporter;
    }

    public void setReporter(UserVO reporter) {
        this.reporter = reporter;
    }

    public List<String> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = photoPaths;
    }
}

class InspectionItemRecordHandleRequest {
    /**
     * 备注
     */
    private String note;
    /**
     * 处理完成后的照片路径列表
     */
    private List<String> handledPhotoPaths;

    public List<String> getHandledPhotoPaths() {
        return handledPhotoPaths;
    }

    public void setHandledPhotoPaths(List<String> handledPhotoPaths) {
        this.handledPhotoPaths = handledPhotoPaths;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}