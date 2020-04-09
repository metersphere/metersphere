<template>
  <el-menu-item :index="this.index">
    <font-awesome-icon :icon="['fa', 'list-ul']"/>
    <span>{{$t('commons.show_all')}}</span>
  </el-menu-item>
</template>

<script>
  export default {
    name: "MsShowAll",
    props: {
      index: String
    }
  }
</script>

<style scoped>
  svg + span {
    padding-left: 5px;
  }
</style>
