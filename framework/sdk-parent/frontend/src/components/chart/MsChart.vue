<template>
  <chart
      v-if="loaded"
      :style="{'height': chartHeight, 'width': chartWidth}"
      class="ms-chart"
      :init-options="defaultInitOptions"
      :option="options"
      :theme="theme"
      :group="group"
      @click="onClick"
      @datazoom="datazoom"
      :watch-shallow="watchShallow"
      :manual-update="manualUpdate"
      :autoresize="autoresize" id="chartsShow"/>
</template>

<script>
export default {
  name: "MsChart",
  props: {
    options: Object,
    theme: [String, Object],
    initOptions: {
      type: Object,
      default() {
        return {
          renderer: 'canvas'
        }
      }
    },
    group: String,
    autoresize: Boolean,
    watchShallow: Boolean,
    manualUpdate: Boolean,
    height: {
      type: [Number, String],
      default() {
        return 400
      }
    },
    width: [Number, String],
  },
  data() {
    return {
      loaded: true,
      defaultInitOptions: this.initOptions
    };
  },
  computed: {
    chartHeight() {
      if (this.height.indexOf) {
        return this.height;
      } else {
        return this.height + 'px';
      }
    },
    chartWidth() {
      if (!this.width) {
        return this.width;
      }
      if (this.width.indexOf) {
        return this.width;
      } else {
        return this.width + 'px';
      }
    }
  },
  mounted() {
    // this.defaultInitOptions = this.defaultInitOptions || {};
    // 默认渲染svg
    // BUG: 渲染svg之后 图上的legend 太多会不显示
    // if (!this.defaultInitOptions.renderer) {
    // this.defaultInitOptions.renderer = 'svg';
    // }
  },
  methods: {
    onClick(params) {
      this.$emit('onClick', params.data);
    },
    reload() {
      this.loaded = false;
      this.$nextTick(() => {
        this.loaded = true;
      })
    },
    exportCharts(fileName, type) {
      if (document.getElementsByClassName('ms-chart')) {
        let chartsCanvas = document.getElementsByClassName('ms-chart')[0].querySelectorAll('canvas')[0];
        let mime = 'image/png';
        if (chartsCanvas) {
          // toDataURL()是canvas对象的一种方法，用于将canvas对象转换为base64位编码
          let imageUrl = chartsCanvas && chartsCanvas.toDataURL("image/png");
          if (navigator.userAgent.indexOf('Trident') > -1) {
            // IE11
            let arr = imageUrl.split(',');
            // atob() 函数对已经使用base64编码编码的数据字符串进行解码
            let baseStr = atob(arr[1]);
            let baseStrLen = baseStr.length;
            // Uint8Array, 开辟 8 位无符号整数值的类型化数组。内容将初始化为 0
            let u8arr = new Uint8Array(baseStrLen);
            while (baseStrLen--) {
              // charCodeAt() 方法可返回指定位置的字符的 Unicode 编码
              u8arr[baseStrLen] = baseStr.charCodeAt(baseStrLen);
            }
            //  msSaveOrOpenBlob 方法允许用户在客户端上保存文件，方法如同从 Internet 下载文件，这是此类文件保存到“下载”文件夹的原因
            window.navigator.msSaveOrOpenBlob(new Blob([u8arr], {type: mime}), fileName + '.' + type.toLowerCase());
          } else {
            // 其他浏览器
            let $a = document.createElement('a');
            $a.setAttribute('href', imageUrl);
            $a.setAttribute('download', fileName + '.' + type.toLowerCase());
            $a.click();
          }
        }
      }
    },
    datazoom(params) {
      this.$emit('datazoom', params);
    }
  }
};
</script>

<style scoped>

</style>
