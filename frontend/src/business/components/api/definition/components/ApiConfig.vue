<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-add-complete-http-api @runTest="runTest" @saveApi="saveApi" :request="request" :headers="headers" :response="response" :basisData="currentApi"
                              :moduleOptions="moduleOptions" :currentProject="currentProject"
                              v-if="currentProtocol === 'HTTP'"/>
  </div>
</template>

<script>
  import MsAddCompleteHttpApi from "./complete/AddCompleteHttpApi";
  import {ResponseFactory, Body} from "../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";
  import {createComponent, Request} from "./jmeter/components";
  import Sampler from "./jmeter/components/sampler/sampler";
  import HeaderManager from "./jmeter/components/configurations/header-manager";

  export default {
    name: "ApiConfig",
    components: {MsAddCompleteHttpApi},
    data() {
      return {
        reqUrl: "",
        request: Sampler,
        response: {},
        headers: [],
      }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProject: {},
      currentProtocol: String,
    },
    created() {
      switch (this.currentProtocol) {
        case Request.TYPES.SQL:
          this.request = createComponent("SQL");
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
      if (this.currentApi.response != null && this.currentApi.response != 'null' && this.currentApi.response != undefined) {
        this.response = new ResponseFactory(JSON.parse(this.currentApi.response));
      } else {
        this.response = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      if (this.currentApi != null && this.currentApi.id != null) {
        this.reqUrl = "/api/definition/update";
      } else {
        this.reqUrl = "/api/definition/create";
        this.currentApi.id = getUUID().substring(0, 8);
      }
    },
    methods: {
      runTest(data) {
        this.setParameters(data);
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('runTest', data);
        });
      },
      createHttp() {
        if (this.currentApi.request != undefined && this.currentApi.request != null) {
          this.request = JSON.parse(this.currentApi.request);
          this.currentApi.request = this.request;
          this.headers = this.request.hashTree[0].headers;
        } else {
          let header = createComponent("HeaderManager");
          this.request = createComponent("HTTPSamplerProxy");
          this.request.hashTree = [header];
          this.currentApi.request = this.request;
        }
      },
      saveApi(data) {
        this.setParameters(data);
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('saveApi', data);
        });

      },
      setParameters(data) {
        data.projectId = this.currentProject.id;
        this.request.hashTree[0].headers = this.headers;
        data.request = this.request;
        data.response = this.response;
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
