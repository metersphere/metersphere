<template>
  <el-row type="flex" justify="end">
    <el-col :span="21">
      <el-menu :unique-opened="true" mode="horizontal" router
               class="header-user-menu align-right"
               background-color="#2c2a48"
               text-color="#fff">
        <el-submenu index="1" popper-class="submenu"
                    v-permission="['org_admin', 'test_manager', 'test_user', 'test_viewer']">
          <template v-slot:title>【{{$t('commons.organization')}}】{{currentOrganizationName}}</template>
          <label v-for="(item,index) in organizationList" :key="index">
            <el-menu-item @click="changeOrg(item)">{{item.name}}
              <i class="el-icon-check"
                 v-if="item.id === currentUserInfo.lastOrganizationId"></i>
            </el-menu-item>
          </label>
        </el-submenu>
        <el-submenu index="2" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
          <template v-slot:title>【{{$t('commons.workspace')}}】{{currentWorkspaceName}}</template>
          <label v-for="(item,index) in workspaceList" :key="index">
            <el-menu-item @click="changeWs(item)">
              {{item.name}}
              <i class="el-icon-check" v-if="item.id === currentUserInfo.lastWorkspaceId"></i>
            </el-menu-item>
          </label>
        </el-submenu>
      </el-menu>
    </el-col>

    <el-col :span="3">
      <el-dropdown size="medium" @command="handleCommand" class="align-right">
        <span class="dropdown-link">
            {{currentUser.name}}<i class="el-icon-caret-bottom el-icon--right"/>
        </span>
        <template v-slot:dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="personal">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout">退出系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-col>
  </el-row>
</template>

<script>
  import {ROLE_ORG_ADMIN, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER, TokenKey, WORKSPACE_ID} from '../../../../common/constants';
  import {hasRoles} from "../../../../common/utils";

  export default {
    name: "MsUser",
    created() {
      this.initMenuData();
      this.getCurrentUserInfo();
    },
    data() {
      return {
        organizationList: [
          {index: '7-1', name: '无组织'},
        ],
        workspaceList: [
          {index: '2-1', name: '无工作空间'},
        ],
        currentUserInfo: {},
        currentUserId: JSON.parse(localStorage.getItem(TokenKey)).id,
        workspaceIds: [],
        currentOrganizationName: '选择组织',
        currentWorkspaceName: '选择工作空间'
      }
    },
    computed: {
      currentUser: () => {
        let user = localStorage.getItem(TokenKey);
        // window.console.log(user);
        return JSON.parse(user);
      }
    },
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            this.$setLang("en-US");
            break;
          case "logout":
            this.$get("/signout", function () {
              localStorage.removeItem(TokenKey);
              window.location.href = "/login";
            });
            break;
          default:
            break;
        }
      },
      initMenuData() {
        if (hasRoles(ROLE_ORG_ADMIN, ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.$get("/organization/list/userorg/" + this.currentUserId, response => {
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
              this.workspaceList = [{index: '1-1', name: '无工作区间'}]
            } else {
              this.workspaceList = data;
              let workspace = data.filter(r => r.id === this.currentUser.lastWorkspaceId);
              if (workspace.length > 0) {
                this.currentWorkspaceName = workspace[0].name;
                localStorage.setItem(WORKSPACE_ID, workspace[0].id);
              }
            }
            // this.workspaceIds = response.data.map(r = r.id);
          })
        }
      },
      getCurrentUserInfo() {
        this.$get("/user/info/" + this.currentUserId, response => {
          this.currentUserInfo = response.data;
        })
      },
      changeOrg(data) {
        let orgId = data.id;
        this.$post("/user/switch/source/org/" + orgId, {}, response => {
          localStorage.setItem(TokenKey, JSON.stringify(response.data));
          window.location.reload();
        })
      },
      changeWs(data) {
        let workspaceId = data.id;
        if (!workspaceId) {
          return false;
        }
        this.$post("/user/switch/source/ws/" + workspaceId, {}, response => {
          localStorage.setItem(TokenKey, JSON.stringify(response.data));
          localStorage.setItem("workspace_id", workspaceId);
          window.location.reload();
        })
      }
    }
  }
</script>
<style>
  .header-user-menu.el-menu--horizontal > li.el-submenu > * {
    height: 40px;
    line-height: 40px;
    color: inherit;
  }
</style>
<style scoped>
  .dropdown-link {
    cursor: pointer;
    font-size: 12px;
    color: rgb(245, 245, 245);
    line-height: 40px;
  }

  .el-icon-check {
    color: #44b349;
    margin-left: 10px;
  }

  .align-right {
    float: right;
  }
</style>


