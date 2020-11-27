<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-edit-complete-http-api @runTest="runTest" @saveApi="saveApi" :request="request" :headers="headers" :response="response"
                              :basisData="currentApi" :moduleOptions="moduleOptions" :currentProject="currentProject" v-if="currentProtocol === 'HTTP'"/>
    <!-- TCP -->
    <ms-edit-complete-tcp-api :request="request" @runTest="runTest" @saveApi="saveApi" :currentProject="currentProject" :basisData="currentApi"
                             :moduleOptions="moduleOptions" v-if="currentProtocol === 'TCP'"/>
    <!--DUBBO-->
    <ms-edit-complete-dubbo-api :request="request" @runTest="runTest" @saveApi="saveApi" :currentProject="currentProject" :basisData="currentApi"
                               :moduleOptions="moduleOptions" v-if="currentProtocol === 'DUBBO'"/>
    <!--SQL-->
    <ms-edit-complete-sql-api :request="request" @runTest="runTest" @saveApi="saveApi" :currentProject="currentProject" :basisData="currentApi"
                             :moduleOptions="moduleOptions" v-if="currentProtocol === 'SQL'"/>

  </div>
</template>

<script>
  import MsEditCompleteHttpApi from "./complete/EditCompleteHTTPApi";
  import MsEditCompleteTcpApi from "./complete/EditCompleteTCPApi";
  import MsEditCompleteDubboApi from "./complete/EditCompleteDubboApi";
  import MsEditCompleteSqlApi from "./complete/EditCompleteSQLApi";

  import {ResponseFactory, Body} from "../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";
  import {createComponent, Request} from "./jmeter/components";
  import Sampler from "./jmeter/components/sampler/sampler";
  import HeaderManager from "./jmeter/components/configurations/header-manager";
  import {WORKSPACE_ID} from '@/common/js/constants';

  export default {
    name: "ApiConfig",
    components: {MsEditCompleteHttpApi, MsEditCompleteTcpApi, MsEditCompleteDubboApi, MsEditCompleteSqlApi},
    data() {
      return {
        reqUrl: "",
        request: Sampler,
        config: {},
        response: {},
        headers: [],
        maintainerOptions: [],
      }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProject: {},
      currentProtocol: String,
    },
    created() {
      this.getMaintainerOptions();
      switch (this.currentProtocol) {
        case Request.TYPES.SQL:
          this.initSql();
          break;
        case Request.TYPES.DUBBO:
          this.initDubbo();
          break;
        case Request.TYPES.TCP:
          this.initTcp();
          break;
        default:
          this.initHttp();
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
        })
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      initSql() {
        if (this.currentApi.request != undefined && this.currentApi.request != null) {
          this.request = JSON.parse(this.currentApi.request);
          this.currentApi.request = this.request;
        } else {
          this.request = createComponent("JDBCSampler");
        }
      },
      initDubbo() {
        if (this.currentApi.request != undefined && this.currentApi.request != null) {
          this.request = JSON.parse(this.currentApi.request);
          this.currentApi.request = this.request;
        } else {
          this.request = createComponent("DubboSampler");
        }
      },
      initTcp() {
        if (this.currentApi.request != undefined && this.currentApi.request != null) {
          this.request = JSON.parse(this.currentApi.request);
          this.currentApi.request = this.request;
        } else {
          this.request = createComponent("TCPSampler");
        }
      },
      initHttp() {
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
        if (this.currentProtocol === 'HTTP') {
          this.request.hashTree[0].headers = this.headers;
        }
        this.request.name = this.currentApi.name;
        data.protocol = this.currentProtocol;
        data.request = this.request;
        data.response = this.response;
      },
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
          if (request.body.kvs) {
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
          }
          if (request.body.binary) {
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
        }
        return bodyUploadFiles;
      },
    }
  }
</script>

<style scoped>


</style>
