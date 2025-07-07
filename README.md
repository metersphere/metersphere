<p align="center"><a href="https://metersphere.io"><img src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/MeterSphere-%E7%B4%AB%E8%89%B2.png" alt="MeterSphere" width="300" /></a></p>
<h3 align="center">新一代的开源持续测试工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://shields.io/github/license/metersphere/metersphere?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://www.codacy.com/gh/metersphere/metersphere/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=metersphere/metersphere&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/metersphere/metersphere/releases"><img src="https://img.shields.io/github/v/release/metersphere/metersphere" alt="GitHub release"></a>
  <a href="https://github.com/metersphere/metersphere"><img src="https://img.shields.io/github/stars/metersphere/metersphere?color=%231890FF&style=flat-square" alt="Stars"></a>
  <a href="https://hub.docker.com/r/metersphere/metersphere-ce-allinone"><img src="https://img.shields.io/docker/pulls/metersphere/metersphere-ce-allinone?label=downloads" alt="Download"></a>
  <a href="https://gitee.com/fit2cloud-feizhiyun/MeterSphere"><img src="https://gitee.com/fit2cloud-feizhiyun/MeterSphere/badge/star.svg?theme=gvp" alt="Gitee Stars"></a>
  <a href="https://gitcode.com/feizhiyun/MeterSphere"><img src="https://gitcode.com/feizhiyun/MeterSphere/star/badge.svg" alt="GitCode Stars"></a><br>
</p>
<hr />

MeterSphere 是新一代的开源持续测试工具，让软件测试工作更简单、更高效，不再成为持续交付的瓶颈。

-   **测试管理**：从测试用例管理，到测试计划执行、缺陷管理、测试报告生成，具有远超 TestLink 等传统测试管理工具的使用体验；
-   **接口测试**：集 Postman 的易用与 JMeter 的灵活于一体，接口调试、接口定义、接口 Mock、场景自动化、接口报告，你想要的都有；
-   **团队协作**：采用“系统-组织-项目”分层设计理念，帮助用户摆脱单机测试工具的束缚，方便快捷地开展团队协作；
-   **插件体系**：提供各种类别的插件，用户可以按需取用，快速实现 MeterSphere 测试能力的扩展以及与 DevOps 流水线的集成。

## 快速开始

```
docker run -d -p 8081:8081 --name=metersphere -v ~/.metersphere/data:/opt/metersphere/data metersphere/metersphere-ce-allinone

# 用户名: admin
# 密码: metersphere
```

你也可以通过 [1Panel 应用商店](https://1panel.cn/) 快速部署 MeterSphere。

如果是内网环境，推荐使用 [离线安装包方式](https://community.fit2cloud.com/#/products/metersphere/downloads) 进行安装部署。

如你有更多问题，可以通过论坛和技术交流群与我们交流。

-   [案例研究](/use-cases.md)
-   [论坛求助](https://bbs.fit2cloud.com/c/ms/8)
-   技术交流群
     

![image](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/wechat-helper.png)


## UI 展示

<table style="border-collapse: collapse; border: 1px solid black;">
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/e330db63-ea48-43b5-9645-b143c3326632" alt="MeterSphere Demo1" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/315a13f6-6565-498d-ab62-6d5b46d49591" alt="MeterSphere Demo2" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/785f7c05-430c-4eab-a0c5-0661bc177df0" alt="MeterSphere Demo3" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/a53dd241-0140-43e4-83ba-95f0f0aeccc5" alt="MeterSphere Demo4" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/fc09f2bc-a822-4c8c-ba58-c8e55f362fa3" alt="MeterSphere Demo5" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/ed689d96-78fc-4e21-a29b-49054291dc59" alt="MeterSphere Demo6" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/8b468704-3741-4f73-a86c-f224f15aeba2" alt="MeterSphere Demo7" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/metersphere/metersphere/assets/23045261/023dad1b-37c6-480c-a32e-4c71dd1010d2" alt="MeterSphere Demo8" /></td>
  </tr>
</table>

## 版本说明

MeterSphere 按年发布 LTS（Long Term Support）版本。

- v1.10-lts：发布时间为 2021 年 5 月 27 日，目前已经停止维护；
- v1.20-lts：发布时间为 2022 年 4 月 27 日，目前已经停止维护；
- v2.10-lts：发布时间为 2023 年 5 月 25 日，仅进行必要的安全类 Bug 修复和严重 Bug 修复。

与 MeterSphere v1.x 和 v2.x 相比，MeterSphere v3.x 产品定位发生变化，聚焦做好测试管理和接口测试，不再提供性能测试和 UI 测试相关的功能和能力，也不支持从 v1.x 和 v2.x 版本升级到 v3.x。

MeterSphere v3.x 的版本发布计划：

- 2024 年 5 月 30 日：发布 v3.0 beta 版本；
- 2024 年 6 月 27 日：发布 v3.0 正式版本；
- 2024 年 12 月 26 日：发布 v3.6-lts LTS 版本。

MeterSphere 产品版本分为社区版和企业版，详情请参见：[MeterSphere 产品版本对比](https://metersphere.io/v3/pricing.html)

## 技术栈

-   后端: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   前端: [Vue.js](https://vuejs.org/)
-   中间件: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/), [MinIO](https://min.io/), [Redis](https://redis.com/)
-   基础设施: [Docker](https://www.docker.com/)
-   测试引擎: [JMeter](https://jmeter.apache.org/)

## 插件

- [TAPD](https://www.tapd.cn/) 需求和缺陷同步插件：通过调用 TAPD 企业版本提供的 API 接口（[TAPD 企业版（API）申请](https://jsj.top/f/Lpk1sh)），将 MeterSphere 的测试用例和 TAPD 需求项进行关联，以及将 MeterSphere 和 TAPD 两个应用的缺陷进行双向同步。
- 禅道需求和缺陷同步插件：将 MeterSphere 的测试用例和禅道需求项进行关联，以及将 MeterSphere 和禅道两个应用的缺陷进行双向同步。
- JIRA 需求和缺陷同步插件：将 MeterSphere 的测试用例和 JIRA 需求项进行关联，以及将 MeterSphere 和 JIRA 两个应用的缺陷进行双向同步。
- [Jenkins](https://www.jenkins.io/) 持续集成插件：实现在 Jenkins 流水线中触发并自动执行 MeteSphere 测试计划。
- 自定义数据库驱动：支持对 达梦、Oracle、SQLite、Microsoft SQL Server 等数据库的连接和数据访问。
- 接口协议插件：实现接口测试中对 TCP、Dubbo、MQTT 等协议的支持。
- IDE 插件：[MeterSphere APl Debugger 插件](https://github.com/metersphere/metersphere-idea-plugin/) 是 MeterSphere 提供的 InteliJ IDEA 插件，它可以快速提取 API 特征，实时进行 API 调试并生成 API 文档，一键同步到 MeterSphere 进行管理。

## 飞致云的其他明星项目

- [1Panel](https://github.com/1panel-dev/1panel/) - 现代化、开源的 Linux 服务器运维管理面板
- [MaxKB](https://github.com/1panel-dev/maxkb) - 基于 LLM 大语言模型的知识库问答系统
- [JumpServer](https://github.com/jumpserver/jumpserver/) - 广受欢迎的开源堡垒机
- [DataEase](https://github.com/dataease/dataease/) - 人人可用的开源数据可视化分析工具
- [Halo](https://github.com/halo-dev/halo/) - 强大易用的开源建站工具

## License & Copyright

Copyright (c) 2014-2025 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
