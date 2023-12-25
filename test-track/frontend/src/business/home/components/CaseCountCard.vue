<template>
  <div class="dashboard-card">
    <el-card shadow="never" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('test_track.home.case_count') }}</span>
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
            <count-chart :chart-data="trackData.chartData" :main-title="chartMainTitle" :color-constant="CASE_DASHBOARD_CHART_COLOR"
                         :week-count="trackData.thisWeekAddedCount" :chart-sub-link="chartRedirectLink" ref="countChart" @redirectPage="redirectPage"/>
          </div>
          <div class="addition-info">
            <el-row :gutter="16" style="margin: 0">
              <el-col :span="12" style="padding-left: 0">
                <hover-card
                  :title="$t('home.rate.case_review')"
                  :main-info="trackData.reviewRage"
                  :tool-tip="caseReviewRangeToolTip"
                >
                  <!--未评审、已评审-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.case_review_dashboard.not_review') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('notReviewed')">
                              {{ formatAmount(trackData.prepareCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.case_review_dashboard.finished_review') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('reviewed')">
                              {{ formatAmount(trackData.passCount + trackData.unPassCount) }}
                            </el-link>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                  </template>
                </hover-card>
              </el-col>

              <el-col :span="12" style="padding-right:0">
                <hover-card
                  :title="$t('home.rate.case_review_pass')"
                  :main-info="trackData.reviewPassRage"
                  :tool-tip="caseFinishedReviewPassRageToolTip"
                >
                  <!--未通过, 已通过-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="8">
                        <span class="addition-info-title">
                          {{ $t("home.case_review_dashboard.not_pass") }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('reviewFail')">
                              {{ formatAmount(trackData.unPassCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="8">
                        <span class="addition-info-title">
                          {{ $t("home.case_review_dashboard.pass") }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('reviewSuccess')">
                              {{ formatAmount(trackData.passCount) }}
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
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getTrackCount} from "@/api/track";
import {formatNumber} from "@/api/track"
import {CASE_DASHBOARD_CHART_COLOR} from "@/business/constants/table-constants";

export default {
  name: "CaseCountCard",
  components: {countChart, hoverCard},
  data() {
    return {
      loading: false,
      loadError: false,
      caseReviewRangeToolTip: this.$t('api_test.home_page.formula.review'),
      caseFinishedReviewPassRageToolTip: this.$t('home.dashboard.case_finished_review_pass_tip'),
      chartMainTitle: this.$t("home.case_review_dashboard.case_count"),
      chartRedirectLink: "/#/track/case/all/" + getUUID() + "/case/thisWeekCount",
      CASE_DASHBOARD_CHART_COLOR: CASE_DASHBOARD_CHART_COLOR,
      trackData: {
        allCaseCountNumber: 0,
        allRelevanceCaseCount: 0,
        apiCaseCount: 0,
        apiCaseCountStr: "",
        coverageCount: 0,
        coverageRage: "0%",
        p0CaseCountNumber: 0,
        p1CaseCountNumber: 0,
        p2CaseCountNumber: 0,
        p3CaseCountNumber: 0,
        passCount: 0,
        performanceCaseCount: 0,
        performanceCaseCountStr: "",
        prepareCount: 0,
        reviewRage: " 0%",
        reviewPassRage: " 0%",
        scenarioCaseCount: 0,
        scenarioCaseStr: "",
        thisWeekAddedCount: 0,
        unPassCount: 0,
        uncoverageCount: 0,
        chartData: {},
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
      getTrackCount(selectProjectId)
        .then(r => {
          this.loading = false;
          this.loadError = false;
          this.trackData = r.data;
          this.$refs.countChart.reload();
        }).catch(() => {
          this.loading = false;
          this.loadError = true;
          this.$refs.countChart.reload();
        });
    },
    formatAmount(number) {
      return formatNumber(number);
    },
    redirectPage(clickType) {
      this.$emit("redirectPage", "testCase", "case", clickType);
    }
  }
}
</script>

<style scoped>
</style>
