<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card class="table-card">
          <el-tabs v-model="activeName" style="height: 600px">
            <el-tab-pane :label="$t('commons.my_workstation')" name="my_workstation" :disabled="true">
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
                <el-col :span="8">
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
                                   @change="switchChange('MOCK_TCP_OPEN', $event, ['MOCK_TCP_PORT', config.mockTcpPort])"></el-switch>
                      </template>
                    </app-manage-item>
                    <timing-item ref="apiTimingItem" :choose.sync="config.cleanApiReport"
                                 :expr.sync="config.cleanApiReportExpr"
                                 @chooseChange="switchChange('CLEAN_API_REPORT', config.cleanApiReport, ['CLEAN_API_REPORT_EXPR', config.cleanApiReportExpr])"
                                 :title="$t('project.timing_clean_api_report')"/>
                    <timing-item ref="trackTimingItem" :choose.sync="config.shareReport"
                                 :expr.sync="config.apiShareReportTime" :share-link="true" :unit-options="applyUnitOptions"
                                 @chooseChange="switchChange('API_SHARE_REPORT_TIME', config.apiShareReportTime)" :title="$t('report.report_sharing_link')"/>
                  </el-row>
                </el-col>
                <el-col :span="8" :offset="4">
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

import {
  getCurrentProjectID,
  getCurrentUserId,
  getCurrentWorkspaceId,
  hasLicense,
} from "@/common/js/utils";
import AppManageItem from "@/business/components/project/menu/appmanage/AppManageItem";
import TimingItem from "@/business/components/project/menu/appmanage/TimingItem";

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
        cleanLoadReportExpr: ""
      },
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
        apiShareReportTime: "",
        caseCustomNum: false,
        scenarioCustomNum: false,
        apiQuickMenu: "",
        casePublic: false,
        mockTcpPort: 0,
        mockTcpOpen: false,
        cleanTrackReport: false,
        cleanTrackReportExpr: "",
        cleanApiReport: false,
        cleanApiReportExpr: "",
        cleanLoadReport: false,
        cleanLoadReportExpr: "",
        urlRepeatable: false,
        shareReport: true
      }
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
      this.$post("/project_application/update/batch", params, () => {
        this.$success(this.$t('commons.save_success'));
        this.init();
      }, () => {
        this.init();
      });
    },
    init() {
      this.$get('/project_application/get/config/' + this.projectId, res => {
        if (res.data) {
          this.config = res.data;
          this.config.shareReport = true;
        }
      });
    }
  }
};
</script>

<style scoped>

</style>
