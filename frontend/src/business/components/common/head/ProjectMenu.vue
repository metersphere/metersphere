<template>
  <span>
    <el-submenu index="10">
      <template v-slot:title>操作</template>
      <el-input
        placeholder="请输入内容"
        prefix-icon="el-icon-search"
        v-model="input2">
      </el-input>
    </el-submenu>
  </span>

</template>

<script>
export default {
  name: "ProjectMenu",
  data() {
    return {
      input2: '1'
    }
  }
}
</script>

<style scoped>

</style>
