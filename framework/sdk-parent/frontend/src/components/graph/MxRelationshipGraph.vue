<template>
  <ms-chart v-if="visible" :options="options" :height="height" :width="width" ref="msChart"/>
</template>

<script>
import MsDrawer from "../MsDrawer";
import DrawerHeader from "../head/DrawerHeader";
import MsChart from "../chart/MsChart";

export default {
  name: "MxRelationshipGraph",
  components: {MsChart, DrawerHeader, MsDrawer},
  props: {
    data: Array, links: Array,
    width: [Number, String],
    height: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 48px)';
      }
    },
  },
  data() {
    return {
      visible: false,
      options: {
        // title: {
        //   text: 'Basic Graph'
        // },
        tooltip: {},
        // toolbox: {
        //   feature: {
        //     saveAsImage: {
        //       type: 'png',
        //       top: '20',
        //     }
        //   }
        // },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        legend: [
          {
            itemWidth: 10,
            itemHeight: 10,
            itemGap: 20,
            left: '18',
            top: '30',
            data: [{icon: 'rect', name: this.$t('commons.relationship.selected')}, {icon: 'rect', name: this.$t('commons.relationship.direct')}, {icon: 'rect', name: this.$t('commons.relationship.indirect')}],
            textStyle: { //图例文字的样式
              fontFamily: 'PingFang SC',
              fontWeight: '400',
              fontSize: 14,
              lineHeight: 22,
              color: '#646A73',
              top: 20
            }
          }
        ],
        series: [
          {
            type: 'graph',
            layout: 'none',
            symbolSize: 70,
            roam: true,
            categories: [
              {
                name: this.$t('commons.relationship.selected'),
                itemStyle: {color: '#9E74AB'}
              },
              {
                name: this.$t('commons.relationship.direct'),
                itemStyle: {color: '#62D256'}
              }, {
                name: this.$t('commons.relationship.indirect'),
                itemStyle: {color: '#FAD355'}
              }
            ],
            label: {
              width: 60,
              show: true,
              overflow: 'truncate',
              color: 'white'
            },
            edgeSymbol: ['circle', 'arrow'],
            edgeSymbolSize: [4, 10],
            edgeLabel: {
              fontSize: 20
            },
            data: [
              {
                name: '前置',
                x: 500,
                y: 0,
                itemStyle: {
                  color: '#F19899'
                }
              },
              {
                name: '接口',
                x: 500,
                y: 200,
                itemStyle: {
                  color: '#9E74AB'
                }
              },
              {
                name: '后置',
                x: 500,
                y: 400,
                itemStyle: {
                  color: '#9CD375'
                }
              },
            ],
            links: [
              {
                source: 1,
                target: 0,
                // symbolSize: [5, 20],
                label: {
                  show: true,
                  formatter: '前置'
                },
                // lineStyle: {
                //   width: 5,
                //   curveness: 0.2
                // }
              },
              {
                source: '后置',
                target: '接口',
                label: {
                  show: true,
                  formatter: '后置'
                },

              },
            ],
            lineStyle: {
              opacity: 1,
              width: 2,
              curveness: 0.2
            }
          }
        ]
      }
    }
  },
  methods: {
    exportCharts(name, type) {
      this.$refs.msChart.exportCharts(name, type);
    },
    reload() {
      this.options.backgroundColor = "#ffffff";
      this.options.series[0].data = this.data;
      this.options.series[0].links = this.links;
      let x = 0;
      let y = 0;
      this.options.series[0].data.forEach(item => {
        // 若没有设置 x y 则设置默认方式
        if (item.x === undefined) {
          item.x = x;
        }
        if (item.y === undefined) {
          item.y = y;
        }
        x += 1;
        y += 1;
      });
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
      });
    }
  }
}
</script>

<style scoped>
/*.ms-chart {*/
/*  height: calc(100vh - 48px);*/
/*  width: 100%;*/
/*}*/
</style>
