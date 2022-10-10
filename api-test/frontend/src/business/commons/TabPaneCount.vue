<template>
  <span>
    <span>{{title}}</span>
    <span class="el-step__icon is-text ms-api-col ms-header" :class="{'fix-color': fixColor}" v-if="count > 0">
      <span class="el-step__icon-inner">{{count}}</span>
    </span>
  </span>
</template>

<script>
export default {
  name: "TabPaneCount",
  props: ['title', 'count', 'fixColor']
}
</script>

<style scoped>
.ms-header {
  /*background: #783887;*/
  /*color: white;*/
  color: #783887;
  height: 20px;
  border: 0 !important;
  display: inline-block;
  /*border-radius: 42%;*/
  margin-left: 6px;
}

.fix-color {
  background: #783887;
  color: white;
  /*color: #783887;*/
  /*height: 20px;*/
  /*border: 0px !important;*/
  border-radius: 42%;
  margin-left: 4px;
}
</style>
