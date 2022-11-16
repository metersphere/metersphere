<template>
  <div v-if="reloadOver">
    <el-row type="flex" justify="left" align="left">
      <div style="height: 184px;width: 100%;margin-left: 30px;margin-right: 30px;">
        <ms-chart :options="options"
                  :height="184"
                  width="100%"
                  :autoresize="true"/>
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
    this.reload();
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
      let dataIsNotEmpty = false;
      let protocolData = [
        {value: 0, name: 'HTTP'},
        {value: 0, name: 'RPC'},
        {value: 0, name: 'TCP'},
        {value: 0, name: 'SQL'},
      ];
      let borderWidth = 0;
      let colorArr = ['#DEE0E3', '#DEE0E3', '#DEE0E3', '#DEE0E3'];
      if (this.getTotal() > 0) {
        colorArr = ['#AA4FBF', '#FAD355', '#14E1C6', '#4E83FD',];
        borderWidth = 3;
        dataIsNotEmpty = true;
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
        legend: {
          orient: 'vertical',
          icon: "rect",
          selectedMode: dataIsNotEmpty,
          itemGap: 16,
          left: '45%',
          y: 'center',
          itemHeight: 8,
          itemWidth: 8, //修改icon图形大小
          itemStyle: {
            borderWidth: 0.1
          },
          textStyle: {
            align: 'right',
            rich: {
              protocol: {
                fontSize: 14,
                color: '#646A73',
                fontWeight: 400,
                width: 50,
                align: 'left',
                lineHeight: 22,
              },
              num: {
                fontSize: 14,
                align: 'right',
                lineHeight: 22,
                color: '#646A73',
                fontWeight: 500,
                padding: [0, 0, 0, 140]
              }
            }
          },
          data: protocolData,
          formatter: function (name) {
            //通过name获取到数组对象中的单个对象
            let singleData = protocolData.filter(function (item) {
              return item.name === name
            });
            let value = singleData[0].value;
            return [`{protocol|${name}}`, `{num|${value}}`].join("");
          }
        },
        title: {
          text: "{mainTitle|" + this.$t("home.dashboard.api.api_total") + "}\n\n{number|" + this.getAmount() + "}\n\n",
          subtext: this.$t("home.dashboard.public.this_week") + "：+" + formatNumber(this.apiData.createdInWeek) + " >",
          top: "center",
          left: "86px",
          textAlign: 'center',
          textStyle: {
            rich: {
              mainTitle: {
                color: '#646A73',
                fontSize: 12,
                align: 'center'
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
            ellipsis: '...',
            overflow: "truncate",
          },
          itemGap: -60,
        },
        series: [
          {
            type: 'pie',
            radius: ['70%', '96%'],
            center: ['92px', '50%'],
            avoidLabelOverlap: false,
            hoverAnimation: dataIsNotEmpty,
            legendHoverLink: false,
            label: {
              show: false,
            },
            itemStyle: {
              borderColor: "#FFF",
              borderWidth: borderWidth,
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

</style>
