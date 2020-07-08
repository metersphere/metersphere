package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;

import java.io.*;

public class MsParser implements ApiImportParser {

    @Override
    public ApiImport parse(InputStream source) {BufferedReader bufferedReader = null;
        StringBuilder testStr = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(source, "UTF-8"));
            testStr = new StringBuilder();
            String inputStr = null;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return JSON.parseObject(testStr.toString(), ApiImport.class);
    }

}
