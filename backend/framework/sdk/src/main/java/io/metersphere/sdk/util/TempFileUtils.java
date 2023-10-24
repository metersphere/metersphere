package io.metersphere.sdk.util;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class TempFileUtils {
    private static final String TEMP_FILE_FOLDER = "/tmp/metersphere/file/";

    public static boolean isImage(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "jpg", "jpeg", "png", "gif", "bmp", "svg", "ico");
    }


    public static String getFileTmpPath(String fileId) {
        return TEMP_FILE_FOLDER + fileId;
    }

    public static void deleteTmpFile(String fileId) {
        File file = new File(getFileTmpPath(fileId));
        if (file.exists()) {
            file.delete();
        }
    }

    public static String catchCompressFileIfNotExists(String fileId, byte[] fileBytes) {
        try {
            String previewPath = getFileTmpPath(fileId);
            compressPic(fileBytes, previewPath);
            return previewPath;
        } catch (Exception ignored) {
            ignored.printStackTrace();
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

            // 指定预览图像的宽度和高度
            int previewWidth = originalImage.getWidth() / 10;
            int previewHeight = originalImage.getHeight() / 10;
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

    public static boolean isFileExists(String fileId) {
        File file = new File(getFileTmpPath(fileId));
        return file.exists();
    }
}
