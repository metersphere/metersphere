<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-add-complete-http-api @runTest="runTest" @saveApi="saveApi" :request="request" :headers="headers" :response="response" :basisData="currentApi"
                              :moduleOptions="moduleOptions" :currentProject="currentProject"
                              v-if="reqType === 'HTTP'"></ms-add-complete-http-api>
  </div>
</template>

<script>
  import MsAddCompleteHttpApi from "./complete/AddCompleteHttpApi";
  import {RequestFactory, ResponseFactory, Test, Body} from "../model/ApiTestModel";
  import JMX from "./jmeter/jmx";
  import JmeterTestPlan from "./jmeter/components/jmeter-test-plan";
  import TestPlan from "./jmeter/components/test-plan";
  import {getUUID} from "@/common/js/utils";
  import {createComponent, Request} from "./jmeter/components";
  import Sampler from "./jmeter/components/sampler/sampler";
  import HeaderManager from "./jmeter/components/configurations/header-manager";

  export default {
    name: "ApiConfig",
    components: {MsAddCompleteHttpApi},
    data() {
      return {
        reqType: RequestFactory.TYPES.HTTP,
        reqUrl: "",
        test: new Test(),
        jmx: new JMX(),
        request: Sampler,
        response: {},
        headers: [],
      }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProject: {},
      protocol: String,
    },
    created() {
      let jmx = new JMX();
      let rootTestPlan = new JmeterTestPlan();
      // 创建一个测试计划
      let testPlan = new TestPlan();
      rootTestPlan.hashTree = [testPlan];
      jmx.elements = [rootTestPlan];
      // 加一个线程组
      let threadGroup = createComponent("ThreadGroup");
      testPlan.hashTree = [threadGroup];

      this.jmx = jmx;
      switch (this.protocol) {
        case Request.TYPES.SQL:
          this.request = createComponent("OT");
          break;
        case Request.TYPES.DUBBO:
          this.request = createComponent("JDBCSampler");
          break;
        case Request.TYPES.TCP:
          this.request = createComponent("TCPSampler");
          break;
        default:
          this.createHttp();
          break;
      }

      threadGroup.hashTree = [this.request];
      this.response = this.currentApi.response != null ? new ResponseFactory(JSON.parse(this.currentApi.response)) : {
        headers: [],
        body: new Body(),
        statusCode: [],
        type: "HTTP"
      };

      if (this.currentApi != null && this.currentApi.id != null) {
        this.reqUrl = "/api/definition/update";
      } else {
        this.reqUrl = "/api/definition/create";
        this.currentApi.id = getUUID().substring(0, 8);
      }
    },
    methods: {
      runTest(data) {
        data.projectId = this.currentProject.id;
        data.request = data.test.request;
        data.response = data.test.response;
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('runTest', data);
        });
      },
      createHttp() {
        if (this.currentApi.request != null) {
          this.request = JSON.parse(this.currentApi.request);
          this.headers = this.request.hashTree[0].headers;
        } else {
          let header = createComponent("HeaderManager");
          this.request = createComponent("HTTPSamplerProxy");
          this.request.hashTree = [header];
        }
      },
      saveApi(data) {
        data.projectId = this.currentProject.id;
        this.request.hashTree[0].headers = this.headers;
        data.request = this.request;
        data.response = this.response;
        console.log(data)
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('saveApi', data);
        });

      },
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
          request.body.binary.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
    }
  }
</script>

<style scoped>


</style>
