# 构建过程

### 基础POM

```bash
# 此命令会将parent pom 安装到本地仓库，其他外部子工程可以获得最新的 <properties></properties>
./mvnw install -N

# 此命令会将 domain sdk ，其他外部子工程可以获得最新的 jar
./mvnw clean install -DskipTests -DskipAntRunForJenkins --file backend/pom.xml

```

### 整体打包

```bash
./mvnw clean package
```
