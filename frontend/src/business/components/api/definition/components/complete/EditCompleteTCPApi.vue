<template xmlns:el-col="http://www.w3.org/1999/html">
  <!-- 操作按钮 -->
  <div style="background-color: white;">
    <el-row>
      <el-col>
        <!--操作按钮-->
        <div style="float: right;margin-right: 20px;margin-top: 20px" class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: #783987; font-size: 25px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: #783987; font-size: 28px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="basisData.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <ms-version-history v-xpack
                              ref="versionHistory"
                              :version-data="versionData"
                              :current-id="basisData.id"
                              @compare="compare" @checkout="checkout" @create="create" @del="del"/>
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s">{{ $t('commons.save') }}</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- 基础信息 -->
    <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>
    <br/>
    <el-row>
      <el-col>
        <ms-tcp-basic-api :method-types="methodTypes" @createRootModelInTree="createRootModelInTree"
                          :moduleOptions="moduleOptions"
                          :basisData="basisData" ref="basicForm"
                          @changeApiProtocol="changeApiProtocol" @callback="callback"/>
      </el-col>
    </el-row>
    <!-- MOCK信息 -->
    <p class="tip">{{ $t('test_track.plan_view.mock_info') }} </p>
    <div class="mock-info">
      <el-row>
        <el-col :span="20">
          Mock地址：
          <el-link v-if="this.mockInfo !== '' " target="_blank" style="color: black"
                   type="primary">{{ this.mockInfo }}
          </el-link>
          <el-link v-else target="_blank" style="color: darkred"
                   type="primary">当前项目未开启Mock服务
          </el-link>
        </el-col>
        <el-col :span="4">
          <el-link @click="mockSetting" type="primary">Mock设置</el-link>
        </el-col>
      </el-row>
    </div>
    <!-- 请求参数 -->
    <div v-if="apiProtocol=='TCP'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <!--      <ms-basis-parameters :show-script="false" :request="request"/>-->
      <ms-tcp-format-parameters :show-pre-script="true" :show-script="false" :request="request"
                                ref="tcpFormatParameter"/>
    </div>
    <div v-else-if="apiProtocol=='ESB'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <esb-definition v-xpack :show-pre-script="true" v-if="showXpackCompnent" :show-script="false" :request="request"
                      ref="esbDefinition"/>
      <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
      <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="true" :show-options-button="true"
                               :request="request"/>
      <!--      <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase"/>-->
    </div>
    <api-other-info :api="basisData" ref="apiOtherInfo"/>

    <ms-change-history ref="changeHistory"/>
    <el-dialog
      :fullscreen="true"
      :visible.sync="dialogVisible"
      :destroy-on-close="true"
      width="100%"
    >
      <t-c-p-api-version-diff
        v-if="dialogVisible"
        :old-data="basisData"
        :show-follow="showFollow"
        :new-data="newData"
        :new-show-follow="newShowFollow"
        :module-options="moduleOptions"
        :request="request"
        :old-request="oldRequest"
        :mock-info="mockInfo"
        :api-protocol="apiProtocol"
        :old-api-protocol="oldApiProtocol"
        :show-xpack-compnent="showXpackCompnent"
        :method-types="methodTypes"
      ></t-c-p-api-version-diff>
    </el-dialog>

    <el-dialog
      :title="$t('commons.sync_other_info')"
      :visible.sync="createNewVersionVisible"
      :show-close="false"
      width="30%"
    >
      <div>
        <el-checkbox v-model="basisData.newVersionRemark">{{ $t('commons.remark') }}</el-checkbox>
        <el-checkbox v-model="basisData.newVersionDeps">{{ $t('commons.relationship.name') }}</el-checkbox>
      </div>

      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="cancelCreateNewVersion"
          :title="$t('commons.edit_info')"
          @confirm="saveApi">
        </ms-dialog-footer>
      </template>
    </el-dialog>
  </div>

</template>

<script>
import MsTcpBasicApi from "./TCPBasicApi";
import MsTcpFormatParameters from "../request/tcp/TcpFormatParameters";
import MsChangeHistory from "../../../../history/ChangeHistory";
import {getCurrentProjectID, getCurrentUser, hasLicense} from "@/common/js/utils";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";
import TCPApiVersionDiff from "./version/TCPApiVersionDiff";
import {createComponent} from ".././jmeter/components";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

