  <template>
  <div class="dashboard-card">
    <el-card shadow="never" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('test_track.home.relevance_case') }}</span>
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
            <count-chart :chart-data="relevanceData.chartData" :main-title="chartMainTitle" :color-constant="RELEVANCE_CASE_DASHBOARD_CHART_COLOR"
                         :week-count="relevanceData.thisWeekAddedCount" :chart-sub-link="chartRedirectLink" ref="countChart" @redirectPage="redirectPage"/>
          </div>
          <div class="addition-info">
            <el-row :gutter="24" style="margin: 0">
              <el-col :span="24" style="padding-left: 0">
                <hover-card
                  :title="$t('test_track.home.coverage')"
                  :main-info="relevanceData.coverageRage"
                  :tool-tip="coverRangeToolTip"
                >
                  <!--未覆盖、已覆盖-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.relevance_dashboard.not_cover') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('uncoverage')">
                              {{ formatAmount(relevanceData.uncoverageCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.relevance_dashboard.cover') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('coverage')">
                              {{ formatAmount(relevanceData.coverageCount) }}
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
import {getTrackRelevanceCount} from "@/api/track";
import {formatNumber} from "@/api/track"
import {getUUID} from "metersphere-frontend/src/utils";
import {RELEVANCE_CASE_DASHBOARD_CHART_COLOR} from "@/business/constants/table-constants";

export default {
  name: "RelevanceCaseCard",
  components: {countChart, hoverCard},
  data() {
    return {
      loading: false,
      loadError: false,
      coverRangeToolTip: this.$t('api_test.home_page.formula.testplan_coverage'),
      chartMainTitle: this.$t("home.relevance_dashboard.relevance_case_count"),
      chartRedirectLink: "/#/track/case/all/" + getUUID() + "/case/thisWeekRelevanceCount",
      RELEVANCE_CASE_DASHBOARD_CHART_COLOR: RELEVANCE_CASE_DASHBOARD_CHART_COLOR,
      relevanceData: {
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
      getTrackRelevanceCount(selectProjectId)
        .then(r => {
          this.loading = false;
          this.loadError = false;
          this.trackData = r.data;
          this.relevanceData = r.data;
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
      this.$emit("redirectPage", "testCase", "relationCase", clickType);
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
