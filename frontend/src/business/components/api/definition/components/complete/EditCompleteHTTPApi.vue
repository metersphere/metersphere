<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">
      <el-form :model="httpForm" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->
        <div style="float: right;margin-right: 20px" class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: var(--primary_color); font-size: 25px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: var(--primary_color); font-size: 28px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="httpForm.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <ms-version-history v-xpack
                              ref="versionHistory"
                              :version-data="versionData"
                              :current-id="httpForm.id"
                              @compare="compare" @checkout="checkout" @create="create" @del="del"/>
          <el-button v-if="!isXpack || !apiSyncRuleRelation.showUpdateRule" type="primary" size="small"
                     @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
          <el-dropdown v-else
                       v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
                       split-button type="primary" size="small" @click="saveApi" @command="handleCommand">
            {{ $t('commons.save') }}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="openSyncRule">{{
                  $t('commons.save') + '&' + $t('workstation.sync') + $t('commons.setting')
                }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <br/>
        <div class="base-info">
          <el-row>
            <el-col :span="16">
              <el-form-item :label="$t('api_report.request')" prop="path">
                <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="httpForm.path"
                          class="ms-http-input" size="small" style="margin-top: 5px" @change="urlChange">
                  <el-select v-model="httpForm.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
                  </el-select>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 请求参数 -->
        <div>
          <ms-form-divider :title="$t('api_test.definition.request.req_param')"/>
          <ms-api-request-form :showScript="false" :request="request" :headers="request.headers"
                               :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <ms-form-divider :title="$t('api_test.definition.request.res_param')"/>
      <ms-response-text :response="response"/>

      <api-other-info :api="httpForm" ref="apiOtherInfo"/>

      <ms-change-history ref="changeHistory"/>

      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%"
      >
        <http-api-version-diff
          v-if="dialogVisible"
          :old-data="httpForm"
          :show-follow="showFollow"
          :new-data="newData"
          :new-show-follow="newShowFollow"
          :old-mock-url="getUrlPrefix"
          :new-mock-url="newMockUrl"
          :rule="rule"
          :maintainer-options="maintainerOptions"
          :module-options="moduleOptions"
          :request="newRequest"
          :old-request="request"
          :response="newResponse"
          :old-response="response"
        ></http-api-version-diff>
      </el-dialog>

    </el-card>

    <el-dialog
      :title="$t('commons.sync_other_info')"
      :visible.sync="createNewVersionVisible"
      :show-close="false"
      width="30%"
    >
      <div>
        <el-checkbox v-model="httpForm.newVersionRemark">{{ $t('commons.remark') }}</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionDeps">{{ $t('commons.relationship.name') }}</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionCase">CASE</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionMock">MOCK</el-checkbox>
      </div>

      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="cancelCreateNewVersion"
          :title="$t('commons.edit_info')"
          @confirm="saveApi">
        </ms-dialog-footer>
      </template>
    </el-dialog>

    <el-dialog :visible.sync="batchSyncApiVisible"
               :title="$t('commons.save')+'&'+$t('workstation.sync')+$t('commons.setting')" v-if="isXpack">
      <el-row style="margin-bottom: 10px;box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)">
        <div class="timeClass">
          <span style="font-size: 16px;font-weight: bold;padding-left: 10px;">{{
              $t('api_test.definition.one_click_sync') + "case"
            }}</span>
          <el-switch v-model="apiSyncRuleRelation.syncCase" style="padding-right: 10px"></el-switch>
        </div>
        <br/>
        <span style="font-size: 12px;padding-left: 10px;">{{ $t('workstation.batch_sync_api_tips') }}</span><br/><br/>
        <span v-if="apiSyncRuleRelation.syncCase" style="font-size: 16px; font-weight: bold;padding-left: 10px;">
        {{ $t('workstation.sync') + $t('commons.setting') }}
        <i class="el-icon-arrow-down" v-if="showApiSyncConfig" @click="showApiSyncConfig=false"/>
        <i class="el-icon-arrow-right" v-if="!showApiSyncConfig" @click="showApiSyncConfig=true"/>
      </span><br/><br/>
        <div v-if="showApiSyncConfig">
          <sync-setting style="padding-left: 20px" v-if="apiSyncRuleRelation.syncCase"
                        v-bind:sync-data="apiSyncRuleRelation.apiSyncConfig"
                        ref="synSetting" @updateSyncData="updateSyncData"></sync-setting>
        </div>
      </el-row>
      <el-row style="margin-bottom: 10px;box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)">
        <div class="timeClass">
          <span style="font-size: 16px;font-weight: bold;padding-left: 10px;">
            {{ $t('api_test.definition.change_notification') }}
            <el-tooltip class="ms-num" effect="dark"
                        :content="$t('project_application.workstation.api_receiver_tip')"
                        placement="top">
                  <i class="el-icon-warning"/>
            </el-tooltip>
          </span>
          <el-switch v-model="apiSyncRuleRelation.sendNotice" style="padding-right: 10px"></el-switch>
        </div>
        <span style="font-size: 12px;padding-left: 10px;">
          {{ $t('api_test.definition.recipient_tips') }}
        </span><br/>
        <p
          style="font-size: 12px;color: var(--primary_color);margin-bottom: 20px;text-decoration:underline;cursor: pointer;padding-left: 10px;"
          @click="gotoApiMessage">
          {{ $t('project_application.workstation.go_to_api_message') }}
        </p>
        <el-row v-if="apiSyncRuleRelation.sendNotice" style="margin-bottom: 5px;margin-top: 5px">
          <el-col :span="4"><span
            style="font-weight: bold;padding-left: 10px;">{{ $t('api_test.definition.recipient') + ":" }}</span>
          </el-col>
          <el-col :span="20" style="color: var(--primary_color)">
            <el-checkbox v-model="apiSyncRuleRelation.caseCreator">{{ 'CASE' + $t('api_test.creator') }}</el-checkbox>
            <el-checkbox v-model="apiSyncRuleRelation.scenarioCreator">
              {{ $t('commons.scenario') + $t('api_test.creator') }}
            </el-checkbox>
          </el-col>
        </el-row>
      </el-row>
      <el-row>
        <el-checkbox v-model="apiSyncRuleRelation.showUpdateRule" style="padding-left: 10px;">{{
            $t('project_application.workstation.no_show_setting')
          }}
        </el-checkbox>
        <el-tooltip class="ms-num" effect="dark"
                    :content="$t('project_application.workstation.no_show_setting_tip')"
                    placement="top">
          <i class="el-icon-warning"/>
        </el-tooltip>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchSyncApiVisible = false">取 消</el-button>
        <el-button type="primary" @click="batchSync()">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>

import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import MsResponseText from "../response/ResponseText";
import {API_STATUS, REQ_METHOD} from "../../model/JsonData";
import {KeyValue} from "../../model/ApiTestModel";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";
import MsSelectTree from "../../../../common/select-tree/SelectTree";
import MsChangeHistory from "../../../../history/ChangeHistory";
import {getCurrentUser, getUUID, hasLicense} from "@/common/js/utils";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";
import HttpApiVersionDiff from "./version/HttpApiVersionDiff";
import {createComponent} from ".././jmeter/components";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getProjectMemberOption} from "@/network/user";
import {deepClone} from "@/common/js/tableUtils";
import SyncSetting from "@/business/components/api/definition/util/SyncSetting";

