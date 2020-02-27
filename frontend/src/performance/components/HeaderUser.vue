<template>
  <el-row>
    <el-col :span="14" :offset="6">
      <el-menu :unique-opened="true" mode="horizontal" router
               menu-trigger="click"
               class="header-user-menu"
               background-color="rgb(44, 42, 72)"
               text-color="#fff">
        <el-submenu index="1" popper-class="submenu" v-permission="['org_admin']">
          <template slot="title">【组织】{{currentOrganizationName}}</template>
          <label v-for="(item,index) in organizationList" :key="index">
            <el-menu-item @click="changeOrg(item)">{{item.name}}
              <i class="el-icon-check"
                 v-if="item.id === currentUserInfo.lastOrganizationId"></i>
            </el-menu-item>
          </label>
        </el-submenu>
        <el-submenu index="2" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
          <template slot="title">【工作空间】{{currentWorkspaceName}}</template>
          <label v-for="(item,index) in workspaceList" :key="index">
            <el-menu-item @click="changeWs(item)">
              {{item.name}}
              <i class="el-icon-check" v-if="item.id === currentUserInfo.lastWorkspaceId"></i>
            </el-menu-item>
          </label>
        </el-submenu>
      </el-menu>
    </el-col>

    <el-col :span="2" :offset="2">
      <el-dropdown size="medium" @command="handleCommand">
        <span class="dropdown-link">
            {{currentUser.name}}<i class="el-icon-caret-bottom el-icon--right"/>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="personal">个人信息</el-dropdown-item>
          <el-dropdown-item command="logout">退出系统</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </el-col>
  </el-row>
</template>

<script>
  import Cookies from 'js-cookie';
  import {ROLE_ORG_ADMIN, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER, TokenKey} from '../../common/constants';

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
        currentUserId: JSON.parse(Cookies.get(TokenKey)).id,
        workspaceIds: [],
        currentOrganizationName: '选择组织',
        currentWorkspaceName: '选择工作空间'
      }
    },
    computed: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
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
              Cookies.remove(TokenKey);
              window.location.href = "/login";
            });
            break;
          default:
            break;
        }
      },
      initMenuData() {
        let roles = this.currentUser.roles.map(r => r.id);
        if (roles.indexOf(ROLE_ORG_ADMIN) > -1) {
          this.$get("/organization/list/userorg/" + this.currentUserId, response => {
            let data = response.data;
            this.organizationList = data;
            let org = data.filter(r => r.id === this.currentUser.lastOrganizationId);
            if (org.length > 0) {
              this.currentOrganizationName = org[0].name;
            }
          });
        }
        if (roles.indexOf(ROLE_TEST_MANAGER) > -1 || roles.indexOf(ROLE_TEST_USER) > -1 || roles.indexOf(ROLE_TEST_VIEWER) > -1) {
          if (this.currentUser.lastOrganizationId === null) {
            return false;
          }
          this.$get("/workspace/list/orgworkspace/", response => {
            let data = response.data;
            if (data.length === 0) {
              this.workspaceList = [{index:'1-1', name: '无工作区间'}]
            } else {
              this.workspaceList = data;
              let workspace = data.filter(r => r.id === this.currentUser.lastWorkspaceId);
              if (workspace.length > 0) {
                this.currentWorkspaceName = workspace[0].name;
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
        let sign = "organization";
        this.$post("/user/switch/source/" + sign + "/"  + orgId, {}, response => {
          Cookies.set(TokenKey, response.data);
          window.location.reload();
        })
      },
      changeWs(data) {
        let sign = "workspace";
        let workspaceId = data.id;
        // todo 工作空间为空判断
        if (typeof(workspaceId) == "undefined") {
          return false;
        }
        this.$post("/user/switch/source/" + sign + "/" + workspaceId, {}, response => {
          Cookies.set(TokenKey, response.data);
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
</style>


