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

    private synchronized WebDriver genWebDriver(String seleniumUrl, String language) {
        if (StringUtils.isEmpty(seleniumUrl)) {
            LogUtil.error("Headless request is null! " + seleniumUrl);
            return null;
        }
        //初始化一个chrome浏览器实例driver
        ChromeOptions options = new ChromeOptions();

        if (StringUtils.isEmpty(language)) {
            language = "zh_cn";
        }
        if (StringUtils.equalsAnyIgnoreCase(language, "zh_cn")) {
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
            driver = new RemoteWebDriver(new URL(seleniumUrl), options);
        } catch (Exception e) {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
            LogUtil.error(e);
        }
        return driver;
    }

    public synchronized Map<String, String> getImageInfo(HeadlessRequest request, String langurage) {
        Map<String, String> returnMap = new HashMap<>();
        if (request.isEmpty()) {
            return returnMap;
        }
        WebDriver driver = this.genWebDriver(request.getRemoteDriverUrl(), langurage);
        if (driver != null) {
            for (Map.Entry<String, String> urlEntry : request.getUrlMap().entrySet()) {
                String id = urlEntry.getKey();
                String url = urlEntry.getValue();
                try {
                    driver.get(url);
                    driver.manage().window().fullscreen();
                    //预留echart动画的加载时间
                    Thread.sleep(3 * 1000);
                    String js = "var chartsCanvas = document.getElementById('picChart').getElementsByTagName('canvas')[0];" +
                            "var imageUrl = null;" +
                            "if (chartsCanvas!= null) {" +
                            " imageUrl = chartsCanvas && chartsCanvas.toDataURL('image/png');" +
                            "return imageUrl;" +
                            "}";
                    String files = ((JavascriptExecutor) driver).executeScript(js).toString();
                    if (StringUtils.isNotEmpty(files)) {
                        returnMap.put(id, files);
                    }
                    Thread.sleep(1 * 1000);
                } catch (Exception e) {
                    LogUtil.error(e);
                }

            }

            if (driver != null) {
                driver.quit();
            }
        }
        return returnMap;
    }
}
