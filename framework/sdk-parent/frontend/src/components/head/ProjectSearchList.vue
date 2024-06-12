<template>
  <div v-loading="loading">
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
          <el-tooltip effect="light" placement="right">
            <div slot="content">{{ i.name }}</div>
            <span class="title"> {{ i.name }}
          </span>
          </el-tooltip>
          <i class="el-icon-check" v-if="i.id === getCurrentProjectID()"></i>
        </template>
      </el-menu-item>
    </div>

  </div>
</template>

<script>
import {fullScreenLoading, stopFullScreenLoading} from "../../utils";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId, getCurrentWorkspaceId} from "../../utils/token";
import {hasPermissions} from "../../utils/permission";
import {getProjectModules, getUserProjectList, switchProject} from "../../api/project";
import {useUserStore} from "@/store";
import {getDefaultSecondLevelMenu} from "../../router";

export default {
  name: "SearchList",
  props: {
    options: Object,
    currentProject: String
  },
  created() {
    this.init();
    this.userStore = useUserStore();
  },
  inject: [
    'reload',
  ],
  mounted() {
    getProjectModules("project", getCurrentProjectID())
      .then(res => {
        let modules = res.data;
        sessionStorage.setItem('project_modules', JSON.stringify(modules));
      });
  },
  data() {
    return {
      loading: false,
      items: [],
      searchArray: [],
      searchString: '',
      userId: getCurrentUser().id,
      userStore: {}
    };
  },
  watch: {
    searchString(val) {
      this.query(val);
    }
  },
  methods: {
    getCurrentProjectID,
    init() {
      let data = {
        userId: getCurrentUserId(),
        workspaceId: this.$route.params.workspaceId || getCurrentWorkspaceId()
      };
      this.loading = true;
      getUserProjectList(data)
        .then(response => {
          this.loading = false;
          this.items = response.data;
          this.searchArray = response.data;
          let projectId = getCurrentProjectID();
          if (projectId) {
            // 路由跳转的项目ID与当前项目ID不一致时, 切换项目(API跨工作空间的场景)
            if (this.$route.fullPath.startsWith("/api") && this.$route.params.projectId && this.$route.params.projectId !== projectId && this.$route.params.projectId !== 'all') {
              this.change(this.$route.params.projectId);
            }
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
    reloadPage() {
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
          copyRedirectUrl = this.rewriteProjectRouteUrl(copyRedirectUrl);
          this.$router.push(copyRedirectUrl);
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

      this.rewriteProjectRouteParam();
      this.reload();
    },
    rewriteProjectRouteParam() {
      if (this.$route.query.projectId) {
        let query = {};
        Object.assign(query, this.$route.query);

        // 切换项目的时候如果有项目 ID 参数，修改为切换后的项目 ID，模块参数置空
        query.projectId = getCurrentProjectID();
        delete query.moduleId;

        this.$router.push({
          query: Object.assign(query)
        });
      }
    },
    rewriteProjectRouteUrl(url) {
      // 切换项目的时候如果有项目 ID 参数，修改为切换后的项目 ID，模块参数置空
      // url 示例 /track/case/all?projectId=AAA&moduleId=BBB
      let urlArray = url.split('?');
      let path = urlArray[0];  // /track/case/all
      if (urlArray.length > 1) {
        let query = urlArray[1];
        if (!query) {
          return path;
        }
        path += '?';
        // projectId=AAA&moduleId=BBB
        let queryParams = query.split('&');
        for (let queryParam of queryParams) {
          if (!queryParam) {
            break;
          }
          let kv = queryParam.split('=');
          let paramKey = kv[0];
          if (kv.length < 1) {
            break;
          }
          let paramValue = kv[1];
          if (!paramValue) {
            continue;
          }
          if (paramKey === 'projectId') {
            paramValue = getCurrentProjectID();
          } else if (paramKey === 'moduleId') {
            break;
          }
          path += paramKey + '=' + paramValue + '&';
        }
      }
      if (path[path.length - 1] === '&') {
        // 去掉末尾 &
        return path.substring(0, path.length - 1);
      }
      if (path.startsWith('/api/definition')) {
        this.$route.params.dataSelectRange = '';
        this.$route.params.projectId = getCurrentProjectID();
        return '/api/definition';
      }
      if (path.startsWith('/api/automation')) {
        this.$route.params.projectId = getCurrentProjectID();
        this.$route.params.dataSelectRange = '';
        return '/api/automation';
      }

      return path;
    },
    change(projectId) {
      let currentProjectId = getCurrentProjectID();
      if (projectId === currentProjectId) {
        return;
      }
      const loading = fullScreenLoading(this);
      switchProject({id: this.userId, lastProjectId: projectId})
        .then(response => {
          this.userStore.switchProject(response);
          this.$EventBus.$emit('projectChange');
          this.changeProjectName(projectId);
          // 刷新路由
          this.reloadPage();
          stopFullScreenLoading(loading);
        })
        .catch(() => {
          stopFullScreenLoading(loading);
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

.search-input :deep(.el-input__inner) {
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
