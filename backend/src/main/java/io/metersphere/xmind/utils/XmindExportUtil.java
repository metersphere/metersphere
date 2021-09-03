package io.metersphere.xmind.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.xmind.pojo.TestCaseXmindData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmind.core.*;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/7/30 11:05 上午
 */
public class XmindExportUtil {

    boolean isUseCustomId = false;

    public XmindExportUtil(boolean isUseCustomId) {
        this.isUseCustomId = isUseCustomId;
    }

    public void exportXmind(HttpServletResponse response, TestCaseXmindData rootXmind) {
        IWorkbook workBook = this.createXmindByTestCase(rootXmind);

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("TestCaseExport", "UTF-8") + ".xmind");
            workBook.save(response.getOutputStream());
//            EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

    public void saveXmind(TestCaseXmindData rootXmind) throws IOException, CoreException {
        this.createXmindByTestCase(rootXmind).save(File.separator + "User" + File.separator + "admin" + "/test.xmind");
    }

    public IWorkbook createXmindByTestCase(TestCaseXmindData rootXmind) {
        // 创建思维导图的工作空间
        IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
        IWorkbook workbook = workbookBuilder.createWorkbook();

        Map<String, IStyle> styleMap = this.initTheme(workbook);

        // 获得默认sheet
        ISheet primarySheet = workbook.getPrimarySheet();
        if (styleMap.containsKey("mapStyle")) {
            primarySheet.setStyleId(styleMap.get("mapStyle").getId());
        }
        // 获得根主题
        ITopic rootTopic = primarySheet.getRootTopic();
        if (styleMap.containsKey("centralTopicStyle")) {
            rootTopic.setStyleId(styleMap.get("centralTopicStyle").getId());
        }
        // 设置根主题的标题
        rootTopic.setTitleText(Translator.get("test_case"));

        if (CollectionUtils.isNotEmpty(rootXmind.getChildren())) {
            for (TestCaseXmindData data : rootXmind.getChildren()) {
                addItemTopic(rootTopic, workbook, styleMap, data, true);
            }
        }
        return workbook;
    }

