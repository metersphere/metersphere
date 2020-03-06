<template>
  <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
           :default-active='$route.path'>
    <el-menu-item index="/setting/personsetting">
      {{ $t("i18n.home") }}
    </el-menu-item>
    <el-submenu index="3" popper-class="submenu" v-permission="['test_manager']" v-if="isCurrentWorkspaceUser">
      <template slot="title">{{$t('commons.project')}}</template>
      <ms-recent-project/>
      <el-divider/>
      <el-menu-item index="/project/all">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
      </el-menu-item>
      <el-menu-item index="/project/create">
        <el-button type="text">{{$t('project.create')}}</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']"
                v-if="isCurrentWorkspaceUser">
      <template slot="title">{{$t('commons.test')}}</template>
      <ms-recent-test-plan/>
      <el-divider/>
      <el-menu-item index="/loadtest/all">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
      </el-menu-item>
      <el-menu-item index="/createTest">
        <el-button type="text">{{$t('load_test.create')}}</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']"
                v-if="isCurrentWorkspaceUser">
      <template slot="title">{{$t('commons.report')}}</template>
      <ms-recent-report/>
      <el-divider/>
      <el-menu-item index="/report/all">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
  import MsRecentTestPlan from "./testPlan/RecentTestPlan";
  import MsRecentProject from "./project/RecentProject";
  import MsRecentReport from "./report/RecentReport";
  import {checkoutCurrentWorkspace} from "../../common/utils";

  export default {
    name: "MsMenus",
    components: {MsRecentReport, MsRecentTestPlan, MsRecentProject},
    data() {
      return {
        isCurrentWorkspaceUser: false,
      }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    }
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
