package io.metersphere.bug.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.domain.BugContentExample;
import io.metersphere.bug.dto.BugCommentDTO;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.BugExportExcelModel;
import io.metersphere.bug.dto.request.BugExportColumn;
import io.metersphere.bug.mapper.BugContentMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugExportService {

    private static final int BATCH_PROCESS_QUANTITY = 2000;
    private static final String EXPORT_TEMP_BASE_FOLDER = "/tmp/metersphere/export/bug/";

    @Resource
    private BugContentMapper bugContentMapper;
    @Resource
    private BugCommentService bugCommentService;

    /**
     * @param list          缺陷数据
     * @param exportColumns excel导出的列
     * @return excel所在的文件夹
     * @throws Exception
     */
    public String generateExcelFiles(List<BugDTO> list, List<BugExportColumn> exportColumns) {
        String filesFolder = EXPORT_TEMP_BASE_FOLDER + IDGenerator.nextStr();
        try {
            FileUtils.forceMkdir(new File(filesFolder));
            int index = 1;
            while (list.size() > 2000) {
                List<BugDTO> excelBugList = list.subList(0, BATCH_PROCESS_QUANTITY);
                this.generateExcelFile(excelBugList, index, filesFolder, exportColumns);
                list.removeAll(excelBugList);
                index = 1;
            }
            this.generateExcelFile(list, index, filesFolder, exportColumns);
        } catch (Exception ignore) {
        }
        return filesFolder;
    }

    private void generateExcelFile(List<BugDTO> list, int fileIndex, String excelPath, List<BugExportColumn> exportColumns) throws Exception {
        if (CollectionUtils.isNotEmpty(list)) {
            boolean exportComment = this.exportComment(exportColumns);
            boolean exportContent = this.exportContent(exportColumns);

            List<String> bugIdList = list.stream().map(BugDTO::getId).toList();
            Map<String, List<BugCommentDTO>> bugCommentMap = new HashMap<>();
            Map<String, BugContent> bugContentMap = new HashMap<>();
            //todo 等昌昌需求确定，再实现
            Map<String, Long> bugCountMap = new HashMap<>();
            if (exportContent) {
                BugContentExample example = new BugContentExample();
                example.createCriteria().andBugIdIn(bugIdList);
                bugContentMap = bugContentMapper.selectByExample(example).stream().collect(Collectors.toMap(BugContent::getBugId, bugContent -> bugContent));
            }
            if (exportComment) {
                bugCommentMap = bugCommentService.getComments(bugIdList);
            }

            //生成excel对象
            BugExportExcelModel bugExportExcelModel = new BugExportExcelModel(exportColumns, list, bugCommentMap, bugContentMap, bugCountMap);

            //生成excel文件
            List<List<String>> data = bugExportExcelModel.getData();
            File createFile = new File(excelPath + File.separatorChar + "bug_" + fileIndex + ".xlsx");
            createFile.createNewFile();

            EasyExcel.write(createFile).excelType(ExcelTypeEnum.XLSX).sheet("sheet").doWrite(data);
        }
    }

    //是否包含缺陷评论
    public boolean exportComment(List<BugExportColumn> exportColumns) {
        for (BugExportColumn exportColumn : exportColumns) {
            if ("comment".equals(exportColumn.getKey()) && "other".equals(exportColumn.getColumnType())) {
                return true;
            }
        }
        return false;
    }

    //是否包含缺陷内容
    public boolean exportContent(List<BugExportColumn> exportColumns) {
        for (BugExportColumn exportColumn : exportColumns) {
            if ("content".equals(exportColumn.getKey()) && "system".equals(exportColumn.getColumnType())) {
                return true;
            }
        }
        return false;
    }
}
