<template>
  <div class="dashboard-card">
    <el-card shadow="never" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('test_track.home.bug_count') }}</span>
      </div>

      <div v-loading="loading" element-loading-background="#FFFFFF">
        <div v-if="loadError"
             style="width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center; align-items: center">
          <img style="height: 100px;width: 100px;"
               src="/assets/module/figma/icon_load_error.svg"/>
          <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
        </div>
        <div v-if="!loadError">
          <div class="main-info">
            <count-chart :chart-data="bugData.chartData" :main-title="chartMainTitle" :color-constant="DEFAULT_DASHBOARD_CHART_COLOR"
                         :week-count="bugData.thisWeekCount" :chart-sub-link="chartRedirectLink" ref="countChart" @redirectPage="redirectPage"/>
          </div>
          <div class="addition-info">
            <el-row :gutter="24" style="margin: 0">
              <el-col :span="24" style="padding-left: 0">
                <hover-card
                  :title="$t('home.bug_dashboard.un_closed_range')"
                  :main-info="bugData.unClosedRage"
                  :tool-tip="unClosedBugRangeToolTip"
                >
                  <!--遗留缺陷、所有缺陷-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.bug_dashboard.un_closed_count') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('unClosedRelatedTestPlan')">
                              {{ formatAmount(bugData.bugUnclosedCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.bug_dashboard.total_count') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('AllRelatedTestPlan')">
                              {{ formatAmount(bugData.bugTotalCount) }}
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
import countChart from "@/business/home/components/chart/CountChart";
import hoverCard from "@/business/home/components/card/HoverCard";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getTrackBugCount} from "@/api/track";
import {formatNumber} from "@/api/track"
import {getUUID} from "metersphere-frontend/src/utils";
import {DEFAULT_DASHBOARD_CHART_COLOR} from "@/business/constants/table-constants";

export default {
  name: "BugCountCard",
  components: {countChart, hoverCard},
  data() {
    return {
      loading: false,
      loadError: false,
      unClosedBugRangeToolTip: this.$t('home.bug_dashboard.un_closed_range_tips'),
      unClosedBugCaseRangeToolTip: this.$t('home.bug_dashboard.un_closed_bug_case_range_tips'),
      chartMainTitle: this.$t("home.bug_dashboard.un_closed_bug_count"),
      chartRedirectLink: "/#/track/issue/" + getUUID() + "/" + getCurrentProjectID() + "/thisWeekUnClosedIssue",
      DEFAULT_DASHBOARD_CHART_COLOR: DEFAULT_DASHBOARD_CHART_COLOR,
      bugData: {
        bugCaseRage:" 0%",
        bugTotalCount: 0,
        bugUnclosedCount: 0,
        caseTotalCount: 0,
        unClosedRage:" 0%",
        newCount: 0,
        resolvedCount: 0,
        rejectedCount: 0,
        unKnownCount: 0,
        thisWeekCount: 0,
        chartData: {}
      },
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
      getTrackBugCount(selectProjectId)
        .then(r => {
          this.loading = false;
          this.loadError = false;
          this.bugData = r.data;
        }).catch(() => {
        this.loading = false;
        this.loadError = true;
        this.$refs.countChart.reload();
      })
    },
    formatAmount(number) {
      return formatNumber(number);
    },
    redirectPage(clickType) {
      let currentProjectId = getCurrentProjectID();
      this.$emit("redirectPage", "issue", currentProjectId, clickType);
    }
  }
}
</script>

<style scoped>

.detail-container {
  margin-top: 30px;
}

.default-property {
  font-size: 14px
}

.main-property {
  color: #F39021;
  font-size: 14px
}

.el-card :deep( .el-card__header ) {
  border-bottom: 0px solid #EBEEF5;
}

.count-info-div {
  margin: 3px;
}

.count-info-div :deep( p ) {
  font-size: 10px;
}

.info-tool-tip {
  position: absolute;
  top: 0;
}

.rows-count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 14px;
  color: var(--count_number) !important;
}
</style>
