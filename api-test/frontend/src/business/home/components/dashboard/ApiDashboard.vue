<template>
  <div class="dashboard-card">
    <el-card shadow="hover" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('home.dashboard.api.title') }}</span>
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
            <count-chart :api-data="apiData" ref="countChart"/>
          </div>
          <div class="addition-info">
            <el-row :gutter="16" style="margin: 0">
              <el-col :span="12" style="padding-left: 0">
                <hover-card
                  :title="$t('home.dashboard.api.covered_rate')"
                  :main-info="apiData.apiCoveredRate "
                  :tool-tip="apiCoveredRageToolTip"
                >
                  <!--未覆盖、已覆盖-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.dashboard.public.covered') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('api', 'api', 'covered', null)">
                              {{ formatAmount(apiData.coveredCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="12">
                        <span class="addition-info-title">
                          {{ $t('home.dashboard.public.not_covered') }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('api', 'api', 'notCovered', null)">
                              {{ formatAmount(apiData.notCoveredCount) }}
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
                  :title="$t('home.dashboard.api.completed_rate')"
                  :main-info="apiData.completedRate"
                  :tool-tip="completedRageToolTip"
                >
                  <!--已完成、进行中、未开始-->
                  <template v-slot:mouseOut>
                    <div style="margin:16px 0px 0px 16px">
                      <el-row>
                        <el-col :span="8">
                        <span class="addition-info-title">
                          {{ $t("home.dashboard.public.completed") }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('api', 'api', 'Completed', null)">
                              {{ formatAmount(apiData.finishedCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="8">
                        <span class="addition-info-title">
                          {{ $t("home.dashboard.public.underway") }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('api', 'api', 'Underway', null)">
                              {{ formatAmount(apiData.runningCount) }}
                            </el-link>
                          </div>
                        </el-col>
                        <el-col :span="8">
                        <span class="addition-info-title">
                          {{ $t("home.dashboard.public.prepared") }}
                        </span>
                          <div class="common-amount">
                            <el-link class="addition-info-num" @click="redirectPage('api', 'api', 'Prepare', null)">
                              {{ formatAmount(apiData.notStartedCount) }}
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
import {apiCountByProjectId, formatNumber} from "@/api/home";

export default {
  name: "ApiDashboard",
  components: {countChart, hoverCard},
  data() {
    return {
      loading: false,
      loadError: false,
      apiCoveredRageToolTip: this.$t('api_test.home_page.formula.api_coverage'),
      completedRageToolTip: this.$t('api_test.home_page.formula.completion'),
      apiData: {
        httpCount: 0,
        tcpCount: 0,
        rpcCount: 0,
        sqlCount: 0,
        createdInWeek: 0,
        apiCoveredRate: "0%",
        completedRate: "0%",
        coveredCount: 0,
        notCoveredCount: 0,
        runningCount: 0,
        finishedCount: 0,
        notRunCount: 0,
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
      apiCountByProjectId(selectProjectId).then(response => {
        this.loading = false;
        this.loadError = false;
        this.apiData = response.data;
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
    redirectPage(redirectPage, dataType, selectRange, selectParam) {
      this.$emit('redirectPage', redirectPage, dataType, selectRange, selectParam);
    }
  }
}
</script>

<style scoped>
</style>