const {Body} = require("@/business/components/api/definition/model/ApiTestModel");
const Sampler = require("@/business/components/api/definition/components/jmeter/components/sampler/sampler");

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "MsAddCompleteHttpApi",
  components: {
    MsDialogFooter,
    'MsVersionHistory': versionHistory.default,
    ApiOtherInfo,
    MsFormDivider,
    MsJsr233Processor, MsResponseText, MsApiRequestForm, MsInputTag, MsSelectTree, MsChangeHistory,
    HttpApiVersionDiff, SyncSetting,
  },
  data() {
    let validateURL = (rule, value, callback) => {
      if (!this.httpForm.path.startsWith("/")) {
        callback(this.$t('api_test.definition.request.path_valid_info'));
      }
      callback();
    };
    let validateModuleId = (rule, value, callback) => {
      if (this.httpForm.moduleId.length === 0 || !this.httpForm.moduleId) {
        callback(this.$t('test_track.case.input_module'));
      } else {
        callback();
      }
    };
    return {
      rule: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {
          validator: validateURL,
          trigger: 'blur'
        }],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        moduleId: [{required: true, validator: validateModuleId, trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
      },
      httpForm: {environmentId: "", path: "", tags: []},
      beforeHttpForm: {environmentId: "", path: "", tags: []},
      beforeRequest: {arguments: []},
      beforeResponse: {},
      newData: {environmentId: "", path: "", tags: []},
      dialogVisible: false,
      isShowEnable: true,
      showFollow: false,
      newShowFollow: false,
      maintainerOptions: [],
      currentModule: {},
      reqOptions: REQ_METHOD,
      options: API_STATUS,
      mockEnvironment: {},
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      mockBaseUrl: "",
      newMockBaseUrl: "",
      count: 0,
      versionData: [],
      newRequest: Sampler,
      newResponse: {},
      createNewVersionVisible: false,
      batchSyncApiVisible: false,
      isXpack: false,
      showApiSyncConfig: true,
      apiSyncRuleRelation: {
        caseCreator: true,
        scenarioCreator: true,
        showUpdateRule: false,
        apiSyncCaseRequest: '',
        apiSyncConfig: {},
        syncCase: true,
        sendNotice: true,
      },
      noShowSyncRuleRelation: false,
      citedScenarioCount: 0
    };
  },
  props: {moduleOptions: {}, request: {}, response: {}, basisData: {}, syncTabs: Array, projectId: String},
  watch: {
    'httpForm.path': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
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
            this.httpForm.path = response.data.path;
            this.httpForm.method = response.data.method;
            Object.assign(this.request, request);
          }
        });
      }
    },
    batchSyncApiVisible() {
      if (!this.batchSyncApiVisible && this.apiSyncRuleRelation.showUpdateRule) {
        this.noShowSyncRuleRelation = true;
      }
    }
  },
  computed: {
    getUrlPrefix() {
      if (this.httpForm.path == null) {
        return this.mockBaseUrl;
      } else {
        let path = this.httpForm.path;
        let protocol = this.httpForm.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.httpForm.request != null && this.httpForm.request.rest != null) {
            let pathUrlArr = path.split("/");
            let newPath = "";
            pathUrlArr.forEach(item => {
              if (item !== "") {
                let pathItem = item;
                if (item.indexOf("{") === 0 && item.indexOf("}") === (item.length - 1)) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.httpForm.request.rest.length; i++) {
                    let param = this.httpForm.request.rest[i];
                    if (param.name === paramItem) {
                      pathItem = param.value;
                    }
                  }
                }
                newPath += "/" + pathItem;
              }
            });
            if (newPath !== "") {
              path = newPath;
            }
          }
        }
        return this.mockBaseUrl + path;
      }
    },
    newMockUrl() {
      if (this.newData.path == null) {
        return this.newMockBaseUrl;
      } else {
        let path = this.newData.path;
        let protocol = this.newData.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.newData.request != null && this.newData.request.rest != null) {
            let pathUrlArr = path.split("/");
            let newPath = "";
            pathUrlArr.forEach(item => {
              if (item !== "") {
                let pathItem = item;
                if (item.indexOf("{") === 0 && item.indexOf("}") === (item.length - 1)) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.newData.request.rest.length; i++) {
                    let param = this.newData.request.rest[i];
                    if (param.name === paramItem) {
                      pathItem = param.value;
                    }
                  }
                }
                newPath += "/" + pathItem;
              }
            });
            if (newPath !== "") {
              path = newPath;
            }
          }
        }
        return this.newMockBaseUrl + path;
      }
    }
  },
  methods: {
    apiMapStatus() {
      this.$store.state.apiStatus.set("fromChange", true);
      if (this.httpForm.id) {
        this.$store.state.apiMap.set(this.httpForm.id, this.$store.state.apiStatus);
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    openHis() {
      this.$refs.changeHistory.open(this.httpForm.id, ["接口定义", "接口定義", "Api definition", "API_DEFINITION"]);
    },
    mockSetting() {
      if (this.basisData.id) {
        this.$store.state.currentApiCase = {mock: getUUID()};
        this.$emit('changeTab', 'mock');
      } else {
        this.$alert(this.$t('api_test.mock.create_error'));
      }
    },
    runTest() {
      this.$refs['httpForm'].validate((valid) => {
        if (valid) {
          this.setParameter();
          this.$emit('runTest', this.httpForm);
        } else {
          return false;
        }
      });
    },
    getMaintainerOptions() {
      getProjectMemberOption(data => {
        this.maintainerOptions = data;
      });
    },
    setParameter() {
      this.request.path = this.httpForm.path;
      this.request.method = this.httpForm.method;
      this.httpForm.request.useEnvironment = undefined;
      if (this.httpForm.tags instanceof Array) {
        this.httpForm.tags = JSON.stringify(this.httpForm.tags);
      }
      if (this.beforeHttpForm.tags instanceof Array) {
        this.beforeHttpForm.tags = JSON.stringify(this.beforeHttpForm.tags);
      }
    },
    saveApi() {
      this.$refs['httpForm'].validate((valid) => {
        if (valid) {
          this.setParameter();
          if (!this.httpForm.versionId) {
            if (this.$refs.versionHistory && this.$refs.versionHistory.currentVersion) {
              this.httpForm.versionId = this.$refs.versionHistory.currentVersion.id;
            }
          }
          if (hasLicense() && (this.httpForm.caseTotal > 0 || this.citedScenarioCount > 0) && !this.httpForm.isCopy) {
            if ((this.httpForm.method !== this.beforeHttpForm.method) && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
            }
            if ((this.httpForm.path !== this.beforeHttpForm.path) && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
            }
            if (this.request.headers && this.beforeRequest.headers) {
              let submitRequestHeaders = JSON.stringify(this.request.headers);
              let beforeRequestHeaders = JSON.stringify(this.beforeRequest.headers);
              if ((submitRequestHeaders !== beforeRequestHeaders) && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
              }
            }
            if (this.request.arguments && this.beforeRequest.arguments) {
              let submitRequestQuery = JSON.stringify(this.request.arguments);
              let beforeRequestQuery = JSON.stringify(this.beforeRequest.arguments);
              if ((submitRequestQuery !== beforeRequestQuery) && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
              }
            }
            if (this.request.rest && this.beforeRequest.rest) {
              let submitRequestRest = JSON.stringify(this.request.rest);
              let beforeRequestRest = JSON.stringify(this.beforeRequest.rest);
              if ((submitRequestRest !== beforeRequestRest) && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
              }
            }
            if (this.request.body && this.beforeRequest.body) {
              let submitRequestBody = JSON.stringify(this.request.body);
              let beforeRequestBody = JSON.stringify(this.beforeRequest.body);
              if ((submitRequestBody !== beforeRequestBody) && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
              }
            }
            if (this.apiSyncRuleRelation.showUpdateRule === true && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
            }
            if (this.batchSyncApiVisible !== true) {
              this.$emit('saveApi', this.httpForm);
            }
          } else {
            this.$emit('saveApi', this.httpForm);
          }
          this.count = 0;
          this.$store.state.apiStatus.set("fromChange", false);
          this.$store.state.apiMap.set(this.httpForm.id, this.$store.state.apiStatus);
        } else {
          if (this.$refs.versionHistory) {
            this.$refs.versionHistory.loading = false;
          }
          return false;
        }
      });
    },
    batchSync() {
      if (hasLicense() && (this.httpForm.caseTotal > 0 || this.citedScenarioCount > 0) && !this.httpForm.isCopy) {
        if (this.$refs.synSetting && this.$refs.synSetting.fromData) {
          let fromData = this.$refs.synSetting.fromData;
          fromData.method = true;
          fromData.path = true;
          fromData.protocol = true;
          this.httpForm.triggerUpdate = JSON.stringify(fromData);
          this.apiSyncRuleRelation.apiSyncCaseRequest = JSON.stringify(fromData);
        }
        if (this.apiSyncRuleRelation.sendNotice && this.apiSyncRuleRelation.sendNotice === true) {
          this.httpForm.sendSpecialMessage = this.apiSyncRuleRelation.sendNotice;
        } else {
          this.httpForm.sendSpecialMessage = false
        }

        if (this.apiSyncRuleRelation.caseCreator && this.apiSyncRuleRelation.caseCreator === true) {
          this.httpForm.caseCreator = this.apiSyncRuleRelation.caseCreator;
        } else {
          this.httpForm.caseCreator = false
        }
        if (this.apiSyncRuleRelation.scenarioCreator && this.apiSyncRuleRelation.scenarioCreator === true) {
          this.httpForm.scenarioCreator = this.apiSyncRuleRelation.scenarioCreator;
        } else {
          this.httpForm.scenarioCreator = false
        }
        this.apiSyncRuleRelation.resourceId = this.httpForm.id;
        this.apiSyncRuleRelation.resourceType = "API";
        this.saveApiSyncRuleRelation(this.apiSyncRuleRelation);
      }
    },
    saveApiSyncRuleRelation(apiSyncRuleRelation) {
      this.$post("/api/update/rule/relation/add/" + apiSyncRuleRelation.resourceId, apiSyncRuleRelation, () => {
        this.$emit('saveApi', this.httpForm);
      });
    },
    createModules() {
      this.$emit("createRootModelInTree");
    },
    urlChange() {
      if (!this.httpForm.path || this.httpForm.path.indexOf('?') === -1) return;
      let url = this.getURL(this.addProtocol(this.httpForm.path));
      if (url) {
        this.httpForm.path = decodeURIComponent(this.httpForm.path.substr(0, this.httpForm.path.indexOf("?")));
      }
    },
    addProtocol(url) {
      if (url) {
        if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
          return "https://" + url;
        }
      }
      return url;
    },
    getURL(urlStr) {
      try {
        let url = new URL(urlStr);
        if (url.search && url.search.length > 1) {
          let params = url.search.substr(1).split("&");
          params.forEach(param => {
            if (param) {
              let keyValues = param.split("=");
              if (keyValues) {
                this.request.arguments.splice(0, 0, new KeyValue({
                  name: keyValues[0],
                  required: false,
                  value: keyValues[1]
                }));
              }
            }
          });
        }
        return url;
      } catch (e) {
        this.$error(this.$t('api_test.request.url_invalid'), 2000);
      }
    },
    setModule(id, data) {
      if (id && id !== "") {
        this.httpForm.moduleId = id;
      }
      if (data && data.path) {
        this.httpForm.modulePath = data.path;
      }
    },
    initMockEnvironment() {
      let protocol = document.location.protocol;
      protocol = protocol.substring(0, protocol.indexOf(":"));
      let url = "/api/definition/getMockEnvironment/";
      this.$get(url + this.projectId, response => {
        this.mockEnvironment = response.data;
        let httpConfig = JSON.parse(this.mockEnvironment.config);
        if (httpConfig != null) {
          httpConfig = httpConfig.httpConfig;
          let httpType = httpConfig.defaultCondition;
          let conditions = httpConfig.conditions;
          conditions.forEach(condition => {
            if (condition.type === httpType) {
              this.mockBaseUrl = condition.protocol + "://" + condition.socket;
              this.newMockBaseUrl = this.mockBaseUrl;
            }
          });
        }
      });
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.httpForm.follows.length; i++) {
          if (this.httpForm.follows[i] === this.currentUser().id) {
            this.httpForm.follows.splice(i, 1);
            break;
          }
        }
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.httpForm.follows) {
          this.httpForm.follows = [];
        }
        this.httpForm.follows.push(this.currentUser().id);
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getVersionHistory() {
      this.$get('/api/definition/versions/' + this.httpForm.id, response => {
        if (this.httpForm.isCopy) {
          this.versionData = response.data.filter(v => v.versionId === this.httpForm.versionId);
        } else {
          this.versionData = response.data;
        }
      });
    },
    compare(row) {
      this.httpForm.createTime = this.$refs.versionHistory.versionOptions.filter(v => v.id === this.httpForm.versionId)[0].createTime;
      this.$get('/api/definition/get/' + row.id + "/" + this.httpForm.refId, response => {
        this.$get('/api/definition/get/' + response.data.id, res => {
          if (res.data) {
            this.newData = res.data;
            this.newData.createTime = row.createTime;
            this.dealWithTag(res.data);
            this.setRequest(res.data);
            if (!this.setRequest(res.data)) {
              this.newRequest = createComponent("HTTPSamplerProxy");
              this.dialogVisible = true;
            }
            this.formatApi(res.data);
          }
        });
      });
    },
    setRequest(api) {
      if (api.request !== undefined) {
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
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
        if (Object.prototype.toString.call(api.tags) === "[object String]") {
          api.tags = JSON.parse(api.tags);
        }
      }
      if (this.httpForm.tags) {
        if (Object.prototype.toString.call(this.httpForm.tags) === "[object String]") {
          this.httpForm.tags = JSON.parse(this.httpForm.tags);
        }
      }
    },
    formatApi(api) {
      if (api.response != null && api.response !== 'null' && api.response !== undefined) {
        if (Object.prototype.toString.call(api.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.newResponse = api.response;
        } else {
          this.newResponse = JSON.parse(api.response);
        }
      } else {
        this.newResponse = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
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
      this.httpForm.versionId = row.id;
      this.httpForm.versionName = row.name;
      this.$set(this.httpForm, 'newVersionRemark', !!this.httpForm.remark);
      this.$set(this.httpForm, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
      this.$set(this.httpForm, 'newVersionCase', this.httpForm.caseTotal > 0);

      this.$post('/mockConfig/genMockConfig', {projectId: this.projectId, apiId: this.httpForm.id}, response => {
        this.$set(this.httpForm, 'newVersionMock', response.data.mockExpectConfigList.length > 0);

        if (this.$refs.apiOtherInfo.relationshipCount > 0 || this.httpForm.remark ||
          this.httpForm.newVersionCase || this.httpForm.newVersionMock) {
          this.createNewVersionVisible = true;
        } else {
          this.saveApi();
          if (this.$refs.versionHistory) {
            this.$refs.versionHistory.loading = false;
          }
        }
      }, error => {
        if (this.$refs.versionHistory) {
          this.$refs.versionHistory.loading = false;
        }
      });
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/definition/delete/' + row.id + '/' + this.httpForm.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    },
    gotoApiMessage() {
      let apiResolve = this.$router.resolve({
        name: 'MessageSettings'
      });
      window.open(apiResolve.href, '_blank');
    },
    getSyncRule() {
      this.$get('/api/update/rule/relation/get/' + this.httpForm.id + '/API', response => {
        if (response.data) {
          this.apiSyncRuleRelation = response.data;
          if (this.apiSyncRuleRelation.apiSyncCaseRequest) {
            this.apiSyncRuleRelation.apiSyncConfig = JSON.parse(this.apiSyncRuleRelation.apiSyncCaseRequest);
          }
          if (this.apiSyncRuleRelation.caseCreator === null || this.apiSyncRuleRelation.caseCreator === undefined) {
            this.apiSyncRuleRelation.caseCreator = true;
          }
          if (this.apiSyncRuleRelation.scenarioCreator === null || this.apiSyncRuleRelation.scenarioCreator === undefined) {
            this.apiSyncRuleRelation.scenarioCreator = true;
          }
          if (this.apiSyncRuleRelation.syncCase === null || this.apiSyncRuleRelation.syncCase === undefined) {
            this.apiSyncRuleRelation.syncCase = true;
          }
          if (this.apiSyncRuleRelation.sendNotice === null || this.apiSyncRuleRelation.sendNotice === undefined) {
            this.apiSyncRuleRelation.sendNotice = true;
          }
          this.noShowSyncRuleRelation = this.apiSyncRuleRelation.showUpdateRule
        }
      });
    },
    updateSyncData(value) {
      this.apiSyncRuleRelation.apiSyncConfig = value;
    },
    handleCommand(command) {
      if (command === 'openSyncRule') {
        this.noShowSyncRuleRelation = false;
        this.saveApi();
      }
    },
    getCitedScenarioCount() {
      this.$get('/api/definition/be/cited/scenario/' + this.httpForm.id, response => {
        if (response.data) {
          this.citedScenarioCount = response.data;
        }
      });
    }
  },

  created() {
    this.getMaintainerOptions();
    this.isXpack = !!hasLicense();
    if (!this.basisData.environmentId) {
      this.basisData.environmentId = "";
    }
    if (this.basisData.moduleId && this.basisData.moduleId === 'default-module') {
      this.basisData.moduleId = this.moduleOptions[0].id;
    }
    if (this.basisData.isCopy) {
      this.basisData.caseTotal = 0;
    }
    this.httpForm = JSON.parse(JSON.stringify(this.basisData));

    this.$get('/api/definition/follow/' + this.basisData.id, response => {
      this.httpForm.follows = response.data;
      this.beforeHttpForm.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === this.currentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    });
    this.initMockEnvironment();

    if (hasLicense()) {
      this.getVersionHistory();
      this.getSyncRule();
      this.getCitedScenarioCount();
    }
  },
  mounted() {
    if (hasLicense()) {
      this.beforeHttpForm = deepClone(this.basisData);
      this.beforeRequest = deepClone(this.request);
      this.beforeResponse = deepClone(this.response);
    }
  }
};
</script>

<style scoped>
.timeClass {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 5px;
}

.base-info .el-form-item {
  width: 100%;
}

.mock-info {
  margin: 20px 45px;
}

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
  top: 128px;
}

/*.base-info .el-form-item >>> .el-form-item__content {*/
/*  width: 80%;*/
/*}*/

.base-info .ms-http-select {
  width: 100%;
}

.ms-http-textarea {
  width: 100%;
}

.ms-left-cell {
  margin-top: 100px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}
</style>
