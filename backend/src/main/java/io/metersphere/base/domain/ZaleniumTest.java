package io.metersphere.base.domain;

public class ZaleniumTest {

    private String seleniumSessionId;
    private String testName;
    private String timestamp;
    private String addedToDashboardTime;
    private String browser;
    private String browserVersion;
    private String proxyName;
    private String platform;
    private String fileName;
    private String fileExtension;
    private String videoFolderPath;
    private String logsFolderPath;
    private String testNameNoExtension;
    private String screenDimension;
    private String timeZone;
    private String build;
    private String testFileNameTemplate;
    private String browserDriverLogFileName;
    private String retentionDate;
    private String testStatus;
    private boolean videoRecorded;

    public String getSeleniumSessionId() {
        return seleniumSessionId;
    }

    public void setSeleniumSessionId(String seleniumSessionId) {
        this.seleniumSessionId = seleniumSessionId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddedToDashboardTime() {
        return addedToDashboardTime;
    }

    public void setAddedToDashboardTime(String addedToDashboardTime) {
        this.addedToDashboardTime = addedToDashboardTime;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getVideoFolderPath() {
        return videoFolderPath;
    }

    public void setVideoFolderPath(String videoFolderPath) {
        this.videoFolderPath = videoFolderPath;
    }

    public String getLogsFolderPath() {
        return logsFolderPath;
    }

    public void setLogsFolderPath(String logsFolderPath) {
        this.logsFolderPath = logsFolderPath;
    }

    public String getTestNameNoExtension() {
        return testNameNoExtension;
    }

    public void setTestNameNoExtension(String testNameNoExtension) {
        this.testNameNoExtension = testNameNoExtension;
    }

    public String getScreenDimension() {
        return screenDimension;
    }

    public void setScreenDimension(String screenDimension) {
        this.screenDimension = screenDimension;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getTestFileNameTemplate() {
        return testFileNameTemplate;
    }

    public void setTestFileNameTemplate(String testFileNameTemplate) {
        this.testFileNameTemplate = testFileNameTemplate;
    }

    public String getBrowserDriverLogFileName() {
        return browserDriverLogFileName;
    }

    public void setBrowserDriverLogFileName(String browserDriverLogFileName) {
        this.browserDriverLogFileName = browserDriverLogFileName;
    }

    public String getRetentionDate() {
        return retentionDate;
    }

    public void setRetentionDate(String retentionDate) {
        this.retentionDate = retentionDate;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public boolean isVideoRecorded() {
        return videoRecorded;
    }

    public void setVideoRecorded(boolean videoRecorded) {
        this.videoRecorded = videoRecorded;
    }
}
