package io.metersphere.xmind.parser;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.examples.Expander;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description zip解压工具
 */
public class ZipUtils {

    private static final String CURRENT_PATH = System.getProperty("user.dir");

    /**
     * 找到压缩文件中匹配的子文件，返回的为 getContents("comments.xml, unzip
     *
     * @param subFileNames
     * @param file
     */
    public static Map<String, String> getContents(List<String> subFileNames, File file, String extractFileDir)
            throws IOException, ArchiveException {
        String destFilePath = extractFileDir;
        Map<String, String> map = new HashMap<>(16);
        File destFile = new File(destFilePath);
        if (destFile.isDirectory()) {
            String[] res = destFile.list(new FileFilter());
            for (int i = 0; i < Objects.requireNonNull(res).length; i++) {
                if (subFileNames.contains(res[i])) {
                    String s = destFilePath + File.separator + res[i];
                    String content = getFileContent(s);
                    map.put(res[i], content);
                }
            }
        }
        return map;
    }

    /**
     * 返回解压后的文件夹名字
     *
     * @return
     * @throws IOException
     * @throws ArchiveException
     */
    public static String extract(File file) throws IOException, ArchiveException {
        Expander expander = new Expander();
        String destFileName = CURRENT_PATH + File.separator + "XMind" + System.currentTimeMillis();
        expander.expand(file, new File(destFileName));
        return destFileName;
    }

    /**
     * 这是一个内部类过滤器,策略模式
     */
    static class FileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            // String的 endsWith(String str)方法 筛选出以str结尾的字符串
            if (name.endsWith(".xml") || name.endsWith(".json")) {
                return true;
            }
            return false;
        }
    }

    public static String getFileContent(String fileName) throws IOException {
        File file;
        try {
            file = new File(fileName);
        } catch (Exception e) {
            throw new RuntimeException("找不到该文件");
        }
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReder = new BufferedReader(fileReader);
        StringBuilder stringBuffer = new StringBuilder();
        while (bufferedReder.ready()) {
            if(stringBuffer.length() > 0){
                stringBuffer.append("\r\n");
            }
            stringBuffer.append(bufferedReder.readLine());
        }
        // 打开的文件需关闭，在unix下可以删除，否则在windows下不能删除（file.delete())
        bufferedReder.close();
        fileReader.close();
        return stringBuffer.toString();
    }
}
