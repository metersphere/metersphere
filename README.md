<p align="center"><a href="https://metersphere.io"><img src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/MeterSphere-%E7%B4%AB%E8%89%B2.png" alt="MeterSphere" width="300" /></a></p>
<h3 align="center">一站式开源持续测试平台</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0"><img src="https://img.shields.io/github/license/metersphere/metersphere?color=%231890FF&style=flat-square" alt="License: GPL v2"></a>
  <a href="https://app.codacy.com/gh/metersphere/metersphere?utm_source=github.com&utm_medium=referral&utm_content=metersphere/metersphere&utm_campaign=Badge_Grade_Dashboar"><img src="https://api.codacy.com/project/badge/Grade/176186d132df448b955f8bdd5e6ef9c0" alt="Codacy"></a>
  <a href="https://github.com/metersphere/metersphere/releases/latest"><img src="https://img.shields.io/github/downloads/metersphere/metersphere/total" alt="Latest release"></a>
  <a href="https://github.com/metersphere/metersphere"><img src="https://img.shields.io/github/stars/metersphere/metersphere?color=%231890FF&style=flat-square" alt="Stars"></a>
</p>
<hr />

MeterSphere `/ˈmitərˌsfɪər/` 是一站式开源持续测试平台，涵盖测试跟踪、接口测试、性能测试、团队协作等功能，兼容JMeter 等开源标准，有效助力开发和测试团队充分利用云弹性进行高度可扩展的自动化测试，加速高质量软件的交付。

### MeterSphere 的功能

-   **测试跟踪**: 远超 TestLink 的使用体验，覆盖从编写用例到生成测试报告的完整流程；
-   **接口测试**: 集 Postman 的易用与 JMeter 的灵活于一体，接口管理、接口 Mock、多协议支持、场景自动化，你想要的全都有；
-   **性能测试**: 兼容 JMeter，支持 Kubernetes 和云环境，轻松支持高并发、分布式的性能测试；
-   **团队协作**: 用户管理、租户管理、权限管理、资源管理，无论团队规模如何，总有适合的落地方式。

### MeterSphere 的优势

-   **开源**：基于开源、兼容开源；按月发布新版本、日均下载安装超过100次、被大量客户验证；
-   **一站式**：一个产品全面涵盖测试跟踪、接口测试、性能测试等功能并形成联动：其中用例管理是底座需求、接口自动化测试是高频需求、性能测试是专家服务为主工具为辅；一个产品全满足从测试计划、测试执行到测试报告分析的全生命周期需求；
-   **持续测试**：能将测试融入持续交付和 DevOps 体系；无缝对接 Bug 管理工具和持续集成工具等；支持团队协作和资产沉淀。

### UI 展示

![UI展示](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/UIdemo.gif)

> 如需进一步了解 MeterSphere 开源项目，推荐阅读 [MeterSphere 的初心和使命](https://mp.weixin.qq.com/s/DpCt3BNgBTlV3sJ5qtPmZw)

### 功能架构

![产品定位](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/ct-devops.png)

### 在线体验

-   环境地址：<https://demo.metersphere.com/>
-   用户名：demo
-   密码：P@ssw0rd123..

| :warning: 注意                 |
| :--------------------------- |
| 该环境仅作体验目的使用，我们会定时清理、重置数据！    |
| 请勿修改体验环境用户的密码！               |
| 请勿在环境中添加业务生产环境地址、用户名密码等敏感信息！ |

### 快速开始

仅需两步快速安装 MeterSphere：

1.  准备一台不小于 8 G内存的 64位 Linux 主机；
2.  以 root 用户执行如下命令一键安装 MeterSphere。

```sh
curl -sSL https://github.com/metersphere/metersphere/releases/latest/download/quick_start.sh | sh
```

文档和演示视频：

-   [完整文档](https://metersphere.io/docs/)
-   [演示视频](https://www.bilibili.com/video/BV1yp4y1p72C/)

### 社区

如果您在使用过程中有任何疑问或对建议，欢迎提交 [GitHub Issue](https://github.com/metersphere/metersphere/issues/new/choose) 或加入到我们的社区当中进行进一步交流沟通。

#### 微信交流群

<img src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/wechat-group.png" alt="微信群二维码" width="200"/>

### 版本说明

MeterSphere 版本号命名规则为：v大版本.功能版本.Bug修复版本。比如：

```text
v1.0.1 是 v1.0.0 之后的Bug修复版本；
v1.1.0 是 v1.0.0 之后的功能版本。
```

像其它优秀开源项目一样，MeterSphere 将每月发布一个功能版本。

MeterSphere v1.10 版本为该项目的第一个LTS（Long Term Support）版本。针对这一版本，MeterSphere开源项目组将对其用户进行长期支持，并且在主线功能版本的基础之上，按需发布LTS版本的问题修复更新，旨在为用户提供更加稳定和高质量的软件使用体验。

### 技术栈

-   后端: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   前端: [Vue.js](https://vuejs.org/)
-   中间件: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/)
-   基础设施: [Docker](https://www.docker.com/), [Kubernetes](https://kubernetes.io/)
-   测试引擎: [JMeter](https://jmeter.apache.org/)

### 致谢

-   [BlazeMeter](https://www.blazemeter.com/)：感谢 BlazeMeter 提供的设计思路
-   [JMeter](https://jmeter.apache.org/)：MeterSphere 使用了 JMeter 作为测试引擎
-   [Element](https://element.eleme.cn/#/)：感谢 Element 提供的优秀组件库

### License & Copyright

Copyright (c) 2014-2021 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 2 (GPLv2)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-2.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
