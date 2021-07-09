<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>

          <el-menu-item v-show="isNewVersion" :index="'/api/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>
          <el-menu-item v-show="isOldVersion" :index="'/api/home_obsolete'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-menu-item v-show="isNewVersion" :index="'/api/definition'" v-permission="['PROJECT_API_DEFINITION:READ']">
            {{ $t("i18n.definition") }}
          </el-menu-item>

          <el-menu-item v-show="isNewVersion" :index="'/api/automation'" v-permission="['PROJECT_API_SCENARIO:READ']">
            {{ $t("i18n.automation") }}
          </el-menu-item>

          <el-menu-item v-show="isNewVersion" :index="'/api/automation/report'"
                        v-permission="['PROJECT_API_REPORT:READ']">
            {{ $t("i18n.report") }}
          </el-menu-item>

          <el-submenu v-show="isOldVersion" index="4">
            <template v-slot:title>{{ $t('commons.test') }}</template>
            <ms-recent-list ref="testRecent" :options="testRecent"/>
            <el-divider class="menu-divider"/>
            <ms-show-all :index="'/api/test/list/all'"/>
            <el-menu-item :index="apiTestProjectPath" class="blank_item"></el-menu-item>
            <ms-create-button :index="'/api/test/create'"
                              :title="$t('load_test.create')"/>
          </el-submenu>

          <el-submenu v-show="isOldVersion" index="5">
            <template v-slot:title>{{ $t('commons.report') }}</template>
            <ms-recent-list ref="reportRecent" :options="reportRecent"/>
            <el-divider class="menu-divider"/>
            <ms-show-all :index="'/api/report/list/all'"/>
          </el-submenu>


          <el-menu-item v-show="isOldVersion" :index="'/api/monitor/view'">
            {{ $t('commons.monitor') }}
          </el-menu-item>
        </el-menu>
      </el-col>
    </el-row>
  </div>

</template>

<script>

import MsRecentList from "../../common/head/RecentList";
import MsShowAll from "../../common/head/ShowAll";
import MsCreateButton from "../../common/head/CreateButton";
import MsCreateTest from "../../common/head/CreateTest";
import SearchList from "@/business/components/common/head/SearchList";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {mapGetters} from "vuex";

export default {
  name: "MsApiHeaderMenus",
  components: {SearchList, MsCreateTest, MsCreateButton, MsShowAll, MsRecentList, ProjectChange},
  data() {
    return {
      testRecent: {
        title: this.$t('load_test.recent'),
        url: "/api/recent/5",
        index: function (item) {
          return '/api/test/edit/' + item.id;
        },
        router: function (item) {
          return {path: '/api/test/edit', query: {id: item.id}};
        }
      },
      reportRecent: {
        title: this.$t('report.recent'),
        showTime: true,
        url: "/api/report/recent/5",
        index: function (item) {
          return '/api/report/view/' + item.id;
        }
      },
      isProjectActivation: true,
      isRouterAlive: true,
      apiTestProjectPath: '',
      currentProject: ''
    };
  },
  computed: {
    ...mapGetters([
      'isNewVersion',
      'isOldVersion',
    ])
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
  },
  mounted() {

  },
  beforeDestroy() {

  }
};

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.menu-divider {
  margin: 0;
}

.blank_item {
  display: none;
}

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}
</style>
