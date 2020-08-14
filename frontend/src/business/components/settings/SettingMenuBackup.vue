<template>
  <el-menu menu-trigger="click" :default-active="$route.path" router>
    <el-submenu index="1" v-permission="['admin']">
      <template v-slot:title>
        <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
        <span>{{$t('commons.system')}}</span>
      </template>
      <el-menu-item index="/setting/user">{{$t('commons.user')}}</el-menu-item>
      <el-menu-item index="/setting/organization">{{$t('commons.organization')}}</el-menu-item>
      <el-menu-item index="/setting/systemworkspace">{{$t('commons.workspace')}}</el-menu-item>
      <el-menu-item index="/setting/testresourcepool">{{$t('commons.test_resource_pool')}}</el-menu-item>
      <el-menu-item index="/setting/systemparametersetting">{{$t('commons.system_parameter_setting')}}</el-menu-item>
    </el-submenu>

    <el-submenu index="2" v-permission="['org_admin']" v-if="isCurrentOrganizationAdmin">
      <template v-slot:title>
        <font-awesome-icon class="icon organization" :icon="['far', 'building']" size="lg"/>
        <span>{{$t('commons.organization')}}</span>
      </template>
      <el-menu-item index="/setting/organizationmember" v-permission="['org_admin']">{{$t('commons.member')}}
      </el-menu-item>
      <el-menu-item index="/setting/organizationworkspace" v-permission="['org_admin']">{{$t('commons.workspace')}}
      </el-menu-item>
      <el-menu-item index="/setting/serviceintegration" v-permission="['org_admin']">{{$t('organization.service_integration')}}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="3" v-permission="['test_manager']" v-if="isCurrentWorkspaceUser">
      <template v-slot:title>
        <font-awesome-icon class="icon workspace" :icon="['far', 'list-alt']" size="lg"/>
        <span>{{$t('commons.workspace')}}</span>
      </template>
      <el-menu-item index="/setting/member">{{$t('commons.member')}}</el-menu-item>
      <el-menu-item index="/setting/testcase/report/template">{{$t('test_track.plan_view.report_template')}}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="4">
      <template v-slot:title>
        <font-awesome-icon class="icon" :icon="['far', 'user']" size="lg"/>
        <span>{{$t('commons.personal_info')}}</span>
      </template>
      <el-menu-item index="/setting/personsetting">{{$t('commons.personal_setting')}}</el-menu-item>
      <el-menu-item v-permission="['admin', 'org_admin', 'test_manager', 'test_user', 'test_viewer']"
                    index="/setting/apikeys">API Keys
      </el-menu-item>
    </el-submenu>

  </el-menu>
</template>

<script>
  import {checkoutCurrentOrganization, checkoutCurrentWorkspace} from "../../../common/js/utils";

  export default {
    name: "MsSettingMenuBackup",
    data() {
      return {
        isCurrentOrganizationAdmin: false,
        isCurrentWorkspaceUser: false,
      }
    },
    mounted() {
      this.isCurrentOrganizationAdmin = checkoutCurrentOrganization();
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
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
