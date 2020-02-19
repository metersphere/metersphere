<template>
  <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
           menu-trigger="click">
    <el-menu-item index="1"><a href="/" style="text-decoration: none;">{{ $t("i18n.home") }}</a></el-menu-item>
    <el-submenu index="7" popper-class="submenu">
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
    <el-submenu index="3" popper-class="submenu" v-permission="['test_manager']">
      <template slot="title">项目</template>
      <el-menu-item index="3-1">项目1</el-menu-item>
      <el-menu-item index="3-2">项目2</el-menu-item>
      <el-divider/>
      <el-menu-item index="/project">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">显示全部</span>
      </el-menu-item>
      <el-menu-item index="/createProject">
        <el-button type="text">创建项目</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
      <template slot="title">测试</template>
      <recent-test-plan/>
      <el-divider/>
      <el-menu-item index="/allTest">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 5px;">所有测试</span>
      </el-menu-item>
      <el-menu-item index="/createTest">
        <el-button type="text">创建测试</el-button>
      </el-menu-item>
    </el-submenu>
    <el-submenu index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
      <template slot="title">报告</template>
      <el-menu-item index="5-1">报告1</el-menu-item>
      <el-menu-item index="5-2">报告2</el-menu-item>
      <el-menu-item index="5-3">显示全部</el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
  import RecentTestPlan from "./testPlan/RecentTestPlan";
  import Cookies from "js-cookie";
  import {TokenKey} from "../../common/constants";

  export default {
    name: "MsMenus",
    components: {RecentTestPlan},
    created() {
      this.initMenuData();
      this.getCurrentUserInfo();
    },
    computed: {
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
    data() {
      return {
        organizationList: [
          { index: '7-1', name: '组织1'},
        ],
        workspaceList: [
          { index: '2-1', name: '无工作空间'},
        ],
        currentUserInfo: {},
        currentUserId: JSON.parse(Cookies.get(TokenKey)).id,
        workspaceIds: []
      }
    },
    methods: {
      initMenuData() {
        this.$get("/organization/list/userorg/" + this.currentUserId,response => {
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
        this.$post("/user/update", user);
        window.location.reload();
      }
    }
  }
</script>

<style>
  .header-menu.el-menu--horizontal > li.el-menu-item {
    padding-left: 0;
  }

  .header-menu.el-menu--horizontal > li {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }

  .header-menu.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }
</style>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }

  .el-icon-check {
    color: #44b349;
  }
</style>
