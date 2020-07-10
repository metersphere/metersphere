package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;

import java.io.*;

public class MsParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source) {
        String testStr = getApiTestStr(source);
        return JSON.parseObject(testStr.toString(), ApiImport.class);
    }

}
