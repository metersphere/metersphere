package io.metersphere.reportstatistics.utils;

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

public class ChromeUtil {
    private static ChromeUtil chromeUtils = new ChromeUtil();

    private ChromeUtil() {
    }

    public static ChromeUtil getInstance() {
        return chromeUtils;
    }

    private WebDriver genWebDriver(String seleniumUrl, String language) {
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
            LogUtil.error(e);
        }
        return driver;
    }

    public Map<String, String> getImageInfo(HeadlessRequest request, String langurage) {
        Map<String, String> returnMap = new HashMap<>();
        if (request.isEmpty()) {
            return returnMap;
        }
        WebDriver driver = this.genWebDriver(request.getRemoteDriverUrl(), langurage);
        if (driver != null) {
            for (Map.Entry<String, String> urlEntry : request.getUrlMap().entrySet()) {
                String id = urlEntry.getKey();
                String url = urlEntry.getValue();
                String files = null;
                try {
                    driver.get(url);
                    driver.manage().window().fullscreen();
                    //预留echart动画的加载时间
                    Thread.sleep(10 * 1000);
                    String js = "var chartsCanvas = document.getElementById('picChart').getElementsByTagName('canvas')[0];" +
                            "var imageUrl = null;" +
                            "if (chartsCanvas!= null) {" +
                            " imageUrl = chartsCanvas && chartsCanvas.toDataURL('image/png');" +
                            "return imageUrl;" +
                            "}";
                    files = ((JavascriptExecutor) driver).executeScript(js).toString();
                    Thread.sleep(1 * 1000);
                } catch (Exception e) {
                    LogUtil.error("使用selenium获取图片报错!", e);
                }
                returnMap.put(id, files);
            }
            driver.quit();
        }
        return returnMap;
    }
}
