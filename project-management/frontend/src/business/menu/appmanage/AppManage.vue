<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="loading">
        <el-card class="table-card">
          <el-tabs v-model="activeName" style="height: 600px" @tab-click="tabClick">
            <el-tab-pane :label="$t('commons.my_workstation')" name="my_workstation" v-if="isXpack">
              <el-row style="margin-top: 10px">
                <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
              </el-row>
              <el-row style="margin-top: 15px">
                <app-manage-item
                  :title="$t('workstation.custom_update_list_rule')"
                  :show-btn="true"
                  @clickBtn="openRuleSetting"
                  :disabled-btn="disabledRuleBtn"
                  v-if="isXpack">
                  <template #append>
                    <el-switch v-model="openUpdateRule" @change="openRule">
                      {{ $t('commons.setting') + $t('commons.rule') }}
                    </el-switch>
                  </template>
                </app-manage-item>
              </el-row>
            </el-tab-pane>

            <el-tab-pane :label="$t('test_track.test_track')" name="test_track">
              <el-row style="margin-top: 10px">
                <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
              </el-row>
              <el-row style="margin-top: 15px">
                <app-manage-item :title="$t('project.public')" :description="$t('project.public_info')"
                                 v-if="isXpack">
                  <template #append>
                    <el-switch v-model="config.casePublic" @change="switchChange('CASE_PUBLIC', $event)"></el-switch>
                  </template>
                </app-manage-item>

                <app-manage-item :title="$t('project.test_case_custom_id')"
                                 :description="$t('project.test_case_custom_id_info')">
                  <template #append>
                    <el-switch v-model="config.caseCustomNum"
                               @change="switchChange('CASE_CUSTOM_NUM', $event)"></el-switch>
                  </template>
                </app-manage-item>
                <timing-item ref="trackTimingItem" :choose.sync="config.cleanTrackReport"
                             :expr.sync="config.cleanTrackReportExpr"
                             @chooseChange="switchChange('CLEAN_TRACK_REPORT', config.cleanTrackReport, ['CLEAN_TRACK_REPORT_EXPR', config.cleanTrackReportExpr])"
                             :title="$t('project.timing_clean_plan_report')"/>
                <timing-item ref="trackTimingItem" :choose.sync="config.shareReport"
                             :expr.sync="config.trackShareReportTime" :share-link="true"
                             :unit-options="applyUnitOptions"
                             @chooseChange="switchChange('TRACK_SHARE_REPORT_TIME', config.trackShareReportTime, config.shareReport)"
                             :title="$t('report.report_sharing_link')"/>
              </el-row>
            </el-tab-pane>

            <el-tab-pane :label="$t('commons.api')" name="api">
              <el-row :gutter="20">
                <el-col :span="8" class="commons-api-enable">
                  <el-row style="margin-top: 10px">
                    <span style="font-weight:bold">{{ $t('commons.enable_settings') }}</span>
                  </el-row>
                  <el-row style="margin-top: 15px">
                    <app-manage-item :title="$t('project.repeatable')" :description="$t('project.repeatable_info')">
                      <template #append>
                        <el-switch v-model="config.urlRepeatable"
                                   @change="switchChange('URL_REPEATABLE', $event)"></el-switch>
                      </template>
                    </app-manage-item>
                    <app-manage-item :title="$t('project.scenario_custom_id')"
                                     :description="$t('project.scenario_custom_id_info')">
                      <template #append>
                        <el-switch v-model="config.scenarioCustomNum"
                                   @change="switchChange('SCENARIO_CUSTOM_NUM', $event)"></el-switch>
                      </template>
                    </app-manage-item>
                    <app-manage-item :title="'TCP Mock Port'" :prepend-span="8" :middle-span="12" :append-span="4">
                      <template #middle>
                        <el-input-number v-model="config.mockTcpPort" :controls="false" size="medium"
                                         :disabled="config.mockTcpOpen"></el-input-number>
                      </template>
                      <template #append>
                        <el-switch v-model="config.mockTcpOpen"
                                   @change="tcpMockSwitchChange($event, ['MOCK_TCP_PORT', config.mockTcpPort])"></el-switch>
                      </template>
                    </app-manage-item>
                    <timing-item ref="apiTimingItem" :choose.sync="config.cleanApiReport"
                                 :expr.sync="config.cleanApiReportExpr"
                                 @chooseChange="switchChange('CLEAN_API_REPORT', config.cleanApiReport, ['CLEAN_API_REPORT_EXPR', config.cleanApiReportExpr])"
                                 :title="$t('project.timing_clean_api_report')"/>
                    <timing-item ref="trackTimingItem" :choose.sync="config.shareReport"
                                 :expr.sync="config.apiShareReportTime" :share-link="true"
                                 :unit-options="applyUnitOptions"
                                 @chooseChange="switchChange('API_SHARE_REPORT_TIME', config.apiShareReportTime, config.shareReport)"
                                 :title="$t('report.report_sharing_link')"/>
                  </el-row>
                </el-col>
                <el-col :span="8" class="commons-view-setting">
                  <el-row style="margin-top: 10px">
                    <span style="font-weight:bold">{{ $t('commons.view_settings') }}</span>
                  </el-row>
                  <el-row style="margin-top: 15px">
                    <app-manage-item :title="$t('api_test.definition.api_quick_button')"
                                     :append-span="12" :prepend-span="12" :middle-span="0">
                      <template #append>
                        <el-radio-group v-model="config.apiQuickMenu" @change="switchChange('API_QUICK_MENU', $event)">
                          <el-radio label="debug" value="debug">
                            {{ $t('api_test.definition.request.fast_debug') }}
                          </el-radio>
                          <el-radio label="api" value="api">
                            {{ $t('api_test.definition.request.title') }}
                          </el-radio>
                        </el-radio-group>
                      </template>
                    </app-manage-item>
                  </el-row>
                </el-col>
              </el-row>
            </el-tab-pane>

            <!-- UI 测试 -->
            <el-tab-pane :label="$t('commons.ui_test')" name="ui_test">
              <!-- 启用设置 -->
              <el-col :span="8" class="commons-api-enable">
                <el-row style="margin-top: 10px">
                  <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
                </el-row>
                <el-row style="margin-top: 15px">
                  <timing-item ref="uiTimingItem" :choose.sync="config.cleanUiReport"
                               :expr.sync="config.cleanUiReportExpr"
                               @chooseChange="switchChange('CLEAN_UI_REPORT', config.cleanUiReport, ['CLEAN_UI_REPORT_EXPR', config.cleanUiReportExpr])"
                               :title="$t('pj_app_manage.timing_clean_ui_report')"/>
                  <timing-item ref="uiTimingItem" :choose.sync="config.shareReport"
                               :expr.sync="config.uiShareReportTime" :share-link="true"
                               :unit-options="applyUnitOptions"
                               @chooseChange="switchChange('UI_SHARE_REPORT_TIME', config.uiShareReportTime, config.shareReport)"
                               :title="$t('report.report_sharing_link')"/>
                </el-row>
              </el-col>

              <!-- 显示设置 -->
              <el-col :span="8" class="commons-view-setting">
                <el-row style="margin-top: 10px">
                  <span style="font-weight:bold">{{ $t('commons.view_settings') }}</span>
                </el-row>
                <el-row style="margin-top: 15px">
                  <app-manage-item :title="$t('ui.ui_debug_mode')"
                                   :append-span="12" :prepend-span="12" :middle-span="0">
                    <template #append>
                      <el-radio-group v-model="config.uiQuickMenu" @change="switchChange('UI_QUICK_MENU', $event)">
                        <el-radio label="local" value="local" :disabled="!isXpack">
                          {{ $t('ui.ui_local_debug') }}
                        </el-radio>
                        <el-radio label="server" value="server" :disabled="!isXpack">
                          {{ $t('ui.ui_server_debug') }}
                        </el-radio>
                      </el-radio-group>
                    </template>
                  </app-manage-item>
                </el-row>
              </el-col>
            </el-tab-pane>

            <el-tab-pane :label="$t('commons.performance')" name="performance">
              <el-row style="margin-top: 10px">
                <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
              </el-row>
              <el-row style="margin-top: 15px">
                <timing-item ref="loadTimingItem" :choose.sync="config.cleanLoadReport"
                             :expr.sync="config.cleanLoadReportExpr"
                             @chooseChange="switchChange('CLEAN_LOAD_REPORT', config.cleanLoadReport, ['CLEAN_LOAD_REPORT_EXPR', config.cleanLoadReportExpr])"
                             :title="$t('project.timing_clean_load_report')"/>
                <timing-item ref="trackTimingItem" :choose.sync="config.shareReport"
                             :expr.sync="config.performanceShareReportTime" :share-link="true"
                             :unit-options="applyUnitOptions"
                             @chooseChange="switchChange('PERFORMANCE_SHARE_REPORT_TIME', config.performanceShareReportTime, config.shareReport)"
                             :title="$t('report.report_sharing_link')"/>
              </el-row>
            </el-tab-pane>

          </el-tabs>
        </el-card>
        <el-dialog
          :title="$t('commons.rule') + $t('commons.setting')"
          :visible.sync="showRuleSetting"
          width="720px"
        >
          <div style="margin-top: -25px; margin-left: -20px; width:720px; height:1px; background:#DCDFE6;"></div>
          <el-row style="margin-top: 15px">
            <div class="timeClass">
              <span>
                <span style="font-size: 16px">{{ $t('api_test.request.time') + $t('commons.setting') }}</span>
                 <i class="el-icon-arrow-down" v-if="showSyncTimeSetting" @click="showSyncTimeSetting=false"/>
                 <i class="el-icon-arrow-right" v-if="!showSyncTimeSetting" @click="showSyncTimeSetting=true"/>
                <el-tooltip class="ms-num" effect="dark"
                            :content="$t('project_application.workstation.time_tip')"
                            placement="top">
                  <i class="el-icon-warning"/>
                </el-tooltip></span>
              <el-switch v-model="config.openUpdateTime"
                         @change="setSyncTime"></el-switch>
            </div>
          </el-row>
          <el-row v-if="showSyncTimeSetting" style="margin-top: 15px">
            <div class="setTimeClass">
              <span>{{ $t('workstation.past') }}</span>
              <el-select style="margin-left: 5px" v-model="pastQuantity" placeholder=" " size="mini" filterable
                         default-first-option
                         allow-create
                         class="timing_select">
                <el-option
                  v-for="item in applyQuantityOptions"
                  :key="item"
                  :label="item"
                  :value="item">
                </el-option>
              </el-select>
              <el-select style="margin-left: 5px" v-model="pastUnit" placeholder=" " size="mini"
                         class="timing_select">
                <el-option
                  v-for="item in applyUnitOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </div>
          </el-row>
          <el-row style="margin-top: 15px">
            <span>
              <span style="font-size: 16px">
                {{ $t('commons.pending_upgrade') + $t('api_test.request.condition') + $t('commons.setting') }}
              </span>
              <i class="el-icon-arrow-down" v-if="showApiConfig" @click="showApiConfig=false"/>
              <i class="el-icon-arrow-right" v-if="!showApiConfig" @click="showApiConfig=true"/>
              <el-tooltip class="ms-num" effect="dark"
                          :content="$t('project_application.workstation.rule_tip')"
                          placement="top">
                <i class="el-icon-warning"/>
              </el-tooltip>
            </span>
          </el-row>
          <div style="margin-top: 15px" class="setApiClass" v-if="showApiConfig">
            <span>
              <span style="font-weight: bold">{{ $t('workstation.api_change') + $t('commons.setting') }}</span>
              <el-tooltip class="ms-num" effect="dark"
                          :content="$t('project_application.workstation.api_tip')"
                          placement="top">
                <i class="el-icon-warning"/>
              </el-tooltip>
            </span>
            <el-row style="margin-bottom: 20px">
              <el-col :span="4">{{ $t('api_test.mock.req_param') + ":" }}</el-col>
              <el-col :span="20" style="color: #783887">
                <el-checkbox v-model="apiSyncCaseRequest.headers">{{ "Header" + '\xa0\xa0' }}</el-checkbox>
                <el-checkbox v-model="apiSyncCaseRequest.query">
                  {{ $t('api_test.definition.request.query_param') }}
                </el-checkbox>
                <el-checkbox v-model="apiSyncCaseRequest.rest">
                  {{ $t('api_test.definition.request.rest_param') }}
                </el-checkbox>
                <el-checkbox v-model="apiSyncCaseRequest.body">{{ $t('api_test.request.body') }}</el-checkbox>
              </el-col>
            </el-row>

            <span>{{ $t('commons.track') + $t('commons.setting') }}<el-tooltip class="ms-num" effect="dark"
                                                                               :content="$t('project_application.workstation.case_tip')"
                                                                               placement="top">
              <i class="el-icon-warning"/>
              </el-tooltip>
            </span>
            <el-row>
              <el-col :span="4">{{ $t('project.code_segment.result') + ":" }}</el-col>
              <el-col :span="20" style="color: #783887">
                <el-checkbox v-model="apiSyncCaseRequest.runError">{{ $t('schedule.event_failed') }}</el-checkbox>
                <el-checkbox v-model="apiSyncCaseRequest.unRun">{{ $t('api_test.home_page.detail_card.unexecute') }}
                </el-checkbox>
              </el-col>
            </el-row>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button @click="showRuleSetting = false">{{ $t('commons.cancel') }}</el-button>
            <el-button type="primary" @click="saveSync">{{ $t('commons.confirm') }}</el-button>
          </span>
        </el-dialog>
      </div>
    </ms-main-container>
  </ms-container>
