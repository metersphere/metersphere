package io.metersphere.xmind.utils;

import io.metersphere.commons.utils.LogUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 工具类
 */
public class FileUtil {

    /**
     * 获取流文件
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try (OutputStream os = new FileOutputStream(file);) {
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) {
        if (file != null && file.getSize() > 0) {
            try (InputStream ins = file.getInputStream();) {
                File toFile = new File(file.getOriginalFilename());
                inputStreamToFile(ins, toFile);
                return toFile;
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return null;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