    private Map<String, IStyle> initTheme(IWorkbook workbook) {
        Map<String, IStyle> styleMap = new HashMap<>();

        IStyleSheet styleSheet = workbook.getStyleSheet();
        IStyle mapStyle = styleSheet.createStyle(IStyle.MAP);
        mapStyle.setProperty("line-tapered","none");
        mapStyle.setProperty("multi-line-colors","none");
        mapStyle.setProperty("svg:fill","#FFFFFF");
        mapStyle.setProperty("color-gradient","none");
        styleSheet.addStyle(mapStyle, IStyleSheet.NORMAL_STYLES);
        styleMap.put("mapStyle",mapStyle);

        IStyle centralTopicStyle = styleSheet.createStyle(IStyle.TOPIC);
        centralTopicStyle.setProperty("line-width", "1pt");
        centralTopicStyle.setProperty("svg:fill", "#DCE6F2");
        centralTopicStyle.setProperty("fo:font-family", "Microsoft YaHei");
        centralTopicStyle.setProperty("border-line-width", "5pt");
        centralTopicStyle.setProperty("shape-class", "org.xmind.topicShape.roundedRect");
        centralTopicStyle.setProperty("fo:color", "#376092");
        centralTopicStyle.setProperty("line-class", "org.xmind.branchConnection.curve");
        centralTopicStyle.setProperty("border-line-color", "#558ED5");
        centralTopicStyle.setProperty("line-color", "#558ED5");
        styleSheet.addStyle(centralTopicStyle, IStyleSheet.NORMAL_STYLES);
        styleMap.put("centralTopicStyle", centralTopicStyle);

        IStyle mainTopicStyle = styleSheet.createStyle(IStyle.TOPIC);
        mainTopicStyle.setProperty("line-width", "1pt");
        mainTopicStyle.setProperty("svg:fill", "#DCE6F2");
        mainTopicStyle.setProperty("fo:font-family", "Microsoft YaHei");
        mainTopicStyle.setProperty("border-line-width", "2pt");
        mainTopicStyle.setProperty("shape-class", "org.xmind.topicShape.roundedRect");
        mainTopicStyle.setProperty("fo:color", "#17375E");
        mainTopicStyle.setProperty("line-class", "org.xmind.branchConnection.curve");
        mainTopicStyle.setProperty("border-line-color", "#558ED5");
        mainTopicStyle.setProperty("line-color", "#558ED5");
        styleSheet.addStyle(mainTopicStyle, IStyleSheet.NORMAL_STYLES);
        styleMap.put("mainTopicStyle", mainTopicStyle);

        IStyle subTopicStyle = styleSheet.createStyle(IStyle.TOPIC);
        subTopicStyle.setProperty("line-width", "1pt");
        subTopicStyle.setProperty("fo:font-family", "Microsoft YaHei");
        subTopicStyle.setProperty("border-line-width", "3pt");
        subTopicStyle.setProperty("line-class", "org.xmind.branchConnection.curve");
        subTopicStyle.setProperty("border-line-color", "#558ED5");
        subTopicStyle.setProperty("line-color", "#558ED5");
        styleSheet.addStyle(subTopicStyle, IStyleSheet.NORMAL_STYLES);
        styleMap.put("subTopicStyle", subTopicStyle);

        IStyle floatingTopicStyle = styleSheet.createStyle(IStyle.TOPIC);
        floatingTopicStyle.setProperty("svg:fill","#558ED5");
        floatingTopicStyle.setProperty("fo:font-family","Microsoft YaHei");
        floatingTopicStyle.setProperty("border-line-width","0pt");
        floatingTopicStyle.setProperty("fo:color","#FFFFFF");
        floatingTopicStyle.setProperty("fo:font-weight","bold");
        floatingTopicStyle.setProperty("line-color","#558ED5");
        styleSheet.addStyle(floatingTopicStyle, IStyleSheet.NORMAL_STYLES);
        styleMap.put("floatingTopicStyle", floatingTopicStyle);

        IStyle summaryTopic = styleSheet.createStyle(IStyle.TOPIC);
        summaryTopic.setProperty("fo:font-style","italic");
        summaryTopic.setProperty("svg:fill","#77933C");
        summaryTopic.setProperty("fo:font-family","Microsoft YaHei");
        summaryTopic.setProperty("border-line-width","0pt");
        summaryTopic.setProperty("fo:font-size","10pt");
        summaryTopic.setProperty("shape-class","org.xmind.topicShape.roundedRect");
        summaryTopic.setProperty("fo:color","#FFFFFF");
        summaryTopic.setProperty("line-class","org.xmind.branchConnection.curve");
        styleSheet.addStyle(summaryTopic, IStyleSheet.NORMAL_STYLES);
        styleMap.put("summaryTopic", floatingTopicStyle);

        IStyle itemTopic = styleSheet.createStyle(IStyle.TOPIC);
        itemTopic.setProperty("fo:text-align","center");
        itemTopic.setProperty("line-width","1pt");
        itemTopic.setProperty("svg:fill","none");
        itemTopic.setProperty("fo:font-family","Microsoft YaHei");
        itemTopic.setProperty("border-line-width","2pt");
        itemTopic.setProperty("shape-class","org.xmind.topicShape.underline");
        itemTopic.setProperty("fo:font-size","14pt");
        itemTopic.setProperty("fo:color","#17375E");
        itemTopic.setProperty("line-class","org.xmind.branchConnection.curve");
        itemTopic.setProperty("border-line-color","#558ED5");
        itemTopic.setProperty("line-color","#558ED5");
        styleSheet.addStyle(itemTopic, IStyleSheet.NORMAL_STYLES);
        styleMap.put("itemTopic",itemTopic);

        return styleMap;
    }

