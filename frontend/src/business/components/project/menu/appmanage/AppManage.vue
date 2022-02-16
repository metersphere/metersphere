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
                    <el-switch v-model="form.casePublic" @change="chooseChange"></el-switch>
                  </template>
                </app-manage-item>

                <app-manage-item :title="$t('project.test_case_custom_id')"
                                 :description="$t('project.test_case_custom_id_info')">
                  <template #append>
                    <el-switch v-model="form.customNum" @change="chooseChange"></el-switch>
                  </template>
                </app-manage-item>
                <timing-item ref="trackTimingItem" :choose.sync="form.cleanTrackReport" :expr.sync="form.cleanTrackReportExpr"
                             @chooseChange="chooseChange" :title="$t('project.timing_clean_plan_report')"/>
                <timing-item ref="trackTimingItem" :choose.sync="application.shareReport" :expr.sync="application.typeValue" :share-link="true" :unit-options="applyUnitOptions"
                             @chooseChange="chooseChangeApply" :title="$t('report.report_sharing_link')"/>
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
                        <el-switch v-model="form.repeatable" @change="chooseChange"></el-switch>
                      </template>
                    </app-manage-item>
                    <app-manage-item :title="$t('project.scenario_custom_id')"
                                     :description="$t('project.scenario_custom_id_info')">
                      <template #append>
                        <el-switch v-model="form.scenarioCustomNum" @change="chooseChange"></el-switch>
                      </template>
                    </app-manage-item>
                    <app-manage-item :title="'TCP Mock Port'" :prepend-span="8" :middle-span="12" :append-span="4">
                      <template #middle>
                        <el-input-number v-model="form.mockTcpPort" :controls="false" size="medium"
                                         :disabled="form.isMockTcpOpen"></el-input-number>
                      </template>
                      <template #append>
                        <el-switch v-model="form.isMockTcpOpen" @change="chooseChange"></el-switch>
                      </template>
                    </app-manage-item>
                    <timing-item ref="apiTimingItem" :choose.sync="form.cleanApiReport" :expr.sync="form.cleanApiReportExpr"
                                 @chooseChange="chooseChange" :title="$t('project.timing_clean_api_report')"/>
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
                        <el-radio-group v-model="form.apiQuick" @change="chooseChange">
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
                <timing-item ref="loadTimingItem" :choose.sync="form.cleanLoadReport" :expr.sync="form.cleanLoadReportExpr"
                             @chooseChange="chooseChange" :title="$t('project.timing_clean_load_report')"/>
                <timing-item ref="trackTimingItem" :choose.sync="application.shareReport" :expr.sync="application.typeValue" :share-link="true" :unit-options="applyUnitOptions"
                             @chooseChange="chooseChangeApply" :title="$t('report.report_sharing_link')"/>
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
  watch: {
    activeName(val) {
      this.getProjectApplication();
    },
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
      application:{
        shareReport:'',
        typeValue:'',
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
      ]
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
    chooseChange() {
      this.form.workspaceId = getCurrentWorkspaceId();
      this.form.createUser = getCurrentUserId();
      this.form.id = this.projectId;
      this.$post("/project/update", this.form, () => {
        this.$success(this.$t('commons.save_success'));
        this.init();
      }, () => {
        this.init();
      });
    },
    chooseChangeApply(){
      if(!this.application.shareReport){
          return;
      }
      this.$post("/project_application/update", this.application, () => {
        this.$success(this.$t('commons.save_success'));
        this.init();
      }, () => {
        this.init();
      });
    },
    getProjectApplication(){
      let type;
      if(this.activeName==='test_track'){
        type = 'TRACK_SHARE_REPORT_TIME'
      }else if(this.activeName==='performance'){
        type = 'PERFORMANCE_SHARE_REPORT_TIME'
      }else if(this.activeName==='api'){
        type = 'API_SHARE_REPORT_TIME'
      }
      if(type){
        this.$get('/project_application/get/' + this.projectId+"/"+type, res => {
          if(res.data){
            res.data.shareReport = true;
            this.application = res.data;
          }
        });
      }
    },
    init() {
      this.result = this.$get('/project/get/' + this.projectId, res => {
        this.form = res.data;
      });
      this.getProjectApplication();
    }
  }
};
</script>

<style scoped>

</style>
