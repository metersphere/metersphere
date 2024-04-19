# MeterSphere : Open-source Continuous Testing Platform

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/metersphere/metersphere)](https://github.com/metersphere/metersphere/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/metersphere/metersphere/total)](https://github.com/metersphere/metersphere/releases)

> [中文](README.md) | English

MeterSphere is a one-stop open-source enterprise-class continuous testing platform. It covers functions such as tests tracking, interface testing, performance testing, team collaboration and is compatible with open-source standards such as JMeter. It helps development and testing teams to conduct highly scalable automated testing, making full use of elasticity of the cloud, and accelerating the delivery process of high-quality software.

-   Test Tracking: Far beyond the user experience of TestLink.
-   API Testing: Similar to Postman's experience.
-   Performance Testing: Compatible with JMeter. Support Kubernetes and Cloud Environment. High concurrency, distributed performance testing with ease.
-   Team Collaboration: duo-levels tenants system, naturally support team co-op.

## Quick Start

Only need two steps to install MeterSphere：

What you need:
1.  Prepare a 64-bit Linux host with no less than 8 G RAM
2.  Log into root user and execute the command down below to install MeterSphere

```sh
curl -sSL https://github.com/metersphere/metersphere/releases/latest/download/quick_start.sh | sh
```

## Technical advantages

-   Full Life Cycle: Full coverage over all STLC phases. Starting from the Test Plan to the Report Creation phase.
-   Automation & Scalable: Automation for interface and performance testings. Fully adopt the elasticity of Cloud to produce a large scale of performance testing.
-   Continuous Testing: Seamlessly integrated with the CI tools. Supporting enterprises for "Shift left" testing.
-   Team Collaboration: Support different proportions of teams. Capable from a group of five to a testing center of several hundred people.

## Features List

<table>
    <tbody>
        <tr>
            <td rowspan="10">Test Tracking</td>
            <td>Project management</td>
            <td>Multi-project support, test cases, test plan, and project association</td>
        </tr>
        <tr>
            <td rowspan="4">Test Cases Management</td>
            <td>Online editing test case</td>
        </tr>
        <tr>
            <td>tree structure display project module and its test cases</td>
        </tr>
        <tr>
            <td>Custom test case attributes</td>
        </tr>
        <tr>
            <td>Quickly import test cases into the system</td>
        </tr>
        <tr>
            <td rowspan="5">Test Plan Tracking</td>
            <td>Initiate a test plan based on existing test cases</td>
        </tr>
        <tr>
            <td>Online update of test case execution results</td>
        </tr>
        <tr>
            <td>Flexible test case allocation</td>
        </tr>
        <tr>
            <td>Generate test reports online, support custom test report templates</td>
        </tr>
        <tr>
            <td>Combine with the interface test and performance test functions in the platform to automatically update the results of associated test cases</td>
        </tr>
        <tr>
            <td rowspan="7">Interface Testing</td>
            <td rowspan="5">Test Script</td>
            <td>Online editing interface testing content</td>
        </tr>
        <tr>
            <td>Support parameterized testing</td>
        </tr>
        <tr>
            <td>Pliable assertion support</td>
        </tr>
        <tr>
            <td>Support multi-interface scenario testing</td>
        </tr>
        <tr>
            <td>Quickly record test script via browser plug-in</td>
        </tr>
        <tr>
            <td rowspan="2">Test Report</td>
            <td>Automatically generate test report after test execution</td>
        </tr>
        <tr>
            <td>Exportable Test report</td>
        </tr>
        <tr>
            <td rowspan="9">Performance Testing</td>
            <td rowspan="5">Test Script</td>
            <td>Fully compatible with JMeter script</td>
        </tr>
        <tr>
            <td>Adjust pressure parameter online</td>
        </tr>
        <tr>
            <td>Distributed pressure testing</td>
        </tr>
        <tr>
            <td>Support parameterized testing</td>
        </tr>
        <tr>
            <td>Quickly record test script via browser plug-in</td>
        </tr>
        <tr>
            <td rowspan="4">Test Report</td>
            <td>Automatically generate test report after test execution</td>
        </tr>
        <tr>
            <td>Rich test report display form</td>
        </tr>
        <tr>
            <td>Exportable test report</td>
        </tr>
        <tr>
            <td>View test log details</td>
        </tr>
        <tr>
            <td rowspan="6">System Management</td>
            <td rowspan="2">Tenant management</td>
            <td>Support multi-level tenant system</td>
        </tr>
        <tr>
            <td>Support multiple tenant roles</td>
        </tr>
        <tr>
            <td rowspan="2">Test resource management</td>
            <td>Performance test resource pool management</td>
        </tr>
        <tr>
            <td>Email notification configuration</td>
        </tr>
        <tr>
            <td rowspan="2">Integration and expansion</td>
            <td>Complete&nbsp;API&nbsp;list</td>
        </tr>
        <tr>
            <td>Supports continuous integration tools such as&nbsp;Jenkins&nbsp;</td>
        </tr>
    </tbody>
</table>

## Technology stack

-   Backend: [Spring Boot](https://www.tutorialspoint.com/spring_boot/spring_boot_introduction.htm)
-   Frontend: [Vue.js](https://vuejs.org/)
-   Middleware: [MySQL](https://www.mysql.com/), [Kafka](https://kafka.apache.org/), [MinIO](https://min.io/), [Redis](https://redis.com/)
-   Basic infrastructure: [Docker](https://www.docker.com/), [Kubernetes](https://kubernetes.io/)
-   Test engine: [JMeter](https://jmeter.apache.org/)

## License & Copyright

Copyright (c) 2014-2024 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
