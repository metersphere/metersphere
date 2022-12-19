<template>
  <div style="margin-top:22px;margin-left:18px;margin-bottom: 22px">
    <ms-chart v-if="visible && pieData.length > 0" :options="options" :autoresize ="true" :height="height"  @mouseover="onMouseover" @mouseout="onMouseleave" ref="pieChart" />
  </div>

</template>

<script>

import MsChart from "./chart/MsChart";

export default {
  name: "MsBorderPieChart",
  components: {MsChart},
  data() {
    return {
      visible: true,
      autoresize:true,
      options: {
        color:this.color,
        title: {
          text: "{mainTitle|" + this.textTitle + "}\n\n{number|" + this.text + "}\n\n",
          link:this.linkText,
          subtext: this.subtext,
          triggerEvent: true,
          textStyle: {
            rich: {
              mainTitle: {
                color: '#646A73',
                fontSize: 12,
                align:'center'
              },
              number: {
                fontSize: 32,
                fontWeight: 500,
                fontStyle: "normal",
                fontFamily: "PinfFang SC",
                margin: "112px 0px 0px 2px"
              },
            }
          },
          subtextStyle: {
            color: "#1F2329",
            fontSize: 12,
            width: 105,
            ellipsis: '... >',
            overflow: "truncate",
            align:'center',
          },
          itemGap: -60,
          top: 'center',
          textAlign: 'center',
          left: this.getPosition()
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          icon:"rect",
          right: 10,
          y: 'center',
          itemHeight: 8,
          itemWidth: 8, //修改icon图形大小
          itemStyle: {
            borderWidth: 0.1
          },
        },
        series: [
          {
            type: 'pie',
            radius: this.radius,
            minAngle: 3,
            itemStyle: {
              borderRadius: 0,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'outside',
              formatter: '{c}, {d}%',
            },
            center: ['126', '128'],
            labelLine: {
              show: false
            },
            data: this.pieData,
            colorBy: "data"
          }
        ]
      }
    }
  },
  props: {
    text: {
      type: String,
      default: ''
    },
    textTitle: {
      type: String,
      default: ''
    },
    name: {
      type: String,
      default: ''
    },
    subtext: {
      type: String,
      default: ''
    },
    radius:{
      type: Array,
      default() {
        return ['40%', '70%']
      }
    },
    pieData: {
      type: Array,
      default() {
        return []
      }
    },
    color:{
      type: Array,
      default() {
        return ['#AA4FBF','#FFD131','#10CECE', '#4261F6']
      }
    },
    height:{
      type: [Number, String],
    },
    width:{
      type: [Number, String],
    },
    linkText:{
      type: String,
      default: ''
    },
    changeColor:{
      type: Boolean,
      default: false
    },
  },
  methods: {
    getPosition() {
      //21是根据字符串大概的宽度计算出来的
      let a = (this.height - this.textTitle.length * 4) / 2;
      return a.toString();
    },
    onMouseover(param) {
      if (this.changeColor && param.componentType==='title') {
        this.options.title = {
          text: "{mainTitle|" + this.textTitle + "}\n\n{number|" + this.text + "}\n\n",
          //link:this.linkText,
          subtext: this.subtext,
          triggerEvent: true,
          textStyle: {
            rich: {
              mainTitle: {
                color: '#646A73',
                fontSize: 12,
                align:'center'
              },
              number: {
                fontSize: 32,
                fontWeight: 500,
                color:'#783887',
                fontStyle: "normal",
                fontFamily: "PinfFang SC",
                margin: "112px 0px 0px 2px",
              },
            }
          },
          subtextStyle: {
            color: "#1F2329",
            fontSize: 12,
            width: 105,
            ellipsis: '... >',
            overflow: "truncate",
            align:'center',
          },
          itemGap: -60,
          top: 'center',
          textAlign: 'center',
          left: this.getPosition()
        }
      }
    },
    onMouseleave(param) {
      if (this.changeColor && param.componentType==='title') {
        this.options.title = {
          text: "{mainTitle|" + this.textTitle + "}\n\n{number|" + this.text + "}\n\n",
          //link:this.linkText,
          subtext: this.subtext,
          triggerEvent: true,
          textStyle: {
            rich: {
              mainTitle: {
                color: '#646A73',
                fontSize: 12,
                align:'center'
              },
              number: {
                fontSize: 32,
                fontWeight: 500,
                color:'#263238',
                fontStyle: "normal",
                fontFamily: "PinfFang SC",
                margin: "112px 0px 0px 2px",
              },
            }
          },
          subtextStyle: {
            color: "#1F2329",
            fontSize: 12,
            width: 105,
            ellipsis: '... >',
            overflow: "truncate",
            align:'center',
          },
          itemGap: -60,
          top: 'center',
          textAlign: 'center',
          left: this.getPosition()
        }
      }
    },
  },
  created() {
    this.$nextTick(function () {
      let self = this;
      this.options.legend = {
        type:'scroll',
        pageIconSize: 12,
        pageIcons: {
          vertical: [
            "path://M325.802667 512l346.965333 346.965333a21.333333 21.333333 0 0 1 0 30.165334l-30.208 30.165333a21.333333 21.333333 0 0 1-30.165333 0l-377.088-377.130667a42.666667 42.666667 0 0 1 0-60.330666l377.088-377.130667a21.333333 21.333333 0 0 1 30.165333 0l30.208 30.165333a21.333333 21.333333 0 0 1 0 30.165334L325.802667 512z",
            "path://M694.528 512L347.562667 165.034667a21.333333 21.333333 0 0 1 0-30.165334l30.208-30.165333a21.333333 21.333333 0 0 1 30.165333 0l377.088 377.130667a42.666667 42.666667 0 0 1 0 60.330666l-377.088 377.130667a21.333333 21.333333 0 0 1-30.165333 0l-30.208-30.165333a21.333333 21.333333 0 0 1 0-30.165334L694.528 512z"
          ]
        },
        pageIconColor: "#1F2329",
        pageIconInactiveColor: "#1F2329",
        orient: 'vertical',
        icon: "rect",
        itemGap: 32,
        left: '50%',
        y: 'center',
        itemHeight: 8,
        itemWidth: 8, //修改icon图形大小
        itemStyle: {
          borderWidth: 0.1
        },
        inactiveBorderWidth: 0.1,
        textStyle: {
          rich: {
            name: {
              fontSize: 14,
              align: 'left',
              width: 50,
              fontWeight: 400,
              color: '#646A73'
            },
            num: {
              fontSize: 14,
              align: 'right',
              color: '#646A73',
              fontWeight: 500,
              padding: [0, 0, 0, 155]
            }
          }
        },
        data:this.pieData,
        formatter:function(name) {
          //通过name获取到数组对象中的单个对象
          let singleData = self.pieData.filter(function (item) {
            return item.name === name;
          });
          let showValue = singleData[0].value;
          if (showValue === '-') {
            showValue = 0;
          }
          return [`{name|${name}}`, `{num|${showValue}}`].join('');
        }
      };
    })
  }
}
</script>

<style scoped>
</style>


