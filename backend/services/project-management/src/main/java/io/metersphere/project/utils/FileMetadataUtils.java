package io.metersphere.project.utils;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * fileMetadata工具类
 */
public class FileMetadataUtils {

    public static final String FILE_TYPE_EMPTY = "unknown";

    private FileMetadataUtils() {
    }

    /**
     * 由于unknown属于位置文件类型，对应的文件类型存储是空字符串，这里进行转化
     *
     * @param request 请求
     */
    public static void transformRequestFileType(FileMetadataTableRequest request) {
        if (StringUtils.equals(request.getFileType(), FILE_TYPE_EMPTY)) {
            request.setFileType("");
        }
    }

    public static String getFileName(FileMetadata fileMetadata) {
        if (StringUtils.isBlank(fileMetadata.getType())) {
            return fileMetadata.getName();
        }
        return fileMetadata.getName() + "." + fileMetadata.getType();
    }

    public static String getFileNameWithId(FileMetadata fileMetadata) {
        if (StringUtils.isBlank(fileMetadata.getType())) {
            return fileMetadata.getName() + "_" + fileMetadata.getId();
        }
        return fileMetadata.getName() + "_" + fileMetadata.getId() + "." + fileMetadata.getType();
    }

    /**
     * 将空文件类型转换为unknown
     *
     * @param fileTypes 文件类型
     */
    public static void transformEmptyFileType(List<String> fileTypes) {
        if (fileTypes.contains(StringUtils.EMPTY)) {
            fileTypes.remove(StringUtils.EMPTY);
            fileTypes.add(FILE_TYPE_EMPTY);
        }
    }

    /**
     * 没有文件后缀 、 以"."结尾、 以"."开头的隐藏文件、以.unknown结尾的文件，都认为是未知文件
     *
     * @param filePath
     * @return
     */
    public static boolean isUnknownFile(String filePath) {
        return StringUtils.endsWith(filePath, ".") || StringUtils.endsWithIgnoreCase(filePath, ".unknown") || filePath.indexOf(".") < 1;
    }
}
