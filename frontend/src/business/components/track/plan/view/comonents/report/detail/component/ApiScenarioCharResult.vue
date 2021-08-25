<template>
  <ms-chart v-if="visible" :options="options"/>
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
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
        },
        legend: {
          data: [],
          // orient: 'vertical',
          // right: 0,
          // bottom: '40%',
        },
        grid: {
          left: 100
        },
        xAxis: {
          // type: 'value',
          // // name: 'Days',
          // axisLabel: {
          //     formatter: '{value}'
          // }
        },
        yAxis: {
          type: 'category',
          inverse: true,
          data: ['场景用例数',  '步骤用例数'],
          axisLabel: {
            formatter: function (value) {
            },
            margin: 20,
            rich: {
              name: {
                lineHeight: 30,
                fontSize: 16,
                align: 'center'
              },
              count: {
                height: 40,
                fontSize: 20,
                align: 'center',
              },
            }
          }
        },
        series: [
          {
            name: '未执行',
            type: 'bar',
            data: [165, 170],
            label: seriesLabel,
            itemStyle: {
              color: '#909399'
            }
          },
          {
            name: '成功',
            type: 'bar',
            label: seriesLabel,
            data: [150, 105],
            itemStyle: {
              color: '#F56C6C'
            }
          },
          {
            name: '失败',
            type: 'bar',
            label: seriesLabel,
            data: [220, 82],
            itemStyle: {
              color: '#67C23A'
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
      let data = this.data;
      let caseCount = 0;
      let stepCount = 0;
      this.data.forEach(item => {
        caseCount += item.data[0];
        if (item.data[1]) {
          stepCount += item.data[1];
        }
      });

      let formatterFuc = function (value) {
        let total = 0;
        if (value.dataIndex == 0) {
          total = caseCount;
        } else {
          total = stepCount;
        }
        return value.data + '/' + ((value.data / total) * 100).toFixed(0) + '%';
      };

      this.data.forEach(item => {
        item.type = 'bar';
        item.label = {
          show: true,
          formatter: formatterFuc
        };
      });

      this.options.series = data;

      this.options.yAxis.axisLabel.formatter =  function (value) {
        if (value === '场景用例数') {
          return '{name|场景用例数}\n' + '{count| ' + caseCount + '}';
        } else {
          return '{name|步骤用例数}\n' + '{count|' + stepCount + '}';
        }
      };

      this.options.legend.data = data.map(i => i.name);
    },
  }
}
</script>

<style scoped>

</style>
