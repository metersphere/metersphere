<template>
  <el-dropdown size="medium" @command="changeWs" placement="bottom" class="align-right"
               v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                  'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                  'PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_CUSTOM_CODE:READ',
                  'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ','WORKSPACE_USER:READ']" >
    <span class="dropdown-link">
      {{ currentWorkspaceName }}
      <i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu style="margin-top: 5px;">
        <el-input :placeholder="$t('project.search_by_name')"
                  prefix-icon="el-icon-search"
                  v-model="searchString"
                  clearable
                  class="search-input"
                  size="small"/>
        <div class="dropdown-content">
          <el-dropdown-item :command="item" v-for="(item, index) in workspaceList" :key="index">
            {{ item.name }}
            <i class="el-icon-check" v-if="getCurrentWorkspaceId() === item.id"/>
          </el-dropdown-item>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import {
  fullScreenLoading,
  getCurrentUser,
  getCurrentWorkspaceId, hasPermissions,
  saveLocalStorage,
  stopFullScreenLoading
} from "@/common/js/utils";
import {PROJECT_ID, WORKSPACE_ID} from "@/common/js/constants";

export default {
  name: "MsHeaderWs",
  created() {
    this.initMenuData();
  },
  inject: [
    'reloadTopMenus',
  ],
  data() {
    return {
      workspaceList: [],
      currentUserId: getCurrentUser().id,
      workspaceIds: [],
      currentWorkspaceName: '',
      wsListCopy: [{name: this.$t('workspace.none')}],
      searchString: ""
    };
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
  },
  watch: {
    searchString(val) {
      this.query(val);
    }
  },
  methods: {
    getCurrentWorkspaceId,
    initMenuData() {
      this.$get("/workspace/list/userworkspace/" + encodeURIComponent(this.currentUserId), response => {
        this.workspaceList = response.data;
        this.wsListCopy = response.data;
        let workspace = response.data.filter(r => r.id === getCurrentWorkspaceId());
        if (workspace.length > 0) {
          this.currentWorkspaceName = workspace[0].name;
          this.workspaceList = response.data.filter(r => r.id !== getCurrentWorkspaceId());
          this.workspaceList.unshift(workspace[0]);
        }
      });
    },
    getRedirectUrl(user) {
      if (!user.lastProjectId || !user.lastWorkspaceId) {
        // 没有项目级的权限直接回到 /setting/project/:type
        // 只是某一个工作空间的用户组也转到 /setting/project/:type
        return "/setting/project/:type";
      }
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      if (redirectUrl.startsWith("/")) {
        redirectUrl = redirectUrl.substring(1);
      }
      redirectUrl = redirectUrl.split("/")[0];
      return '/' + redirectUrl + '/';
    },
    changeWs(data) {
      let workspaceId = data.id;

      if (!workspaceId || getCurrentWorkspaceId() === workspaceId) {
        return false;
      }
      const loading = fullScreenLoading(this);
      this.$post("/user/switch/source/ws/" + workspaceId, {}, response => {
        saveLocalStorage(response);
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);
        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
          this.reloadTopMenus(stopFullScreenLoading(loading));
        }).catch(err => err);
      });
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
  margin-left: 10px;
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

.search-input >>> .el-input__inner {
  border-radius: 0;
  color: #d2ced8;
  border-color: #b4aebe;
}

/deep/ .el-submenu__title {
  padding-left: 5px;
}

.dropdown-link {
  cursor: pointer;
  font-size: 12px;
  color: rgb(245, 245, 245);
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

</style>
