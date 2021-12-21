package io.metersphere.reportstatistics.utils;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.HeadlessRequest;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ChromeUtils {
    private static ChromeUtils chromeUtils = new ChromeUtils();

    private ChromeUtils() {
    }

    public static ChromeUtils getInstance() {
        return chromeUtils;
    }

    private synchronized WebDriver genWebDriver(HeadlessRequest headlessRequest, String language) {
        if (headlessRequest.isEmpty()) {
            LogUtil.error("Headless request is null! " + JSON.toJSONString(headlessRequest));
            return null;
        }
        //初始化一个chrome浏览器实例driver
        ChromeOptions options = new ChromeOptions();

        if (StringUtils.isEmpty(language)) {
            language = "zh-cn";
        }
        if (StringUtils.equalsAnyIgnoreCase(language, "zh-cn")) {
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put("intl.accept_languages", "zh-CN,en,en_US");
            options.setExperimentalOption("prefs", optionMap);
        } else if (StringUtils.equalsAnyIgnoreCase(language, "zh_tw")) {
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put("intl.accept_languages", "zh-TW,en,en_US");
            options.setExperimentalOption("prefs", optionMap);
        }

        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL(headlessRequest.getRemoteDriverUrl()), options);
            driver.get(headlessRequest.getUrl());
            driver.manage().window().fullscreen();
        } catch (Exception e) {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
            LogUtil.error(e);
        }
        return driver;
    }

    public synchronized String getImageInfo(HeadlessRequest request, String langurage) {
        WebDriver driver = this.genWebDriver(request, langurage);
        String files = null;
        if (driver != null) {
            try {
                //预留echart动画的加载时间
                Thread.sleep(3 * 1000);
                String js = "var chartsCanvas = document.getElementById('picChart').getElementsByTagName('canvas')[0];" +
                        "var imageUrl = null;" +
                        "if (chartsCanvas!= null) {" +
                        " imageUrl = chartsCanvas && chartsCanvas.toDataURL('image/png');" +
                        "return imageUrl;" +
                        "}";
                files = ((JavascriptExecutor) driver).executeScript(js).toString();
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                driver.quit();
            }
        }
        if (StringUtils.isNotEmpty(files)) {
            return files;
        } else {
            LogUtil.error("获取报表图片失败！参数：" + JSON.toJSONString(request));
            return null;
        }
    }
}
