<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card class="table-card">
          <el-tabs v-model="activeName" style="height: 600px">
            <el-tab-pane :label="$t('commons.my_workstation')" name="my_workstation" v-if="isXpack" :disabled="true">
              {{ $t('commons.my_workstation') }}
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
                             @chooseChange="switchChange('TRACK_SHARE_REPORT_TIME', config.trackShareReportTime)"
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
                                 @chooseChange="switchChange('API_SHARE_REPORT_TIME', config.apiShareReportTime)"
                                 :title="$t('report.report_sharing_link')"/>

                    <!-- 接口测试资源池 -->
                    <app-manage-item :title="$t('commons.api_run_pool_title')" :prepend-span="8" :middle-span="12"
                                     :append-span="4">
                      <template #middle>
                        <el-select v-model="config.resourcePoolId"
                                   size="mini"
                                   @change="runModeChange(true, ['RESOURCE_POOL_ID', config.resourcePoolId])">
                          <el-option
                            v-for="item in resourcePools"
                            :key="item.id"
                            :label="item.name"
                            :disabled="!item.api"
                            :value="item.id">
                          </el-option>
                        </el-select>
                      </template>
                      <template #append>
                        <el-switch v-model="config.poolEnable"
                                   @change="runModeChange($event, ['RESOURCE_POOL_ID', config.resourcePoolId])"></el-switch>
                      </template>
                    </app-manage-item>
                    <!-- 接口审核 -->
                    <reviewer-config
                      :name="$t('project.config.api_script_review')"
                      :popTitle="$t('project.config.api_script_review_tips')"
                      :reviewers="userInProject"
                      :reviewer.sync="config.apiScriptReviewer"
                      :reviewerSwitch.sync="config.apiReviewTestScript"
                      @reviewerChange="
                    switchChange(
                      'API_SCRIPT_REVIEWER',
                      config.apiScriptReviewer
                    )
                  "
                      @chooseChange="
                    switchChange(
                      'API_REVIEW_TEST_SCRIPT',
                      config.apiReviewTestScript
                    )
                  "
                    />

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
                               :title="$t('project.timing_clean_ui_report')"/>
                  <timing-item ref="uiTimingItem" :choose.sync="config.shareReport"
                               :expr.sync="config.uiShareReportTime" :share-link="true"
                               :unit-options="applyUnitOptions"
                               @chooseChange="switchChange('UI_SHARE_REPORT_TIME', config.uiShareReportTime)"
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
                             @chooseChange="switchChange('PERFORMANCE_SHARE_REPORT_TIME', config.performanceShareReportTime)"
                             :title="$t('report.report_sharing_link')"/>
                <reviewer-config
                  :name="$t('project.config.load_test_script_review')"
                  :popTitle="$t('project.config.load_test_script_review_detail')"
                  :reviewers="userInProject"
                  :reviewer.sync="config.performanceScriptReviewer"
                  :reviewerSwitch.sync="config.performanceReviewLoadTestScript"
                  :placeholder="$t('commons.create_user')"
                  @reviewerChange="
                    switchChange(
                      'PERFORMANCE_SCRIPT_REVIEWER',
                      config.performanceScriptReviewer
                    )
                  "
                  @chooseChange="
                    switchChange(
                      'PERFORMANCE_REVIEW_LOAD_TEST_SCRIPT',
                      config.performanceReviewLoadTestScript
                    )
                  "
                />
              </el-row>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </div>
    </ms-main-container>
  </ms-container>
</template>


<script>

import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import ReviewerConfig from "@/business/components/project/menu/appmanage/ReviewerConfig.vue";
import {getCurrentProjectID, hasLicense,} from "@/common/js/utils";
import AppManageItem from "@/business/components/project/menu/appmanage/AppManageItem";
import TimingItem from "@/business/components/project/menu/appmanage/TimingItem";
import {getProjectMember} from "@/network/user";

export default {
  name: "appManage",
  components: {
    TimingItem,
    AppManageItem,
    MsMainContainer,
    MsContainer,
    ReviewerConfig
  },
  data() {
    return {
      activeName: 'test_track',
      resourcePools: [],
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
      userInProject: [],
      count: 0,
      isXpack: false,
      result: {},
      quantity: "",
      unit: "",
      choose: false,
      applyUnitOptions: [
        {value: "H", label: this.$t('commons.date_unit.hour')},
        {value: "D", label: this.$t('commons.date_unit.day')},
        {value: "M", label: this.$t('commons.date_unit.month')},
        {value: "Y", label: this.$t('commons.date_unit.year')},
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
        performanceScriptReviewer: "",
        performanceReviewLoadTestScript: false,
        apiScriptReviewer: "",
        apiReviewTestScript: false,
      },
      isPool: false
    };
  },
  created() {
    this.init();
    this.getResourcePools();
    this.isXpack = !!hasLicense();
  },
  activated() {
    this.selectUserInProject();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
        let delIndex = this.resourcePools.findIndex(item =>
          item.id === this.config.resourcePoolId);
        if (delIndex === -1) {
          this.config.resourcePoolId = undefined;
          this.config.poolEnable = false;
        }
      });
    },
    tcpMockSwitchChange(value, other) {
      if (value && this.config.mockTcpPort === 0) {
        this.$get('/project/genTcpMockPort/' + this.projectId).then(res => {
          let port = res.data.data;
          this.config.mockTcpPort = port;
          this.$nextTick(() => {
            this.switchChange("MOCK_TCP_OPEN", value, ['MOCK_TCP_PORT', this.config.mockTcpPort]);
          })
        }).catch(resp => {
          this.config.mockTcpOpen = false;
          if (resp.response && resp.response.data && resp.response.data.message) {
            this.$error(resp.response.data.message);
          }
        });
      } else {
        this.switchChange("MOCK_TCP_OPEN", value, other);
      }
    },
    runModeChange(value, other) {
      if (value && !this.config.resourcePoolId) {
        this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
        this.config.poolEnable = false;
      } else {
        this.switchChange("POOL_ENABLE", value, other);
      }
    },
    switchChange(type, value, other) {
      let configs = [];
      if (other && value) {
        // 在开启开关时需要保存的其它信息
        configs.push({projectId: this.projectId, typeValue: other[1], type: other[0]});
      }
      // 开关信息在最后保存
      // 后台按照顺序先校验其它数据合法性，如tcp端口合法性，合法后保存是否开启
      configs.push({projectId: this.projectId, typeValue: value, type});
      let params = {configs};
      this.$post("/project_application/update/batch", params).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.init();
      }).catch(resp => {
        this.init();
        if (resp.response && resp.response.data && resp.response.data.message) {
          this.$error(resp.response.data.message);
        }
      });
    },
    init() {
      this.$get('/project_application/get/config/' + this.projectId, res => {
        if (res.data) {
          this.config = res.data;
          this.config.shareReport = true;
          if (!this.config.uiQuickMenu) {
            this.config.uiQuickMenu = "server";
          }
        }
      });
    },
    selectUserInProject() {
      getProjectMember((data) => {
        this.userInProject = data;
        //判断审核人是否在其中，如果不在则置空。
        let isExist = false;
        this.userInProject.forEach((item) => {
          if (item.id === this.config.performanceScriptReviewer) {
            isExist = true;
          }
        });
        if (!isExist) {
          this.$set(this.config, "performanceScriptReviewer", null);
          this.$set(this.config, "performanceReviewLoadTestScript", false);
          this.$set(this.config, "apiScriptReviewer", null);
          this.$set(this.config, "apiReviewTestScript", false);
        }
      });
    },
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
</style>
