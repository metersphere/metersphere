<template>
  <div v-loading="result.loading">
    <el-input :placeholder="$t('project.search_by_name')"
              prefix-icon="el-icon-search"
              v-model="searchString"
              clearable
              class="search-input"
              size="small"/>
    <div v-if="items.length === 0" style="text-align: center; margin: 15px 0">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('project.no_data') }}
        </span>
    </div>
    <div v-else style="height: 150px;overflow: auto">
      <el-menu-item :key="i.id" v-for="i in items" @click="change(i.id)">
        <template slot="title">
          <div class="title">
            {{ i.name }}
          </div>
          <i class="el-icon-check" v-if="i.id === getCurrentProjectID()"></i>
        </template>
      </el-menu-item>
    </div>

  </div>
</template>

<script>
import {
  fullScreenLoading,
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId,
  hasPermissions,
  saveLocalStorage,
  stopFullScreenLoading
} from "@/common/js/utils";
import {PROJECT_ID} from "@/common/js/constants";
import {getDefaultSecondLevelMenu} from "@/business/components/common/router/router";

export default {
  name: "SearchList",
  props: {
    options: Object,
    currentProject: String
  },
  created() {
    this.init();
  },
  inject: [
    'reload',
    'reloadTopMenus'
  ],
  data() {
    return {
      result: {},
      items: [],
      searchArray: [],
      searchString: '',
      userId: getCurrentUser().id,
    };
  },
  watch: {
    searchString(val) {
      this.query(val);
    }
  },
  methods: {
    getCurrentProjectID,
    init: function () {
      let data = {
        userId: getCurrentUserId(),
        workspaceId: getCurrentWorkspaceId()
      };
      this.result = this.$post("/project/list/related", data, response => {
        this.items = response.data;
        this.searchArray = response.data;
        let projectId = getCurrentProjectID();
        if (projectId) {
          // 保存的 projectId 在当前项目列表是否存在; 切换工作空间后
          if (this.searchArray.length > 0 && this.searchArray.map(p => p.id).indexOf(projectId) === -1) {
            this.change(this.items[0].id);
          }
        } else {
          if (this.items.length > 0) {
            this.change(this.items[0].id);
          }
        }
        this.changeProjectName(projectId);
      });
    },
    query(queryString) {
      this.items = queryString ? this.searchArray.filter(this.createFilter(queryString)) : this.searchArray;
    },
    createFilter(queryString) {
      return item => {
        return (item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
      };
    },
    reloadPage: function () {
      // todo refactor permission check
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      let copyRedirectUrl = redirectUrl;
      if (!copyRedirectUrl) {
        this.$router.push("/");
        this.reload();
        return;
      }
      if (copyRedirectUrl.startsWith("/track") || copyRedirectUrl.startsWith("/performance")
        || copyRedirectUrl.startsWith("/api") || copyRedirectUrl.startsWith("/ui")) {
        // 获取有权限的跳转路径
        copyRedirectUrl = getDefaultSecondLevelMenu(copyRedirectUrl);
        if (copyRedirectUrl !== '/') {
          this.$router.push(copyRedirectUrl);
          this.reloadTopMenus();
          this.reload();
          return;
        }
      }
      // 跳转至下一个有权限的菜单
      let projectPermission = hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ');
      let uiPermission = hasPermissions('PROJECT_UI_ELEMENT:READ', 'PROJECT_UI_SCENARIO:READ', 'PROJECT_UI_REPORT:READ');
      let redirectMap = {
        project: projectPermission,
        ui: uiPermission,
      };
      let locations = redirectUrl.split('/');
      if (locations.length > 2 && !redirectMap[locations[1]]) {
        let v = true;
        for (const k in redirectMap) {
          if (redirectMap[k]) {
            this.$router.push("/" + k);
            v = false;
            break;
          }
        }
        if (v) {
          this.$router.push("/");
        }
      }
      this.reloadTopMenus();
      this.reload();
    },
    change(projectId) {
      let currentProjectId = getCurrentProjectID();
      if (projectId === currentProjectId) {
        return;
      }
      const loading = fullScreenLoading(this);
      this.$post("/user/update/current", {id: this.userId, lastProjectId: projectId}, (response) => {
        saveLocalStorage(response);
        this.currentProjectId = projectId;

        this.$EventBus.$emit('projectChange');
        // 保存session里的projectId
        sessionStorage.setItem(PROJECT_ID, projectId);
        // 刷新路由
        this.reloadPage();
        stopFullScreenLoading(loading, 1500);
        this.changeProjectName(projectId);
      }, () => {
        stopFullScreenLoading(loading, 1500);
      });
    },
    changeProjectName(projectId) {
      if (projectId) {
        let project = this.searchArray.filter(p => p.id === projectId);
        if (project.length > 0) {
          this.$emit("update:currentProject", project[0].name);
        }
      } else {
        this.$emit("update:currentProject", this.$t('project.select'));
      }
    }
  }
};
</script>

<style scoped>

.search-input {
  padding: 0;
  margin-top: -5px;
}

.search-input >>> .el-input__inner {
  border-radius: 0;
}

.title {
  display: inline-block;
  padding-left: 10px;
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.el-icon-check {
  color: #773888;
  margin-left: 2px;
  font-weight: bold;
}
</style>
