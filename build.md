# 构建过程

### 基础POM

```bash
# parent pom 安装到本地仓库, sdk 也进行安装
./mvnw install -N
./mvnw clean install -pl framework,framework/sdk-parent,framework/sdk-parent/domain,framework/sdk-parent/sdk,framework/sdk-parent/xpack-interface
```

### xPack 部分(可选)

```bash
# metersphere-xpack
# 需要在IDEA里配置library，让xpack的jar出现在classpath里
./mvnw clean package
```

### 整体打包

```bash
./mvnw clean package
```

# UI和Workstation模块开发

```bash
# 现在主工程里把基础POM部分执行后再执行
./mvnw clean package

```
