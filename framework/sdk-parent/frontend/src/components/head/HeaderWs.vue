<template>
  <el-dropdown size="medium" @command="changeWs" placement="bottom" class="align-right shepherd-workspace" trigger="click"
               v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                  'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                  'PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_CUSTOM_CODE:READ',
                  'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ','WORKSPACE_USER:READ']">
    <el-tooltip effect="light" placement="right">
      <div slot="content">{{ currentWorkspaceName }}</div>
      <span class="dropdown-link" style="max-width: 100px; text-overflow: ellipsis; white-space: nowrap; overflow: hidden; display: inline-block">
      {{ currentWorkspaceName }}
      </span>
    </el-tooltip>

    <i class="el-icon-caret-bottom el-icon--right"/>
    <template v-slot:dropdown>
      <el-dropdown-menu style="margin-top: 5px;" :style="{'color':color}">
        <el-input :placeholder="$t('project.search_by_name')"
                  prefix-icon="el-icon-search"
                  v-model="searchString"
                  clearable
                  class="search-input"
                  size="small"/>
        <div class="dropdown-content">
          <el-dropdown-item :command="item" v-for="(item, index) in workspaceList" :key="index"
              style="max-width: 250px; text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">
            <i class="el-icon-check" :style="{'visibility': workspaceId === item.id ? 'visible' : 'hidden'}"/>
            {{ item.name }}
          </el-dropdown-item>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import {fullScreenLoading, stopFullScreenLoading} from "../../utils";
import {getCurrentUser, getCurrentWorkspaceId} from "../../utils/token";
import {getUserWorkspaceList, switchWorkspace} from "../../api/workspace";
import {useUserStore} from "@/store";
import {getDefaultSecondLevelMenu} from "../../router";

const userStore = useUserStore();

export default {
  name: "MsHeaderWs",
  created() {
    this.$EventBus.$on('changeWs', this._changeWs);
    this.initMenuData();
  },
  destroyed() {
    this.$EventBus.$off('changeWs', this._changeWs);
  },
  inject: [
    'reloadTopMenus',
  ],
  data() {
    return {
      workspaceList: [],
      currentUserId: getCurrentUser().id,
      workspaceIds: [],
      currentWorkspaceName: sessionStorage.getItem("workspace_name"),
      wsListCopy: [{name: this.$t('workspace.none')}],
      searchString: ""
    };
  },
  computed: {
    workspaceId: () => {
      return getCurrentWorkspaceId();
    },
    color: function () {
      return `var(--primary_color)`;
    }
  },
  watch: {
    searchString(val) {
      this.query(val);
    },
    currentWorkspaceName() {
      sessionStorage.setItem("workspace_name", this.currentWorkspaceName);
    }
  },
  methods: {
    initMenuData() {
      getUserWorkspaceList()
        .then(response => {
          this.workspaceList = response.data;
          this.wsListCopy = response.data;
          let workspace = response.data.filter(r => r.id === this.workspaceId);
          if (workspace.length > 0) {
            this.currentWorkspaceName = workspace[0].name;
            this.workspaceList = response.data.filter(r => r.id !== this.workspaceId);
            this.workspaceList.unshift(workspace[0]);
          } else {
            // 工作空间不存在, 切换到查询的第一个
            this.currentWorkspaceName = response.data[0].name;
            this._changeWs(response.data[0].id);
          }
        });
    },
    getRedirectUrl(user) {
      // todo refactor permission check
      if (!user.lastProjectId || !user.lastWorkspaceId) {
        // 没有项目级的权限直接回到 /setting/project/:type
        // 只是某一个工作空间的用户组也转到 /setting/project/:type
        return "/setting/project/:type";
      }
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      if (!redirectUrl) {
        return '/setting';
      }
      if (redirectUrl.startsWith("/track") || redirectUrl.startsWith("/performance")
        || redirectUrl.startsWith("/api") || redirectUrl.startsWith("/ui")) {
        // 获取有权限的跳转路径
        redirectUrl = getDefaultSecondLevelMenu(redirectUrl);
      } else {
        if (redirectUrl.startsWith("/")) {
          redirectUrl = redirectUrl.substring(1);
        }
        redirectUrl = redirectUrl.split("/")[0];
        redirectUrl = '/' + redirectUrl + '/';
      }
      return redirectUrl;
    },
    changeWs(data) {
      let workspaceId = data.id;

      if (!workspaceId || this.workspaceId === workspaceId) {
        return false;
      }
      // todo
      // let isTestCaseMinderChanged = this.$store.state.isTestCaseMinderChanged;
      // if (isTestCaseMinderChanged) {
      //   // 脑图提示保存
      //   this.$store.commit('setTemWorkspaceId', workspaceId);
      //   return;
      // }
      this._changeWs(workspaceId);
    },
    _changeWs(workspaceId) {
      if (workspaceId) {
        const loading = fullScreenLoading(this);
        switchWorkspace(workspaceId)
          .then(response => {
            userStore.switchWorkspace(response);
            // 工作空间变了之后项目一定会变
            this.$EventBus.$emit('projectChange');
            this.$router.push(this.getRedirectUrl(response.data))
              .then(() => {
                stopFullScreenLoading(loading);
                this.reloadTopMenus();
              })
              .catch(err => err);
          });
      }
    },
    query(queryString) {
      this.workspaceList = queryString ? this.wsListCopy.filter(this.createFilter(queryString)) : this.wsListCopy;
    },
    createFilter(queryString) {
      return item => {
        return (item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
      };
    },
  }
};
</script>

<style scoped>
.el-icon-check {
  color: #783887;
  font-weight: bold;
}

::-webkit-scrollbar {
  width: 10px;
  height: 10px;
  position: fixed;
}

::-webkit-scrollbar-thumb {
  border-radius: 1em;
  background-color: var(--color_shallow);
  position: fixed;
}

::-webkit-scrollbar-track {
  border-radius: 1em;
  background-color: transparent;
  position: fixed;
}

.search-input {
  padding: 0;
  margin-top: -5px;
}

.search-input :deep(.el-input__inner) {
  border-radius: 0;
  border-color: #b4aebe;
}

:deep(el-submenu__title) {
  padding-left: 5px;
}

.dropdown-link {
  cursor: pointer;
  font-size: 12px;
  line-height: 40px;
  padding-right: 10px;
  padding-left: 5px;
}

.align-right {
  float: right;
}

.dropdown-content {
  height: 240px;
  overflow: auto;
  /*margin-top: 5px;*/
}

/* 设置滚动条的样式 */
.dropdown-content::-webkit-scrollbar {
  width: 8px;
}

/* 滚动槽 */
.dropdown-content::-webkit-scrollbar-track {
  border-radius: 10px;
}

/* 滚动条滑块 */
.dropdown-content::-webkit-scrollbar-thumb {
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.2);
}

.dropdown-content::-webkit-scrollbar-thumb:window-inactive {
  background: rgba(255, 0, 0, 0.4);
}

.global {
  color: var(--color);
}

.el-icon-caret-bottom:before {
  position: relative;
  top: -15px;
  left: -10px;
}
</style>
