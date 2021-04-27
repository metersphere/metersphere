package io.metersphere.commons.utils;

import java.io.*;
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
        if (!(data instanceof byte[])) {
            return data;
        }

        byte[] temp = (byte[]) data;
        byte[] b = (byte[]) data;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(temp.length);
            zip.putNextEntry(entry);
            zip.write(temp);
            zip.closeEntry();
            zip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }

        return b;
    }

    private final static String ZIP_PATH = "/opt/metersphere/data/tmp/";

    public static File getFile(String fileName) throws IOException {
        // 创建文件对象
        File file;
        if (ZIP_PATH != null && !ZIP_PATH.equals("")) {
            file = new File(ZIP_PATH, fileName);
        } else {
            file = new File(fileName);
        }
        if (!file.exists()) {
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
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipEntry entry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(entry);

                final int MAX_BYTE = 10 * 1024 * 1024; // 最大流为10MB
                long streamTotal = 0; // 接收流的容量
                int streamNum = 0; // 需要分开的流数目
                int leaveByte = 0; // 文件剩下的字符数
                byte[] buffer; // byte数据接受文件的数据

                streamTotal = bis.available(); // 获取流的最大字符数
                streamNum = (int) Math.floor(streamTotal / MAX_BYTE);
                leaveByte = (int) (streamTotal % MAX_BYTE);

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

                // 关闭输入流
                bis.close();
                fis.close();
            }
        }
    }

    /**
     * 将多个文件压缩
     *
     * @param fileList    待压缩的文件列表
     * @param zipFileName 压缩文件名
     * @return 返回压缩好的文件
     * @throws IOException
     */
    public static File zipFiles(String zipFileName, List<File> fileList) throws IOException {
        File zipFile = getFile(zipFileName);
        // 文件输出流
        FileOutputStream outputStream = getFileStream(zipFile);
        // 压缩流
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        int size = fileList.size();
        // 压缩列表中的文件
        for (int i = 0; i < size; i++) {
            File file = fileList.get(i);
            zipFile(file, zipOutputStream);
        }
        // 关闭压缩流、文件流
        zipOutputStream.close();
        outputStream.close();
        return zipFile;
    }

    public static void deleteFile(String delPath) {
        try {
            File file = new File(delPath);
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] fileList = file.list();
                for (int i = 0; i < fileList.length; i++) {
                    File delfile = new File(delPath + File.separator + fileList[i]);
                    if (!delfile.isDirectory()) {
                        delfile.delete();
                    } else if (delfile.isDirectory()) {
                        deleteFile(delPath + File.separator + fileList[i]);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * Zip解压
     *
     * @param data 待解压数据
     * @return 解压后数据
     */
    public static Object unzip(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] temp = (byte[]) data;
        byte[] b = (byte[]) data;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(temp);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return b;
    }

    /**
     * GZip压缩
     *
     * @param data 待压缩数据
     * @return 压缩后数
     */
    public static Object compress(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] bytes = (byte[]) data;
        try (ByteArrayOutputStream obj = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return obj.toByteArray();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return data;
        }
    }

    /**
     * GZip解压
     *
     * @param data 待解压数据
     * @return 解压后数据
     */
    public static Object decompress(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] bytes = (byte[]) data;
        if (bytes.length == 0) {
            return bytes;
        }
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes)); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return data;
        }
    }
}
