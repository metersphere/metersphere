package io.metersphere.sdk.util;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class TempFileUtils {
    private static final String TEMP_FILE_FOLDER = "/tmp/metersphere/file/";

    private TempFileUtils() {
    }

    public static boolean isImage(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "jpg", "jpeg", "png", "gif", "bmp", "svg", "ico");
    }

    public static String getFileNameByPath(String filePath) {
        if (StringUtils.contains(filePath, "/")) {
            return StringUtils.substringAfterLast(filePath, "/");
        } else {
            return filePath;
        }
    }


    public static String getImgFileTmpPath(String fileId) {
        return TEMP_FILE_FOLDER + fileId + ".jpg";
    }

    public static void deleteTmpFile(String fileId) {
        File file = new File(getImgFileTmpPath(fileId));
        if (file.exists()) {
            file.delete();
        }
    }

    public static String catchCompressImgIfNotExists(String fileId, byte[] fileBytes) {
        try {
            String previewPath = getImgFileTmpPath(fileId);
            compressPic(fileBytes, previewPath);
            return previewPath;
        } catch (Exception ignore) {
        }
        return null;
    }

    public static void compressPic(byte[] fileBytes, String compressPicAbsolutePath) throws Exception {
        // 读取原始图像
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileBytes));
        if (originalImage == null) {
            //如果将一个不是图片的文件强行转换为BufferedImage对象，那么得到的就是null
            createFile(compressPicAbsolutePath, fileBytes);
        } else {
            File file = new File(compressPicAbsolutePath);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            //计算压缩系数
            int compressFactor = getCompressFactor(width, height);

            // 指定预览图像的宽度和高度
            int previewWidth = width / compressFactor;
            int previewHeight = height / compressFactor;
            // 创建一个缩小后的图像
            BufferedImage previewImage = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = previewImage.createGraphics();

            // 绘制缩小后的图像
            g2d.drawImage(originalImage, 0, 0, previewWidth, previewHeight, null);
            g2d.dispose();
            ImageIO.setUseCache(false);
            // 保存预览图像到文件
            ImageIO.write(previewImage, "JPEG", new File(compressPicAbsolutePath));
        }
    }

    private static int getCompressFactor(int width, int height) {
        int compressFactor = 1;

        int maxSize = width > height ? width : height;
        if (maxSize > 999) {
            compressFactor = maxSize / 999;
        }
        return compressFactor;
    }


    private static void createFile(String filePath, byte[] fileBytes) {
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
            final int MAX = 4096;
            byte[] buf = new byte[MAX];
            for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
                out.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            LogUtils.error(e);
        }
    }

    public static boolean isImgFileExists(String fileId) {
        File file = new File(getImgFileTmpPath(fileId));
        return file.exists();
    }

    public static byte[] getPreviewFile(String filePreviewPath) {
        File file = new File(filePreviewPath);
        byte[] previewByte = new byte[0];
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] b = new byte[1024];
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
}
