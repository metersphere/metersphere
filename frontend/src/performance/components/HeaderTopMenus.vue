<template>
  <el-menu mode="horizontal" menu-trigger="click"
           background-color="rgb(44, 42, 72)"
           class="header-top-menus"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :default-active="$route.path"
           router>
    <el-menu-item index="1">功能测试</el-menu-item>
    <el-menu-item index="/createTest" onselectstart="return false">性能测试</el-menu-item>
    <el-menu-item index="/setting" onselectstart="return false">系统设置</el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "MsTopMenus"
  }
</script>
<style>
  .header-top-menus.el-menu--horizontal > li {
    height: 40px;
    line-height: 40px;
    color: inherit;
  }

  .header-top-menus.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }

  .header-top-menus.el-menu--horizontal > li.is-active {
    background: #595591 !important;
  }
</style>
<style scoped>

</style>
