package io.metersphere.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.exception.ExcelException;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

public class EasyExcelExporter {

    private Class clazz;

    public EasyExcelExporter(Class clazz) {
        this.clazz = clazz;
    }

    public void export(HttpServletResponse response, List data, String fileName, String sheetName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setWrapped(true);
        try {
            HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, Set<String> excludeColumnFiledNames,  List data, String fileName, String sheetName, WriteHandler writeHandler) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            if(CollectionUtils.isNotEmpty(excludeColumnFiledNames)){
                EasyExcel.write(response.getOutputStream(), this.clazz).
                        registerWriteHandler(writeHandler).
                        excludeColumnFiledNames(excludeColumnFiledNames).
                        sheet(sheetName).doWrite(data);
            }else {
                EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(writeHandler).sheet(sheetName).doWrite(data);
            }
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

}
