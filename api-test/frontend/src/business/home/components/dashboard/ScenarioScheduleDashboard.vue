<template>
  <div class="dashboard-card">
    <el-card shadow="hover" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('home.dashboard.scenario_schedule.title') }}</span>
      </div>
      <div v-loading="loading" element-loading-background="#FFFFFF">
        <div v-show="loadError"
             style="width: 100%; height: 300px; display: flex; flex-direction: column;     justify-content: center;align-items: center">
          <img style="height: 100px;width: 100px;"
               src="/assets/figma/icon_load.svg"/>
          <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
        </div>
        <div v-show="!loadError">
          <div class="main-info">
            <el-row :gutter="16">
              <el-col :span="12">
                <main-info-card :title="$t('home.dashboard.scenario_schedule.title')" :count-data="scenarioScheduleData"
                                redirect-page-name="scenario"
                                redirect-data-type="schedule"
                                @redirectPage="redirectPage"
                                :is-execute-info="false"/>
              </el-col>
              <el-col :span="12">
                <main-info-card :title="$t('home.dashboard.public.executed_times_in_week')"
                                :count-data="scenarioScheduleData"
                                :is-execute-info="true"/>
              </el-col>
            </el-row>
          </div>
          <div class="addition-info">
            <el-row :gutter="16" style="margin: 0">
              <!--当前运行数-->
              <el-col :span="12" style="padding-left: 0">
                <hover-card
                  :title="$t('home.dashboard.scenario_schedule.running_count')"
                  :main-info="scenarioScheduleData.runningCount"
                  :tool-tip="runningCountToolTip"
                >
                  <!--运行中、运行完成、未运行-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                            <span class="addition-info-title">
                              {{ $t("home.dashboard.public.running") }}
                            </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num"
                                     @click="redirectPage('scenario', 'schedule', 'running', null)">
                              {{ formatAmount(scenarioScheduleData.runningCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                            <span class="addition-info-title">
                              {{ $t("home.dashboard.public.not_run") }}
                            </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num"
                                     @click="redirectPage('scenario', 'schedule', 'notRun', null)">
                              {{ formatAmount(scenarioScheduleData.notRunCount) }}
                            </el-link>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                  </template>
                </hover-card>
              </el-col>
              <!--运行通过率-->
              <el-col :span="12" style="padding-right:0">
                <hover-card
                  :title="$t('home.dashboard.scenario_schedule.pass_rate')"
                  :main-info="scenarioScheduleData.passRate "
                  :tool-tip="passRateToolTip"
                >
                  <!--已完成、进行中、未开始-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                            <span class="addition-info-title">
                              {{ $t("home.dashboard.public.pass") }}
                            </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num"
                                     @click="redirectPage('scenarioReport', 'scenario', 'scheduleExecutionPassCount', null)">
                              {{ formatAmount(scenarioScheduleData.passCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                            <span class="addition-info-title">
                              {{ $t("home.dashboard.public.not_pass") }}
                            </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num"
                                     @click="redirectPage('scenarioReport', 'scenario', 'scheduleExecutionFailedCount', null)">
                              {{ formatAmount(scenarioScheduleData.unPassCount) }}
                            </el-link>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                  </template>
                </hover-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import hoverCard from "@/business/home/components/card/HoverCard";
import mainInfoCard from "@/business/home/components/card/MainInfoCard";
import {formatNumber, scheduleTaskCountByProjectId} from "@/api/home";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "ScenarioScheduleDashboard",
  components: {hoverCard, mainInfoCard},
  data() {
    return {
      loading: false,
      loadError: false,
      runningCountToolTip: this.$t('api_test.home_page.formula.schedule_running'),
      passRateToolTip: this.$t('api_test.home_page.formula.schedule_pass'),
      scenarioScheduleData: {
        total: 0,
        createdInWeek: 0,
        executedTimesInWeek: 0,
        executedTimes: 0,
        passRate: "0%",
        running: 0,
        notRun: 0,
        passCount: 0,
        unPassCount: 0,
        fakeErrorCount: 0,
      }
    }
  },
  activated() {
    this.search();
  },
  methods: {
    search() {
      this.loading = true;
      this.loadError = false;
      let selectProjectId = getCurrentProjectID();
      scheduleTaskCountByProjectId(selectProjectId).then(response => {
        this.loading = false;
        this.loadError = false;
        this.scenarioScheduleData = response.data;
      }).catch(() => {
        this.loading = false;
        this.loadError = true;
      });
    },
    formatAmount(number) {
      return formatNumber(number);
    },
    redirectPage(redirectPage, dataType, selectRange, selectParam) {
      if (selectRange === 'fakeError') {
        redirectPage = "scenarioReport";
        selectRange = 'scheduleExecutionFakeErrorCount';
        dataType = 'scenario';
      }
      this.$emit('redirectPage', redirectPage, dataType, selectRange, selectParam);
    }
  }
}
</script>

<style scoped>
</style>