const {Body} = require("@/business/components/api/definition/model/ApiTestModel");
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "MsAddCompleteTcpApi",
  components: {
    MsDialogFooter,
    ApiOtherInfo, MsTcpBasicApi, MsTcpFormatParameters, MsChangeHistory,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default,
    'MsVersionHistory': versionHistory.default,
    TCPApiVersionDiff,
  },
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    syncTabs: Array,
  },
  data() {
    return {
      validated: false,
      apiProtocol: "TCP",
      mockInfo: "",
      showFollow: false,
      methodTypes: [
        {
          'key': "TCP",
          'value': this.$t('api_test.request.tcp.general_format'),
        }
      ],
      showXpackCompnent: false,
      versionData: [],
      dialogVisible: false,
      newShowFollow: false,
      newData: {},
      oldRequest: {},
      oldResponse: {},
      oldApiProtocol: "TCP",
      createNewVersionVisible: false,
    };
  },
  created: function () {
    if (this.basisData.method != 'TCP' && this.basisData.method != 'ESB') {
      this.basisData.method = this.basisData.protocol;
    }
    this.apiProtocol = this.basisData.method;
    if (this.apiProtocol == null || this.apiProtocol == "") {
      this.apiProtocol = "TCP";
    }
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
      this.showXpackCompnent = true;
      if (hasLicense()) {
        if (this.methodTypes.length == 1) {
          let esbMethodType = {};
          esbMethodType.key = "ESB";
          esbMethodType.value = "ESB";
          this.methodTypes.push(esbMethodType);
        }
      }
    }
    this.$get('/api/definition/follow/' + this.basisData.id, response => {
      this.basisData.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === getCurrentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    });
    this.getMockInfo();
    if (hasLicense()) {
      this.getVersionHistory();
    }
  },
  watch: {
    syncTabs() {
      if (this.basisData && this.syncTabs && this.syncTabs.includes(this.basisData.id)) {
        // 标示接口在其他地方更新过，当前页面需要同步
        let url = "/api/definition/get/";
        this.$get(url + this.basisData.id, response => {
          if (response.data) {
            let request = JSON.parse(response.data.request);
            let index = this.syncTabs.findIndex(item => {
              if (item === this.basisData.id) {
                return true;
              }
            });
            this.syncTabs.splice(index, 1);
            Object.assign(this.request, request);
          }
        });
      }
    },
  },
  methods: {
    openHis() {
      this.$refs.changeHistory.open(this.basisData.id, ["接口定义", "接口定義", "Api definition", "API_DEFINITION"]);
    },
    callback() {
      this.validated = true;
    },
    validateApi() {
      this.validated = false;
      this.basisData.method = this.apiProtocol;
      this.$refs['basicForm'].validate();
    },
    saveApi() {
      this.validateApi();
      if (this.validated) {
        if (this.basisData.tags instanceof Array) {
          this.basisData.tags = JSON.stringify(this.basisData.tags);
        }
        if (this.basisData.method == 'ESB') {
          let validataResult = this.$refs.esbDefinition.validateEsbDataStruct(this.request.esbDataStruct);
          if (!validataResult) {
            return;
          }
          if (this.request.esbDataStruct != null) {
            this.esbDataStruct = JSON.stringify(this.request.esbDataStruct);
            this.basisData.esbDataStruct = this.esbDataStruct;
          }
          if (this.request.backEsbDataStruct != null) {
            this.basisData.backEsbDataStruct = JSON.stringify(this.request.backEsbDataStruct);
          }
          if (this.request.backScript != null) {
            this.basisData.backScript = JSON.stringify(this.request.backScript);
          }
        } else {
          if (this.$refs.tcpFormatParameter) {
            this.$refs.tcpFormatParameter.validateXmlDataStruct();
          }
        }
        this.$emit('saveApi', this.basisData);
      }
    },
    runTest() {
      this.validateApi();
      if (this.validated) {
        this.basisData.request = this.request;
        if (this.basisData.tags instanceof Array) {
          this.basisData.tags = JSON.stringify(this.basisData.tags);
        }
        if (this.basisData.method == 'ESB') {
          let validataResult = this.$refs.esbDefinition.validateEsbDataStruct(this.request.esbDataStruct);
          if (!validataResult) {
            return;
          }
          if (this.request.esbDataStruct != null) {
            this.esbDataStruct = JSON.stringify(this.request.esbDataStruct);
            this.basisData.esbDataStruct = this.esbDataStruct;
          }
          if (this.request.backEsbDataStruct != null) {
            this.basisData.backEsbDataStruct = JSON.stringify(this.request.backEsbDataStruct);
          }
          if (this.request.backScript != null) {
            this.basisData.backScript = JSON.stringify(this.request.backScript);
          }
        } else {
          if (this.$refs.tcpFormatParameter) {
            this.$refs.tcpFormatParameter.validateXmlDataStruct();
          }
        }
        this.$emit('runTest', this.basisData);
      }
    },
    createRootModelInTree() {
      this.$emit("createRootModelInTree");
    },
    changeApiProtocol(protocol) {
      this.apiProtocol = protocol;
    },
    getMockInfo() {
      let projectId = getCurrentProjectID();
      this.$get("/api/environment/getTcpMockInfo/" + projectId, response => {
        this.mockInfo = response.data;
      });
    },
    mockSetting() {
      if (this.basisData.id) {
        this.$emit('changeTab', 'mock');
      } else {
        this.$alert(this.$t('api_test.mock.create_error'));
      }
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.basisData.follows.length; i++) {
          if (this.basisData.follows[i] === getCurrentUser().id) {
            this.basisData.follows.splice(i, 1);
            break;
          }
        }
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.basisData.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.basisData.follows) {
          this.basisData.follows = [];
        }
        this.basisData.follows.push(getCurrentUser().id);
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.basisData.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getVersionHistory() {
      this.$get('/api/definition/versions/' + this.basisData.id, response => {
        if (this.basisData.isCopy) {
          this.versionData = response.data.filter(v => v.versionId === this.basisData.versionId);
        } else {
          this.versionData = response.data;
        }
      });
    },
    compare(row) {
      this.$get('/api/definition/get/' +  row.id+"/"+this.basisData.refId, response => {
        this.$get('/api/definition/get/' + response.data.id, res => {
          if (res.data) {
            this.newData = res.data;
            if (this.newData.method !== 'TCP' && this.newData.method !== 'ESB') {
              this.newData.method = this.newData.protocol;
            }
            this.oldApiProtocol = this.basisData.method;
            if (this.oldApiProtocol == null || this.oldApiProtocol === "") {
              this.oldApiProtocol = "TCP";
            }
            this.dealWithTag(res.data);
            this.setRequest(res.data)
            if (!this.setRequest(res.data)) {
              this.oldRequest = createComponent("TCPSampler");
              this.dialogVisible = true;
            }
            this.formatApi(res.data)
          }
        });
      });

    },
    setRequest(api) {
      if (api.request !== undefined) {
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.oldRequest = api.request;
        } else {
          this.oldRequest = JSON.parse(api.request);
        }
        if (!this.oldRequest.headers) {
          this.oldRequest.headers = [];
        }
        this.dialogVisible = true;
        return true;
      }
      return false;
    },
    dealWithTag(api){
      if(api.tags){
        if(Object.prototype.toString.call(api.tags)==="[object String]"){
          api.tags = JSON.parse(api.tags);
        }
      }
      if(this.basisData.tags){
        if(Object.prototype.toString.call(this.basisData.tags)==="[object String]"){
          this.basisData.tags = JSON.parse(this.basisData.tags);
        }
      }
    },
    formatApi(api) {
      if (api.response != null && api.response !== 'null' && api.response !== undefined) {
        if (Object.prototype.toString.call(api.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.oldResponse = api.response;
        } else {
          this.oldResponse = JSON.parse(api.response);
        }
      } else {
        this.oldResponse = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      if (!this.oldRequest.hashTree) {
        this.oldRequest.hashTree = [];
      }
      if (this.oldRequest.body && !this.oldRequest.body.binary) {
        this.oldRequest.body.binary = [];
      }
      // 处理导入数据缺失问题
      if (this.oldResponse.body) {
        let body = new Body();
        Object.assign(body, this.oldResponse.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.oldResponse.body = body;
      }
      this.oldRequest.clazzName = TYPE_TO_C.get(this.oldRequest.type);

      this.sort(this.oldRequest.hashTree);
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
    cancelCreateNewVersion() {
      this.createNewVersionVisible = false;
      this.getVersionHistory();
    },
    checkout(row) {
      let api = this.versionData.filter(v => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      this.$emit("checkout", api);
    },
    create(row) {
      // 创建新版本
      this.basisData.versionId = row.id;
      this.basisData.versionName = row.name;
      this.$set(this.basisData, 'newVersionRemark', !!this.basisData.remark);
      this.$set(this.basisData, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
      if (this.$refs.apiOtherInfo.relationshipCount > 0 || this.basisData.remark) {
        this.createNewVersionVisible = true;
      } else {
        this.saveApi();
      }
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/definition/delete/' + row.id + '/' + this.basisData.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    }
  },
};
</script>

<style scoped>
.mock-info {
  margin: 20px 45px;
}

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 120;
  top: 107px;
}
</style>