</template>


<script>

import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID,} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import AppManageItem from "./AppManageItem";
import TimingItem from "./TimingItem";
import {genTcpMockPort} from "../../../api/project";
import {batchModifyAppSetting, getProjectAppSetting} from "../../../api/app-setting";
import {PROJECT_APP_SETTING} from "../../../common/js/constants";

export default {
  name: "appManage",
  components: {
    TimingItem,
    AppManageItem,
    MsMainContainer,
    MsContainer
  },
  data() {
    return {
      activeName: 'test_track',
      form: {
        cleanTrackReport: false,
        cleanTrackReportExpr: "",
        cleanApiReport: false,
        cleanApiReportExpr: "",
        cleanLoadReport: false,
        cleanLoadReportExpr: "",
        cleanUiReport: false,
        cleanUiReportExpr: ""
      },
      count: 0,
      isXpack: false,
      result: {},
      loading: false,
      quantity: "",
      unit: "",
      choose: false,
      applyUnitOptions: [
        {value: "H", label: this.$t('commons.date_unit.hour')},
        {value: "D", label: this.$t('commons.date_unit.day')},
        {value: "M", label: this.$t('commons.date_unit.month')},
        {value: "Y", label: this.$t('commons.date_unit.year')},
      ],
      applyQuantityOptions: [
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
        "31"
      ],
      config: {
        trackShareReportTime: "",
        performanceShareReportTime: "",
        uiShareReportTime: "",
        apiShareReportTime: "",
        caseCustomNum: false,
        scenarioCustomNum: false,
        apiQuickMenu: "",
        uiQuickMenu: "server",
        casePublic: false,
        mockTcpPort: 0,
        mockTcpOpen: false,
        cleanTrackReport: false,
        cleanTrackReportExpr: "",
        cleanApiReport: false,
        cleanApiReportExpr: "",
        cleanLoadReport: false,
        cleanLoadReportExpr: "",
        cleanUiReport: false,
        cleanUiReportExpr: "",
        urlRepeatable: false,
        shareReport: true,
        openUpdateTime: false,
        openUpdateRuleTime: "",
        triggerUpdate: "",
        openUpdateRule: false,
      },
      showRuleSetting: false,
      showSyncTimeSetting: true,
      apiSyncCaseRequest: {},
      pastQuantity: '',
      pastUnit: '',
      showApiConfig: true,
      disabledRuleBtn: false,
      openUpdateRule: true
    };
  },
  created() {
    this.init();
    this.isXpack = !!hasLicense();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    tcpMockSwitchChange(value, other) {
      if (value && this.config.mockTcpPort === 0) {
        genTcpMockPort(this.projectId).then(res => {
          let port = res.data;
          this.config.mockTcpPort = port;
          this.$nextTick(() => {
            this.switchChange("MOCK_TCP_OPEN", value, ['MOCK_TCP_PORT', this.config.mockTcpPort]);
          })
        }).catch(() => {
          this.config.mockTcpOpen = false;
        })
      } else {
        this.switchChange("MOCK_TCP_OPEN", value, other);
      }
    },
    switchChange(type, value, other) {
      if ([PROJECT_APP_SETTING.TRACK_SHARE_REPORT_TIME, PROJECT_APP_SETTING.API_SHARE_REPORT_TIME,
          PROJECT_APP_SETTING.UI_SHARE_REPORT_TIME, PROJECT_APP_SETTING.PERFORMANCE_SHARE_REPORT_TIME].indexOf(type) >= 0
        && other === false) {
        // 分享报告时，关闭开关后不做操作
        return;
      }
      let configs = [];
      if (other && value) {
        // 在开启开关时需要保存的其它信息
        configs.push({projectId: this.projectId, typeValue: other[1], type: other[0]});
      }
      // 开关信息在最后保存
      // 后台按照顺序先校验其它数据合法性，如tcp端口合法性，合法后保存是否开启
      configs.push({projectId: this.projectId, typeValue: value, type});
      let params = {configs};
      this.startSaveData(params)
    },
    startSaveData(params) {
      this.loading = batchModifyAppSetting(params).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.init();
      }).catch(() => {
        this.init();
      });
    },
    init() {
      this.loading = getProjectAppSetting(this.projectId).then(res => {
        if (!res.data) {
          return false;
        }
        this.config = res.data;
        this.config.shareReport = true;
        if (!this.config.apiQuickMenu) {
          this.config.apiQuickMenu = "debug";
        }
        if (!this.config.uiQuickMenu) {
          this.config.uiQuickMenu = "server";
        }
        if (this.config.triggerUpdate) {
          this.apiSyncCaseRequest = JSON.parse(this.config.triggerUpdate);
        } else {
          if (!this.config.openUpdateRuleTime) {
            this.config.openUpdateTime = true;
            this.showSyncTimeSetting = true;
            this.pastUnit = 'D'
            this.pastQuantity = 3
          }
        }
        if (this.config.openUpdateRuleTime) {
          this.pastUnit = this.config.openUpdateRuleTime.substring(this.config.openUpdateRuleTime.length - 1);
          this.pastQuantity = this.config.openUpdateRuleTime.substring(0, this.config.openUpdateRuleTime.length - 1);
          if (this.config.openUpdateTime) {
            this.showSyncTimeSetting = true;
          }
        }
        this.openUpdateRule = this.config.openUpdateRule !== false;
        this.disabledRuleBtn = !this.openUpdateRule;
      })
    },
    openRuleSetting() {
      this.showRuleSetting = true;
      if (JSON.stringify(this.apiSyncCaseRequest) === '{}') {
        this.apiSyncCaseRequest = {
          protocol: true,
          method: true,
          path: true,
          headers: true,
          query: true,
          rest: true,
          body: true,
          runError: true,
          unRun: false
        }
      }
      this.apiSyncCaseRequest.protocol = true;
      this.apiSyncCaseRequest.method = true;
      this.apiSyncCaseRequest.path = true;
    },
    openRule() {
      let configs = [];
      configs.push({
        projectId: this.projectId,
        typeValue: this.openUpdateRule,
        type: 'OPEN_UPDATE_RULE'
      });
      let params = {configs};
      this.startSaveData(params)
      this.disabledRuleBtn = !this.disabledRuleBtn;
    },
    buildSyncTime(configs) {
      if (this.config.openUpdateTime) {
        if (!this.pastQuantity) {
          this.$message.error("请选择时间");
          this.config.openUpdateTime = false;
          return
        }
        if (!this.pastUnit) {
          this.$message.error("请选择时间单位");
          this.config.openUpdateTime = false;
          return
        }
        this.config.openUpdateRuleTime = this.pastQuantity + this.pastUnit;
        configs.push({
          projectId: this.projectId,
          typeValue: this.config.openUpdateRuleTime,
          type: 'OPEN_UPDATE_RULE_TIME'
        });
      }
      configs.push({projectId: this.projectId, typeValue: this.config.openUpdateTime, type: 'OPEN_UPDATE_TIME'});
      return configs;
    },
    setSyncTime() {
      let configs = [];
      this.buildSyncTime(configs);
      let params = {configs};
      this.startSaveData(params);
    },
    saveSync() {
      let configs = [];
      configs.push({
        projectId: this.projectId,
        typeValue: JSON.stringify(this.apiSyncCaseRequest),
        type: 'TRIGGER_UPDATE'
      });
      this.buildSyncTime(configs);
      let params = {configs};
      this.startSaveData(params);
      this.showRuleSetting = false;
    },
    tabClick() {
      this.config.shareReport = true;
    }
  }
};
</script>

<style scoped>
@media only screen and (max-width: 1425px) {
  .commons-api-enable {
    width: 100%;
  }
}

@media only screen and (min-width: 1426px) {
  .commons-view-setting {
    margin-left: 200px;
  }
}

.timeClass {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.setTimeClass {
  border-radius: 2px;
  width: 680px;
  height: 35px;
  border: 1px solid #DCDFE6;
  padding-bottom: 6px;
  padding-top: 10px;
}

.setApiClass {
  border-radius: 2px;
  width: 680px;
  border: 1px solid #DCDFE6;
  padding-bottom: 6px;
  padding-top: 10px;
  padding-left: 5px;
}
</style>
