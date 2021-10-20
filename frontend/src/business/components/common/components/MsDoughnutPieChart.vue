<template>
  <div>
    <ms-chart v-if="visible && data.length > 0" :options="options"/>
    <div v-if="visible && data.length <= 0" style="height: 300px">

    </div>
  </div>
</template>

<script>

import MsChart from "@/business/components/common/chart/MsChart";
export default {
  name: "MsDoughnutPieChart",
  components: {MsChart},
  data() {
    return {
      visible: false,
      options: {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          right: 50,
          bottom: '40%',
          formatter: function (name) {
            return name;
          }
        },
        series: [
          {
            name: this.name,
            type: 'pie',
            left: -150,
            radius: ['40%', '50%'],
            avoidLabelOverlap: false,
            label: {
              // padding: [10, 10, 20, 10],
              lineHeight: 35,
              fontSize: 20,
              // fontWeight: 'bold',
              position: 'center',
              color: 'gray',
              formatter: function (params) {
                return '';
              },
            },
            labelLine: {
              show: false
            },
            data: this.data
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
  created() {
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
      let count = 0;
      let title = this.name;

      this.options.series[0].data = data;

      this.data.forEach(item => {
        count += item.value;
      });

      this.options.legend.formatter = (name) => {
        let total = 0;
        let target = 0;
        for (let i = 0, l = data.length; i < l; i++) {
          total += data[i].value;
          if (data[i].name == name) {
            target = data[i].value;
          }
        }
        return name + '  |  ' + target + '     ' + ((target / total) * 100).toFixed(0) + '%';
      };

      this.options.series[0].label.formatter = (params) => {
        return title + '\n' + count;
      };
    },
  }
}
</script>

<style scoped>
</style>
