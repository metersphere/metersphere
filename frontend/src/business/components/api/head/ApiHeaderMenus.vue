<template>
  <div id="menu-bar">
    <el-row type="flex">
      <el-col :span="8">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item :index="'/api/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="3">
            <template v-slot:title>{{ $t('commons.project') }}</template>
            <ms-recent-list ref="projectRecent" :options="projectRecent"/>
            <el-divider class="menu-divider"/>
            <ms-show-all :index="'/api/project/all'"/>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/api/project/create'"
                              :title="$t('project.create')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="4">
            <template v-slot:title>{{ $t('commons.test') }}</template>
            <ms-recent-list ref="testRecent" :options="testRecent"/>
            <el-divider class="menu-divider"/>
            <ms-show-all :index="'/api/test/list/all'"/>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/api/test/create'"
                              :title="$t('load_test.create')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="5">
            <template v-slot:title>{{ $t('commons.report') }}</template>
            <ms-recent-list ref="reportRecent" :options="reportRecent"/>
            <el-divider class="menu-divider"/>
            <ms-show-all :index="'/api/report/list/all'"/>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="8">
        <el-row type="flex" justify="center">
          <ms-create-test :to="'/api/test/create'"/>
        </el-row>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>

<script>

import MsRecentList from "../../common/head/RecentList";
import MsShowAll from "../../common/head/ShowAll";
import MsCreateButton from "../../common/head/CreateButton";
import MsCreateTest from "../../common/head/CreateTest";
import {ApiEvent, LIST_CHANGE} from "@/business/components/common/head/ListEvent";

export default {
  name: "MsApiHeaderMenus",
  components: {MsCreateTest, MsCreateButton, MsShowAll, MsRecentList},
  data() {
    return {
      projectRecent: {
        title: this.$t('project.recent'),
        url: "/project/recent/5",
        index: function (item) {
          return '/api/test/list/' + item.id;
        },
        router: function (item) {
          return {name: 'ApiTestList', params: {projectId: item.id, projectName: item.name}}
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
        showTime: true,
        url: "/api/report/recent/5",
        index: function (item) {
          return '/api/report/view/' + item.id;
        }
      }
    }
  },
  methods: {
    registerEvents() {
      ApiEvent.$on(LIST_CHANGE, () => {
        this.$refs.projectRecent.recent();
        this.$refs.testRecent.recent();
        this.$refs.reportRecent.recent();
      });
    }
  },
  mounted() {
    this.registerEvents();
  }
}

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.menu-divider {
  margin: 0;
}
</style>
