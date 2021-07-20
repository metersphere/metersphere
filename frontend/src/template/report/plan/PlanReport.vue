<template>
  <div class="container" ref="resume" id="app">
    <el-main>
      <div v-for="(item, index) in preview" :key="item.id">
        <template-component :isReportView="true" :metric="metric" :preview="item" :index="index" ref="templateComponent"/>
      </div>
    </el-main>
  </div>
</template>

<script>
import TemplateComponent
  from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
export default {
  name: "PlanReport",
  components: {TemplateComponent},
  data() {
    return {
      preview: [{"name":"基础信息","id":1,"type":"system"},{"name":"测试结果","id":2,"type":"system"},{"name":"测试结果分布","id":3,"type":"system"},{"name":"失败用例","id":4,"type":"system"},{"name":"缺陷列表","id":5,"type":"system"}]
      ,metric: {"executeResult":{"functionalResult":[{"status":"Failure","count":1},{"status":"Prepare","count":4}],"apiResult":[{"status":"Prepare","count":13}],"scenarioResult":[],"loadResult":[],"executedScenarioIds":null},"moduleExecuteResult":[{"moduleId":"d0785ec7-46b3-441d-a5ff-aff13f055241","moduleName":"默认模块","caseCount":1,"passCount":0,"passRate":0,"issuesCount":0,"prepareCount":1,"skipCount":0,"failureCount":0,"blockingCount":0,"underwayCount":0,"projectName":"Local"},{"moduleId":"2f477110-0976-4d4a-84a5-9b33b82f01e8","moduleName":"a","caseCount":1,"passCount":0,"passRate":0,"issuesCount":0,"prepareCount":1,"skipCount":0,"failureCount":0,"blockingCount":0,"underwayCount":0,"projectName":"jira"},{"moduleId":"8631f33c-222b-4c3e-b4b4-e643dacdfe70","moduleName":"默认模块","caseCount":2,"passCount":0,"passRate":0,"issuesCount":0,"prepareCount":2,"skipCount":0,"failureCount":0,"blockingCount":0,"underwayCount":0,"projectName":"jira"}],"failureTestCases":{"functionalTestCases":[{"id":"0ca40965-e40a-4956-9026-7a3604f71562","nodeId":"root","nodePath":"","projectId":null,"name":"copy_sdfadasa","type":"functional","maintainer":"tests","priority":"P0","method":"","createTime":1626075279957,"updateTime":1626348003718,"testId":"[]","sort":null,"num":null,"otherTestName":null,"reviewStatus":null,"tags":"[]","demandId":null,"demandName":null,"followPeople":null,"status":"Failure","customNum":"100002","stepModel":null,"createUser":null,"originalStatus":null,"deleteTime":null,"deleteUserId":null,"prerequisite":null,"remark":null,"steps":null,"stepDescription":null,"expectedResult":null,"customFields":"[{\"id\":\"0d2b90ec-9c56-4f5d-ae06-522a93f48e93\",\"name\":\"责任人\",\"value\":\"tests\",\"customData\":null},{\"id\":\"661e1e29-c401-43b0-972e-a713b7b90c37\",\"name\":\"用例等级\",\"value\":\"P0\",\"customData\":null},{\"id\":\"f448e8d2-fad1-4c23-a013-3f3ba282bb81\",\"name\":\"用例状态\",\"value\":\"Prepare\",\"customData\":null}]","executor":"admin","executorName":null,"results":null,"planId":"96791ef8-8a75-4335-a48c-832e0ddece5f","planName":null,"caseId":"9883f2f8-5e33-433d-9960-aec6698d167b","issues":"[{\"caseCount\":0,\"createTime\":1625057764241,\"creator\":\"admin\",\"customFields\":\"[{\\\"id\\\":\\\"52f64983-aa06-4bf2-bf7a-1ad6b48ad197\\\",\\\"name\\\":\\\"状态\\\",\\\"value\\\":\\\"resolved\\\",\\\"customData\\\":null}]\",\"description\":\"sdfsdf\",\"id\":\"1133268703001004718\",\"num\":100007,\"platform\":\"Tapd\",\"platformStatus\":\"新\",\"projectId\":\"8fed2fce-d4f0-11eb-a517-dce9941665c4\",\"status\":\"new\",\"title\":\"sdfsd\",\"updateTime\":1625057764241}]","reportId":null,"model":null,"projectName":"默认项目","actualResult":null,"maintainerName":null,"issuesCount":1,"list":null}],"apiTestCases":[],"scenarioTestCases":[],"loadTestCases":[]},"executors":["admin"],"executorNames":["Administrator"],"principal":"admin","principalName":"Administrator","startTime":null,"endTime":null,"projectName":"默认项目","issues":[{"id":"1133268703001004718","title":"sdfsd","status":"new","createTime":1625057764241,"updateTime":1625057764241,"reporter":null,"lastmodify":null,"platform":"Tapd","projectId":"8fed2fce-d4f0-11eb-a517-dce9941665c4","creator":"admin","resourceId":null,"num":100007,"platformStatus":"新","description":"sdfsdf","customFields":"[{\"id\":\"52f64983-aa06-4bf2-bf7a-1ad6b48ad197\",\"name\":\"状态\",\"value\":\"resolved\",\"customData\":null}]","model":"","projectName":"默认项目","creatorName":null,"resourceName":null,"caseCount":0,"caseIds":null}]}
      // preview: "#preview",
      // metric: "#metric"
    }
  },

  methods: {

  }
}
</script>

<style scoped>
</style>
