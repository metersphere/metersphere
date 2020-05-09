<template>
  <div id="menu-bar">
    <el-row type="flex">
      <el-col :span="8">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item :index="'/api/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="3" popper-class="submenu" v-permission="['test_manager']">
            <template v-slot:title>{{$t('commons.project')}}</template>
            <ms-recent-list :options="projectRecent"/>
            <el-divider/>
            <ms-show-all :index="'/api/project/all'"/>
            <ms-create-button :index="'/api/project/create'" :title="$t('project.create')"/>
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
            <template v-slot:title>{{$t('commons.test')}}</template>
            <ms-recent-list :options="testRecent"/>
            <el-divider/>
            <ms-show-all :index="'/api/test/list/all'"/>
            <ms-create-button :index="'/api/test/create'" :title="$t('load_test.create')"/>
            <!--            <el-menu-item :index="testCaseProjectPath" class="blank_item"></el-menu-item>-->
            <!--            <el-menu-item :index="testEditPath" class="blank_item"></el-menu-item>-->
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
            <template v-slot:title>{{$t('commons.report')}}</template>
            <ms-recent-list :options="reportRecent"/>
            <el-divider/>
            <ms-show-all :index="'/api/report/list/all'"/>
            <!--            <el-menu-item :index="reportViewPath" class="blank_item"></el-menu-item>-->
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="8">
        <el-row type="flex" justify="center">
          <ms-create-test :show="isCurrentWorkspaceUser" :to="'/api/test/create'"/>
        </el-row>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>

<script>

  import {checkoutCurrentWorkspace} from "../../../../common/js/utils";
  import MsRecentList from "../../common/head/RecentList";
  import MsShowAll from "../../common/head/ShowAll";
  import MsCreateButton from "../../common/head/CreateButton";
  import MsCreateTest from "../../common/head/CreateTest";

  export default {
    name: "MsApiHeaderMenus",
    components: {MsCreateTest, MsCreateButton, MsShowAll, MsRecentList},
    data() {
      return {
        isCurrentWorkspaceUser: false,
        // testCaseProjectPath: '',
        // testEditPath: '',
        // reportViewPath: '',
        // isRouterAlive: true,
        projectRecent: {
          title: this.$t('project.recent'),
          url: "/project/recent/5",
          index: function (item) {
            return '/api/' + item.id;
          },
          router: function (item) {
            return {name: 'fucPlan', params: {projectId: item.id, projectName: item.name}}
          }
        },
        testRecent: {
          title: this.$t('load_test.recent'),
          url: "/api/recent/5",
          index: function (item) {
            return '/api/test/edit/' + item.id;
          },
          router: function (item) {
            return {path: '/api/test/edit', query: {id: item.id}}
          }
        },
        reportRecent: {
          title: this.$t('report.recent'),
          url: "/api/report/recent/5",
          index: function (item) {
            return '/api/report/view/' + item.id;
          }
        }
      }
    },
    // watch: {
    //   '$route'(to, from) {
    //     let path = to.path;
    //     //激活菜单栏
    //     if (path.indexOf("/api/test/") >= 0) {
    //       this.testCaseProjectPath = '/api/test/' + this.$route.params.projectId;
    //       this.reload();
    //     }
    //     if (path.indexOf("/api/test/edit/") >= 0) {
    //       this.testEditPath = '/api/test/edit/' + this.$route.params.testId;
    //       this.reload();
    //     }
    //     if (path.indexOf("/api/report/view/") >= 0) {
    //       this.reportViewPath = '/api/report/view/' + this.$route.params.reportId;
    //       this.reload();
    //     }
    //   }
    // },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    // methods: {
    //   reload() {
    //     this.isRouterAlive = false;
    //     this.$nextTick(function () {
    //       this.isRouterAlive = true;
    //     })
    //   }
    // }
  }

</script>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }

  .el-menu.el-menu--horizontal {
    border-bottom: none;
  }

  #menu-bar {
    border-bottom: 1px solid #E6E6E6;
    background-color: #FFF;
  }

  .blank_item {
    display: none;
  }

</style>
