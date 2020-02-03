<template>
  <el-row class="settings" type="flex" justify="end" align="middle">
    <router-link to="/content">
      <font-awesome-icon :icon="['fas', 'user-plus']" size="lg"/>
    </router-link>
    <router-link to="/content">
      <font-awesome-icon :icon="['fas', 'cog']" size="lg"/>
    </router-link>
  </el-row>
</template>

<script>
  export default {
    name: "MsSetting"
  }
</script>

<style scoped>
  .settings > * {
    padding-left: 15px;
    cursor: pointer;
    line-height: 40px;
    color: inherit;
  }

  .settings > * :hover {
    opacity: 0.7;
  }

  .settings > * :active {
    opacity: 0.8;
  }
</style>
