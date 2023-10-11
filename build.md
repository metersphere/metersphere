# 构建过程

### 基础POM

```bash
# 此命令会将parent pom 安装到本地仓库，其他外部子工程可以获得最新的 <properties></properties>
./mvnw install -N

# 此命令会将 domain sdk xpack-interface ms-jmeter-core，其他外部子工程可以获得最新的 jar
./mvnw clean install -pl framework,framework/sdk-parent,framework/sdk-parent/domain,framework/sdk-parent/sdk,framework/sdk-parent/xpack-interface,framework/sdk-parent/jmeter
```

### 整体打包

```bash
./mvnw clean package
mvn clean package -DskipTests -DskipAntRunForJenkins -pl "!framework/sdk-parent/frontend,!api-test/frontend,!performance-test/frontend,!project-management/frontend,!report-stat/frontend,!system-setting/frontend,!test-track/frontend,!workstation/frontend"
```
