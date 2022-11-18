<template>
  <div v-if="reloadOver">
    <el-row type="flex" justify="left" align="left">
      <div style="height: 184px; width: 100%; margin-left: 30px; margin-right: 30px;">
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
import {formatNumber} from "@/api/track";
import {DASHBOARD_CHART_COLOR} from "@/business/constants/table-constants";

export default {
  name: "CountChart",
  components: {MsChart},
  props: {
    chartData: Object,
    weekCount: Number,
    totalTime: Number,
    mainTitle: String,
    chartSubLink: String,
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
      for (let name in this.chartData) {
        total += this.chartData[name];
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
    },
    getChartData(empty) {
      let elementArr = [];
      if (empty) {
        for (let name in this.chartData) {
          let element = {name: name, value: 0};
          elementArr.push(element);
        }
        return elementArr;
      } else {
        for (let name in this.chartData) {
          let element = {name: name, value: this.chartData[name]};
          elementArr.push(element);
        }
      }
      return elementArr;
    },
    getChartColor(size, empty) {
      let colorArr = [];
      if (empty) {
        for (let i = 0; i < size; i++) {
          colorArr.push("#DEE0E3");
        }
      } else {
        colorArr = DASHBOARD_CHART_COLOR.slice(0, size);
      }
      return colorArr;
    }
  },
  computed: {
    options() {
      let dataIsNotEmpty = false;
      let borderWidth = 0;
      let elementArr = this.getChartData(true);
      let colorArr = this.getChartColor(elementArr.length, true);
      if (this.getTotal() > 0) {
        borderWidth = 3;
        dataIsNotEmpty = true;
        elementArr = this.getChartData(false);
        colorArr = this.getChartColor(elementArr.length, false);
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
          data: elementArr,
          formatter: function (name) {
            //通过name获取到数组对象中的单个对象
            let singleData = elementArr.filter(function (item) {
              return item.name === name
            });
            let value = singleData[0].value;
            return [`{protocol|${name}}`, `{num|${value}}`].join("");
          }
        },
        title: {
          text: "{mainTitle|" + this.mainTitle + "}\n\n{number|" + this.getAmount() + "}\n\n",
          subtext: this.$t("home.dashboard.public.this_week") + ": +" + formatNumber(this.weekCount) + " >",
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
          sublink: this.chartSubLink,
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
            data: elementArr,
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
