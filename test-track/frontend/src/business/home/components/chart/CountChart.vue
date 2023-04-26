<template>
  <div v-if="reloadOver">
    <el-row type="flex" justify="left" align="left">
      <div style="height: 208px; width: 100%; margin-left: 26px; margin-right: 30px;">
        <ms-chart :options="options"
                  :height="208"
                  width="100%"
                  :autoresize="true"/>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsChart from "metersphere-frontend/src/components/chart/MsChart";
import {formatNumber} from "@/api/track";

export default {
  name: "CountChart",
  components: {MsChart},
  props: {
    chartData: Object,
    weekCount: Number,
    totalTime: Number,
    mainTitle: String,
    chartSubLink: String,
    colorConstant: Array,
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
        colorArr = this.colorConstant.slice(0, size);
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
          type: 'scroll',
          orient: 'vertical',
          icon: "rect",
          selectedMode: dataIsNotEmpty,
          itemGap: 16,
          left: '50%',
          top: 50,
          y: 'center',
          itemHeight: 8,
          itemWidth: 8, //修改icon图形大小
          itemStyle: {
            borderWidth: 0.1
          },
          inactiveBorderWidth: 0.1,
          pageIcons: {
            vertical: [
              'path://M387.84 164.906667a22.122667 22.122667 0 0 0-0.362667-30.72 20.522667 20.522667 0 0 0-29.674666 0.362666L0 512.853333l357.802667 378.282667c8.042667 8.533333 21.290667 8.746667 29.674666 0.341333 8.32-8.32 8.533333-22.016 0.384-30.72L60.330667 512.853333 387.861333 164.906667z',
              'path://M709.845333 250.346667a22.4 22.4 0 0 1 0.533334-30.848 20.48 20.48 0 0 1 29.717333 0.64l272.426667 292.693333-272.426667 292.650667a20.458667 20.458667 0 0 1-29.717333 0.64c-8.32-8.32-8.746667-21.973333-0.533334-30.848l242.346667-262.464-242.346667-262.464z',
            ],
          },
          pageIconColor: '#1F2329', // 可以点击的翻页按钮颜色
          pageIconInactiveColor: '#7f7f7f', // 禁用的按钮颜色
          pageIconSize: 14, //这当然就是按钮的大小
          textStyle: {
            align: 'right',
            rich: {
              protocol: {
                fontSize: 14,
                color: '#646A73',
                fontWeight: 400,
                width: 40,
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
          left: "100px",
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
            radius: [75, 100],
            center: ['104px', '50%'],
            avoidLabelOverlap: false,
            hoverAnimation: dataIsNotEmpty,
            legendHoverLink: false,
            minAngle: 5,
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
