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
      let percentCount = 0;
      for (let i = 0; i < data.length; i++) {
        let dataName = data[i].name;
        let value = data[i].value;
        let percent = 100 - percentCount;
        if (i !== data.length - 1) {
          percent = new Number(((value / total) * 100).toFixed(0));
          percentCount += percent;
        }
        dataPercentObj[dataName] = percent;
      }
      this.options.legend.formatter = (name) => {
        let target = 0;
        for (let i = 0, l = data.length; i < l; i++) {
          if (data[i].name == name) {
            target = data[i].value;
          }
        }
        return name + "  |  " + target + "     " + dataPercentObj[name] + "%";
      };

      this.options.series[0].label.formatter = (params) => {
        return title + "\n" + count;
      };
    },
  },
};
</script>

<style scoped></style>
