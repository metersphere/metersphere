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
          <span class="ms-point-p3"/>
          <span class="count-title">P3</span>
          <span class="count-value">
              {{ formatAmount(bugData.unClosedP3Size) }}
          </span>
        </div>
        <div class="count-row">
          <span class="ms-point-p2"/>
          <span class="count-title">P2</span>
          <span class="count-value">
              {{ formatAmount(bugData.unClosedP2Size) }}
          </span>
        </div>
        <div class="count-row">
          <span class="ms-point-p1"/>
          <span class="count-title">P1</span>
          <span class="count-value">
              {{ formatAmount(bugData.unClosedP1Size) }}
            </span>
        </div>
        <div class="count-row">
          <div>
            <span class="ms-point-p0"/>
            <span class="count-title">P0</span>
            <span class="count-value">
              {{ formatAmount(bugData.unClosedP0Size) }}
            </span>
          </div>
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
  name: "CountChart",
  components: {MsChart},
  props: {
    bugData: Object,
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
      if (this.bugData.unClosedP0Size) {
        total += this.bugData.unClosedP0Size;
      }
      if (this.bugData.unClosedP1Size) {
        total += this.bugData.unClosedP1Size;
      }
      if (this.bugData.unClosedP2Size) {
        total += this.bugData.unClosedP2Size;
      }
      if (this.bugData.unClosedP3Size) {
        total += this.bugData.unClosedP3Size;
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
        colorArr = ['#F54A45', '#FFD131', '#C1A9C8', '#10CECE',]
        protocolData = [
          {value: this.bugData.unClosedP3Size, name: 'P3'},
          {value: this.bugData.unClosedP2Size, name: 'P2'},
          {value: this.bugData.unClosedP1Size, name: 'P1'},
          {value: this.bugData.unClosedP0Size, name: 'P0'},
        ];
      }
      let optionData = {
        color: colorArr,
        tooltip: {
          trigger: 'item'
        },
        title: {
          text: "{mainTitle|" + this.$t("home.bug_dashboard.un_closed_bug_count") + "}\n\n{number|" + this.getAmount() + "}\n\n",
          // subtext: this.$t("home.dashboard.public.this_week") + ": +" + this.relevanceData.thisWeekAddedCount + " >",
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
          // sublink: "/#/track/case/all/" + getUUID() + "/case/thisWeekRelevanceCount",
          // subtextStyle: {
          //   color: "#1F2329",
          //   fontSize: 12,
          //   width: 105,
          //   ellipsis: '... >',
          //   overflow: "truncate",
          // },
          itemGap: -60,
        },
        series: [
          {
            type: 'pie',
            radius: ['75%', '96%'],
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

.ms-point-p3 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #F54A45;
}

.ms-point-p2 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #FFD131;
}

.ms-point-p1 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #C1A9C8;
}

.ms-point-p0 {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #10CECE;
}
</style>
