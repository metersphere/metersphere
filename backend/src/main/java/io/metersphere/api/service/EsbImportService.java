package io.metersphere.api.service;

import io.metersphere.api.dto.definition.parse.ESBParser;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.excel.domain.UserExcelData;
import io.metersphere.excel.domain.UserExcelDataFactory;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/22 7:02 下午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EsbImportService {

    public void esbTemplateExport(HttpServletResponse response) {
        try {
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(new UserExcelDataFactory().getExcelDataByLocal());
            easyExcelExporter.export(response, generateExportTemplate(),
                    Translator.get("user_import_template_name"), Translator.get("user_import_template_sheet"));
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }

    private List<UserExcelData> generateExportTemplate() {
        List<UserExcelData> list = new ArrayList<>();
        List<String> types = TestCaseConstants.Type.getValues();
        List<String> methods = TestCaseConstants.Method.getValues();
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 2; i++) {
            UserExcelData data = new UserExcelData();
            data.setId("user_id_" + i);
            data.setName(Translator.get("user") + i);
            String workspace = "";
            for (int workspaceIndex = 1; workspaceIndex <= i; workspaceIndex++) {
                if (workspaceIndex == 1) {
                    workspace = "workspace" + workspaceIndex;
                } else {
                    workspace = workspace + "\n" + "workspace" + workspaceIndex;
                }
            }
            data.setUserIsAdmin(Translator.get("options_no"));
            data.setUserIsTester(Translator.get("options_no"));
            data.setUserIsViewer(Translator.get("options_no"));
            data.setUserIsTestManager(Translator.get("options_no"));
            list.add(data);
        }

        list.add(new UserExcelData());
        UserExcelData explain = new UserExcelData();
        explain.setName(Translator.get("do_not_modify_header_order"));
        list.add(explain);
        return list;
    }

    public void templateExport(HttpServletResponse response) {
        try {
            ESBParser.export(response,"EsbTemplate");
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }
}
