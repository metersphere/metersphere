# 工程主体

- 开源部分主体都在本工程里
- ui模块在 git@github.com:metersphere/ui-test.git
- 工作台模块 git@github.com:metersphere/workstation.git
- xpack杂项 git@github.com:metersphere/metersphere-xpack.git

# 构建过程

### 基础POM

```bash
# 配置npm镜像仓库
npm config set registry https://npm.fit2cloud.com
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

# 前端

- 组件迁移，大多数的公共组件和js方法已经移到sdk-frontend中了，后面再提取新的公共组件
- 分离ajax和组件，每个工程都有一个api文件夹，放置ajax，组件里不再直接写url
- 不再使用vuex，使用pinia代替，可以自行配置持久化位置

# 后端

- 代码分离不同模块之间不再相互引入
- 公共service dto 提到sdk-backend里
- 模块内service也避免相互注入（尽管@Lazy可以避免报错），推荐提取出关联service

# Q&A

1. 前端拆分服务的必要性
    - 后端拆分成微服务这个已经形成共识，这里不再进行讨论。
    - 前端代码是否进行拆分这里做一些统一说明：
        1. 拆分服务的目的就是让各个模块的耦合度降低，权责清晰。当我们的App复杂度上升后，一个很小的改动都可能影响到整个系统的稳定行，其他模块莫名其妙的就不好用了
        2. 前后分离开发后，我们没有统一的前端开发人员负责整个系统的前端。这使得我们每一个开发人员都要从前端展示到后端实现负责到底，这样可以减少沟通成本，开发人员灵活控制开发进度
        3. 模块内提升内聚性，这不仅是后端java代码，前端vue代码同样如此。尽管这样会产生一些重复的代码，从长远来看这些是必要的

2. 不再引入 Mybatis-Plus
    - 我们现在用原生的Mybatis Mapper 完成了基础查询，再引入一个Mapper-plus 会使得代码中同时存在两种不同的操作数据库的方式，这样增加我们代码的维护成本
    - 如果我们将基础Mapper全部替换成Mapper-plus又会增加迁移难度，我们不仅需要测试功能还需要验证新组件的带来的新问题

