<p align="center"><a href="https://metersphere.io"><img src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/MeterSphere-%E7%B4%AB%E8%89%B2.png" alt="MeterSphere" width="300" /></a></p>
<h3 align="center">现代化、开源的测试管理和接口测试工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://shields.io/github/license/metersphere/metersphere" alt="License: GPL v3"></a>
  <a href="https://www.codacy.com/gh/metersphere/metersphere/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=metersphere/metersphere&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/metersphere/metersphere/releases"><img src="https://img.shields.io/github/v/release/metersphere/metersphere" alt="GitHub release"></a>
  <a href="https://github.com/metersphere/metersphere"><img src="https://img.shields.io/github/stars/metersphere/metersphere?color=%231890FF&style=flat-square" alt="Stars"></a>
</p>
<hr />

MeterSphere 是新一代的测试管理和接口测试工具，让测试工作更简单、更高效，不再成为持续交付的瓶颈。

-   **测试管理**: 从测试用例管理，到测试计划执行、缺陷管理、测试报告生成，具有远超禅道和 TestLink 的使用体验；
-   **接口测试**: 集 Postman 的易用与 JMeter 的灵活于一体，接口定义、接口调试、接口 Mock、场景自动化、接口报告，你想要的都有。

## 快速开始

```
docker run -d -p 8081:8081 --name=metersphere -v ~/.metersphere/data:/opt/metersphere/data registry.fit2cloud.com/metersphere/metersphere-ce-allinone:v3.0.0-beta

# 用户名: admin
# 密码: metersphere
```

你也可以通过 [1Panel 应用商店](https://1panel.cn/) 快速部署 MeterSphere。如果是内网环境，推荐使用 [离线安装包方式](https://community.fit2cloud.com/#/products/metersphere/downloads) 进行安装部署。

如你有更多问题，可以通过论坛和技术交流群与我们交流。

-   [论坛求助](https://bbs.fit2cloud.com/c/ms/8)
-   技术交流群     
    <image height="150px" width="150px" src="https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/wechat-group.png"/>

## UI 展示

<table style="border-collapse: collapse; border: 1px solid black;">
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/7bcdd73e-7010-4468-8510-00b45cd06a50" alt="MeterSphere Demo1" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/bc06bf43-169e-414a-8a1d-a52888025179" alt="MeterSphere Demo2" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/5fd7a3cd-5689-42fa-9efa-b5303a488933" alt="MeterSphere Demo3" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/ca30824e-3315-4823-ae6f-e7e68c89df34" alt="MeterSphere Demo4" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/ad9f5dab-3d60-434c-b335-78ecfe8ec5c4" alt="MeterSphere Demo5" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/f2523565-228d-4691-890e-eda5add7a46b" alt="MeterSphere Demo6" /></td>
  </tr>
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/7c68c936-e411-488e-ac6b-d90533d2f52a" alt="MeterSphere Demo7" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/maninhill/metersphere/assets/23045261/9d7ba30e-b461-4918-9fb6-d16a650e71be" alt="MeterSphere Demo8" /></td>
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
- 2024 年 6 月 27 日: 发布 v3.0 正式版本
- 2024 年 12 月 26 日：发布 v3.6-lts LTS 版本。

## 技术栈

-   后端: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   前端: [Vue.js](https://vuejs.org/)
-   中间件: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/), [MinIO](https://min.io/), [Redis](https://redis.com/)
-   基础设施: [Docker](https://www.docker.com/)
-   测试引擎: [JMeter](https://jmeter.apache.org/)

## 我们的其他明星开源项目

- [1Panel](https://github.com/1panel-dev/1panel/) - 现代化、开源的 Linux 服务器运维管理面板
- [JumpServer](https://github.com/jumpserver/jumpserver/) - 广受欢迎的开源堡垒机
- [DataEase](https://github.com/dataease/dataease/) - 人人可用的开源数据可视化分析工具
- [MaxKB](https://github.com/1panel-dev/maxkb) - 基于 LLM 大语言模型的知识库问答系统
- [Halo](https://github.com/halo-dev/halo/) - 强大易用的开源建站工具

## License & Copyright

Copyright (c) 2014-2024 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
