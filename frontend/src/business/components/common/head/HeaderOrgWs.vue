<template>
  <el-menu :unique-opened="true" mode="horizontal" router
           class="header-user-menu align-right"
           background-color="#2c2a48"
           text-color="#fff">
    <el-submenu index="1" popper-class="submenu"
                v-roles="['org_admin', 'test_manager', 'test_user', 'test_viewer']">
      <template v-slot:title>{{$t('commons.organization')}}: {{currentOrganizationName}}</template>
      <label v-for="(item,index) in organizationList" :key="index">
        <el-menu-item @click="changeOrg(item)">{{item.name}}
          <i class="el-icon-check"
             v-if="item.id === currentUserInfo.lastOrganizationId"></i>
        </el-menu-item>
      </label>
    </el-submenu>
    <el-submenu index="2" popper-class="submenu" v-roles="['test_manager', 'test_user', 'test_viewer']">
      <template v-slot:title>{{$t('commons.workspace')}}: {{currentWorkspaceName}}</template>
      <label v-for="(item,index) in workspaceList" :key="index">
        <el-menu-item @click="changeWs(item)">
          {{item.name}}
          <i class="el-icon-check" v-if="item.id === currentUserInfo.lastWorkspaceId"></i>
        </el-menu-item>
      </label>
    </el-submenu>
  </el-menu>
</template>

<script>
  import {
    ROLE_ORG_ADMIN,
    ROLE_TEST_MANAGER,
    ROLE_TEST_USER,
    ROLE_TEST_VIEWER,
    WORKSPACE_ID
  } from '../../../../common/js/constants';
  import {getCurrentUser, hasRoles, saveLocalStorage} from "../../../../common/js/utils";

  export default {
    name: "MsHeaderOrgWs",
    created() {
      this.initMenuData();
      this.getCurrentUserInfo();
    },
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
        currentWorkspaceName: ''
      }
    },
    computed: {
      currentUser: () => {
        return getCurrentUser();
      }
    },
    methods: {
      initMenuData() {
        if (hasRoles(ROLE_ORG_ADMIN, ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.$get("/organization/list/userorg/" + encodeURIComponent(this.currentUserId), response => {
            let data = response.data;
            this.organizationList = data;
            let org = data.filter(r => r.id === this.currentUser.lastOrganizationId);
            if (org.length > 0) {
              this.currentOrganizationName = org[0].name;
            }
          });
        }
        if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          if (!this.currentUser.lastOrganizationId) {
            return false;
          }
          this.$get("/workspace/list/orgworkspace/", response => {
            let data = response.data;
            if (data.length === 0) {
              this.workspaceList = [{name: this.$t('workspace.none')}]
            } else {
              this.workspaceList = data;
              let workspace = data.filter(r => r.id === this.currentUser.lastWorkspaceId);
              if (workspace.length > 0) {
                this.currentWorkspaceName = workspace[0].name;
                localStorage.setItem(WORKSPACE_ID, workspace[0].id);
              }
            }
          })
        }
      },
      getCurrentUserInfo() {
        this.$get("/user/info/" + encodeURIComponent(this.currentUserId), response => {
          this.currentUserInfo = response.data;
        })
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
          this.$router.push('/');
          window.location.reload();
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
          this.$router.push('/');
          window.location.reload();
        })
      }
    }
  }
</script>

<style scoped>
  .el-icon-check {
    color: #44b349;
    margin-left: 10px;
  }
</style>
