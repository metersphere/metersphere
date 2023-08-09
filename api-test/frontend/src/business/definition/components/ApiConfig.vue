<template>
  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-edit-complete-http-api
      @runTest="runTest"
      @saveApi="saveApiValidate"
      @createRootModelInTree="createRootModelInTree"
      :request="request"
      :response="response"
      :project-id="projectId"
      @mockConfig="mockConfig"
      @changeTab="changeTab"
      @checkout="checkout"
      :basisData="currentApi"
      :moduleOptions="moduleOptions"
      :syncTabs="syncTabs"
      v-if="currentProtocol === 'HTTP'"
      ref="httpApi" />
    <!-- TCP -->
    <ms-edit-complete-tcp-api
      :request="request"
      @runTest="runTest"
      @createRootModelInTree="createRootModelInTree"
      @saveApi="saveApiValidate"
      :basisData="currentApi"
      @changeTab="changeTab"
      @checkout="checkout"
      :moduleOptions="moduleOptions"
      :syncTabs="syncTabs"
      v-if="currentProtocol === 'TCP'"
      ref="tcpApi" />
    <!--DUBBO-->
    <ms-edit-complete-dubbo-api
      :request="request"
      @runTest="runTest"
      @createRootModelInTree="createRootModelInTree"
      @saveApi="saveApiValidate"
      :basisData="currentApi"
      @checkout="checkout"
      :moduleOptions="moduleOptions"
      :syncTabs="syncTabs"
      v-if="currentProtocol === 'DUBBO'"
      ref="dubboApi" />
    <!--SQL-->
    <ms-edit-complete-sql-api
      :request="request"
      @runTest="runTest"
      @createRootModelInTree="createRootModelInTree"
      @saveApi="saveApiValidate"
      :basisData="currentApi"
      @checkout="checkout"
      :moduleOptions="moduleOptions"
      :syncTabs="syncTabs"
      v-if="currentProtocol === 'SQL'"
      ref="sqlApi" />
  </div>
</template>

<script>
import { updateDefinition } from '@/api/definition';
import { getMaintainer } from '@/api/project';
import MsEditCompleteHttpApi from './complete/EditCompleteHTTPApi';
import MsEditCompleteTcpApi from './complete/EditCompleteTCPApi';
import MsEditCompleteDubboApi from './complete/EditCompleteDubboApi';
import MsEditCompleteSqlApi from './complete/EditCompleteSQLApi';

import { Body } from '../model/ApiTestModel';
import { getUUID } from 'metersphere-frontend/src/utils';
import { handleCtrlSEvent } from '@/api/base-network';
import { createComponent, Request } from './jmeter/components';
import Sampler from './jmeter/components/sampler/sampler';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import { useApiStore } from '@/store';

