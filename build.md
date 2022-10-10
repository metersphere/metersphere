# 构建过程

### 基础POM

```bash
# parent pom 安装到本地仓库, sdk 也进行安装
./mvnw install -N
./mvnw clean install -pl framework,framework/sdk-parent,framework/sdk-parent/domain,framework/sdk-parent/sdk,framework/sdk-parent/xpack-interface
```

### 整体打包

```bash
./mvnw clean package
```
