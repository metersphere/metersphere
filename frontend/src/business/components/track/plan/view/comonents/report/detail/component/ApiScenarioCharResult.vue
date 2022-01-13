<template>
  <ms-chart style="height: 250px" v-if="visible" :options="options"/>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";
let seriesLabel = {
  show: true,
    formatter: function (value) {
    return value.data + '/' + ((value.data / 100) * 100).toFixed(0) + '%';
  },
};

export default {
  name: "ApiScenarioCharResult",
  components: {MsChart},
  data() {
    return {
      visible: false,
      options:  {
        title: {
          text: this.$t('test_track.plan.test_plan_api_scenario_count'),
          subtext: '55',
          textAlign:'center',
          y: 'center',
          padding: 40,
          itemGap: 5,
          textStyle: {
            lineHeight: 30,
            fontSize: 16,
            fontWeight: 500,
            color: 'gray'
          },
          subtextStyle: {
            height: 30,
            fontSize: 18,
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
        },
        legend: {
          data: [],
        },
        grid: {
          left: 100
        },
        xAxis: {
          type: 'value',
        },
        yAxis: {
          type: 'category',
          data: [],
          axisLabel: {
            formatter: function (value) {
              return '';
            },
          }
        },
        series: [
          {
            data: [],
            type: 'bar',
            barWidth: 20,
            label: {
              show: true,
              formatter: {}
            }
          }
        ]
      }
    }
  },
  props: {
    name: {
      type: String,
      default: '数据名称'
    },
    data: {
      type: Array,
      default() {
        return []
      }
    },
  },
  watch: {
    data() {
      this.reload();
    }
  },
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

      let dataCount = 0;
      this.data.forEach(item => {
        dataCount += item.value;
      });

      let formatterFuc = function (item) {
        return item.data.value + '/' + ((item.data.value / dataCount) * 100).toFixed(0) + '%';
      };

      this.options.series[0].data = this.data;
      this.options.series[0].label.formatter = formatterFuc;

      this.options.title.text = this.name;
      this.options.title.subtext = dataCount;

      this.options.legend.data = this.data.map(i => i.name);
      this.options.yAxis.data = this.data.map(i => i.name);
    },
  }
}
</script>

<style scoped>

</style>
