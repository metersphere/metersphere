<template>
  <el-tooltip placement="top" v-if="showTooltip">
    <div slot="content">{{content}}</div>
    <el-tag :type="type" :effect="effect" :color="color" size="mini">{{content}}</el-tag>
  </el-tooltip>
  <el-tag :type="type" :effect="effect" :color="color" size="mini" v-else>{{content}}</el-tag>
</template>

<script>
  export default {
    name: "MsTag",
    props: {
      type: {
        type: String,
        default: 'primary',
      },
      color: {
        type: String,
        default: '',
      },
      content: {
        type: String
      },
      effect: {
        type: String,
        default: 'dark',
      },
      showTooltip: {
        type: Boolean,
        default: false,
      }
    }
  }
</script>

<style scoped>

</style>
