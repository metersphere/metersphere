<template>
  <div v-loading="loading">
    <el-card class="ms-test-chart" :style="{height: h + 'px'}" ref="msDrawer">
      <el-row class="ms-row">
        <p class="tip"><span style="margin-left: 5px"></span> {{ $t('commons.report_statistics.chart') }} </p>
        <div class="ms-test-chart-header" v-if="!readOnly">
          <el-dropdown @command="exportCommand" :hide-on-click="false">
            <span class="el-dropdown-link">
              {{ $t('commons.export') }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="jpg">JPG</el-dropdown-item>
              <el-dropdown-item command="png">PNG</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <span style="margin: 0px 10px 10px">|</span>
          <el-select v-model="chartType" class="ms-col-type" size="mini" style="width: 100px" @change="generateOption">
            <el-option :key="t.id" :value="t.id" :label="t.name" v-for="t in charts"/>
          </el-select>
          <span style="margin: 0px 10px 10px">|</span>
          <el-select v-model="order" class="ms-col-type" size="mini" style="width: 120px" @change="orderCharts">
            <el-option :key="t.id" :value="t.id" :label="t.name" v-for="t in orders"/>
          </el-select>
          <span style="margin: 0px 10px 10px">|</span>
          <font-awesome-icon v-if="!isFullScreen && showFullScreen" class="report-alt-ico" :icon="['fa', 'expand-alt']"
                             size="lg" @click="fullScreen"/>
          <font-awesome-icon v-if="isFullScreen && showFullScreen" class="report-alt-ico" :icon="['fa', 'compress-alt']"
                             size="lg" @click="unFullScreen"/>
        </div>
      </el-row>
      <el-row style="overflow: auto">
        <ms-chart ref="chart1" v-if="!loading" :options="dataOption"
                  :style="{width: chartWidthNumber+'px', height: (h-70) + 'px'}" class="chart-config" :autoresize="true"
                  :id="reportId"/>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import echarts from "echarts";
import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "ReportChart",
  components: {MsChart},
  props: {
    loadOption: {},
    pieOption: {},
    reportId: String,
    chartType: {
      type: String,
      default() {
        return "bar";
      }
    },
    chartWidth: Number,
    needFullScreen: Boolean,
    readOnly: Boolean,
  },
  data() {
    return {
      dataOption: {},
      x: 0,
      y: 0,
      w: document.documentElement.clientWidth - 760,
      h: document.documentElement.clientHeight * 0.5,
      chartWidthNumber: document.documentElement.clientWidth - 760,
      isFullScreen: false,
      originalW: 100,
      originalH: 100,
      showFullScreen: {
        type: Boolean,
        default() {
          return true;
        }
      },
      charts: [
        {id: 'bar', name: this.$t('commons.report_statistics.bar')},
        {id: 'pie', name: this.$t('commons.report_statistics.pie')}
      ],
      order: "",
      orders: [{id: '', name: this.$t('commons.sort_default')}, {
        id: 'desc',
        name: this.$t('commons.report_statistics.desc')
      }, {
        id: 'asc',
        name: this.$t('commons.report_statistics.asc')
      }],
      loading: false,
      options: {},
      pieItemOption: {
        dataset: [{
          source: [
            ['Product', 'Sales', 'Price', 'Year'],
            ['Cake', 123, 32, 2011],
            ['Cereal', 231, 14, 2011],
            ['Tofu', 235, 5, 2011],
            ['Dumpling', 341, 25, 2011],
            ['Biscuit', 122, 29, 2011],
            ['Cake', 143, 30, 2012],
            ['Cereal', 201, 19, 2012],
            ['Tofu', 255, 7, 2012],
            ['Dumpling', 241, 27, 2012],
            ['Biscuit', 102, 34, 2012],
            ['Cake', 153, 28, 2013],
            ['Cereal', 181, 21, 2013],
            ['Tofu', 395, 4, 2013],
            ['Dumpling', 281, 31, 2013],
            ['Biscuit', 92, 39, 2013],
            ['Cake', 223, 29, 2014],
            ['Cereal', 211, 17, 2014],
            ['Tofu', 345, 3, 2014],
            ['Dumpling', 211, 35, 2014],
            ['Biscuit', 72, 24, 2014],
          ],
        }, {
          transform: {
            type: 'filter',
            config: {dimension: 'Year', value: 2011}
          },
        }, {
          transform: {
            type: 'filter',
            config: {dimension: 'Year', value: 2012}
          }
        }, {
          transform: {
            type: 'filter',
            config: {dimension: 'Year', value: 2013}
          }
        }],
        series: [{
          type: 'pie', radius: 50, center: ['50%', '25%'],
          datasetIndex: 1
        }, {
          type: 'pie', radius: 50, center: ['50%', '50%'],
          datasetIndex: 2
        }, {
          type: 'pie', radius: 50, center: ['50%', '75%'],
          datasetIndex: 3
        }],
        media: [{
          query: {minAspectRatio: 1},
          option: {
            series: [
              {center: ['25%', '50%']},
              {center: ['50%', '50%']},
              {center: ['75%', '50%']}
            ]
          }
        }, {
          option: {
            series: [
              {center: ['50%', '25%']},
              {center: ['50%', '50%']},
              {center: ['50%', '75%']}
            ]
          }
        }]
      },
    }
  },
  created() {
    if (this.chartType === 'pie') {
      this.dataOption = this.pieOption;
    } else {
      this.dataOption = this.loadOption;
    }

    if (this.needFullScreen) {
      this.w = document.documentElement.clientWidth;
    }
    this.countChartWidth();
    this.reload();
  },
  watch: {
    chartWidth() {
      this.countChartWidth();
    },
    chartType() {
      this.generateOption();
      this.countChartWidth();
    },
  },
  methods: {
    countChartWidth() {
      if (this.chartWidth === 0 || this.chartType === 'bar') {
        this.chartWidthNumber = this.w;
      } else {
        this.chartWidthNumber = this.chartWidth;
      }
    },
    orderCharts() {
      this.$emit('orderCharts', this.order);
    },
    generateOption() {
      if (this.chartType === 'pie') {
        this.dataOption = this.pieOption;
      } else {
        this.dataOption = this.loadOption;
      }
      this.dataOption.series.forEach(item => {
        item.type = this.chartType;
      });
      // this.reload();
    },
    reload() {
      this.loading = true
      this.generateOption();
      this.$nextTick(() => {
        this.loading = false;
      })
    },
    fullScreen() {
      this.originalW = this.w;
      this.originalH = this.h;
      this.w = document.body.clientWidth - 50;
      this.h = document.body.clientHeight;
      this.isFullScreen = true;
      this.$emit('hidePage', true);
    },
    unFullScreen() {
      this.w = this.originalW;
      this.h = this.originalH;
      this.isFullScreen = false;
      this.$emit('hidePage', false);
    },
    getImages(command) {
      let imageType = 'image/png';
      if (command === 'jpg') {
        imageType = 'image/jpg';
      }
      let returnImageDatas = "";
      if (document.getElementById(this.reportId)) {
        let chartsCanvas = document.getElementById(this.reportId).querySelectorAll('canvas')[0];
        if (chartsCanvas) {
          // toDataURL()是canvas对象的一种方法，用于将canvas对象转换为base64位编码
          returnImageDatas = chartsCanvas && chartsCanvas.toDataURL(imageType);
        }
      }
      this.$emit("getImage", returnImageDatas);
      return returnImageDatas;
    },
    exportCommand(command) {
      let fileName = 'report_pic.' + command;
      if (document.getElementById('picChart')) {
        let chartsCanvas = document.getElementById('picChart').querySelectorAll('canvas')[0]
        let mime = 'image/png';
        if (command === 'jpg') {
          mime = 'image/jpg';
        }
        if (chartsCanvas) {
          // toDataURL()是canvas对象的一种方法，用于将canvas对象转换为base64位编码
          let imageUrl = chartsCanvas && chartsCanvas.toDataURL(mime)
          if (navigator.userAgent.indexOf('Trident') > -1) {
            // IE11
            let arr = imageUrl.split(',')
            // atob() 函数对已经使用base64编码编码的数据字符串进行解码
            let bstr = atob(arr[1])
            let bstrLen = bstr.length
            // Uint8Array, 开辟 8 位无符号整数值的类型化数组。内容将初始化为 0
            let u8arr = new Uint8Array(bstrLen)
            while (bstrLen--) {
              // charCodeAt() 方法可返回指定位置的字符的 Unicode 编码
              u8arr[bstrLen] = bstr.charCodeAt(bstrLen)
            }
            //  msSaveOrOpenBlob 方法允许用户在客户端上保存文件，方法如同从 Internet 下载文件，这是此类文件保存到“下载”文件夹的原因
            window.navigator.msSaveOrOpenBlob(new Blob([u8arr], {type: mime}), fileName);
          } else {
            // 其他浏览器
            let $a = document.createElement('a')
            $a.setAttribute('href', imageUrl)
            $a.setAttribute('download', fileName)
            $a.click()
          }
        }
      }
    },
  },
}
</script>

<style scoped>

.ms-test-chart-header {
  z-index: 999;
  width: 380px;
  float: right;
  margin-right: 10px;
}

.report-alt-ico {
  font-size: 15px;
  margin: 0px 10px 0px;
  color: #8c939d;
}

.report-alt-ico:hover {
  color: black;
  cursor: pointer;
  font-size: 18px;
}

/deep/ .echarts {
  height: calc(100vh / 1.95);
}

.tip {
  float: left;
  font-size: 14px;
  border-radius: 2px;
  border-left: 2px solid #783887;
  margin: 0px 20px 0px;
}

.ms-row {
  padding-top: 10px;
}

.chart-config {
  width: 100%;
}

/deep/ .el-card__body {
  padding: 0px;
}

</style>
