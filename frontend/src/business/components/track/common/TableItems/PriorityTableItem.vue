<template>
  <div>
    <el-tag v-if="value == 'P0'"
            type="danger"
            effect="dark"
            size="mini">{{value}}</el-tag>
    <el-tag v-if="value == 'P1'"
            type="danger"
            effect="light"
            size="mini">{{value}}</el-tag>
    <el-tag v-if="value == 'P2'"
            type="warning"
            effect="dark"
            size="mini">{{value}}</el-tag>
    <el-tag v-if="value == 'P3'"
            type="warning"
            effect="light"
            size="mini">{{value}}</el-tag>
  </div>

</template>

<script>
    export default {
      name: "PriorityTableItem",
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
