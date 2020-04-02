<template>

  <div id="menu-bar">
    <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
             :default-active='$route.path'>
      <el-menu-item :index="'/' + beaseUrl + '/home'">
        {{ $t("i18n.home") }}
      </el-menu-item>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="3" popper-class="submenu" v-permission="['test_manager']" >
        <template slot="title">{{$t('commons.project')}}</template>
        <performance-recent-project v-if="beaseUrl == 'performance'"/>
        <functional-recent-project v-if="beaseUrl == 'functional'"/>
        <track-recent-project v-if="beaseUrl == 'tack'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/project/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/' + beaseUrl + '/project/create'">
          <el-button type="text">{{$t('project.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser && (beaseUrl == 'performance' || beaseUrl == 'functional')"
                  index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
        <template slot="title">{{$t('commons.test')}}</template>
        <performance-recent-test-plan v-if="beaseUrl == 'performance'"/>
        <functional-recent-test-plan v-if="beaseUrl == 'functional'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/test/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/' + beaseUrl + '/test/create'">
          <el-button type="text">{{$t('load_test.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser && (beaseUrl == 'performance' || beaseUrl == 'functional')"
                  index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
        <template slot="title">{{$t('commons.report')}}</template>
        <performance-recent-report v-if="beaseUrl == 'performance'"/>
        <functional-recent-report v-if="beaseUrl == 'functional'"/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/report/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser && beaseUrl == 'track'"
                  index="6" popper-class="submenu" v-permission="['test_manager', 'test_user']">
        <template slot="title">{{$t('test_track.test_case')}}</template>
        <recent-case-plan/>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/case/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('test_track.case_list')}}</span>
        </el-menu-item>
        <!--<el-menu-item :index="'/' + beaseUrl + '/case/create'">-->
          <!--<el-button type="text">{{$t('test_track.create_case')}}</el-button>-->
        <!--</el-menu-item>-->
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser && beaseUrl == 'track'"
                  index="7" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
        <template slot="title">{{$t('test_track.test_plan')}}</template>
        <el-divider/>
        <el-menu-item :index="'/' + beaseUrl + '/test/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/' + beaseUrl + '/test/create'">
          <el-button type="text">{{$t('test_track.create_plan')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <router-link  v-if="isCurrentWorkspaceUser && (beaseUrl == 'performance' || beaseUrl == 'functional')"
                    class="header-bottom" :to="'/' + beaseUrl + '/test/create'" v-permission="['test_user','test_manager']">
        <el-button type="primary" size="small">{{$t('load_test.create')}}</el-button>
      </router-link>

    </el-menu>
  </div>

</template>

<script>

  import PerformanceRecentTestPlan from "../../performance/test/PerformanceRecentTestPlan";
  import FunctionalRecentTestPlan from "../../functional/plan/FunctionalRecentTestPlan";
  import PerformanceRecentProject from "../../performance/project/PerformanceRecentProject";
  import FunctionalRecentProject from "../../functional/project/FunctionalRecentProject";
  import PerformanceRecentReport from "../../performance/report/PerformanceRecentReport";
  import FunctionalRecentReport from "../../functional/report/FunctionalRecentReport";
  import {checkoutCurrentWorkspace} from "../../../../common/utils";
  import TrackRecentProject from "../../track/project/TrackRecentProject";
  import RecentCasePlan from "../../track/case/RecentCasePlan";

  export default {
    name: "MsMenus",
    components: {PerformanceRecentReport, PerformanceRecentTestPlan, FunctionalRecentTestPlan, FunctionalRecentReport,
      PerformanceRecentProject, FunctionalRecentProject, TrackRecentProject, RecentCasePlan},
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
