<template>
  <chart
    :init-options="defaultInitOptions"
    :options="options"
    :theme="theme"
    :group="group"
    @click="onClick"
    @datazoom="datazoom"
    :watch-shallow="watchShallow"
    :manual-update="manualUpdate"
    :autoresize="autoresize"/>
</template>

<script>
export default {
  name: "MsChart",
  props: {
    options: Object,
    theme: [String, Object],
    initOptions: Object,
    group: String,
    autoresize: Boolean,
    watchShallow: Boolean,
    manualUpdate: Boolean
  },
  data() {
    return {
      defaultInitOptions: this.initOptions
    };
  },
  mounted() {

    this.defaultInitOptions = this.defaultInitOptions || {};
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
    datazoom(params) {
      this.$emit('datazoom', params);
    }
  }
};
</script>

<style scoped>

</style>
