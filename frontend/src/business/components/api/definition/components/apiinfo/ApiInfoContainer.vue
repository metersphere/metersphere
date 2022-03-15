<template>
  <div v-if="operationType === 'edit' || operationType === 'create'|| operationType === 'debug'">
    <api-definition-edit v-loading="loading" :api="currentApi" :module-options="moduleOptions"
                         :protocol="protocol" :project-id="projectId" :is-edit="operationType !== 'debug'"
                         @saveApiAndCase="saveApiAndCase" @saveApi="saveApi"/>
  </div>
  <div v-else-if="operationType === 'addApiAndCase'">
    <api-case-edit v-loading="loading" :api="currentApi" :module-options="moduleOptions"
                   :protocol="protocol" :project-id="projectId" :is-edit="operationType !== 'debug'"
                   @saveApi="saveApiBeforeCase"/>
  </div>
  <div v-else>
    <api-definition-information v-show="apiInfo && apiInfo.id" v-loading="loading"
                                :api-id="apiId"
                                :project-id="projectId" :current-protocol="protocol"
                                :current-api="currentApi" :api-info="apiInfo"
                                @editApi="editApi"
    />
  </div>
</template>

<script>
import ApiTestBaseContainer from "@/business/components/common/layout/ApiTestBaseContainer";
import ApiInformation from "@/business/components/api/definition/components/document/components/ApiInformation";
import ApiDefinitionEdit from "@/business/components/api/definition/components/apiinfo/edit/ApiDefinitionEdit";
import ApiDefinitionInformation
  from "@/business/components/api/definition/components/apiinfo/information/ApiDefinitionInformation";
import {getUUID} from "@/common/js/utils";
import ApiBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/ApiBaseInfo";
import ApiTestInfo from "@/business/components/api/definition/components/apiinfo/edit/ApiTestInfo";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import ApiCaseEdit from "@/business/components/api/definition/components/apiinfo/edit/ApiCaseEdit";

