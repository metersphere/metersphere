<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-edit-complete-http-api @runTest="runTest" @saveApi="saveApi" @createRootModelInTree="createRootModelInTree"
                               :request="request" :response="response" :project-id="projectId"
                               @mockConfig="mockConfig"
                               @changeTab="changeTab"
                               @checkout="checkout"
                               :basisData="currentApi" :moduleOptions="moduleOptions" :syncTabs="syncTabs"
                               v-if="currentProtocol === 'HTTP'" ref="httpApi"/>
    <!-- TCP -->
    <ms-edit-complete-tcp-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree"
                              @saveApi="saveApi" :basisData="currentApi"
                              @changeTab="changeTab"
                              @checkout="checkout"
                              :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'TCP'"
                              ref="tcpApi"/>
    <!--DUBBO-->
    <ms-edit-complete-dubbo-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree"
                                @saveApi="saveApi" :basisData="currentApi"
                                @checkout="checkout"
                                :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'DUBBO'"
                                ref="dubboApi"/>
    <!--SQL-->
    <ms-edit-complete-sql-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree"
                              @saveApi="saveApi" :basisData="currentApi"
                              @checkout="checkout"
                              :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'SQL'"
                              ref="sqlApi"/>
  </div>
</template>

<script>
import MsEditCompleteHttpApi from "./complete/EditCompleteHTTPApi";
import MsEditCompleteTcpApi from "./complete/EditCompleteTCPApi";
import MsEditCompleteDubboApi from "./complete/EditCompleteDubboApi";
import MsEditCompleteSqlApi from "./complete/EditCompleteSQLApi";

