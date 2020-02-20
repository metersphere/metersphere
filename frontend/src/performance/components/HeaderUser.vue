<template>
  <div class="ms-org-ws">
    <el-row :gutter="10" style="height:200px;">
      <el-col :span="16" :offset="4">
        <el-menu :unique-opened="true" mode="horizontal" router
                 menu-trigger="click"
                 class="header-user-menu"
                 background-color="rgb(44, 42, 72)"
                 text-color="#fff">
          <el-submenu index="1" popper-class="submenu">
            <template slot="title">组织</template>
            <label v-for="(item,index) in organizationList" :key="index">
              <el-menu-item @click="clickMenu(item)">{{item.name}}
                <i class="el-icon-check"
                   v-if="item.id === currentUserInfo.lastSourceId || item.id === workspaceParentId"></i>
              </el-menu-item>
            </label>
          </el-submenu>
          <el-submenu index="2" popper-class="submenu">
            <template slot="title">工作空间</template>
            <label v-for="(item,index) in workspaceList" :key="index">
              <el-menu-item @click="clickMenu(item)">
                {{item.name}}
                <i class="el-icon-check" v-if="item.id === currentUserInfo.lastSourceId"></i>
              </el-menu-item>
            </label>
          </el-submenu>
        </el-menu>
      </el-col>

      <el-col :span="4">
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
  </div>
</template>

<script>
  import Cookies from 'js-cookie';
  import {TokenKey} from '../../common/constants';

  export default {
    name: "MsUser",
    created() {
      this.initMenuData();
      this.getCurrentUserInfo();
    },
    data() {
      return {
        organizationList: [
          {index: '7-1', name: '组织1'},
        ],
        workspaceList: [
          {index: '2-1', name: '无工作空间'},
        ],
        currentUserInfo: {},
        currentUserId: JSON.parse(Cookies.get(TokenKey)).id,
        workspaceIds: []
      }
    },
    computed: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
        window.console.log(user);
        return JSON.parse(user);
      },
      workspaceParentId() {
        let result = '';
        if (this.workspaceIds.includes(this.currentUserInfo.lastSourceId)) {
          let obj = this.workspaceList.filter(r => r.id === this.currentUserInfo.lastSourceId);
          if (obj.length > 0) {
            result = obj[0].organizationId;
          }
        }
        return result;
      }
    },
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            this.$i18n.locale = "zh_CN";
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
        this.$get("/organization/list/userorg/" + this.currentUserId, response => {
          this.organizationList = response.data;
        })
        this.$get("/workspace/list/userworkspace/" + this.currentUserId, response => {
          this.workspaceList = response.data;
          this.workspaceIds = response.data.map(r => r.id);
        })
      },
      getCurrentUserInfo() {
        this.$get("/user/info/" + this.currentUserId, response => {
          this.currentUserInfo = response.data;
        })
      },
      clickMenu(data) {
        if (data.id === this.currentUserInfo.lastSourceId) {
          return false;
        }
        window.console.log(data.id);
        let user = {};
        user.id = this.currentUserInfo.id;
        user.lastSourceId = data.id;
        this.$post("/user/switch/source/" + user.lastSourceId, {});
        window.location.reload();
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
  }

  .ms-org-ws {
    width: 30%;
    height: 40px;
    line-height: 40px;
  }

  .el-icon-check {
    color: #44b349;
    margin-left: 10px;
  }
</style>


