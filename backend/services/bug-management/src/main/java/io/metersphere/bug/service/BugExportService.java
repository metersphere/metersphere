package io.metersphere.bug.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.domain.BugContentExample;
import io.metersphere.bug.dto.BugExportColumn;
import io.metersphere.bug.dto.BugExportExcelModel;
import io.metersphere.bug.dto.BugExportHeaderModel;
import io.metersphere.bug.dto.response.BugCommentDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.mapper.BugContentMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
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
    private BugCommentService bugCommentService;
    @Resource
    private BugContentMapper bugContentMapper;

    /**
     * @param list      缺陷数据
     * @param headerModel     excel导出表头
     * @return excel所在的文件夹
     */
    public String generateExcelFiles(List<BugDTO> list, BugExportHeaderModel headerModel) {
        String filesFolder = EXPORT_TEMP_BASE_FOLDER + IDGenerator.nextStr();
        try {
            FileUtils.forceMkdir(new File(filesFolder));
            int index = 1;
            while (list.size() > BATCH_PROCESS_QUANTITY) {
                List<BugDTO> excelBugList = list.subList(0, BATCH_PROCESS_QUANTITY);
                this.generateExcelFile(excelBugList, headerModel.getXlsxFileNamePrefix() + index + ".xlsx", filesFolder, headerModel);
                list.removeAll(excelBugList);
                index += 1;
            }
            this.generateExcelFile(list, headerModel.getXlsxFileNamePrefix() + index + ".xlsx", filesFolder, headerModel);
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
            throw new MSException(e.getMessage());
        }
        return filesFolder;
    }

    /**
     * 生成excel文件
     * @param list 列表数据
     * @param xlsxFileName excel文件名
     * @param excelPath excel文件路径
     * @param headerModel excel导出表头
     * @throws Exception 异常信息
     */
    private void generateExcelFile(List<BugDTO> list, String xlsxFileName, String excelPath, BugExportHeaderModel headerModel) throws Exception {
        if (CollectionUtils.isNotEmpty(list)) {
            // 准备数据 {评论, 内容, 关联用例数}
            boolean exportComment = this.exportComment(headerModel.getExportColumns());
            boolean exportContent = this.exportContent(headerModel.getExportColumns());

            List<String> bugIdList = list.stream().map(BugDTO::getId).toList();
            Map<String, List<BugCommentDTO>> bugCommentMap = new HashMap<>(16);
            Map<String, BugContent> bugContentMap = new HashMap<>(16);
            if (exportContent) {
                BugContentExample example = new BugContentExample();
                example.createCriteria().andBugIdIn(bugIdList);
                bugContentMap = bugContentMapper.selectByExampleWithBLOBs(example).stream().collect(Collectors.toMap(BugContent::getBugId, bugContent -> bugContent));
            }
            if (exportComment) {
                bugCommentMap = bugCommentService.getComments(bugIdList);
            }

            //生成excel对象
            BugExportExcelModel bugExportExcelModel = new BugExportExcelModel(headerModel, list, bugCommentMap, bugContentMap);

            //生成excel文件
            List<List<String>> data = bugExportExcelModel.getData();
            File createFile = new File(excelPath + File.separatorChar + xlsxFileName);
            createFile.createNewFile();

            EasyExcel.write(createFile).excelType(ExcelTypeEnum.XLSX).sheet("sheet").doWrite(data);
        }
    }

    /**
     * 是否包含缺陷评论
     * @param exportColumns 导出列
     * @return 是否包含评论列
     */
    public boolean exportComment(List<BugExportColumn> exportColumns) {
        for (BugExportColumn exportColumn : exportColumns) {
            if ("comment".equals(exportColumn.getKey()) && "other".equals(exportColumn.getColumnType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含缺陷内容
     * @param exportColumns 导出列
     * @return 是否包含缺陷内容列
     */
    public boolean exportContent(List<BugExportColumn> exportColumns) {
        for (BugExportColumn exportColumn : exportColumns) {
            if ("content".equals(exportColumn.getKey()) && "system".equals(exportColumn.getColumnType())) {
                return true;
            }
        }
        return false;
    }
}