import {Body} from "../model/ApiTestModel";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import {createComponent, Request} from "./jmeter/components";
import Sampler from "./jmeter/components/sampler/sampler";
import {WORKSPACE_ID} from '@/common/js/constants';
import {handleCtrlSEvent} from "../../../../../common/js/utils";
import {ELEMENT_TYPE, TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "ApiConfig",
  components: {MsEditCompleteHttpApi, MsEditCompleteTcpApi, MsEditCompleteDubboApi, MsEditCompleteSqlApi},
  data() {
    return {
      reqUrl: "",
      request: Sampler,
      config: {},
      response: {},
      maintainerOptions: [],
      count: 0,
      responseCount: 0,
    }
  },
  props: {
    currentApi: {},
    moduleOptions: {},
    currentProtocol: String,
    syncTabs: Array,
    projectId: String
  },
  watch: {
    request: {
      handler(newObj, oldObj) {
        this.count++
        if (this.count > 2) {
          this.$store.state.apiStatus.set("requestChange", true);
          this.$store.state.apiMap.set(this.currentApi.id, this.$store.state.apiStatus);
        }
      },
      deep: true
    },
    response: {
      handler(newObj, oldObj) {
        this.responseCount++;
        if (this.responseCount > 1) {
          this.$store.state.apiStatus.set("responseChange", true);
          this.$store.state.apiMap.set(this.currentApi.id, this.$store.state.apiStatus);
        }
      },
      deep: true
    },
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
    this.formatApi();
    this.addListener();
    if (!(this.$store.state.apiMap instanceof Map)) {
      this.$store.state.apiMap = new Map();
    }
    if (!(this.$store.state.apiStatus instanceof Map)) {
      this.$store.state.apiStatus = new Map();
    }
  },
  methods: {
    changeTab(type) {
      this.$emit("changeTab", type);
    },
    addListener() {
      document.addEventListener("keydown", this.createCtrlSHandle);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
    },
    createCtrlSHandle(event) {
      if (this.$refs.httpApi) {
        handleCtrlSEvent(event, this.$refs.httpApi.saveApi);
      } else if (this.$refs.tcpApi) {
        handleCtrlSEvent(event, this.$refs.tcpApi.saveApi);
      } else if (this.$refs.dubboApi) {
        handleCtrlSEvent(event, this.$refs.dubboApi.saveApi);
      } else if (this.$refs.sqlApi) {
        handleCtrlSEvent(event, this.$refs.sqlApi.saveApi);
      }
    },
    runTest(data) {
      this.setParameters(data);
      let bodyFiles = this.getBodyUploadFiles(data);
      this.$fileUpload(this.reqUrl, null, bodyFiles, data, response => {
        this.$success(this.$t('commons.save_success'));
        this.reqUrl = "/api/definition/update";
        let newData = response.data;
        data.request = JSON.parse(newData.request);
        this.$emit('runTest', data);
      });
    },
    mockConfig(data) {
      this.$emit('mockConfig', data);
    },
    checkout(data) {
      this.$emit('checkout', data);
    },
    createRootModelInTree() {
      this.$emit("createRootModel");
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    setRequest() {
      if (this.currentApi.request != undefined && this.currentApi.request != null) {
        if (Object.prototype.toString.call(this.currentApi.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.request = this.currentApi.request;
        } else {
          this.request = JSON.parse(this.currentApi.request);
        }
        if (!this.request.headers) {
          this.request.headers = [];
        }
        this.currentApi.request = this.request;
        return true;
      }
      return false;
    },
    initSql() {
      if (!this.setRequest()) {
        this.request = createComponent("JDBCSampler");
        this.currentApi.request = this.request;
      }
      if (!this.currentApi.request.variables) {
        this.currentApi.request.variables = [];
      }
    },
    initDubbo() {
      if (!this.setRequest()) {
        this.request = createComponent("DubboSampler");
        this.currentApi.request = this.request;
      }
    },
    initTcp() {
      if (!this.setRequest()) {
        this.request = createComponent("TCPSampler");
        this.currentApi.request = this.request;
      }
    },
    initHttp() {
      if (!this.setRequest()) {
        this.request = createComponent("HTTPSamplerProxy");
        this.currentApi.request = this.request;
      }
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    formatApi() {
      if (this.currentApi.response != null && this.currentApi.response != 'null' && this.currentApi.response != undefined) {
        if (Object.prototype.toString.call(this.currentApi.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.response = this.currentApi.response;
        } else {
          this.response = JSON.parse(this.currentApi.response);
        }
      } else {
        this.response = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      if (this.currentApi && this.currentApi.id && !this.currentApi.isCopy) {
        this.reqUrl = "/api/definition/update";
      } else {
        this.reqUrl = "/api/definition/create";
      }
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.body && !this.request.body.binary) {
        this.request.body.binary = [];
      }
      // 处理导入数据缺失问题
      if (this.response.body) {
        let body = new Body();
        Object.assign(body, this.response.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.response.body = body;
      }
      this.request.clazzName = TYPE_TO_C.get(this.request.type);

      this.sort(this.request.hashTree);
    },
    saveApi(data) {
      this.setParameters(data);
      let bodyFiles = this.getBodyUploadFiles(data);
      data.requestId = data.request.id;
      if (data.request) {
        // 历史数据处理
        if (data.request.authManager) {
          data.request.authManager.clazzName = TYPE_TO_C.get("AuthManager");
        }
        this.sort(data.request.hashTree);
      }
      this.$fileUpload(this.reqUrl, null, bodyFiles, data, (response) => {
        this.$success(this.$t('commons.save_success'));
        this.reqUrl = "/api/definition/update";
        this.currentApi.isCopy = false;
        // 创建了新版本的api，之后id变了，ref_id 保存了原始id
        data.id = response.data.id;
        this.$emit('saveApi', data);
      });
      this.$store.state.apiMap.delete(this.currentApi.id);
      this.responseCount = 0;
      this.count = 0
    },
    handleSave() {
      if (this.$refs.httpApi) {
        this.$refs.httpApi.saveApi();
      } else if (this.$refs.tcpApi) {
        this.$refs.tcpApi.saveApi();
      } else if (this.$refs.dubboApi) {
        this.$refs.dubboApi.saveApi();
      } else if (this.$refs.sqlApi) {
        this.$refs.sqlApi.saveApi();
      }
    },
    setParameters(data) {
      data.projectId = this.projectId;
      this.request.name = this.currentApi.name;
      data.protocol = this.currentProtocol;
      data.request = this.request;
      data.request.name = data.name;
      if (this.currentProtocol === "DUBBO" || this.currentProtocol === "dubbo://") {
        data.request.protocol = "dubbo://";
      } else {
        data.request.protocol = this.currentProtocol;
      }
      if (data.isCopy) {
        data.id = getUUID();
      } else {
        if (data.id) {
          data.request.id = data.id;
        } else {
          data.id = data.request.id;
        }
      }

      if (!data.method) {
        data.method = this.currentProtocol;
      }
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
                  item.name = item.file.name;
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
