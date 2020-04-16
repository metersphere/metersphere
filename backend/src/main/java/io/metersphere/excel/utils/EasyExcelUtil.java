package io.metersphere.excel.utils;

import com.alibaba.excel.EasyExcel;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.exception.ExcelException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class EasyExcelUtil {

    public static void export(HttpServletResponse response, Class clazz, List data, String fileName, String sheetName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("不支持UTF-8编码");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO异常");
        }

    }
}
