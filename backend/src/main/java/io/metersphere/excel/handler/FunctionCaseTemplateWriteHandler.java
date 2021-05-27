package io.metersphere.excel.handler;

import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import io.metersphere.i18n.Translator;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * @author song.tianyang
 * @Date 2021/5/7 2:17 下午
 * @Description
 */
public class FunctionCaseTemplateWriteHandler extends AbstractRowHeightStyleStrategy {

    private boolean isNeedId;
    public FunctionCaseTemplateWriteHandler(boolean isNeedId){
        this.isNeedId = isNeedId;
    }
    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {
        super.afterRowDispose(writeSheetHolder, writeTableHolder, row, relativeRowIndex, isHead);
        if (isHead) {
            Sheet sheet = writeSheetHolder.getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();

            if(isNeedId){
                // 在第一行 第1列创建一个批注
                Comment comment1 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 3, 1));
                // 输入批注信息
                comment1.setString(new XSSFRichTextString(Translator.get("do_not_modify_header_order")+"，"+Translator.get("id_required")));

                // 在第一行 第3列创建一个批注
                Comment comment2 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 2, 0, (short) 3, 1));
                // 输入批注信息
                comment2.setString(new XSSFRichTextString(Translator.get("module_created_automatically")));

//                // 在第一行 第5列创建一个批注
//                Comment comment3 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 0, (short) 3, 1));
//                // 输入批注信息
//                comment3.setString(new XSSFRichTextString(Translator.get("options") + "（functional、performance、api）"));


                // 在第一行 第4列创建一个批注
                Comment comment4 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 0, (short) 3, 1));
                // 输入批注信息
                comment4.setString(new XSSFRichTextString(Translator.get("please_input_workspace_member")));

                // 在第一行 第5列创建一个批注
                Comment comment5 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 0, (short) 3, 1));
                // 输入批注信息
                comment5.setString(new XSSFRichTextString(Translator.get("options") + "（P0、P1、P2、P3）"));

                // 在第一行 第6列创建一个批注
                Comment comment6 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 5, 0, (short) 3, 1));
                // 输入批注信息
                comment6.setString(new XSSFRichTextString(Translator.get("tag_tip_pattern")));

                // 将批注添加到单元格对象中
                sheet.getRow(0).getCell(1).setCellComment(comment1);
                sheet.getRow(0).getCell(1).setCellComment(comment2);
                sheet.getRow(0).getCell(1).setCellComment(comment4);
                sheet.getRow(0).getCell(1).setCellComment(comment5);
                sheet.getRow(0).getCell(1).setCellComment(comment6);
            }else {
                // 在第一行 第2列创建一个批注
                Comment comment2 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 1, 0, (short) 3, 1));
                // 输入批注信息
                comment2.setString(new XSSFRichTextString(Translator.get("do_not_modify_header_order") + ","+Translator.get("module_created_automatically")));

                // 在第一行 第3列创建一个批注
                Comment comment4 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 2, 0, (short) 3, 1));
                // 输入批注信息
                comment4.setString(new XSSFRichTextString(Translator.get("please_input_workspace_member")));

                // 在第一行 第4列创建一个批注
                Comment comment5 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 0, (short) 3, 1));
                // 输入批注信息
                comment5.setString(new XSSFRichTextString(Translator.get("options") + "（P0、P1、P2、P3）"));

                // 在第一行 第5列创建一个批注
                Comment comment6 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 0, (short) 3, 1));
                // 输入批注信息
                comment6.setString(new XSSFRichTextString(Translator.get("tag_tip_pattern")));

                // 将批注添加到单元格对象中
                sheet.getRow(0).getCell(1).setCellComment(comment2);
                sheet.getRow(0).getCell(1).setCellComment(comment4);
                sheet.getRow(0).getCell(1).setCellComment(comment5);
                sheet.getRow(0).getCell(1).setCellComment(comment6);
            }

        }
    }

    @Override
    protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
    }

    @Override
    protected void setContentColumnHeight(Row row, int relativeRowIndex) {

    }
}
