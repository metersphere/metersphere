package io.metersphere.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressUtils {

    /***
     * 压缩Zip
     *
     * @param data
     * @return
     */
    public static Object zip(Object data) {
        if (!(data instanceof byte[]) && !(data instanceof String)) {
            return data;
        }
        boolean isString = false;
        if (data instanceof String) {
            isString = true;
            data = ((String) data).getBytes();
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

        if (isString) {
            return new String(b);
        }
        return b;
    }

    /***
     * 解压Zip
     *
     * @param data
     * @return
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
}
