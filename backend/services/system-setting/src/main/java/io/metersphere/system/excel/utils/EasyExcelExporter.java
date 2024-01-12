package io.metersphere.system.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.SpreadsheetVersion;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EasyExcelExporter {


    private Class clazz;

    public EasyExcelExporter(Class clazz) {
        this.clazz = clazz;
    }

    public void export(HttpServletResponse response, List data, String fileName, String sheetName) {
        buildExportResponse(response, fileName);
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setWrapped(true);
        try {
            HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
            EasyExcel.write(response.getOutputStream(), this.clazz)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, List<List<String>> headList,
                                           List<List<Object>> data, String fileName, String sheetName) {
        buildExportResponse(response, fileName);
        try {
            EasyExcel.write(response.getOutputStream())
                    .head(Optional.ofNullable(headList).orElse(new ArrayList<>()))
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public void buildExportResponse(HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + ".xlsx");
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, List<List<String>> headList, List<List<Object>> data,
                                           String fileName, String sheetName, WriteHandler writeHandler) {
        buildExportResponse(response, fileName);
        try {
            EasyExcel.write(response.getOutputStream())
                    .head(Optional.ofNullable(headList).orElse(new ArrayList<>()))
                    .registerWriteHandler(writeHandler)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, List<List<String>> headList, List<List<Object>> data,
                                           String fileName, String sheetName, WriteHandler writeHandler1, WriteHandler writeHandler2) {
        buildExportResponse(response, fileName);
        try {
            EasyExcel.write(response.getOutputStream())
                    .head(Optional.ofNullable(headList).orElse(new ArrayList<>()))
                    .registerWriteHandler(writeHandler1)
                    .registerWriteHandler(writeHandler2)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public static void resetCellMaxTextLength() {
        SpreadsheetVersion excel2007 = SpreadsheetVersion.EXCEL2007;
        if (excel2007.getMaxTextLength() < Integer.MAX_VALUE) {
            Field field;
            try {
                field = excel2007.getClass().getDeclaredField("_maxTextLength");
                field.setAccessible(true);
                field.set(excel2007, Integer.MAX_VALUE);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(e.getMessage());
            }
        }
    }
}
