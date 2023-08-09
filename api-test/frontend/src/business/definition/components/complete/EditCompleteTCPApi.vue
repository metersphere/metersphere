<template xmlns:el-col="http://www.w3.org/1999/html">
  <!-- 操作按钮 -->
  <div style="background-color: white">
    <el-row>
      <el-col>
        <!--操作按钮-->
        <div class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i
              class="el-icon-star-off"
              style="
                color: var(--primary_color);
                font-size: 25px;
                margin-right: 5px;
                position: relative;
                top: 5px;
                cursor: pointer;
              "
              @click="saveFollow" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i
              class="el-icon-star-on"
              style="
                color: var(--primary_color);
                font-size: 28px;
                margin-right: 5px;
                position: relative;
                top: 5px;
                cursor: pointer;
              "
              @click="saveFollow" />
          </el-tooltip>
          <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="basisData.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <mx-version-history
            v-xpack
            ref="versionHistory"
            :version-data="versionData"
            :current-id="basisData.id"
            :has-latest="hasLatest"
            @compare="compare"
            @checkout="checkout"
            @create="create"
            @setLatest="setLatest"
            @del="del" />
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s" :disabled="disableSaveBtn" v-prevent-re-click
                     v-permission="[
                       'PROJECT_API_DEFINITION:READ+EDIT_API',
                       'PROJECT_API_DEFINITION:READ+CREATE_API',
                       'PROJECT_API_DEFINITION:READ+COPY_API'
                     ]">{{ $t('commons.save') }}</el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 请求参数 -->
    <p class="tip">{{ $t('api_test.definition.request.req_param') }}</p>
    <ms-tcp-format-parameters
      :show-pre-script="true"
      :show-script="false"
      :request="request"
      ref="tcpFormatParameter" />
    <api-other-info :api="basisData" ref="apiOtherInfo" />

    <ms-change-history ref="changeHistory" />
    <el-dialog :fullscreen="true" :visible.sync="dialogVisible" :destroy-on-close="true" width="100%">
      <t-c-p-api-version-diff
        v-if="dialogVisible"
        :old-data="basisData"
        :show-follow="showFollow"
        :new-data="newData"
        :new-show-follow="newShowFollow"
        :module-options="moduleOptions"
        :request="newRequest"
        :old-request="request"
        :mock-info="mockInfo"
        :api-protocol="apiProtocol"
        :old-api-protocol="newApiProtocol"
        :method-types="methodTypes"></t-c-p-api-version-diff>
    </el-dialog>

    <el-dialog
      :title="$t('commons.sync_other_info')"
      :visible.sync="createNewVersionVisible"
      :show-close="false"
      width="30%">
      <div>
        <el-checkbox v-model="basisData.newVersionRemark">{{ $t('commons.remark') }}</el-checkbox>
        <el-checkbox v-model="basisData.newVersionDeps">{{ $t('commons.relationship.name') }}</el-checkbox>
        <el-checkbox v-model="basisData.newVersionCase">CASE</el-checkbox>
        <el-checkbox v-model="basisData.newVersionMock">MOCK</el-checkbox>
      </div>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="cancelCreateNewVersion" :title="$t('commons.edit_info')" @confirm="saveApi">
        </ms-dialog-footer>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { createMockConfig, getTcpMockInfo } from '@/api/api-mock';
import {
  definitionFollow,
  delDefinitionByRefId,
  getDefinitionById,
  getDefinitionByIdAndRefId,
  getDefinitionVersions,
  updateDefinitionFollows,
} from '@/api/definition';
import MsTcpBasicApi from './TCPBasicApi';
import MsTcpFormatParameters from '../request/tcp/TcpFormatParameters';
import MsChangeHistory from '@/business/history/ApiHistory';
import { getCurrentProjectID, getCurrentUser } from 'metersphere-frontend/src/utils/token';
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import ApiOtherInfo from '@/business/definition/components/complete/ApiOtherInfo';
import TCPApiVersionDiff from './version/TCPApiVersionDiff';
import { createComponent } from '.././jmeter/components';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import { useApiStore } from '@/store';
import { apiTestCaseCount } from '@/api/api-test-case';
import { getDefaultVersion, setLatestVersionById } from 'metersphere-frontend/src/api/version';

