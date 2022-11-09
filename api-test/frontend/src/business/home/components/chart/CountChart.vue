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
            <span class="ms-point-http"/>
            <span class="count-title">HTTP</span>
            <span class="count-value">
              {{ formatAmount(apiData.httpCount) }}
            </span>
          </div>
        </div>
        <div class="count-row">
          <span class="ms-point-rpc"/>
          <span class="count-title">RPC</span>
          <span class="count-value">
              {{ formatAmount(apiData.rpcCount) }}
            </span>
        </div>
        <div class="count-row">
          <span class="ms-point-tcp"/>
          <span class="count-title">TCP</span>
          <span class="count-value">
              {{ formatAmount(apiData.tcpCount) }}
            </span>
        </div>
        <div class="count-row">
          <span class="ms-point-sql"/>
          <span class="count-title">SQL</span>
          <span class="count-value">
              {{ formatAmount(apiData.sqlCount) }}
            </span>
        </div>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsChart from "metersphere-frontend/src/components/chart/MsChart";
import {formatNumber} from "@/api/home";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "CountChart",
  components: {MsChart},
  props: {
    apiData: Object,
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
      if (this.apiData.httpCount) {
        total += this.apiData.httpCount;
      }
      if (this.apiData.rpcCount) {
        total += this.apiData.rpcCount;
      }
      if (this.apiData.tcpCount) {
        total += this.apiData.tcpCount;
      }
      if (this.apiData.sqlCount) {
        total += this.apiData.sqlCount;
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
          {value: this.apiData.httpCount, name: 'HTTP'},
          {value: this.apiData.rpcCount, name: 'RPC'},
          {value: this.apiData.tcpCount, name: 'TCP'},
          {value: this.apiData.sqlCount, name: 'SQL'},
        ];
      }
      let optionData = {
        color: colorArr,
        tooltip: {
          trigger: 'item'
        },
        title: {
          text: "{mainTitle|" + this.$t("home.dashboard.api.api_total") + "}\n\n{number|" + this.getAmount() + "}\n\n",
          subtext: this.$t("home.dashboard.public.this_week") + "：+" + this.apiData.createdInWeek + " >",
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
          sublink: "/#/api/definition/" + getUUID() + "/api/thisWeekCount",
          subtextStyle: {
            color: "#1F2329",
            fontSize: 12,
            width: 105,
            ellipsis: '... >',
            overflow: "truncate",
          },
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

.ms-point-http {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #AA4FBF;
}

.ms-point-rpc {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #FFD131;
}

.ms-point-tcp {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #10CECE;
}

.ms-point-sql {
  height: 8px;
  width: 8px;
  margin-right: 8px;
  display: inline-block;
  background-color: #4261F6;
}
</style>
