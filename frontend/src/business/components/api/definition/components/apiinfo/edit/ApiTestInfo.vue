<template>
  <div style="margin:5px 5px 5px 5px">
    <http-base-info
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="api"
        :project-id="projectId"
        :is-edit="isEdit"
        @refresh="refresh"
        @saveApiAndCase="saveApiAndCase"
        ref="httpTestPage"
        v-if="currentProtocol==='HTTP'">
      <template v-slot:rightHeader>
        <div v-show="isEdit" class="ms-opt-btn">
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
        </div>
      </template>
    </http-base-info>

    <tcp-base-info
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="api"
        :project-id="projectId"
        :is-edit="isEdit"
        @refresh="refresh"
        @saveApiAndCase="saveApiAndCase"
        ref="tcpTestPage"
        v-else-if="currentProtocol==='TCP'"
    >
      <template v-slot:rightHeader>
        <div v-show="isEdit" class="ms-opt-btn">
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
        </div>
      </template>
    </tcp-base-info>

    <sql-base-info
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="api"
        :project-id="projectId"
        :is-edit="isEdit"
        @refresh="refresh"
        @saveApiAndCase="saveApiAndCase"
        ref="sqlTestPage"
        v-else-if="currentProtocol==='SQL'"
    >
      <template v-slot:rightHeader>
        <div v-show="isEdit" class="ms-opt-btn">
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
        </div>
      </template>
    </sql-base-info>

    <dubbo-base-info
        :syncTabs="syncTabs"
        :currentProtocol="currentProtocol"
        :api-data="api"
        :project-id="projectId"
        :is-edit="isEdit"
        ref="dubboTestPage"
        @saveApiAndCase="saveApiAndCase"
        @refresh="refresh"
        v-else-if="currentProtocol==='DUBBO'"
    >
      <template v-slot:rightHeader>
        <div v-show="isEdit" class="ms-opt-btn">
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
        </div>
      </template>
    </dubbo-base-info>
  </div>
</template>

<script>
import HttpBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/compnents/HttpBaseInfo";
import TcpBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/compnents/TcpBaseInfo";
import SqlBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/compnents/SqlBaseInfo";
import DubboBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/compnents/DubboBaseInfo";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import _ from 'lodash';
import {Body} from "@/business/components/api/definition/model/ApiTestModel";

export default {
  name: "ApiTestInfo",
  components: {HttpBaseInfo, TcpBaseInfo, SqlBaseInfo, DubboBaseInfo},
  props: {
    api: {},
    projectId: String,
    currentProtocol: String,
    syncTabs: Array,
    isEdit: Boolean,
  },
  data() {
    return {
      formCheckResult: false,
    }
  },
  created() {
    this.formatApi();
  },
  methods: {
    refresh() {
      this.$emit("refresh");
    },
    formatApi() {
      if (this.api.request != null && this.api.request != 'null' && this.api.request != undefined) {
        if (Object.prototype.toString.call(this.api.request).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
          this.api.request = JSON.parse(this.api.request);
          if (this.currentProtocol === 'HTTP') {
            if (this.api.request.body && !this.api.request.body.type) {
              let tempRequest = _.cloneDeep(this.api.request);
              tempRequest.body = {type: null};
              this.api.request = tempRequest;
            }
          }
        }
      }
      if (this.api && this.api.request && !this.api.request.hashTree) {
        this.api.request.hashTree = [];
      }
      if (this.currentProtocol === "HTTP") {
        if (this.api && this.api.request && this.api.request.body && !this.api.request.body.binary) {
          this.api.request.body.binary = [];
        }
      }
      if (this.currentProtocol === "TCP" || this.currentProtocol === "SQL") {
        if (this.api && this.api.request && !this.api.request.variables) {
          this.api.request.variables = [];
        }
      }
      if (this.api.request) {
        this.api.request.clazzName = TYPE_TO_C.get(this.api.request.type);
        this.sort(this.api.request.hashTree);
      }

      if (this.api.response != null && this.api.response != 'null' && this.api.response != undefined) {
        if (Object.prototype.toString.call(this.api.response).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
          this.api.response = JSON.parse(this.api.response);
        }
      } else {
        this.api.response = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      // 处理导入数据缺失问题
      if (this.api.response.body) {
        let body = new Body();
        Object.assign(body, this.api.response.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.api.response.body = body;
      }
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
    getApi() {
      this.formCheckResult = false;
      let newApiInfo = null;
      if (this.currentProtocol === 'HTTP' && this.$refs.httpTestPage) {
        this.formCheckResult = this.$refs.httpTestPage.validateForm();
        newApiInfo = this.$refs.httpTestPage.getApi();
      } else if (this.currentProtocol === 'TCP' && this.$refs.tcpTestPage) {
        this.formCheckResult = true;
        newApiInfo = this.$refs.tcpTestPage.getApi();
      } else if (this.currentProtocol === 'SQL' && this.$refs.sqlTestPage) {
        this.formCheckResult = true;
        newApiInfo = this.$refs.sqlTestPage.getApi();
      } else if (this.currentProtocol === 'DUBBO' && this.$refs.dubboTestPage) {
        this.formCheckResult = true;
        newApiInfo = this.$refs.dubboTestPage.getApi();
      }
      return newApiInfo;
    },
    saveApi() {
      let newApiInfo = this.getApi();
      if (this.formCheckResult && newApiInfo) {
        newApiInfo.path = newApiInfo.request.path;
        console.info(newApiInfo);
        this.$emit("saveApi", newApiInfo);
      }
    },
    saveApiAndCase(apiStruct) {
      this.$emit("saveApiAndCase", apiStruct);
    },
  },
}
</script>

<style scoped>
/deep/ .ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
  top: 128px;
  float: right;
  margin-right: 20px;
  margin-top: 5px;
}
</style>
