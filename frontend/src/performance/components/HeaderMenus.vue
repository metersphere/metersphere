<template>

  <div id="menu-bar">
    <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
             :default-active='$route.path'>
      <el-menu-item :index="'/' + beaseUrl + '/home'">
        {{ $t("i18n.home") }}
      </el-menu-item>

      <el-submenu index="3" popper-class="submenu" v-permission="['test_manager']" v-if="isCurrentWorkspaceUser">
        <template slot="title">{{$t('commons.project')}}</template>
        <performance-recent-project v-if="beaseUrl == 'performance'"/>
        <functional-recent-project v-if="beaseUrl == 'functional'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/project/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/' + beaseUrl + '/project/create'">
          <el-button type="text">{{$t('project.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']"
                  v-if="isCurrentWorkspaceUser">
        <template slot="title">{{$t('commons.test')}}</template>
        <performance-recent-test-plan v-if="beaseUrl == 'performance'"/>
        <functional-recent-test-plan v-if="beaseUrl == 'functional'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/plan/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/' + beaseUrl + '/plan/create'">
          <el-button type="text">{{$t('load_test.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']"
                  v-if="isCurrentWorkspaceUser">
        <template slot="title">{{$t('commons.report')}}</template>
        <performance-recent-report v-if="beaseUrl == 'performance'"/>
        <functional-recent-report v-if="beaseUrl == 'functional'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/report/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
      </el-submenu>

      <router-link class="header-bottom" :to="'/' + beaseUrl + '/plan/create'" v-permission="['test_user','test_manager']"
                   v-if="isCurrentWorkspaceUser">
        <el-button type="primary" size="small">{{$t('load_test.create')}}</el-button>
      </router-link>

    </el-menu>
  </div>

</template>

<script>

  import PerformanceRecentTestPlan from "./testPlan/PerformanceRecentTestPlan";
  import FunctionalRecentTestPlan from "./testPlan/FunctionalRecentTestPlan";
  import PerformanceRecentProject from "./project/PerformanceRecentProject";
  import FunctionalRecentProject from "./project/FunctionalRecentProject";
  import PerformanceRecentReport from "./report/PerformanceRecentReport";
  import FunctionalRecentReport from "./report/FunctionalRecentReport";
  import {checkoutCurrentWorkspace} from "../../common/utils";

  export default {
    name: "MsMenus",
    components: {PerformanceRecentReport, PerformanceRecentTestPlan, FunctionalRecentTestPlan, FunctionalRecentReport,
      PerformanceRecentProject,FunctionalRecentProject},
    data() {
      return {
        isCurrentWorkspaceUser: false,
      }
    },
    props: {
        beaseUrl: {
          type: String
        }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    }
  }

</script>

<style>

  .header-menu.el-menu--horizontal > li {
    height: 39px;
    line-height: 40px;
    color: dimgray;
  }

  .header-menu.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: dimgray;
  }

  .header-bottom {
    line-height: 40px;
    margin-left: 20%;
  }

</style>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }
  #menu-bar {
    border-bottom: 1px solid #E6E6E6;
  }
</style>