    private void addItemTopic(ITopic parentTpoic, IWorkbook workbook, Map<String, IStyle> styleMap, TestCaseXmindData xmindData, boolean isFirstLevel) {
        ITopic topic = workbook.createTopic();
        topic.setTitleText(xmindData.getModuleName());
        if (isFirstLevel) {
            if (styleMap.containsKey("mainTopicStyle")) {
                topic.setStyleId(styleMap.get("mainTopicStyle").getId());
            }
        } else {
            if (styleMap.containsKey("subTopicStyle")) {
                topic.setStyleId(styleMap.get("subTopicStyle").getId());
            }
        }
        parentTpoic.add(topic);

        if (CollectionUtils.isNotEmpty(xmindData.getTestCaseList())) {
            IStyle style = null;
            if (styleMap.containsKey("subTopicStyle")) {
                style = styleMap.get("subTopicStyle");
            }
            for (TestCaseDTO dto : xmindData.getTestCaseList()) {
                // 创建小节节点
                ITopic itemTopic = workbook.createTopic();
                if (style != null) {
                    itemTopic.setStyleId(style.getId());
                }
                String caseNameType = "tc:";
                if(StringUtils.isNotEmpty(dto.getPriority())){
                    caseNameType = "tc-"+dto.getPriority()+":";
                }
                itemTopic.setTitleText(caseNameType + dto.getName());

                String id = dto.getNum().toString();
                if (this.isUseCustomId) {
                    id = dto.getCustomNum();
                }
                ITopic idTopic = workbook.createTopic();
                idTopic.setTitleText("id:" + id);
                if (style != null) {
                    idTopic.setStyleId(style.getId());
                }
                itemTopic.add(idTopic, ITopic.ATTACHED);

                if (dto.getPrerequisite() != null) {
                    ITopic pcTopic = workbook.createTopic();
                    pcTopic.setTitleText("pc:" + dto.getPrerequisite());
                    if (style != null) {
                        pcTopic.setStyleId(style.getId());
                    }
                    itemTopic.add(pcTopic, ITopic.ATTACHED);
                }

                if (dto.getRemark() != null) {
                    ITopic rcTopic = workbook.createTopic();
                    rcTopic.setTitleText("rc:" + dto.getRemark());
                    if (style != null) {
                        rcTopic.setStyleId(style.getId());
                    }
                    itemTopic.add(rcTopic, ITopic.ATTACHED);
                }

                if (dto.getTags() != null) {
                    try {
                        JSONArray arr = JSONArray.parseArray(dto.getTags());
                        String tagStr = "";
                        for (int i = 0; i < arr.size(); i++) {
                            tagStr = tagStr + arr.getString(i) + ",";
                        }
                        if (tagStr.endsWith(",")) {
                            tagStr = tagStr.substring(0, tagStr.length() - 1);
                        }
                        ITopic tagTopic = workbook.createTopic();
                        tagTopic.setTitleText("tag:" + tagStr);
                        if (style != null) {
                            tagTopic.setStyleId(style.getId());
                        }
                        itemTopic.add(tagTopic, ITopic.ATTACHED);
                    } catch (Exception e) {
                    }
                }

                if (dto.getSteps() != null) {
                    try {
                        JSONArray arr = JSONArray.parseArray(dto.getSteps());
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            if (obj.containsKey("desc")) {
                                ITopic stepTopic = workbook.createTopic();
                                String desc = obj.getString("desc");
                                stepTopic.setTitleText(desc);
                                if (style != null) {
                                    stepTopic.setStyleId(style.getId());
                                }

                                boolean hasResult = false;
                                if (obj.containsKey("result")) {
                                    String result = obj.getString("result");
                                    if (StringUtils.isNotEmpty(result)) {
                                        hasResult = true;
                                        ITopic resultTopic = workbook.createTopic();
                                        resultTopic.setTitleText(result);
                                        if (style != null) {
                                            resultTopic.setStyleId(style.getId());
                                        }
                                        stepTopic.add(resultTopic, ITopic.ATTACHED);
                                    }
                                }

                                if (StringUtils.isNotEmpty(desc) || hasResult) {
                                    itemTopic.add(stepTopic, ITopic.ATTACHED);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                topic.add(itemTopic);
            }
        }

        if (CollectionUtils.isNotEmpty(xmindData.getChildren())) {
            for (TestCaseXmindData data : xmindData.getChildren()) {
                addItemTopic(topic, workbook, styleMap, data, false);
            }
        }
    }
}
