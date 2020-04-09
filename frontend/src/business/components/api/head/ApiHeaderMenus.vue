<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
             :default-active='$route.path'>
      <el-menu-item :index="'/api/home'">
        {{ $t("i18n.home") }}
      </el-menu-item>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="3" popper-class="submenu" v-permission="['test_manager']">
        <template v-slot:title>{{$t('commons.project')}}</template>
        <api-recent-project/>
        <el-divider/>
        <el-menu-item :index="'/api/project/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span>{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/api/project/create'">
          <el-button type="text">{{$t('project.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
        <template v-slot:title>{{$t('commons.test')}}</template>
        <api-recent-test/>
        <el-divider/>
        <el-menu-item :index="'/api/test/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span>{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/api/test/create'">
          <el-button type="text">{{$t('load_test.create')}}</el-button>
        </el-menu-item>
        <el-menu-item :index="testCaseProjectPath" class="blank_item"></el-menu-item>
        <el-menu-item :index="testEditPath" class="blank_item"></el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
        <template v-slot:title>{{$t('commons.report')}}</template>
        <api-recent-report/>
        <el-divider/>
        <el-menu-item :index="'/api/report/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span>{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="reportViewPath" class="blank_item"></el-menu-item>
      </el-submenu>

      <router-link v-if="isCurrentWorkspaceUser"
                   class="header-bottom" :to="'/api/test/create'" v-permission="['test_user','test_manager']">
        <el-button type="primary" size="small">{{$t('load_test.create')}}</el-button>
      </router-link>

    </el-menu>
  </div>

</template>

<script>

  import ApiRecentTest from "../../api/test/ApiRecentTest";
  import ApiRecentProject from "../../api/project/ApiRecentProject";
  import ApiRecentReport from "../../api/report/ApiRecentReport";
  import {checkoutCurrentWorkspace} from "../../../../common/utils";

  export default {
    name: "ApiHeaderMenus",
    components: {ApiRecentTest, ApiRecentReport, ApiRecentProject},
    data() {
      return {
        isCurrentWorkspaceUser: false,
        testCaseProjectPath: '',
        testEditPath: '',
        reportViewPath: '',
        isRouterAlive: true
      }
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        //激活菜单栏
        if (path.indexOf("/api/test/") >= 0){
          this.testCaseProjectPath = '/api/test/' + this.$route.params.projectId;
          this.reload();
        }
        if (path.indexOf("/api/test/edit/") >= 0){
          this.testEditPath = '/api/test/edit/' + this.$route.params.testId;
          this.reload();
        }
        if (path.indexOf("/api/report/view/") >= 0){
          this.reportViewPath = '/api/report/view/' + this.$route.params.reportId;
          this.reload();
        }
      }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    methods: {
      reload () {
        this.isRouterAlive = false;
        this.$nextTick(function () {
          this.isRouterAlive = true;
        })
      }
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
  svg + span {
    padding-left: 5px;
  }

  .submenu button {
    padding-left: 18px;
  }

  .el-divider--horizontal {
    margin: 0;
  }

  #menu-bar {
    border-bottom: 1px solid #E6E6E6;
  }

  .blank_item {
    display: none;
  }

</style>
