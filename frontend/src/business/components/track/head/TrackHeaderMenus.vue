<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <el-col :span="8">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active='$route.path'>
          <el-menu-item :index="'/track/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>
          <el-submenu :class="{'deactivation':!isProjectActivation}"
                      v-permission="['test_manager','test_user','test_viewer']" index="3" popper-class="submenu">
            <template v-slot:title>{{ $t('commons.project') }}</template>
            <ms-recent-list ref="projectRecent" :options="projectRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/project/all'"/>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/project/create'"
                              :title="$t('project.create')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']"
                      index="6" popper-class="submenu">
            <template v-slot:title>{{ $t('test_track.case.test_case') }}</template>
            <ms-recent-list ref="caseRecent" :options="caseRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/case/all'"/>
            <el-menu-item :index="testCaseEditPath" class="blank_item"></el-menu-item>
            <el-menu-item :index="testCaseProjectPath" class="blank_item"></el-menu-item>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/case/create'"
                              :title="$t('test_track.case.create_case')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="7" popper-class="submenu">
            <template v-slot:title>{{ $t('test_track.plan.test_plan') }}</template>
            <ms-recent-list ref="planRecent" :options="planRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/plan/all'"/>
            <el-menu-item :index="testPlanViewPath" class="blank_item"></el-menu-item>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/plan/create'"
                              :title="$t('test_track.plan.create_plan')"/>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="16"/>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "../../common/head/ShowAll";
import MsRecentList from "../../common/head/RecentList";
import MsCreateButton from "../../common/head/CreateButton";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "TrackHeaderMenus",
  components: {MsShowAll, MsRecentList, MsCreateButton},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      projectRecent: {
        title: this.$t('project.recent'),
        url: "/project/recent/5",
        index: function (item) {
          return '/track/case/' + item.id;
        },
        router: function (item) {
          return {name: 'testCase', params: {projectId: item.id, projectName: item.name}}
        }
      },
      caseRecent: {
        title: this.$t('test_track.recent_case'),
        url: "/test/case/recent/5",
        index: function (item) {
          return '/track/case/edit/' + item.id;
        },
        router: function (item) {
        }
      },
      planRecent: {
        title: this.$t('test_track.recent_plan'),
        url: "/test/plan/recent/5",
        index: function (item) {
          return '/track/plan/view/' + item.id;
        },
        router: function (item) {
        }
      }
    }
  },
  watch: {
    '$route'(to) {
      this.init();
    }
  },
  mounted() {
    this.init();
    this.registerEvents();
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
    init() {
      let path = this.$route.path;
      if (path.indexOf("/track/case") >= 0 && !!this.$route.params.projectId) {
        this.testCaseProjectPath = path;
        //不激活项目菜单栏
        this.isProjectActivation = false;
        this.reload();
      } else {
        this.isProjectActivation = true;
      }
      if (path.indexOf("/track/plan/view") >= 0) {
        this.testPlanViewPath = path;
        this.reload();
      }
      if (path.indexOf("/track/case/edit") >= 0) {
        this.testCaseEditPath = path;
        this.reload();
      }
    },
    registerEvents() {
      TrackEvent.$on(LIST_CHANGE, () => {
        this.$refs.projectRecent.recent();
        this.$refs.planRecent.recent();
        this.$refs.caseRecent.recent();
      });
    }
  }
}

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.blank_item {
  display: none;
}

.el-divider--horizontal {
  margin: 0;
}

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}

</style>
