package io.metersphere.project.utils;

import io.metersphere.project.dto.FileInformationDTO;
import io.metersphere.project.dto.FileTableResult;
import io.metersphere.project.request.filemanagement.FileMetadataTableRequest;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.util.FilePreviewUtil;
import io.metersphere.sdk.util.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class FileManagementBaseUtils {
    public static BaseTreeNode getNodeByName(List<BaseTreeNode> preliminaryTreeNodes, String nodeName) {
        for (BaseTreeNode firstLevelNode : preliminaryTreeNodes) {
            if (StringUtils.equals(firstLevelNode.getName(), nodeName)) {
                return firstLevelNode;
            }
            if (CollectionUtils.isNotEmpty(firstLevelNode.getChildren())) {
                for (BaseTreeNode secondLevelNode : firstLevelNode.getChildren()) {
                    if (StringUtils.equals(secondLevelNode.getName(), nodeName)) {
                        return secondLevelNode;
                    }
                    if (CollectionUtils.isNotEmpty(secondLevelNode.getChildren())) {
                        for (BaseTreeNode thirdLevelNode : secondLevelNode.getChildren()) {
                            if (StringUtils.equals(thirdLevelNode.getName(), nodeName)) {
                                return thirdLevelNode;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        byte[] buffer = new byte[0];
        try (FileInputStream fi = new FileInputStream(file)) {
            buffer = new byte[(int) file.length()];
            int offset = 0;
            int numRead;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (Exception ignore) {
        }
        return buffer;
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[8192];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileMD5(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes, 0, bytes.length);
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void checkFilePage(FileTableResult result, FileMetadataTableRequest request, boolean hasData) {
        //返回值的页码和当前页码相同
        Assertions.assertEquals(result.getTableData().getCurrent(), request.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getTableData().getList())).size() <= request.getPageSize());
        List<FileInformationDTO> fileInformationDTOList = JSON.parseArray(JSON.toJSONString(result.getTableData().getList()), FileInformationDTO.class);
        for (FileInformationDTO fileInformationDTO : fileInformationDTOList) {
            if (FilePreviewUtil.isImage(fileInformationDTO.getFileType())) {
                //检查是否有预览文件
                String previewPath = fileInformationDTO.getPreviewSrc();
                File file = new File(previewPath);
                Assertions.assertTrue(file.exists());
            }
        }

        //判断返回的节点统计总量是否和表格总量匹配
        long allResult = 0;
        for (Long countByModuleId : result.getModuleCount().values()) {
            allResult += countByModuleId;
        }
        Assertions.assertEquals(allResult, result.getTableData().getTotal());
        Assertions.assertEquals(request.getPageSize(), result.getTableData().getPageSize());
        if (hasData) {
            Assertions.assertTrue(allResult > 0);
        } else {
            Assertions.assertTrue(allResult == 0);
        }
    }
}