export default {
  name: "ApiInfoContainer",
  components: {
    ApiTestInfo,
    ApiBaseInfo,
    ApiDefinitionInformation,
    ApiTestBaseContainer,
    ApiInformation,
    ApiDefinitionEdit,
    ApiCaseEdit
  },
  props: {
    isEdit: Boolean,
    operationType: String,
    apiId: String,
    projectId: String,
    currentApi: {},
    currentProtocol: String,
    moduleOptions: [],
  },
  data() {
    return {
      apiInfo: {},
      api: {},
      loading: true,
      protocol: "",
      apiIsEditing: false,
    }
  },
  created() {
    this.initData();
  },
  watch: {
    operationType() {
      this.initData();
    }
  },
  methods: {
    editApi() {
      this.$emit("updateApiStatus", "edit", this.api.name);
    },
    initData() {
      this.loading = true;
      if (this.operationType === "debug" || this.operationType === "create") {
        this.protocol = this.currentApi.protocol;
        this.loading = false;
      } else if (this.operationType === 'addApiAndCase') {
        this.protocol = this.currentApi.protocol;
        this.loading = false;
      } else if (this.apiId) {
        this.$get("/api/definition/get/apiDetail/" + this.apiId, response => {
          if (response.data) {
            if (response.data.apiInfo) {
              this.apiInfo = response.data.apiInfo;
            }
            if (response.data.apiDefinition) {
              this.api = response.data.apiDefinition;
              this.protocol = this.api.protocol;
            }
          }
          this.loading = false;
        }, () => {
          this.loading = false;
        });
      } else {
        this.loading = false;
      }
    },
    saveApi(apiStruct) {
      let bodyFiles = this.getBodyUploadFiles(apiStruct);
      apiStruct.requestId = apiStruct.request.id;
      if (apiStruct.request) {
        // 历史数据处理
        if (apiStruct.request.authManager) {
          apiStruct.request.authManager.clazzName = TYPE_TO_C.get("AuthManager");
        }
        this.sort(apiStruct.request.hashTree);
      }

      this.setParameter(apiStruct);
      let reqUrl = "/api/definition/update";
      if (apiStruct.operationType === 'create') {
        reqUrl = "/api/definition/create";
      }
      this.loading = true;
      this.$fileUpload(reqUrl, null, bodyFiles, apiStruct, (response) => {
        this.$success(this.$t('commons.save_success'));
        this.currentApi.isCopy = false;
        // 创建了新版本的api，之后id变了，ref_id 保存了原始id
        apiStruct.id = response.data.id;
        apiStruct.remark = response.data.remark;
        this.loading = false;
        if (!this.apiId) {
          this.apiId = apiStruct.id;
        }
        this.$nextTick(() => {
          this.$emit('saveApi', apiStruct);
        });
      }, () => {
        this.loading = false;
      });
    },
    saveApiAndCase(apiStruct) {
      this.$emit("saveApiAndCase", apiStruct);
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    saveApiBeforeCase(api) {
      let apiData = JSON.parse(JSON.stringify(api));
      let apiCase = JSON.parse(JSON.stringify(api));
      apiData.moduleId = module;
      apiData.modulePath = "/" + this.$t('commons.module_title');
      this.setParameter(apiData);
      let bodyFiles = this.getBodyUploadFiles(apiData);
      apiData.response = null;
      this.loading = true;
      this.$fileUpload("/api/definition/create", null, bodyFiles, apiData, () => {
        apiCase.apiDefinitionId = apiData.id;
        apiCase.id = getUUID();
        apiCase.name = apiData.name;
        apiCase.priority = this.currentApi.priority;
        apiCase.tags = apiData.tags;
        apiCase.description = apiData.description;
        this.saveCase(apiCase, api);
      }, () => {
        this.loading = false;
      });
    },
    saveCase(newCase, api) {
      let tmp = newCase;
      tmp.request.body = newCase.request.body;
      let bodyFiles = this.getBodyUploadFiles(tmp);
      tmp.projectId = this.projectId;
      tmp.active = true;
      tmp.request.id = getUUID();
      tmp.id = tmp.request.id;
      tmp.request.path = this.api.path;
      tmp.versionId = this.api.versionId;
      if (tmp.request.protocol !== "dubbo://" && tmp.request.protocol !== "DUBBO") {
        tmp.request.method = this.api.method;
      }

      if (tmp.request.esbDataStruct != null) {
        tmp.esbDataStruct = JSON.stringify(tmp.request.esbDataStruct);
      }
      if (tmp.request.backEsbDataStruct != null) {
        tmp.backEsbDataStruct = JSON.stringify(tmp.request.backEsbDataStruct);
      }

      if (tmp.tags instanceof Array) {
        tmp.tags = JSON.stringify(tmp.tags);
      } else if (!tmp.tags || tmp.tags === "") {
        tmp.tags = "[]";
      }
      if (tmp.request) {
        tmp.request.clazzName = TYPE_TO_C.get(tmp.request.type);
        if (tmp.request.authManager) {
          tmp.request.authManager.clazzName = TYPE_TO_C.get(tmp.request.authManager.type);
        }
        this.sort(tmp.request.hashTree);
      }
      if (tmp.response) {
        tmp.response = JSON.stringify(tmp.response);
      }
      let url = "/api/testcase/create";
      this.result = this.$fileUpload(url, null, bodyFiles, tmp, (response) => {
        this.$success(this.$t('commons.save_success'));
        this.$emit("saveCaseCallback", api);
        this.loading = false;
      }, (error) => {
        this.loading = false;
      });
    },
    setParameter(apiStruct) {
      apiStruct.name = this.currentApi.name;
      apiStruct.moduleId = this.currentApi.moduleId;
      apiStruct.userId = this.currentApi.userId;
      apiStruct.status = this.currentApi.status;
      apiStruct.tags = this.currentApi.tags;
      apiStruct.description = this.currentApi.description;

      if (!apiStruct.isCopy) {
        apiStruct.isCopy = false;
      }
      if (apiStruct.request) {
        if (apiStruct.protocol !== "DUBBO") {
          apiStruct.request.method = apiStruct.method;
        }
        apiStruct.request.path = apiStruct.path;
        apiStruct.request.useEnvironment = undefined;
      }
      if (!apiStruct.follows) {
        apiStruct.follows = [];
      }

      if (apiStruct.tags instanceof Array) {
        apiStruct.tags = JSON.stringify(apiStruct.tags);
      }

      if (!apiStruct.method) {
        apiStruct.method = this.currentProtocol;
      }
      apiStruct.projectId = this.projectId;
      apiStruct.request.name = this.currentApi.name;
      apiStruct.protocol = this.currentProtocol;
      if (this.currentProtocol === "DUBBO" || this.currentProtocol === "dubbo://") {
        apiStruct.request.protocol = "dubbo://";
      } else {
        apiStruct.request.protocol = this.currentProtocol;
      }
      if (apiStruct.isCopy) {
        apiStruct.id = this.apiId;
      } else {
        if (apiStruct.id) {
          apiStruct.request.id = apiStruct.id;
        } else {
          apiStruct.id = apiStruct.request.id;
        }
      }
      if (this.currentProtocol !== 'HTTP') {
        apiStruct.response = null;
      }
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
