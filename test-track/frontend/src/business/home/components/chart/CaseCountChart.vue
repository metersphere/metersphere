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
            <span class="ms-point-p0"/>
            <span class="count-title">P0</span>
            <span class="count-value">
              {{ formatAmount(trackData.p0CaseCountNumber) }}
            </span>
          </div>
        </div>
        <div class="count-row">
          <span class="ms-point-p1"/>
          <span class="count-title">P1</span>
          <span class="count-value">
              {{ formatAmount(trackData.p1CaseCountNumber) }}
            </span>
        </div>
        <div class="count-row">
          <span class="ms-point-p2"/>
          <span class="count-title">P2</span>
          <span class="count-value">
              {{ formatAmount(trackData.p2CaseCountNumber) }}
          </span>
        </div>
        <div class="count-row">
          <span class="ms-point-p3"/>
          <span class="count-title">P3</span>
          <span class="count-value">
              {{ formatAmount(trackData.p3CaseCountNumber) }}
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

export default {
  name: "CaseCountChart",
  components: {MsChart},
  props: {
    trackData: Object,
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
      if (this.trackData.p0CaseCountNumber) {
        total += this.trackData.p0CaseCountNumber;
      }
      if (this.trackData.p0CaseCountNumber) {
        total += this.trackData.p1CaseCountNumber;
      }
      if (this.trackData.p0CaseCountNumber) {
        total += this.trackData.p2CaseCountNumber;
      }
      if (this.trackData.p0CaseCountNumber) {
        total += this.trackData.p3CaseCountNumber;
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
        colorArr = ['#F76964', '#FFD131', '#AA4FBF', '#10CECE']
        protocolData = [
          {value: this.trackData.p0CaseCountNumber, name: 'P0'},
          {value: this.trackData.p1CaseCountNumber, name: 'P1'},
          {value: this.trackData.p2CaseCountNumber, name: 'P2'},
          {value: this.trackData.p3CaseCountNumber, name: 'P3'},
        ];
      }
      let optionData = {
        color: colorArr,
        tooltip: {
          trigger: 'item'
        },
        title: {
          text: "{mainTitle|" + this.$t("home.case_review_dashboard.case_count") + "}\n\n{number|" + this.getAmount() + "}\n\n",
          subtext: this.$t("home.dashboard.public.this_week") + ": +" + this.trackData.thisWeekAddedCount + " >",
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
          sublink: "/#/track/case/all/" + getUUID() + "/case/thisWeekCount",
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

.ms-point-p0 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #F76964;
}

.ms-point-p1 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #FFD131;
}

.ms-point-p2 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #AA4FBF;
}

.ms-point-p3 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #14E1C6;
}
</style>
