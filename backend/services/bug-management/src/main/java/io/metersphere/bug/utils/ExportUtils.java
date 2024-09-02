package io.metersphere.bug.utils;

import io.metersphere.bug.dto.BugExportHeaderModel;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.sdk.util.CompressUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

public class ExportUtils {

    private final List<BugDTO> bugs;
    private final BugExportHeaderModel headerModel;

    public ExportUtils(
            List<BugDTO> bugs,
            BugExportHeaderModel headerModel) {
        this.bugs = bugs;
        this.headerModel = headerModel;
    }

    /**
     *  1.生成包含excel文件目录
     *  2.压缩
     *  3.删除该目录
     */
    public byte[] exportToZipFile(BiFunction<List, BugExportHeaderModel, String> generateExcelFilesFunction) throws Exception {
        //生成包含excel文件目录
        String folderPath = generateExcelFilesFunction.apply(bugs, headerModel);
        File excelFolder = new File(folderPath);
        //压缩文件
        File zipFile = CompressUtils.zipFiles(folderPath + File.separatorChar + "bug-export.zip", List.of(excelFolder.listFiles()));
        byte[] returnByte = FileUtils.readFileToByteArray(zipFile);
        //删除目录
        FileUtils.deleteDirectory(excelFolder);
        return returnByte;
    }
}
