package io.metersphere.reportstatistics.utils;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.HeadlessRequest;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeUtils {
    private static final String DEFAULT_DRIVERPATH = "/Users/handsomesong/chromeDriver/chromedriver_mac64_m1/chromedriver";

    private WebDriver genWebDriver(HeadlessRequest headlessRequest) {
        String driverPath = headlessRequest.driverPath;
        if (StringUtils.isEmpty(driverPath)) {
            driverPath = DEFAULT_DRIVERPATH;
        }
        //初始化一个chrome浏览器实例driver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("no-sandbox");
        options.addArguments("disable-gpu");
        options.addArguments("disable-features=NetworkService");
        options.addArguments("ignore-certificate-errors");
        options.addArguments("silent-launch");
        options.addArguments("disable-application-cache");
        options.addArguments("disable-web-security");
        options.addArguments("no-proxy-server");
        options.addArguments("disable-dev-shm-usage");
        options.addArguments("lang=zh_CN.UTF-8");
        WebDriver driver = null;
        try {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);
            driver = new ChromeDriver(options);
            driver.get(headlessRequest.url);
            driver.manage().window().fullscreen();
        }catch (Exception e){
            if(driver != null){
                driver.quit();
                driver = null;
            }
            LogUtil.error(e);
        }
        return  driver;
    }

    public String getImageInfo(HeadlessRequest request){
        WebDriver driver = this.genWebDriver(request);
        String files = null;
        if(driver != null){
            try {
                //预留echart动画的加载时间
                Thread.sleep(3 * 1000);
                String js = "var chartsCanvas = document.getElementById('picChart').getElementsByTagName('canvas')[0];" +
                        "var imageUrl = null;" +
                        "if (chartsCanvas!= null) {" +
                        " imageUrl = chartsCanvas && chartsCanvas.toDataURL('image/png');"+
                        "return imageUrl;" +
                        "}";
                files = ((JavascriptExecutor)driver).executeScript(js).toString();
            }catch (Exception e){
                LogUtil.error(e);
            }finally {
                driver.quit();
            }
        }
        if(StringUtils.isNotEmpty(files)){
            return files;
        }else {
            return null;
        }
    }
}
