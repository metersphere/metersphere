<template>
  <el-menu menu-trigger="click" :default-active="$route.path" router>
    <el-submenu index="1" v-permission="['admin']">
      <template slot="title">
        <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
        <span>系统</span>
      </template>
      <el-menu-item index="/setting/user">用户</el-menu-item>
      <el-menu-item index="/setting/organization">组织</el-menu-item>
      <el-menu-item index="/setting/systemworkspace">工作空间</el-menu-item>
      <el-menu-item index="/setting/testresourcepool">测试资源池</el-menu-item>
    </el-submenu>

    <el-submenu index="2" v-permission="['org_admin']">
      <template slot="title">
        <font-awesome-icon class="icon organization" :icon="['far', 'building']" size="lg"/>
        <span>组织</span>
      </template>
      <el-menu-item index="/setting/organizationmember" v-permission="['org_admin']">成员</el-menu-item>
      <el-menu-item index="/setting/organizationworkspace" v-permission="['org_admin']">工作空间</el-menu-item>
    </el-submenu>

    <el-submenu index="3" v-permission="['test_manager','test_user','test_viewer']">
      <template slot="title">
        <font-awesome-icon class="icon workspace" :icon="['far', 'list-alt']" size="lg"/>
        <span>工作空间</span>
      </template>
      <el-menu-item index="/setting/member">成员</el-menu-item>
    </el-submenu>

    <el-submenu index="4">
      <template slot="title">
        <font-awesome-icon class="icon" :icon="['far', 'user']" size="lg"/>
        <span>个人</span>
      </template>
      <el-menu-item index="/setting/personsetting">个人设置</el-menu-item>
    </el-submenu>

  </el-menu>
</template>

<script>
  export default {
    name: "MsSettingMenu"
  }
</script>

<style scoped>
  .el-menu {
    border-right: 0;
  }

  .el-menu-item {
    height: 40px;
    line-height: 40px;
  }

  .icon {
    width: 24px;
    margin-right: 10px;
  }

  .account {
    color: #5a78f0;
  }

  .organization {
    color: #b33a5b;
  }

  .workspace {
    color: #44b349;
  }
</style>
