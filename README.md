<p align="center"><a href="https://metersphere.io"><img src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/MeterSphere-%E7%B4%AB%E8%89%B2.png" alt="MeterSphere" width="300" /></a></p>
<h3 align="center">一站式开源持续测试平台</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://shields.io/github/license/metersphere/metersphere" alt="License: GPL v3"></a>
  <a href="https://www.codacy.com/gh/metersphere/metersphere/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=metersphere/metersphere&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/metersphere/metersphere/releases"><img src="https://img.shields.io/github/v/release/metersphere/metersphere" alt="GitHub release"></a>
  <a href="https://github.com/metersphere/metersphere/releases/latest"><img src="https://img.shields.io/github/downloads/metersphere/metersphere/total" alt="Latest release"></a>
  <a href="https://github.com/metersphere/metersphere"><img src="https://img.shields.io/github/stars/metersphere/metersphere?color=%231890FF&style=flat-square" alt="Stars"></a>
</p>
<hr />

MeterSphere 是一站式开源持续测试平台, 涵盖测试跟踪、接口测试、UI 测试和性能测试等功能，全面兼容 JMeter、Selenium 等主流开源标准，有效助力开发和测试团队充分利用云弹性进行高度可扩展的自动化测试，加速高质量的软件交付，推动中国测试行业整体效率的提升。

### MeterSphere 的功能

-   **测试跟踪**: 对接主流项目管理平台，测试过程全链路跟踪管理；列表脑图模式自由切换，用例编写更简单、测试报告更清晰；
-   **接口测试**: 比 JMeter 易用，比 Postman 强大； API 管理、Mock 服务、场景编排、多协议支持，你想要的全都有；
-   **UI 测试**: 基于 Selenium 浏览器自动化，高度可复用的测试脚本； 无需复杂的代码编写，人人都可开展的低代码自动化测试；
-   **性能测试**: 兼容 JMeter 的同时补足其分布式、监控与报告以及管理短板; 轻松帮助团队实现高并发、分布式的性能压测，完成压测任务的统一调度与管理。

### MeterSphere 的优势

-   **开源**：基于开源、兼容开源；按月发布新版本、日均下载安装超过100次、被大量客户验证；
-   **一站式**：一个产品全面涵盖测试跟踪、接口测试、UI测试、性能测试等功能并形成联动；
-   **全生命周期**：一个产品全满足从测试计划、测试执行到测试报告分析的全生命周期需求；
-   **持续测试**：无缝对接 Bug 管理工具和持续集成工具等，能将测试融入持续交付和 DevOps 体系；
-   **团队协作**：支持团队协作和资产沉淀，无论团队规模如何，总有适合的落地方式。

### UI 展示

![UI展示](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/UIdemo.gif)

> 如需进一步了解 MeterSphere 开源项目，推荐阅读 [MeterSphere 的初心和使命](https://mp.weixin.qq.com/s/DpCt3BNgBTlV3sJ5qtPmZw)

### 功能架构

![产品定位](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/ms-architecture.png)

### 在线体验

> 2022 年 5 月 5 日，MeterSphere 专业测试云（Beta）正式上线！

**[免费试用](https://www.metersphere.com/)**

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

像其它优秀开源项目一样，MeterSphere 将按月发布功能版本、按年度发布 LTS（Long Term Support）版本。

MeterSphere 的最新 LTS 版本为 v1.20 LTS。MeterSphere 下一个 LTS 版本为 v2.10 LTS，预计在 2023 年第二季度发布。

### 技术栈

-   后端: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   前端: [Vue.js](https://vuejs.org/)
-   中间件: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/)
-   基础设施: [Docker](https://www.docker.com/), [Kubernetes](https://kubernetes.io/)
-   测试引擎: [JMeter](https://jmeter.apache.org/)

### 安全说明

如果您在使用过程中发现任何安全问题，请通过以下方式直接联系我们：

- 邮箱：support@fit2cloud.com 
- 电话：400-052-0755

### 致谢

-   [BlazeMeter](https://www.blazemeter.com/)：感谢 BlazeMeter 提供的设计思路
-   [JMeter](https://jmeter.apache.org/)：MeterSphere 使用了 JMeter 作为测试引擎
-   [Element](https://element.eleme.cn/#/)：感谢 Element 提供的优秀组件库

### License & Copyright

Copyright (c) 2014-2022 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