const store = useApiStore();
const { Body } = require('@/business/definition/model/ApiTestModel');
store;
export default {
  name: 'MsAddCompleteTcpApi',
  components: {
    MsDialogFooter,
    ApiOtherInfo,
    MsTcpBasicApi,
    MsTcpFormatParameters,
    MsChangeHistory,
    TCPApiVersionDiff,
    MxVersionHistory: () => import('metersphere-frontend/src/components/version/MxVersionHistory'),
  },
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    syncTabs: Array,
  },
  data() {
    return {
      validated: false,
      apiProtocol: 'TCP',
      mockInfo: '',
      showFollow: false,
      methodTypes: [
        {
          key: 'TCP',
          value: this.$t('api_test.request.tcp.general_format'),
        },
      ],
      versionData: [],
      dialogVisible: false,
      newShowFollow: false,
      newData: {},
      newRequest: {},
      newResponse: {},
      newApiProtocol: 'TCP',
      createNewVersionVisible: false,
      latestVersionId: '',
      hasLatest: false,
      disableSaveBtn: false
    };
  },
  created: function () {
    if (this.basisData.method != 'TCP' && this.basisData.method != 'ESB') {
      this.basisData.method = this.basisData.protocol;
    }
    this.apiProtocol = this.basisData.method;
    if (this.apiProtocol == null || this.apiProtocol == '') {
      this.apiProtocol = 'TCP';
    }
    definitionFollow(this.basisData.id).then((response) => {
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
      this.getDefaultVersion();
    }
  },
  watch: {
    syncTabs() {
      if (this.basisData && this.syncTabs && this.syncTabs.includes(this.basisData.id)) {
        // 标示接口在其他地方更新过，当前页面需要同步
        getDefinitionById(this.basisData.id).then((response) => {
          if (response.data) {
            let request = JSON.parse(response.data.request);
            let index = this.syncTabs.findIndex((item) => {
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
      this.$refs.changeHistory.open(this.basisData.id, ['接口定义', '接口定義', 'Api definition', 'API_DEFINITION']);
    },
    callback() {
      this.validated = true;
    },
    saveApi() {
      if (this.basisData.tags instanceof Array) {
        this.basisData.tags = JSON.stringify(this.basisData.tags);
      }

      if (this.$refs.tcpFormatParameter) {
        this.$refs.tcpFormatParameter.validateXmlDataStruct();
      }
      this.$emit('saveApi', this.basisData);
      if (this.$refs.versionHistory) {
        this.$refs.versionHistory.loading = false;
      }
    },
    runTest() {
      this.validateApi();
      if (this.validated) {
        this.basisData.request = this.request;
        if (this.basisData.tags instanceof Array) {
          this.basisData.tags = JSON.stringify(this.basisData.tags);
        }

        if (this.$refs.tcpFormatParameter) {
          this.$refs.tcpFormatParameter.validateXmlDataStruct();
        }
        this.$emit('runTest', this.basisData);
      }
    },
    createRootModelInTree() {
      this.$emit('createRootModelInTree');
    },
    changeApiProtocol(protocol) {
      this.apiProtocol = protocol;
    },
    getMockInfo() {
      let projectId = getCurrentProjectID();
      getTcpMockInfo(projectId).then((response) => {
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
          updateDefinitionFollows(this.basisData.id, this.basisData.follows).then(() => {
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
          updateDefinitionFollows(this.basisData.id, this.basisData.follows).then(() => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getDefaultVersion() {
      getDefaultVersion(getCurrentProjectID()).then((response) => {
        this.latestVersionId = response.data;
        this.getVersionHistory();
      });
    },

    getVersionHistory() {
      getDefinitionVersions(this.basisData.id).then((response) => {
        if (this.basisData.isCopy) {
          this.versionData = response.data.filter((v) => v.versionId === this.basisData.versionId);
        } else {
          this.versionData = response.data;
        }
        let latestVersionData = response.data.filter((v) => v.versionId === this.latestVersionId);
        if (latestVersionData.length > 0) {
          this.hasLatest = false;
        } else {
          this.hasLatest = true;
        }
      });
    },
    compare(row) {
      this.basisData.createTime = this.$refs.versionHistory.versionOptions.filter(
        (v) => v.id === this.basisData.versionId
      )[0].createTime;
      getDefinitionByIdAndRefId(row.id, this.basisData.refId).then((response) => {
        getDefinitionById(response.data.id).then((res) => {
          if (res.data) {
            this.newData = res.data;
            this.newData.createTime = row.createTime;
            if (this.newData.method !== 'TCP' && this.newData.method !== 'ESB') {
              this.newData.method = this.newData.protocol;
            }
            this.newApiProtocol = this.basisData.method;
            if (this.newApiProtocol == null || this.newApiProtocol === '') {
              this.newApiProtocol = 'TCP';
            }
            this.dealWithTag(res.data);
            this.setRequest(res.data);
            if (!this.setRequest(res.data)) {
              this.newRequest = createComponent('TCPSampler');
              this.dialogVisible = true;
            }
            this.formatApi(res.data);
          }
        });
      });
    },
    setRequest(api) {
      if (api.request) {
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          this.newRequest = api.request;
        } else {
          this.newRequest = JSON.parse(api.request);
        }
        if (!this.newRequest.headers) {
          this.newRequest.headers = [];
        }
        this.dialogVisible = true;
        return true;
      }
      return false;
    },
    dealWithTag(api) {
      if (api.tags) {
        if (Object.prototype.toString.call(api.tags) === '[object String]') {
          api.tags = JSON.parse(api.tags);
        }
      }
      if (this.basisData.tags) {
        if (Object.prototype.toString.call(this.basisData.tags) === '[object String]') {
          this.basisData.tags = JSON.parse(this.basisData.tags);
        }
      }
    },
    formatApi(api) {
      if (api.response != null && api.response !== 'null' && api.response) {
        if (
          Object.prototype.toString
            .call(api.response)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          this.newResponse = api.response;
        } else {
          this.newResponse = JSON.parse(api.response);
        }
      } else {
        this.newResponse = {
          headers: [],
          body: new Body(),
          statusCode: [],
          type: 'HTTP',
        };
      }
      if (!this.newRequest.hashTree) {
        this.newRequest.hashTree = [];
      }
      if (this.newRequest.body && !this.newRequest.body.binary) {
        this.newRequest.body.binary = [];
      }
      // 处理导入数据缺失问题
      if (this.newResponse.body) {
        let body = new Body();
        Object.assign(body, this.newResponse.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.newResponse.body = body;
      }
      this.newRequest.clazzName = TYPE_TO_C.get(this.newRequest.type);

      this.sort(this.newRequest.hashTree);
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
    cancelCreateNewVersion() {
      this.createNewVersionVisible = false;
      this.getVersionHistory();
    },
    checkout(row) {
      let api = this.versionData.filter((v) => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      this.$emit('checkout', api);
    },
    create(row) {
      // 创建新版本
      this.basisData.versionId = row.id;
      this.basisData.versionName = row.name;
      apiTestCaseCount({ id: this.basisData.id }).then((response) => {
        if (response.data > 0) {
          this.basisData.caseTotal = response.data;
        }
        this.$set(this.basisData, 'newVersionRemark', !!this.basisData.remark);
        this.$set(this.basisData, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
        this.$set(this.basisData, 'newVersionCase', this.basisData.caseTotal > 0);

        createMockConfig({
          projectId: getCurrentProjectID(),
          apiId: this.basisData.id,
        }).then((response) => {
          this.$set(this.basisData, 'newVersionMock', response.data.mockExpectConfigList.length > 0);

          if (
            this.$refs.apiOtherInfo.relationshipCount > 0 ||
            this.basisData.remark ||
            this.basisData.newVersionCase ||
            this.basisData.newVersionMock
          ) {
            this.createNewVersionVisible = true;
          } else {
            this.saveApi();
            if (this.$refs.versionHistory) {
              this.$refs.versionHistory.loading = false;
            }
          }
        });
      });
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            delDefinitionByRefId(row.id, this.basisData.refId).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        },
      });
    },
    setLatest(row) {
      let param = {
        projectId: getCurrentProjectID(),
        type: 'API',
        versionId: row.id,
        resourceId: this.basisData.id,
      };
      setLatestVersionById(param).then(() => {
        this.$success(this.$t('commons.modify_success'));
        this.checkout(row);
      });
    },
  },
};
</script>

<style scoped>
.mock-info {
  margin: 20px 45px;
}

.ms-opt-btn {
  position: fixed;
  right: 10px !important;
  z-index: 120;
  top: 85px;
}
</style>
