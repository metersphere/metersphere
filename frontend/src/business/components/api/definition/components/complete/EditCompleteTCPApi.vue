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
      <ms-tcp-format-parameters :show-script="false" :request="request" ref="tcpFormatParameter"/>
    </div>
    <div v-else-if="apiProtocol=='ESB'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <esb-definition v-xpack v-if="showXpackCompnent" :show-script="false" :request="request" ref="esbDefinition"/>
      <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
      <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="true" :show-options-button="true"
                               :request="request"/>
      <!--      <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase"/>-->
    </div>
    <api-other-info :api="basisData"/>

    <ms-change-history ref="changeHistory"/>

  </div>

</template>

<script>
import MsTcpBasicApi from "./TCPBasicApi";
import MsTcpFormatParameters from "../request/tcp/TcpFormatParameters";
import MsChangeHistory from "../../../../history/ChangeHistory";
import {getCurrentProjectID, getCurrentUser, hasLicense} from "@/common/js/utils";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "MsAddCompleteTcpApi",
  components: {
    ApiOtherInfo, MsTcpBasicApi, MsTcpFormatParameters, MsChangeHistory,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default,
    'MsVersionHistory': versionHistory.default,
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
      this.$refs.changeHistory.open(this.basisData.id, ["接口定义", "接口定義", "Api definition"]);
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
        this.versionData = response.data;
      });
    },
    compare(row) {
      // console.log(row);
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
      this.saveApi();
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
