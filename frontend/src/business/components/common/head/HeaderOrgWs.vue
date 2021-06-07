<template>
  <el-menu :unique-opened="true" mode="horizontal" router
           class="header-user-menu align-right"
           :background-color="color"
           active-text-color="#fff"
           default-active="1"
           text-color="#fff">
    <el-menu-item index="1" v-show="false">Placeholder</el-menu-item>
    <el-submenu index="1" popper-class="org-ws-submenu"
                v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ', 'ORGANIZATION_USER:READ',
                'WORKSPACE_USER:READ']">
      <template v-slot:title>{{ $t('commons.organization') }}:
        <span class="org-ws-name" :title="currentOrganizationName">
          {{ currentOrganizationName }}
        </span>
      </template>
      <el-input :placeholder="$t('project.search_by_name')"
                prefix-icon="el-icon-search"
                v-model="searchOrg"
                clearable
                class="search-input"
                size="small"/>
      <div class="org-ws-menu">
        <el-menu-item @click="changeOrg(item)" v-for="(item,index) in organizationList" :key="index">
          <span class="title">
            {{ item.name }}
          </span>
          <i class="el-icon-check"
             v-if="item.id === currentUserInfo.lastOrganizationId"></i>
        </el-menu-item>
      </div>
    </el-submenu>
    <el-submenu index="2" popper-class="submenu"
                v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ','WORKSPACE_USER:READ']"
    >
      <template v-slot:title>{{ $t('commons.workspace') }}:
        <span class="org-ws-name" :title="currentWorkspaceName">
          {{ currentWorkspaceName }}
        </span>
      </template>
      <el-input :placeholder="$t('project.search_by_name')"
                prefix-icon="el-icon-search"
                v-model="searchWs"
                clearable
                class="search-input"
                size="small"/>
      <div class="org-ws-menu">
        <el-menu-item @click="changeWs(item)" v-for="(item,index) in workspaceList" :key="index">
          <span class="title">
            {{ item.name }}
          </span>
          <i class="el-icon-check" v-if="item.id === currentUserInfo.lastWorkspaceId"></i>
        </el-menu-item>
      </div>
    </el-submenu>
  </el-menu>
</template>

<script>
import {getCurrentUser, saveLocalStorage} from "@/common/js/utils";

export default {
  name: "MsHeaderOrgWs",
  created() {
    this.initMenuData();
    this.getCurrentUserInfo();
  },
  inject: [
    'reloadTopMenus',
  ],
  data() {
    return {
      organizationList: [
        {name: this.$t('organization.none')},
      ],
      workspaceList: [
        {name: this.$t('workspace.none')},
      ],
      currentUserInfo: {},
      currentUserId: getCurrentUser().id,
      workspaceIds: [],
      currentOrganizationName: '',
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
    initMenuData() {
      this.$get("/organization/list/userorg/" + encodeURIComponent(this.currentUserId), response => {
        let data = response.data;
        this.organizationList = data;
        this.orgListCopy = data;
        let org = data.filter(r => r.id === this.currentUser.lastOrganizationId);
        if (org.length > 0) {
          this.currentOrganizationName = org[0].name;
        }
      });
      if (!this.currentUser.lastOrganizationId) {
        return false;
      }
      this.$get("/workspace/list/orgworkspace/", response => {
        let data = response.data;
        if (data.length === 0) {
          this.workspaceList = [{name: this.$t('workspace.none')}];
        } else {
          this.workspaceList = data;
          this.wsListCopy = data;
          let workspace = data.filter(r => r.id === this.currentUser.lastWorkspaceId);
          if (workspace.length > 0) {
            this.currentWorkspaceName = workspace[0].name;
          }
        }
      });
    },
    getCurrentUserInfo() {
      this.$get("/user/info/" + encodeURIComponent(this.currentUserId), response => {
        this.currentUserInfo = response.data;
      });
    },
    changeOrg(data) {
      let orgId = data.id;
      if (!orgId) {
        return false;
      }
      this.$post("/user/switch/source/org/" + orgId, {}, response => {
        saveLocalStorage(response);
        if (response.data.workspaceId) {
          localStorage.setItem("workspace_id", response.data.workspaceId);
        }
        // if (response.data.lastProjectId) {
        //   localStorage.setItem(PROJECT_ID, response.data.lastProjectId);
        // } else {
        //   localStorage.removeItem(PROJECT_ID);
        // }
        this.$router.push('/').then(() => {
          this.reloadTopMenus();
        }).catch(err => err);
      });
    },
    changeWs(data) {
      let workspaceId = data.id;
      if (!workspaceId) {
        return false;
      }
      this.$post("/user/switch/source/ws/" + workspaceId, {}, response => {
        saveLocalStorage(response);
        localStorage.setItem("workspace_id", workspaceId);
        // if (response.data.lastProjectId) {
        //   localStorage.setItem(PROJECT_ID, response.data.lastProjectId);
        // } else {
        //   localStorage.removeItem(PROJECT_ID);
        // }
        this.$router.push('/').then(() => {
          this.reloadTopMenus();
        }).catch(err => err);
      });
    },
    query(sign, queryString) {
      if (sign === 'org') {
        this.organizationList = queryString ? this.orgListCopy.filter(this.createFilter(queryString)) : this.orgListCopy;
      }
      if (sign === 'ws') {
        this.workspaceList = queryString ? this.wsListCopy.filter(this.createFilter(queryString)) : this.wsListCopy;
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
  max-width: 110px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
