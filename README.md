# MeterSphere 一站式开源持续测试平台 

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/176186d132df448b955f8bdd5e6ef9c0)](https://app.codacy.com/gh/metersphere/metersphere?utm_source=github.com&utm_medium=referral&utm_content=metersphere/metersphere&utm_campaign=Badge_Grade_Dashboard)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/metersphere/metersphere)](https://github.com/metersphere/metersphere/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/metersphere/metersphere/total)](https://github.com/metersphere/metersphere/releases)
[![TesterHome](https://img.shields.io/badge/TTF-TesterHome-2955C5.svg)](https://testerhome.com/github_statistics)

> [English](README-EN.md) | 中文

| 《持续测试白皮书 v1.0》成功发布                                                                                            |
| ------------------------------------------------------------------------------------------------------------ |
| “软件质量报道”公众号和MeterSphere开源项目组历时四个月，结合自己的经验和业界各方面的专家反馈，完成《持续测试白皮书 v1.0》的编写工作。期待本白皮书可以帮助业界更多企业和专业用户在日常工作中更好地将“持续测试”理念付诸实践。下载链接： [https://jinshuju.net/f/KqFUhq](https://jinshuju.net/f/KqFUhq) |

MeterSphere 是一站式开源持续测试平台，涵盖测试跟踪、接口测试、性能测试、团队协作等功能，兼容JMeter 等开源标准，有效助力开发和测试团队充分利用云弹性进行高度可扩展的自动化测试，加速高质量软件的交付。

-   测试跟踪: 远超 TestLink 的使用体验，覆盖从编写用例到生成测试报告的完整流程；
-   接口测试: 集 Postman 的易用与 JMeter 的灵活于一体，接口管理、接口 Mock、多协议支持、场景自动化，你想要的全都有；
-   性能测试: 兼容 JMeter，支持 Kubernetes 和云环境，轻松支持高并发、分布式的性能测试；
-   团队协作: 用户管理、租户管理、权限管理、资源管理，无论团队规模如何，总有适合的落地方式。

![产品定位](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/ct-devops.png)

> 如需进一步了解 MeterSphere 开源项目，推荐阅读 [MeterSphere 的初心和使命](https://mp.weixin.qq.com/s/DpCt3BNgBTlV3sJ5qtPmZw)

## 在线体验

-   环境地址：<https://demo.metersphere.com/>
-   用户名：demo
-   密码：P@ssw0rd123..

| :warning: 注意                 |
| :--------------------------- |
| 该环境仅作体验目的使用，我们会定时清理、重置数据！    |
| 请勿修改体验环境用户的密码！               |
| 请勿在环境中添加业务生产环境地址、用户名密码等敏感信息！ |

## 快速开始

仅需两步快速安装 MeterSphere：

1.  准备一台不小于 8 G内存的 64位 Linux 主机；
2.  以 root 用户执行如下命令一键安装 MeterSphere。

```sh
curl -sSL https://github.com/metersphere/metersphere/releases/latest/download/quick_start.sh | sh
```

文档和演示视频：

-   [完整文档](https://metersphere.io/docs/)
-   [演示视频](http://video.fit2cloud.com/%E3%80%90%E6%BC%94%E7%A4%BA%E8%A7%86%E9%A2%91%E3%80%91202006%20MeterSphere%20v1.0%20%E5%8A%9F%E8%83%BD%E6%BC%94%E7%A4%BA.mp4)

## MeterSphere 企业版

[申请企业版使用](https://jinshuju.net/f/CzzAOe)

> 注: 企业版支持离线安装，申请通过后会提供高速下载链接

## 相关工具

-   [Jenkins 插件](https://github.com/metersphere/jenkins-plugin)
-   [浏览器插件](https://github.com/metersphere/chrome-extensions)

## 版本说明

MeterSphere 版本号命名规则为：v大版本.功能版本.Bug修复版本。比如：

```text
v1.0.1 是 v1.0.0 之后的Bug修复版本；
v1.1.0 是 v1.0.0 之后的功能版本。
```

像其它优秀开源项目一样，MeterSphere 将每月发布一个功能版本。

## 产品优势

-   开源：基于开源、兼容开源；按月发布新版本、日均下载安装超过100次、被大量客户验证；
-   一站式：一个产品全面涵盖测试跟踪、接口测试、性能测试等功能并形成联动：其中用例管理是底座需求、接口自动化测试是高频需求、性能测试是专家服务为主工具为辅；一个产品全满足从测试计划、测试执行到测试报告分析的全生命周期需求；
-   持续测试：能将测试融入持续交付和 DevOps 体系；无缝对接 Bug 管理工具和持续集成工具等；支持团队协作和资产沉淀。

## 功能列表

 </head>
 <body link="blue" vlink="purple" class="xl65">
  <table width="505.15" border="0" cellpadding="0" cellspacing="0" style='width:505.15pt;border-collapse:collapse;table-layout:fixed;'>
   <col width="73" class="xl65" style='mso-width-source:userset;mso-width-alt:3114;'/>
   <col width="95.55" class="xl65" style='mso-width-source:userset;mso-width-alt:4076;'/>
   <col width="336.60" class="xl65" style='mso-width-source:userset;mso-width-alt:14361;'/>
   <col width="91.55" span="253" class="xl65" style='mso-width-source:userset;mso-width-alt:3906;'/>
   <tr height="22.55" style='height:22.55pt;mso-height-source:userset;mso-height-alt:451;'>
    <td class="xl68" height="424.85" width="73" rowspan="19" style='height:424.85pt;width:73.00pt;border-right:.5pt solid #3F3F3F;border-bottom:.5pt solid #A5A5A5;' x:str>测试跟踪</td>
    <td class="xl76" width="95.55" rowspan="8" style='width:95.55pt;border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试用例管理</td>
    <td class="xl70" width="336.60" style='width:336.60pt;' x:str>在线编辑用例</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl77" x:str>编辑窗口支持上传附件</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>查看与编辑窗口显示评审评论</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>以树状形式展示项目的模块及其用例</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持测试用例模块树拖拽排序</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>自定义用例等级/用例类型/测试方式</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持Excel/Xmind格式快速导入用例到系统</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持Excel格式快速导出用例到本地</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="4" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试用例评审</td>
    <td class="xl73" x:str>基于已有用例发起评审</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持添加多个评审人</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>在线更新评审结果</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持多人在线添加评审评论</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="7" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试计划跟踪</td>
    <td class="xl73" x:str>基于已有用例发起测试计划</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持在线更新用例执行结果</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>在线生成测试报告，支持自定义测试报告模板</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>与平台中的接口测试、性能测试功能联动，自动更新关联用例的结果</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>记录测试用例关联的缺陷</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>缺陷记录支持关联到 Jira/TAPD</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持PDF格式测试报告导出</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl75" height="603.45" rowspan="27" style='height:603.45pt;border-right:.5pt solid #3F3F3F;border-bottom:.5pt solid #A5A5A5;' x:str>接口测试</td>
    <td class="xl72" rowspan="13" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>接口定义</td>
    <td class="xl73" x:str>在线编辑接口测试内容</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持 HTTP/Dubbo/SQL/TCP 类型接口请求</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持接口快捷调制</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持接口列表和用例列表切换显示</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持用例编辑窗口正则/jsonpath/Xpath等多种类型的断言规则</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持用例编辑窗口正则/jsonpath/Xpath类型的参数提取</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持用例编辑窗口前后置 BeanShell/Python 脚本</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>测试环境信息管理</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持单接口测试引用环境信息</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持通过浏览器插件快速录制测试脚本</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持Metersphere json/Postman/Swagger格式快速导入用例到系统</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持Metersphere json格式快速导出用例到本地</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持上传并引用自定义 Jar 包</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="12" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>接口自动化</td>
    <td class="xl73" x:str>创建多接口的场景化测试</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持自定义场景标签</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持多层级场景嵌套结构</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持接口列表快速导入测试场景</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持测试场景复用</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持添加自定义请求/自定义脚本</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持添加等待时间/条件判断等多类型逻辑控制器</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>场景调试支持引用已有环境信息</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持定时任务</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持通过 Jenkins 插件触发测试执行</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>Jenkins 插件支持 Pipeline 方式调用</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持一键创建性能测试</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="2" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试报告</td>
    <td class="xl73" x:str>测试执行后自动生成测试报告</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持PDF格式测试报告导出</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl75" height="178.80" rowspan="8" style='height:178.80pt;border-right:.5pt solid #3F3F3F;border-bottom:.5pt solid #A5A5A5;' x:str>性能测试</td>
    <td class="xl72" rowspan="4" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>性能测试脚本</td>
    <td class="xl73" x:str>支持上传JMX/CSV/JAR格式文件创建性能测试</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持分线程组配置压力参数</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持下载 JTL 文件</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持通过浏览器插件快速录制测试脚本</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="2" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试执行</td>
    <td class="xl73" x:str>内置定时任务支持</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持通过 Jenkins 插件触发测试执行</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="2" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>测试报告</td>
    <td class="xl73" x:str>测试执行后自动生成动态实时测试报告</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持PDF格式测试报告导出</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl75" height="223.50" rowspan="10" style='height:223.50pt;border-right:.5pt solid #3F3F3F;border-bottom:.5pt solid #A5A5A5;' x:str>系统管理</td>
    <td class="xl72" rowspan="3" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>用户租户管理</td>
    <td class="xl73" x:str>支持多级租户体系</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持多种租户角色</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持LDAP 认证对接</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" x:str>测试资源管理</td>
    <td class="xl73" x:str>性能测试资源池管理</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="2" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>消息通知配置</td>
    <td class="xl73" x:str>支持企业微信/钉钉等多种IM 工具通知配置</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持邮件通知配置</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" rowspan="3" style='border-right:.5pt solid #A5A5A5;border-bottom:.5pt solid #A5A5A5;' x:str>集成与扩展</td>
    <td class="xl73" x:str>配置API 列表</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持对接 Jenkins 等持续集成工具</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl73" x:str>支持对接 Jira/TAPD 等缺陷管理工具</td>
   </tr>
   <tr height="22.35" style='height:22.35pt;mso-height-source:userset;mso-height-alt:447;'>
    <td class="xl72" x:str>项目管理</td>
    <td class="xl73" x:str>多项目支持，测试用例、测试计划与项目关联</td>
   </tr>
  </table>
 </body>

详细的版本规划请参考 [版本路线图](https://github.com/metersphere/metersphere/blob/master/ROADMAP.md)

## 技术栈

-   后端: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   前端: [Vue.js](https://vuejs.org/)
-   中间件: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/)
-   基础设施: [Docker](https://www.docker.com/), [Kubernetes](https://kubernetes.io/)
-   测试引擎: [JMeter](https://jmeter.apache.org/)

## 致谢

-   [BlazeMeter](https://www.blazemeter.com/)：感谢 BlazeMeter 提供的设计思路
-   [JMeter](https://jmeter.apache.org/)：MeterSphere 使用了 JMeter 作为测试引擎
-   [Element](https://element.eleme.cn/#/)：感谢 Element 提供的优秀组件库

## 微信群

![wechat-group](https://metersphere.oss-cn-hangzhou.aliyuncs.com/img/wechat-group.png)

## License & Copyright

Copyright (c) 2014-2021 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 2 (GPLv2)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-2.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
