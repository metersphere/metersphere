package io.metersphere.sdk.util;

import io.metersphere.sdk.exception.MSException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class TempFileUtils {
    private static final String TEMP_FILE_FOLDER = "/tmp/metersphere/file/";
    private static final int CREATE_FILE_BYTES_MAX_LENGTH = 256;
    private TempFileUtils() {
    }

    public static boolean isImage(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "jpg", "jpeg", "png", "gif", "bmp", "svg", "ico", "webp", "apng", "avif","pdf");
    }

    public static String getFileNameByPath(String filePath) {
        if (StringUtils.contains(filePath, "/")) {
            return StringUtils.substringAfterLast(filePath, "/");
        } else {
            return filePath;
        }
    }

    //获取临时文件路径
    public static String getTmpFilePath(String fileId) {
        return TEMP_FILE_FOLDER + "tmp/" + fileId;
    }

    public static void deleteTmpFile(String fileId) {
        try {
            File file = new File(getTmpFilePath(fileId));
            FileUtils.forceDelete(file);
        } catch (Exception ignore) {

        }
    }
    public static byte[] compressPic(byte[] fileBytes) throws IOException {
        byte[] compressBytes = compressPic(new ByteArrayInputStream(fileBytes));
        return compressBytes.length > 0 ? compressBytes : fileBytes;
    }

    public static byte[] compressPic(InputStream imgInputStream) throws IOException {
        // 读取原始图像
        BufferedImage originalImage = ImageIO.read(imgInputStream);
        if (originalImage != null) {
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            //计算压缩系数
            int compressFactor = getCompressFactor(width, height);

            // 指定预览图像的宽度和高度
            int previewWidth = width / compressFactor;
            int previewHeight = height / compressFactor;

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                // 创建一个缩小后的图像
                BufferedImage previewImage = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = previewImage.createGraphics();

                // 绘制缩小后的图像
                g2d.drawImage(originalImage, 0, 0, previewWidth, previewHeight, null);
                g2d.dispose();
                ImageIO.setUseCache(false);
                ImageIO.write(previewImage, "JPEG", outputStream);
                return outputStream.toByteArray();
            } catch (Exception ignore) {
            }
        }
        return new byte[0];
    }

    private static int getCompressFactor(int width, int height) {
        int compressFactor = 1;

        int maxSize = width > height ? width : height;
        if (maxSize > 999) {
            compressFactor = maxSize / 999;
        }
        return compressFactor;
    }


    public static String createFile(String filePath, byte[] fileBytes) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            LogUtils.error(e);
        }

        try (InputStream in = new ByteArrayInputStream(fileBytes); OutputStream out = new FileOutputStream(file)) {

            byte[] buf = new byte[CREATE_FILE_BYTES_MAX_LENGTH];
            int num;
            while ((num = in.read(buf)) > 0) {
                out.write(buf, 0, num);
            }
        } catch (IOException e) {
            LogUtils.error(e);
        }
        return filePath;
    }

    //图片原图是否存在
    public static boolean isImgTmpFileExists(String fileId) {
        File file = new File(getTmpFilePath(fileId));
        return file.exists();
    }

    public static byte[] getFile(String filePath) {
        File file = new File(filePath);
        byte[] previewByte = new byte[0];
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] b = new byte[CREATE_FILE_BYTES_MAX_LENGTH];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                previewByte = bos.toByteArray();
            } catch (Exception ignore) {

            }
        }
        return previewByte;
    }

    public static void writeExportFile(String fileAbsoluteName, Object exportResponse) {
        File createFile = new File(fileAbsoluteName);
        if (!createFile.exists()) {
            try {
                createFile.getParentFile().mkdirs();
                createFile.createNewFile();
            } catch (IOException e) {
                throw new MSException(e);
            }
        }
        try {
            if (exportResponse instanceof String exportResponseStr) {
                FileUtils.writeByteArrayToFile(createFile, exportResponseStr.getBytes());
            } else {
                FileUtils.writeByteArrayToFile(createFile, JSON.toJSONString(exportResponse).getBytes());
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}
