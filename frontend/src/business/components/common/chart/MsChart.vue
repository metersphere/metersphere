<template>
  <chart
    :init-options="defaultInitOptions"
    :options="options"
    :theme="theme"
    :group="group"
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
    }
  },
  mounted() {
    this.defaultInitOptions = this.defaultInitOptions || {};
    // 默认渲染svg
    if (!this.defaultInitOptions.renderer) {
      this.defaultInitOptions.renderer = 'svg';
    }
  }
}
</script>

<style scoped>

</style>
