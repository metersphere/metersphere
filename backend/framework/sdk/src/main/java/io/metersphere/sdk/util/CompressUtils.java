package io.metersphere.sdk.util;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.*;

public class CompressUtils {

    /***
     * Zip压缩
     *
     * @param data 待压缩数据
     * @return 压缩后数据
     */
    public static Object zip(Object data) {
        if (!(data instanceof byte[] temp)) {
            return data;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(temp.length);
            zip.putNextEntry(entry);
            zip.write(temp);
            zip.closeEntry();
            zip.close();
            temp = bos.toByteArray();
            bos.close();
        } catch (Exception ignore) {
        }

        return temp;
    }


    private static File getFile(String filePath) throws IOException {
        // 创建文件对象
        File file;
        file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            file.createNewFile();
        }
        // 返回文件
        return file;
    }

    public static FileOutputStream getFileStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    private static void zipFile(File file, ZipOutputStream zipOutputStream) throws IOException {
        if (file.exists()) {
            if (file.isFile()) {
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(entry);

                    final int MAX_BYTE = 10 * 1024 * 1024; // 最大流为10MB
                    int streamTotal = 0; // 接收流的容量
                    int streamNum = 0; // 需要分开的流数目
                    int leaveByte = 0; // 文件剩下的字符数
                    byte[] buffer; // byte数据接受文件的数据

                    streamTotal = bis.available(); // 获取流的最大字符数
                    streamNum = (int) Math.floor((double) streamTotal / MAX_BYTE);
                    leaveByte = (streamTotal % MAX_BYTE);

                    if (streamNum > 0) {
                        for (int i = 0; i < streamNum; i++) {
                            buffer = new byte[MAX_BYTE];
                            bis.read(buffer, 0, MAX_BYTE);
                            zipOutputStream.write(buffer, 0, MAX_BYTE);
                        }
                    }

                    // 写入剩下的流数据
                    buffer = new byte[leaveByte];
                    bis.read(buffer, 0, leaveByte); // 读入流
                    zipOutputStream.write(buffer, 0, leaveByte); // 写入流
                    zipOutputStream.closeEntry(); // 关闭当前的zip entry
                }
            }
        }
    }

    /**
     * 将多个文件压缩
     *
     * @param zipFilePath 压缩文件所在路径
     * @param fileList    要压缩的文件
     * @return
     * @throws IOException
     */
    public static File zipFiles(String zipFilePath, List<File> fileList) throws IOException {
        File zipFile = getFile(zipFilePath);
        // 文件输出流
        FileOutputStream outputStream = getFileStream(zipFile);
        // 压缩流
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        // 压缩列表中的文件
        for (File file : fileList) {
            zipFile(file, zipOutputStream);
        }
        // 关闭压缩流、文件流
        zipOutputStream.close();
        outputStream.close();
        return zipFile;
    }

    /**
     * 将多个文件压缩至指定路径
     *
     * @param fileList    待压缩的文件列表
     * @param zipFilePath 压缩文件路径
     * @return 返回压缩好的文件
     * @throws IOException
     */
    public static File zipFilesToPath(String zipFilePath, List<File> fileList) throws IOException {
        File zipFile = new File(zipFilePath);
        try( // 文件输出流
             FileOutputStream outputStream = getFileStream(zipFile);
             // 压缩流
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)
        ){
            int size = fileList.size();
            // 压缩列表中的文件
            for (int i = 0; i < size; i++) {
                File file = fileList.get(i);
                zipFile(file, zipOutputStream);
            }
        }
        return zipFile;
    }

    /***
     * Zip解压
     *
     * @param data 待解压数据
     * @return 解压后数据
     */
    public static Object unzip(Object data) {
        if (!(data instanceof byte[] temp)) {
            return data;
        }
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(temp);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num;
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    bas.write(buf, 0, num);
                }
                temp = bas.toByteArray();
                bas.flush();
                bas.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ignore) {
        }
        return temp;
    }

    public static Object zipString(Object data) {
        if (!(data instanceof String)) {
            return data;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(out)) {
                deflaterOutputStream.write(((String) data).getBytes(StandardCharsets.UTF_8));
            }
            return Base64.encodeBase64String(out.toByteArray());
        } catch (Exception e) {
            return data;
        }
    }

    public static Object unzipString(Object data) {
        if (!(data instanceof String)) {
            return data;
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (OutputStream outputStream = new InflaterOutputStream(os)) {
                outputStream.write(Base64.decodeBase64((String) data));
            }
            return os.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return data;
        }
    }
}
