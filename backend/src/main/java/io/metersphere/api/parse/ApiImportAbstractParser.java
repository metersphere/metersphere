package io.metersphere.api.parse;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class ApiImportAbstractParser implements ApiImportParser {

    protected String getApiTestStr(InputStream source) {
        BufferedReader bufferedReader = null;
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
        return testStr.toString();
    }

}
