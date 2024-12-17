package io.metersphere.project.service;

import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileAssociationExample;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.filemanagement.FileAssociationDTO;
import io.metersphere.project.dto.filemanagement.FileAssociationSource;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.dto.filemanagement.response.FileAssociationResponse;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.invoker.FileAssociationUpdateServiceInvoker;
import io.metersphere.project.mapper.ExtFileAssociationMapper;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileAssociationService {

    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileAssociationLogService fileAssociationLogService;
    @Resource
    private FileAssociationUpdateServiceInvoker fileAssociationUpdateServiceInvoker;
    @Resource
    private ExtFileAssociationMapper extFileAssociationMapper;

    /**
     * 通过文件ID，检查该文件所有版本对应的资源关联关系
     * @param id
     * @return
     */
    public List<FileAssociationResponse> selectFileAllVersionAssociation(String id) {
        FileInformationResponse fileMetadata = fileMetadataService.getFileInformation(id);
        if (fileMetadata.getId() == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        //通过引用ID查找关联数据
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileRefIdEqualTo(fileMetadata.getRefId());
        List<FileAssociation> associationList = fileAssociationMapper.selectByExample(example);
        //查找文件信息
        Map<String,FileMetadata> fileIdMap = this.getFileIdMap( associationList.stream().map(FileAssociation::getFileId).collect(Collectors.toList()));
        //查找资源信息
        Map<String, FileAssociationSource> sourceIdNameMap = this.getAssociationSourceMap(
                associationList.stream().collect(
                        Collectors.groupingBy(FileAssociation::getSourceType,Collectors.mapping(FileAssociation::getSourceId,Collectors.toList()))
                ));

        //组装返回参数
        List<FileAssociationResponse> responseList = new ArrayList<>();
        associationList.forEach(item -> {
            if (fileIdMap.containsKey(item.getFileId()) && sourceIdNameMap.containsKey(item.getSourceId())) {
                FileAssociationResponse response = new FileAssociationResponse();
                response.setId(item.getId());
                response.setSourceId(item.getSourceId());
                response.setFileId(item.getFileId());
                response.setSourceName(sourceIdNameMap.get(item.getSourceId()).getSourceName());
                response.setSourceNum(sourceIdNameMap.get(item.getSourceId()).getSourceNum());
                response.setRedirectId(sourceIdNameMap.get(item.getSourceId()).getRedirectId());
                response.setSourceType(item.getSourceType());
                response.setFileVersion(fileIdMap.get(item.getFileId()).getFileVersion());
                responseList.add(response);
            }
        });
        return responseList;
    }

    private Map<String, FileMetadata> getFileIdMap(List<String> fileIdList) {
        if(CollectionUtils.isEmpty(fileIdList)){
            return new HashMap<>();
        }else {
            List<FileMetadata> fileMetadataList = fileMetadataService.selectByList(fileIdList);
            return fileMetadataList.stream().collect(Collectors.toMap(FileMetadata::getId, item -> item));
        }
    }

    //通过资源类型Map查找关联表
    private Map<String, FileAssociationSource> getAssociationSourceMap(Map<String, List<String>> sourceTypeToIdMap) {
        List<FileAssociationSource> sourceQueryList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : sourceTypeToIdMap.entrySet()) {
            String sourceType =entry.getKey();
            sourceQueryList.addAll(
                    extFileAssociationMapper.selectAssociationSourceBySourceTableAndIdList(FileAssociationSourceUtil.getQuerySql(sourceType),entry.getValue()));
        }
        return sourceQueryList.stream().collect(Collectors.toMap(FileAssociationSource::getSourceId, item -> item));
    }

    /**
     * 检查文件版本是否是最新的
     * @param fileIdList    不是最新的文件ID
     * @return
     */
    public List<String> checkFilesVersion(List<String> fileIdList){
        if (CollectionUtils.isEmpty(fileIdList)) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        List<String> updatedFileId = new ArrayList<>();
        List<FileMetadata> fileMetadataList = fileMetadataService.selectByList(fileIdList);
        fileMetadataList.forEach(item -> {
            if (!item.getLatest()) {
                updatedFileId.add(item.getId());
            }
        });
        return updatedFileId;
    }

    /**
     * 资源关联文件
     *
     * @param sourceId   资源id
     * @param sourceType 资源类型
     * @param fileIds    文件id集合
     * @param fileLogRecord   日志记录相关。包含操作人、日志所属模块、触发日志记录的请求路径和请求方法
     * @return  本次涉及到关联的关联表ID
     */
    public List<String> association(String sourceId, String sourceType, List<String> fileIds, @Validated FileLogRecord fileLogRecord) {
        if (CollectionUtils.isEmpty(fileIds)) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileAssociationSource source = extFileAssociationMapper.selectNameBySourceTableAndId(FileAssociationSourceUtil.getQuerySql(sourceType),sourceId);
        this.validateSourceName(source);

        List<FileMetadata> fileMetadataList = fileMetadataService.selectByList(fileIds);

        //校验文件是否已经关联
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId);
        List<FileAssociation> associationdList = fileAssociationMapper.selectByExample(example);
        Map<String,FileAssociation> refIdFileAssociationMap = associationdList.stream().collect(Collectors.toMap(FileAssociation::getFileRefId, item -> item));

        List<FileMetadata> addFileList = new ArrayList<>();
        for (FileMetadata fileMetadata : fileMetadataList) {
            FileAssociation fileAssociation = refIdFileAssociationMap.get(fileMetadata.getRefId());
            if(fileAssociation == null){
                addFileList.add(fileMetadata);
            }
        }
        return this.createFileAssociation(addFileList, sourceId, source.getSourceName(), sourceType, fileLogRecord);
    }

    private List<String> createFileAssociation(List<FileMetadata> addFileList, String sourceId, String sourceName, String sourceType, @Validated FileLogRecord logRecord) {
        FileAssociationSourceUtil.validate(sourceType);
        if(CollectionUtils.isNotEmpty(addFileList)){
            List<FileAssociation> createFile = new ArrayList<>();
            long operatorTime = System.currentTimeMillis();
            addFileList.forEach(item -> {
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setId(IDGenerator.nextStr());
                fileAssociation.setFileId(item.getId());
                fileAssociation.setFileRefId(item.getRefId());
                fileAssociation.setSourceId(sourceId);
                fileAssociation.setSourceType(sourceType);
                fileAssociation.setCreateTime(operatorTime);
                fileAssociation.setCreateUser(logRecord.getOperator());
                fileAssociation.setUpdateTime(operatorTime);
                fileAssociation.setUpdateUser(logRecord.getOperator());
                fileAssociation.setFileVersion(item.getFileVersion());
                fileAssociation.setDeleted(false);
                createFile.add(fileAssociation);
            });
            fileAssociationMapper.batchInsert(createFile);
            fileAssociationLogService.saveBatchInsertLog(sourceName,addFileList,logRecord);
            return createFile.stream().map(FileAssociation::getId).collect(Collectors.toList());
        }else {
            return new ArrayList<>();
        }
    }

    /**
     * 将资源关联的文件更新到最新版本
     * @param fileAssociationId  关联表ID
     * @param fileLogRecord 日志记录相关
     * @return  更新后的文件ID
     */
    public String upgrade(String fileAssociationId,@Validated FileLogRecord fileLogRecord){
        FileAssociation fileAssociation = fileAssociationMapper.selectByPrimaryKey(fileAssociationId);
        if(fileAssociation == null){
            throw new MSException(Translator.get("file.association.not.exist"));
        }

        FileMetadata newFileMetadata = this.getNewVersionFileMetadata(fileAssociation.getFileId());
        // 通知其他服务更新引用的文件
        fileAssociationUpdateServiceInvoker.handleUpgrade(fileAssociation, newFileMetadata);
        if(StringUtils.equals(newFileMetadata.getId(),fileAssociation.getFileId())){
            return fileAssociation.getFileId();
        } else {
            fileAssociation.setFileId(newFileMetadata.getId());
            fileAssociation.setFileVersion(newFileMetadata.getFileVersion());
            fileAssociationMapper.updateByPrimaryKeySelective(fileAssociation);
            FileAssociationSource source  = extFileAssociationMapper.selectNameBySourceTableAndId(FileAssociationSourceUtil.getQuerySql(fileAssociation.getSourceType()),fileAssociation.getSourceId());
            this.validateSourceName(source);
            fileAssociationLogService.saveUpdateLog(source.getSourceName(),newFileMetadata,fileLogRecord);
            return newFileMetadata.getId();
        }
    }

    private FileMetadata getNewVersionFileMetadata(String fileId){
        FileMetadata fileMetadata = fileMetadataService.selectById(fileId);
        if(fileMetadata == null){
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileMetadata newVersionFileMetadata = fileMetadataService.selectLatestFileByRefId(fileMetadata.getRefId());
        return newVersionFileMetadata;
    }

    /**
     * 取消关联
     * @param idList    取消关联的id
     * @param logRecord 日志记录相关
     * @return
     */
    public int deleteByIds(List<String> idList, @Validated FileLogRecord logRecord) {
        if(CollectionUtils.isEmpty(idList)){
            return 0;
        }
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andIdIn(idList);
        return this.deleteAndSaveLog(example, logRecord);
    }

    /**
     * 取消关联
     *
     * @param sourceIds 取消关联的资源id
     * @param logRecord 日志记录相关
     * @return
     */
    public int deleteBySourceIds(List<String> sourceIds, @Validated FileLogRecord logRecord) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return 0;
        }
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdIn(sourceIds);
        return this.deleteAndSaveLog(example, logRecord);
    }

    public List<FileAssociation> selectBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return List.of();
        }
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdIn(sourceIds);
        return fileAssociationMapper.selectByExample(example);
    }

    /**
     * 取消关联
     *
     * @param sourceId  资源ID
     * @param fileIds   文件ID
     * @param logRecord 日志记录相关
     * @return
     */
    public int deleteBySourceIdAndFileIds(String sourceId, List<String> fileIds, @Validated FileLogRecord logRecord) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return 0;
        }
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andFileIdIn(fileIds);
        return this.deleteAndSaveLog(example, logRecord);
    }

    private int deleteAndSaveLog(FileAssociationExample example, FileLogRecord logRecord) {
        List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(example);
        Map<String, List<String>> sourceToFileNameMap = this.genSourceNameFileNameMap(fileAssociationList);
        int deleteCount = fileAssociationMapper.deleteByExample(example);
        if (MapUtils.isNotEmpty(sourceToFileNameMap)) {
            fileAssociationLogService.saveDeleteLog(sourceToFileNameMap, logRecord);
        }
        return deleteCount;
    }

    private Map<String, List<String>> genSourceNameFileNameMap(List<FileAssociation> fileAssociationList) {
        Map<String,List<String>> sourceNameFileNameMap = new HashMap<>();
        Map<String,String> fileIdMap = new HashMap<>();
        for (FileAssociation fileAssociation:fileAssociationList){
            FileAssociationSource source  = extFileAssociationMapper.selectNameBySourceTableAndId(FileAssociationSourceUtil.getQuerySql(fileAssociation.getSourceType()),fileAssociation.getSourceId());
            if (source == null) {
                continue;
            }
            this.validateSourceName(source);
            String fileName = null;
            if(fileIdMap.containsKey(fileAssociation.getFileId())){
                fileName = fileIdMap.get(fileAssociation.getFileId());
            }else {
                FileMetadata fileMetadata = fileMetadataService.selectById(fileAssociation.getFileId());
                if(fileMetadata != null){
                    fileName = fileMetadata.getName();
                    fileIdMap.put(fileMetadata.getId(),fileName);
                }
            }
            if(StringUtils.isNotEmpty(fileName)){
                if(sourceNameFileNameMap.containsKey(source.getSourceName())){
                    sourceNameFileNameMap.get(source.getSourceName()).add(fileName);
                }else {
                    List<String> fileNameList = new ArrayList<>();
                    fileNameList.add(fileName);
                    sourceNameFileNameMap.put(source.getSourceName(), fileNameList);
                }
            }
        }
        return sourceNameFileNameMap;
    }

    /**
     * 转存并关联文件
     * @return
     * @throws Exception
     */
    public String transferAndAssociation(@Validated FileAssociationDTO fileAssociationDTO) {
        FileAssociationSource source = extFileAssociationMapper.selectNameBySourceTableAndId(FileAssociationSourceUtil.getQuerySql(fileAssociationDTO.getSourceType()), fileAssociationDTO.getSourceId());
        this.validateSourceName(source);
        String fileId = fileMetadataService.transferFile(
                fileAssociationDTO.getFileName(),
                fileAssociationDTO.getOriginalName(),
                fileAssociationDTO.getFileLogRecord().getProjectId(),
                fileAssociationDTO.getModuleId(),
                fileAssociationDTO.getFileLogRecord().getOperator(),
                fileAssociationDTO.getFileBytes());
        List<String> accociationList = new ArrayList<>();
        accociationList.add(fileId);
        this.association(fileAssociationDTO.getSourceId(), fileAssociationDTO.getSourceType(), accociationList, fileAssociationDTO.getFileLogRecord());
        fileAssociationLogService.saveTransferAssociationLog(fileAssociationDTO.getSourceId(), fileAssociationDTO.getFileName(), source.getSourceName(), fileAssociationDTO.getFileLogRecord());
        return fileId;
    }

    private void validateSourceName(FileAssociationSource source){
        if(source == null){
             throw new MSException(Translator.get("file.association.source.not.exist"));
        }
    }

    /**
     * 获取文件列表接口
     *
     * @param sourceId
     * @return
     */
    public List<FileInfo> getFiles(String sourceId) {
        return extFileAssociationMapper.selectAssociationFileInfo(sourceId);
    }

    /**
     * 获取文件列表接口
     *
     * @param sourceIds
     * @return
     */
    public List<FileInfo> getFiles(List<String> sourceIds) {
        return extFileAssociationMapper.selectFileInfoBySourceIds(sourceIds);
    }

    public List<FileAssociation> getFileAssociations(List<String> sourceIds, String sourceType) {
        return extFileAssociationMapper.selectFileIdsBySourceId(sourceIds, sourceType);
    }

    public List<FileAssociation> getByFileIdAndSourceId(String sourceId, String fileId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andFileIdEqualTo(fileId);
        return fileAssociationMapper.selectByExample(example);
    }
}
