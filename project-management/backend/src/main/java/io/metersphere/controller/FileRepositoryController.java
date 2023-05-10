package io.metersphere.controller;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileModule;
import io.metersphere.commons.constants.FileModuleTypeConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.FileRelevanceCaseDTO;
import io.metersphere.dto.FileVersionDTO;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.utils.GitRepositoryUtil;
import io.metersphere.request.QueryProjectFileRequest;
import io.metersphere.service.FileRepositoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/file/repository")
@RestController
public class FileRepositoryController {
    @Resource
    private FileRepositoryService fileRepositoryService;
    @Resource
    private FileMetadataService fileMetadataService;

    @PostMapping(value = "/git/pull")
    public FileMetadata pullFromRepository(@RequestPart("request") FileMetadata request) {
        return fileMetadataService.pullFromRepository(request);
    }

    @PostMapping("/connect")
    public String connect(@RequestBody FileModule node) {
        if (StringUtils.equalsIgnoreCase(node.getModuleType(), FileModuleTypeConstants.REPOSITORY.getValue())) {
            GitRepositoryUtil utils = new GitRepositoryUtil(node.getRepositoryPath(), node.getRepositoryUserName(), node.getRepositoryToken());
            utils.getBranches();
        }
        return "success";
    }

    @GetMapping(value = "/fileVersion/{refId}")
    public List<FileVersionDTO> selectFileVersion(@PathVariable String refId) {
        return fileRepositoryService.selectFileVersion(refId);
    }

    @PostMapping("/relevance/case/{refId}/{goPage}/{pageSize}")
    public Pager<List<FileRelevanceCaseDTO>> getFileRelevanceCase(@PathVariable String refId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryProjectFileRequest request) {
        return fileRepositoryService.getFileRelevanceCase(refId, goPage, pageSize);
    }

    @PostMapping("/case/version/update/{refId}")
    @MsRequestLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT)
    public String updateCaseVersion(@PathVariable String refId, @RequestBody QueryProjectFileRequest request) {
        return fileRepositoryService.updateCaseVersion(refId, request);
    }
}
