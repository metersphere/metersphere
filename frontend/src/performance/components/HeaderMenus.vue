<template>
  <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
           :default-active='$route.path'
           menu-trigger="click">
    <el-menu-item index="/setting/personsetting">
      {{ $t("i18n.home") }}
    </el-menu-item>
    <el-submenu index="3" popper-class="submenu" v-permission="['test_manager']">
      <template slot="title">项目</template>
      <ms-recent-project/>
      <el-divider/>
      <el-menu-item index="/project/all">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">显示全部</span>
      </el-menu-item>
      <el-menu-item index="/project/create">
        <el-button type="text">创建项目</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
      <template slot="title">测试</template>
      <ms-recent-test-plan/>
      <el-divider/>
      <el-menu-item index="/allTest/all">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">所有测试</span>
      </el-menu-item>
      <el-menu-item index="/createTest">
        <el-button type="text">创建测试</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
      <template slot="title">报告</template>
      <el-menu-item index="5-1">报告1</el-menu-item>
      <el-menu-item index="5-2">报告2</el-menu-item>
      <el-menu-item index="5-3">显示全部</el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
  import MsRecentTestPlan from "./testPlan/RecentTestPlan";
  import MsRecentProject from "./project/RecentProject";

  export default {
    name: "MsMenus",
    components: {MsRecentTestPlan, MsRecentProject}
  }
</script>

<style>
  .header-menu.el-menu--horizontal > li.el-menu-item {
    padding-left: 0;
  }

  .header-menu.el-menu--horizontal > li {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }

  .header-menu.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }
</style>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }
</style>
