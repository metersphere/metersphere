package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.parse.ApiImport;

import java.io.InputStream;

public class MsParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source) {
        String testStr = getApiTestStr(source);
        return JSON.parseObject(testStr, ApiImport.class);
    }

}
