<template>
  <span>
    <span>{{title}}</span>
    <span class="el-step__icon is-text ms-api-col ms-header" v-if="count > 0">
      <span class="el-step__icon-inner">{{count}}</span>
    </span>
  </span>
</template>

<script>
export default {
  name: "TabPaneCount",
  props: ['title', 'count']
}
</script>

<style scoped>
.ms-header {
  background: #783887;
  color: white;
  height: 20px;
  border-radius: 42%;
  margin-left: 4px;
}
</style>
