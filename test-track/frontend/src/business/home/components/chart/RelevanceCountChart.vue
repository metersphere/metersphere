<template>
  <div v-if="reloadOver">
    <el-row type="flex" justify="left" align="left">
      <div style="height: 184px;width: 184px;margin-left: 30px;margin-right: 30px;">
        <ms-chart :options="options"
                  :height="184"
                  :width="184"
                  :autoresize="true"/>
      </div>

      <!--   总数统计      -->
      <div style="margin: auto;width: 260px;padding-right: 30px">
        <div class="count-row">
          <div>
            <span class="ms-point-api"/>
            <span class="count-title">{{ $t('home.relevance_dashboard.api_case') }}</span>
            <span class="count-value">
              {{ formatAmount(relevanceData.apiCaseCount) }}
            </span>
          </div>
        </div>
        <div class="count-row">
          <span class="ms-point-scenario"/>
          <span class="count-title">{{ $t('home.relevance_dashboard.scenario_case') }}</span>
          <span class="count-value">
              {{ formatAmount(relevanceData.scenarioCaseCount) }}
            </span>
        </div>
        <div class="count-row">
          <span class="ms-point-performance"/>
          <span class="count-title">{{ $t('home.relevance_dashboard.performance_case') }}</span>
          <span class="count-value">
              {{ formatAmount(relevanceData.performanceCaseCount) }}
          </span>
        </div>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsChart from "metersphere-frontend/src/components/chart/MsChart";
import {getUUID} from "metersphere-frontend/src/utils";
import {formatNumber} from "@/api/track";
import {hasPermission} from "@/business/utils/sdk-utils";

export default {
  name: "RelevanceCountChart",
  components: {MsChart},
  props: {
    relevanceData: Object,
    totalTime: Number,
    isExport: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      reloadOver: true,
      pieChartStyle: {
        amountFontSize: 32,
      },
    }
  },
  created() {
  },
  methods: {
    reload() {
      this.reloadOver = false;
      this.$nextTick(() => {
        this.reloadOver = true;
      });
    },
    getTotal() {
      let total = 0;
      if (this.relevanceData.apiCaseCount) {
        total += this.relevanceData.apiCaseCount;
      }
      if (this.relevanceData.scenarioCaseCount) {
        total += this.relevanceData.scenarioCaseCount;
      }
      if (this.relevanceData.performanceCaseCount) {
        total += this.relevanceData.performanceCaseCount;
      }
      return total;
    },
    getAmount() {
      let total = this.getTotal();
      if (total > 999999999) {
        this.pieChartStyle.amountFontSize = 20;
      } else if (total > 99999999) {
        this.pieChartStyle.amountFontSize = 22;
      } else if (total > 9999999) {
        this.pieChartStyle.amountFontSize = 24;
      } else if (total > 999999) {
        this.pieChartStyle.amountFontSize = 26;
      } else {
        this.pieChartStyle.amountFontSize = 32;
      }
      total = this.formatAmount(total);
      return total;
    },
    formatAmount(param) {
      return formatNumber(param);
    }
  },
  computed: {
    options() {
      let protocolData = [{value: 0}];
      let colorArr = ['#DEE0E3'];
      if (this.getTotal() > 0) {
        colorArr = ['#AA4FBF', '#FFD131', '#10CECE', '#4261F6',]
        protocolData = [
          {value: this.relevanceData.apiCaseCount, name: this.$t('home.relevance_dashboard.api_case')},
          {value: this.relevanceData.scenarioCaseCount, name: this.$t('home.relevance_dashboard.scenario_case')},
          {value: this.relevanceData.performanceCaseCount, name: this.$t('home.relevance_dashboard.performance_case')}
        ];
      }
      let optionData = {
        color: colorArr,
        tooltip: {
          trigger: 'item'
        },
        title: {
          text: "{mainTitle|" + this.$t("home.relevance_dashboard.relevance_case_count") + "}\n\n{number|" + this.getAmount() + "}\n\n",
          subtext: this.$t("home.dashboard.public.this_week") + ": +" + this.relevanceData.thisWeekAddedCount + " >",
          top: "center",
          left: "center",
          textStyle: {
            rich: {
              mainTitle: {
                color: '#646A73',
                fontSize: 12,
              },
              number: {
                fontSize: this.pieChartStyle.amountFontSize,
                fontWeight: 500,
                fontStyle: "normal",
                fontFamily: "PinfFang SC",
                margin: "112px 0px 0px 2px0",
              },
            }
          },
          sublink: hasPermission('PROJECT_TRACK_CASE:READ') ? "/#/track/case/all/" + getUUID() + "/case/thisWeekRelevanceCount" : "",
          subtextStyle: {
            color: "#1F2329",
            fontSize: 12,
            width: 105,
            ellipsis: '...',
            overflow: "truncate",
          },
          itemGap: -60,
        },
        series: [
          {
            type: 'pie',
            radius: ['70%', '96%'],
            avoidLabelOverlap: false,
            hoverAnimation: true,
            label: {
              show: false,
            },
            itemStyle: {
              borderColor: "#FFF",
              borderWidth: 3,
              borderRadius: 1,
            },
            labelLine: {
              show: false
            },
            data: protocolData,
          }
        ]
      };
      return optionData;
    },
  },
}
</script>

<style scoped>

.count-row {
  padding: 8px 0px 8px 0px;
}

.count-title {
  color: #646A73;
  font-size: 14px;
  font-weight: 400;
}

.count-value {
  color: #646A73;
  float: right;
  font-size: 14px;
  font-weight: 500;
}

.ms-point-api {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #AA4FBF;
}

.ms-point-scenario {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #FFD131;
}

.ms-point-performance {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #10CECE;
}
</style>
