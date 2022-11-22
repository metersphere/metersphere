package io.metersphere.service.definition;

import io.metersphere.api.parse.api.ESBParser;
import io.metersphere.commons.exception.MSException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

/**
 * @author song.tianyang
 * @Date 2021/3/22 7:02 下午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EsbImportService {

    public void templateExport(HttpServletResponse response) {
        try {
            ESBParser.export(response, "EsbTemplate");
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }
}