const store = useApiStore();
export default {
  name: 'ApiConfig',
  components: {
    MsEditCompleteHttpApi,
    MsEditCompleteTcpApi,
    MsEditCompleteDubboApi,
    MsEditCompleteSqlApi,
  },
  data() {
    return {
      reqUrl: '',
      request: Sampler,
      config: {},
      response: {},
      maintainerOptions: [],
      count: 0,
      responseCount: 0,
    };
  },
  props: {
    currentApi: {},
    moduleOptions: {},
    currentProtocol: String,
    syncTabs: Array,
    projectId: String,
  },
  watch: {
    request: {
      handler(newObj) {
        this.count++;
        if (this.count > 2) {
          store.apiStatus.set('requestChange', true);
          store.apiMap.set(this.currentApi.id, store.apiStatus);
        }
      },
      deep: true,
    },
    response: {
      handler(newObj, oldObj) {
        this.responseCount++;
        if (this.responseCount > 3) {
          store.apiStatus.set('responseChange', true);
          store.apiMap.set(this.currentApi.id, store.apiStatus);
        }
      },
      deep: true,
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
    if (!(store.apiMap instanceof Map)) {
      store.apiMap = new Map();
    }
    if (!(store.apiStatus instanceof Map)) {
      store.apiStatus = new Map();
    }
    // 记录原始数据源ID
    if (this.currentApi && this.currentApi.request && this.currentApi.request.hashTree) {
      this.setOriginal(this.currentApi.request.hashTree);
    }
  },
  methods: {
    setOriginal(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = ['JDBCPostProcessor', 'JDBCSampler', 'JDBCPreProcessor'];
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          scenarioDefinition[i].originalDataSourceId = scenarioDefinition[i].dataSourceId;
        }
        if (scenarioDefinition[i].hashTree && scenarioDefinition[i].hashTree.length > 0) {
          this.setOriginal(scenarioDefinition[i].hashTree);
        }
      }
    },
    changeTab(type) {
      this.$emit('changeTab', type);
    },
    addListener() {
      document.addEventListener('keydown', this.createCtrlSHandle);
    },
    removeListener() {
      document.removeEventListener('keydown', this.createCtrlSHandle);
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
      updateDefinition(this.reqUrl, null, bodyFiles, data).then((response) => {
        this.$success(this.$t('commons.save_success'));
        this.reqUrl = '/api/definition/update';
        let newData = response.data.data;
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
      this.$emit('createRootModel');
    },
    getMaintainerOptions() {
      getMaintainer().then((response) => {
        this.maintainerOptions = response.data;
      });
    },
    setRequest() {
      if (this.currentApi.request != undefined && this.currentApi.request != null) {
        if (
          Object.prototype.toString
            .call(this.currentApi.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
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
        this.request = createComponent('JDBCSampler');
        this.currentApi.request = this.request;
      }
      if (!this.currentApi.request.variables) {
        this.currentApi.request.variables = [];
      }
      this.currentApi.request.originalDataSourceId = this.currentApi.request.dataSourceId;
    },
    initDubbo() {
      if (!this.setRequest()) {
        this.request = createComponent('DubboSampler');
        this.currentApi.request = this.request;
      }
    },
    initTcp() {
      if (!this.setRequest()) {
        this.request = createComponent('TCPSampler');
        this.currentApi.request = this.request;
      }
    },
    initHttp() {
      if (!this.setRequest()) {
        this.request = createComponent('HTTPSamplerProxy');
        this.currentApi.request = this.request;
      }
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === 'Assertions' && !stepArray[i].document) {
            stepArray[i].document = {
              type: 'JSON',
              data: {
                xmlFollowAPI: false,
                jsonFollowAPI: false,
                json: [],
                xml: [],
              },
            };
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    formatApi() {
      if (
        this.currentApi.response != null &&
        this.currentApi.response != 'null' &&
        this.currentApi.response != undefined
      ) {
        if (
          Object.prototype.toString
            .call(this.currentApi.response)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          this.response = this.currentApi.response;
        } else {
          this.response = JSON.parse(this.currentApi.response);
        }
      } else {
        this.response = {
          headers: [],
          body: new Body(),
          statusCode: [],
          type: 'HTTP',
        };
      }
      if (this.currentApi && this.currentApi.id && !this.currentApi.isCopy) {
        this.reqUrl = '/api/definition/update';
      } else {
        this.reqUrl = '/api/definition/create';
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
    setParameter(data) {
      data.name = this.currentApi.name;
      data.moduleId = this.currentApi.moduleId;
      data.modulePath = this.currentApi.modulePath;
      data.userId = this.currentApi.userId;
      data.status = this.currentApi.status;
      if (this.currentApi.tags instanceof Array) {
        data.tags = JSON.stringify(this.currentApi.tags);
      }
      data.description = this.currentApi.description;
    },
    saveApiValidate(data) {
      this.$emit('validateBasic', data);
    },
    saveApi(data) {
      this.setParameter(data);
      this.setParameters(data);
      let bodyFiles = this.getBodyUploadFiles(data);
      data.requestId = data.request.id;
      if (data.request) {
        // 历史数据处理
        if (data.request.authManager) {
          data.request.authManager.clazzName = TYPE_TO_C.get('AuthManager');
        }
        this.sort(data.request.hashTree);
      }
      if (data.response) {
        this.parseResponseStructureDefaultKeyValue(data.response);
      }
      this.setProtocolBtn(true);
      updateDefinition(this.reqUrl, null, bodyFiles, data).then((response) => {
        this.setProtocolBtn(false);
        this.$success(this.$t('commons.save_success'));
        this.reqUrl = '/api/definition/update';
        this.currentApi.isCopy = false;
        this.currentApi.sourceId = '';
        // 创建了新版本的api，之后id变了，ref_id 保存了原始id
        let res = response.data.data;
        data.id = res.id;
        data.remark = res.remark || '';
        data.versionId = res.versionId;
        data.versionName = res.versionName;
        data.refId = res.refId;
        this.$emit('saveApi', data);
      });
      this.responseCount = 0;
      this.count = 0;
    },
    setProtocolBtn(disable) {
      switch (this.currentProtocol) {
        case Request.TYPES.SQL:
          this.$refs.sqlApi.disableSaveBtn = disable;
          break;
        case Request.TYPES.DUBBO:
          this.$refs.dubboApi.disableSaveBtn = disable;
          break;
        case Request.TYPES.TCP:
          this.$refs.tcpApi.disableSaveBtn = disable;
          break;
        default:
          this.$refs.httpApi.disableSaveBtn = disable;
          break;
      }
    },
    parseResponseStructureDefaultKeyValue(response) {
      if (response.headers && response.headers.length === 1) {
        let kv = response.headers[0];
        if (!kv.name || !kv.value) {
          response.headers = [];
        }
        if ((kv.name == null || kv.name === '') && (kv.value  == null || kv.value === '')) {
          response.headers = [];
        }
      }
      if (response.statusCode && response.statusCode.length === 1) {
        let kv = response.statusCode[0];
        if (!kv.name || !kv.value) {
          response.statusCode = [];
        }
        if ((kv.name == null || kv.name === '') && (kv.value  == null || kv.value === '')) {
          response.statusCode = [];
        }
      }
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
      if (this.currentProtocol === 'DUBBO' || this.currentProtocol === 'dubbo://') {
        data.request.protocol = 'dubbo://';
      } else {
        data.request.protocol = this.currentProtocol;
      }
      if (data.isCopy) {
        data.id = getUUID();
        data.request.id = data.id;
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
          request.body.kvs.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
                if (item.file) {
                  item.name = item.file.name;
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        if (request.body.binary) {
          request.body.binary.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
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
  },
};
</script>

<style scoped></style>
