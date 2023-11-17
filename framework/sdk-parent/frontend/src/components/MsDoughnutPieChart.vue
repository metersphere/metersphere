<template>
  <div>
    <ms-chart
      v-if="visible && data.length > 0"
      :options="options"
      :height="height"
    />
    <div v-if="visible && data.length <= 0" style="height: 300px"></div>
  </div>
</template>

<script>
import MsChart from "./chart/MsChart";
export default {
  name: "MsDoughnutPieChart",
  components: { MsChart },
  data() {
    return {
      visible: false,
      options: {
        tooltip: {
          trigger: "item",
        },
        legend: {
          orient: "vertical",
          left: "60%",
          bottom: "40%",
          formatter: function (name) {
            return name;
          },
        },
        series: [
          {
            name: this.name,
            type: "pie",
            left: -150,
            radius: ["40%", "50%"],
            avoidLabelOverlap: false,
            label: {
              lineHeight: 35,
              fontSize: 20,
              position: "center",
              color: "gray",
              formatter: function (params) {
                return "";
              },
            },
            labelLine: {
              show: false,
            },
            data: this.data,
          },
        ],
      },
    };
  },
  props: {
    name: {
      type: String,
      default: "数据名称",
    },
    data: {
      type: Array,
      default() {
        return [];
      },
    },
    height: {
      type: Number,
      default() {
        return 400;
      },
    },
  },
  watch: {
    data() {
      this.reload();
    },
  },
  created() {},
  mounted() {
    this.reload();
  },
  methods: {
    reload() {
      this.setFormatterFunc();
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
      });
    },
    setFormatterFunc() {
      let data = this.data;
      let count = 0;
      let title = this.name;

      this.options.series[0].data = data;

      this.data.forEach((item) => {
        count += item.value;
      });

      let total = 0;
      for (let i = 0; i < data.length; i++) {
        total += data[i].value;
      }

      let dataPercentObj = {};
      for (let i = 0; i < data.length; i++) {
        let dataName = data[i].name;
        let value = data[i].value;
        dataPercentObj[dataName] = new Number(((value / total) * 100).toFixed(2));
      }
      this.options.legend.formatter = (name) => {
        let target = 0;
        for (let i = 0, l = data.length; i < l; i++) {
          if (data[i].name == name) {
            target = data[i].value;
          }
        }

        return name + "  |  " + target + "     " + this.formatNumber( dataPercentObj[name], 2) + "%";
      };

      this.options.series[0].label.formatter = (params) => {
        return title + "\n" + count;
      };
    },
    formatNumber(num, decimalPlaces) {
      let fixedNum = num.toFixed(decimalPlaces); // 先使用 toFixed 获取指定小数位数的字符串
      let parts = fixedNum.split("."); // 将整数部分和小数部分分开

      // 如果小数部分存在，且小数部分的长度小于指定的小数位数，则补齐0
      if (parts.length > 1 && parts[1].length < decimalPlaces) {
        parts[1] = parts[1].padEnd(decimalPlaces, "0");
      }

      // 将整数部分和小数部分重新拼接起来
      return parts.join(".");
    }
  },
};
</script>

<style scoped></style>
