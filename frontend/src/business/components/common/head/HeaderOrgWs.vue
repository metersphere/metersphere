<template>
  <el-menu :unique-opened="true" mode="horizontal"
           class="header-user-menu align-right"
           :background-color="color"
           active-text-color="#fff"
           default-active="1"
           text-color="#fff">
    <el-menu-item index="1" v-show="false">Placeholder</el-menu-item>
    <el-submenu index="1" popper-class="org-ws-submenu"
                :popper-append-to-body="true"
                v-permission="['PROJECT_TRACK_CASE:READ','PROJECT_TRACK_PLAN:READ','PROJECT_TRACK_REVIEW:READ',
                'PROJECT_API_DEFINITION:READ','PROJECT_API_SCENARIO:READ','PROJECT_API_REPORT:READ',
                'PROJECT_PERFORMANCE_TEST:READ','PROJECT_PERFORMANCE_REPORT:READ', 'ORGANIZATION_USER:READ',
                'WORKSPACE_USER:READ']">
      <template v-slot:title>
        <div class="org-ws-name" :title="currentOrganizationName + '-' + currentWorkspaceName">
          <div>{{ currentWorkspaceName || currentOrganizationName }}</div>
        </div>
      </template>
      <el-input :placeholder="$t('project.search_by_name')"
                prefix-icon="el-icon-search"
                v-model="searchOrg"
                clearable
                class="search-input"
                size="small"/>
      <div class="org-ws-menu">
        <el-submenu :index="1+'-'+index" v-for="(item, index) in organizationList"
                    :popper-append-to-body="true"
                    :key="index">
          <template v-slot:title>
            <div @click="changeOrg(item)">
              {{ item.name }}
              <i class="el-icon-check" v-if="item.id === getCurrentOrganizationId()"></i>
            </div>
          </template>
          <el-input :placeholder="$t('project.search_by_name')"
                    prefix-icon="el-icon-search"
                    v-model="searchWs"
                    clearable
                    class="search-input"
                    size="small"/>
          <div class="org-ws-menu">
            <el-menu-item :index="1+'-'+index+'-'+index2" @click="changeWs(ws)"
                          v-for="(ws,index2) in item.workspaceList" :key="index2">
              <span class="title">
                {{ ws.name }}
              </span>
              <i class="el-icon-check" v-if="ws.id === getCurrentWorkspaceId()"></i>
            </el-menu-item>
          </div>
        </el-submenu>
      </div>
    </el-submenu>
  </el-menu>
</template>

<script>
import {getCurrentOrganizationId, getCurrentUser, getCurrentWorkspaceId, saveLocalStorage} from "@/common/js/utils";
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
      workspaceList: [
        {name: this.$t('workspace.none')},
      ],
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
    getCurrentOrganizationId,
    getCurrentWorkspaceId,
    initMenuData() {
      this.$get("/organization/list/userorg/" + encodeURIComponent(this.currentUserId), response => {
        let data = response.data;
        this.organizationList = data;
        this.orgListCopy = data;
        let org = data.filter(r => r.id === getCurrentOrganizationId());
        if (org.length > 0) {
          this.currentOrganizationName = org[0].name;
        }
        this.organizationList.forEach(org => {
          this.$get("/workspace/list/orgworkspace/" + encodeURIComponent(this.currentUserId) + "/" + org.id, response => {
            let d = response.data;
            if (d.length === 0) {
              // org.workspaceList = [{name: this.$t('workspace.none')}];
              // this.$set(org, 'workspaceList', [{name: this.$t('workspace.none')}]);
            } else {
              this.$set(org, 'workspaceList', d);
              // org.workspaceList = d;
              org.wsListCopy = d;
              let workspace = d.filter(r => r.id === getCurrentWorkspaceId());
              if (workspace.length > 0) {
                this.currentWorkspaceName = workspace[0].name;
              }
            }
          });
        });
      });
      if (!this.currentUser.lastOrganizationId) {
        return false;
      }
      /*this.$get("/workspace/list/orgworkspace/" + getCurrentOrganizationId(), response => {
        let data = response.data;
        if (data.length === 0) {
          this.workspaceList = [{name: this.$t('workspace.none')}];
        } else {
          this.workspaceList = data;
          this.wsListCopy = data;
          let workspace = data.filter(r => r.id === getCurrentWorkspaceId());
          if (workspace.length > 0) {
            this.currentWorkspaceName = workspace[0].name;
          }
        }
      });*/
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
      this.$post("/user/switch/source/org/" + orgId, {}, response => {
        saveLocalStorage(response);

        sessionStorage.setItem(ORGANIZATION_ID, orgId);
        sessionStorage.setItem(WORKSPACE_ID, response.data.lastWorkspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);

        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
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

        sessionStorage.setItem(ORGANIZATION_ID, response.data.lastOrganizationId);
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);

        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
          this.reloadTopMenus();
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
  max-width: 110px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/deep/ .el-submenu__title {
  padding-left: 5px;
}

</style>
