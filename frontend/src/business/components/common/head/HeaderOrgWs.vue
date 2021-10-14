<template>
  <el-dropdown size="medium" @command="changeWs" class="align-right">
    <span class="dropdown-link">
        {{ currentWorkspaceName }}<i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                  'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                  'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ', 'ORGANIZATION_USER:READ',
                  'WORKSPACE_USER:READ']">
        <el-dropdown-item :command="item" v-for="(item, index) in workspaceList" :key="index">
          {{ item.name }} <i class="el-icon-check" v-if="getCurrentWorkspaceId() === item.id"/>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import {
  fullScreenLoading,
  getCurrentUser,
  getCurrentWorkspaceId,
  saveLocalStorage,
  stopFullScreenLoading
} from "@/common/js/utils";
import {ORGANIZATION_ID, PROJECT_ID, WORKSPACE_ID} from "@/common/js/constants";

export default {
  name: "MsHeaderOrgWs",
  created() {
    this.initMenuData();
  },
  inject: [
    'reloadTopMenus',
  ],
  data() {
    return {
      organizationList: [
        {name: this.$t('organization.none')},
      ],
      workspaceList: [],
      currentUserId: getCurrentUser().id,
      workspaceIds: [],
      currentWorkspaceName: '',
      searchOrg: '',
      searchWs: '',
      orgListCopy: [{name: this.$t('organization.none')}],
      wsListCopy: [{name: this.$t('workspace.none')}]
    };
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
  },
  props: {
    color: String
  },
  watch: {
    searchOrg(val) {
      this.query('org', val);
    },
    searchWs(val) {
      this.query('ws', val);
    }
  },
  methods: {
    getCurrentWorkspaceId,
    initMenuData() {
      this.$get("/workspace/list/userworkspace/" + encodeURIComponent(this.currentUserId), response => {
        this.workspaceList = response.data;
        let workspace = response.data.filter(r => r.id === getCurrentWorkspaceId());
        if (workspace.length > 0) {
          this.currentWorkspaceName = workspace[0].name;
        }
      });
    },
    getRedirectUrl(user) {
      // console.log(user);
      if (!user.lastProjectId || !user.lastWorkspaceId) {
        // 没有项目级的权限直接回到 /
        // 只是某一个工作空间的用户组也转到 /
        return "/";
      }
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      if (redirectUrl.startsWith("/")) {
        redirectUrl = redirectUrl.substring(1);
      }
      redirectUrl = redirectUrl.split("/")[0];
      return '/' + redirectUrl + '/';
    },
    changeOrg(data) {
      let orgId = data.id;
      if (!orgId) {
        return false;
      }
      const loading = fullScreenLoading(this);
      this.$post("/user/switch/source/org/" + orgId, {}, response => {
        saveLocalStorage(response);

        sessionStorage.setItem(ORGANIZATION_ID, orgId);
        sessionStorage.setItem(WORKSPACE_ID, response.data.lastWorkspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);

        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
          this.reloadTopMenus(stopFullScreenLoading(loading));
        }).catch(err => err);
      });
    },
    changeWs(data) {
      let workspaceId = data.id;

      if (!workspaceId || getCurrentWorkspaceId() === workspaceId) {
        return false;
      }
      const loading = fullScreenLoading(this);
      this.$post("/user/switch/source/ws/" + workspaceId, {}, response => {
        saveLocalStorage(response);

        sessionStorage.setItem(ORGANIZATION_ID, response.data.lastOrganizationId);
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);

        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
          this.reloadTopMenus(stopFullScreenLoading(loading));
        }).catch(err => err);
      });
    },
    query(sign, queryString) {
      if (sign === 'org') {
        this.organizationList = queryString ? this.orgListCopy.filter(this.createFilter(queryString)) : this.orgListCopy;
      }
      if (sign === 'ws') {
        this.organizationList.forEach(org => {
          let wsListCopy = org.wsListCopy;
          org.workspaceList = queryString ? wsListCopy?.filter(this.createFilter(queryString)) : wsListCopy;
        });
      }
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
  color: #44b349;
  margin-left: 10px;
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

.org-ws-menu {
  height: 180px;
  overflow: auto;
}

.search-input {
  padding: 0;
  margin-top: -4px;
  background-color: var(--color_shallow);
}

.search-input >>> .el-input__inner {
  border-radius: 0;
  background-color: var(--color);
  color: #d2ced8;
  border-color: #b4aebe;
}

.title {
  display: inline-block;
  padding-left: 15px;
  max-width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.org-ws-name {
  display: inline-block;
  padding-left: 15px;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

</style>
