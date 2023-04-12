package io.metersphere.metadata.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.FileMetadataDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.vo.DownloadRequest;
import io.metersphere.metadata.vo.DumpFileRequest;
import io.metersphere.metadata.vo.FileMetadataCreateRequest;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.request.QueryProjectFileRequest;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/file/metadata")
@RestController
public class FileMetadataController {
    @Resource
    private FileMetadataService fileMetadataService;

    @GetMapping(value = "/info/{fileId}")
    public ResponseEntity<byte[]> image(@PathVariable("fileId") String fileId) {
        return fileMetadataService.getFile(fileId);
    }

    @PostMapping("/project/{projectId}/{goPage}/{pageSize}")
    public Pager<List<FileMetadataDTO>> getProjectFiles(@PathVariable String projectId,
                                                        @PathVariable int goPage, @PathVariable int pageSize,
                                                        @RequestBody QueryProjectFileRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, fileMetadataService.getFileMetadataByProject(projectId, request));
    }

    @PostMapping(value = "/create")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = FileMetadataService.class)
    public List<FileMetadata> create(@RequestPart("request") FileMetadataCreateRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return fileMetadataService.create(request, files);
    }

    @PostMapping(value = "/upload")
    public FileMetadata upload(@RequestPart("request") FileMetadataWithBLOBs request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return fileMetadataService.reLoad(request, files);
    }

    @GetMapping(value = "/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        return fileMetadataService.getFile(id);
    }

    @PostMapping(value = "/download/zip")
    public ResponseEntity<byte[]> downloadBodyFiles(@RequestBody DownloadRequest request) {
        try {
            byte[] bytes = fileMetadataService.exportZip(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "fileList.zip")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    @GetMapping(value = "/delete/{fileId}")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#fileId)", msClass = FileMetadataService.class)
    public void deleteFile(@PathVariable String fileId) {
        fileMetadataService.deleteFile(fileId);
    }

    @PostMapping(value = "/delete/batch")
    public void deleteBatch(@RequestBody List<String> ids) {
        fileMetadataService.deleteBatch(ids);
    }

    @GetMapping(value = "/get/type/all")
    public List<String> getTypes() {
        return fileMetadataService.getTypes();
    }

    @PostMapping(value = "/move")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = FileMetadataService.class)
    public void move(@RequestBody MoveFIleMetadataRequest request) {
        fileMetadataService.move(request);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = FileMetadataService.class)
    public void update(@RequestBody FileMetadataWithBLOBs request) {
        fileMetadataService.update(request);
    }

    @PostMapping(value = "/dump/file", consumes = {"multipart/form-data"})
    public void dumpFile(@RequestPart("request") DumpFileRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        fileMetadataService.dumpFile(request, files);
    }

    @GetMapping(value = "/count/{projectId}/{createUser}")
    public long myFiles(@PathVariable String projectId, @PathVariable String createUser) {
        return fileMetadataService.myFiles(createUser, projectId);
    }

    @GetMapping(value = "/exist/{fileId}")
    public boolean exist(@PathVariable("fileId") String fileId) {
        return fileMetadataService.exist(fileId);
    }

    @PostMapping(value = "/exists")
    public List<String> exist(@RequestBody List<String> fileIds) {
        return fileMetadataService.exists(fileIds);
    }

}
