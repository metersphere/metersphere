<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">

      <el-tab-pane :label="$t('system_config.base_config')" name="base">
        <base-setting/>
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.mailbox_service_settings')" name="email">
        <email-setting/>
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.ldap_setting')" name="ldap">
        <ldap-setting/>
      </el-tab-pane>
      <el-tab-pane v-if="hasLicense()" :label="$t('display.title')" name="display">
        <mx-display/>
      </el-tab-pane>
      <el-tab-pane v-if="hasLicense()" :label="$t('auth_source.title')" name="auth">
        <mx-auth/>
      </el-tab-pane>
      <el-tab-pane v-if="hasLicense()" :label="$t('module.title')" name="module">
        <mx-module/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import EmailSetting from "./EmailSetting";
import LdapSetting from "./LdapSetting";
import BaseSetting from "./BaseSetting";
import MxAuth from "./MxAuth";
import MxDisplay from "./MxDisplay";
import MxModule from "./MxModule";
import {hasLicense} from 'metersphere-frontend/src/utils/permission';

export default {
  name: "SystemParameterSetting",
  components: {
    BaseSetting,
    EmailSetting,
    LdapSetting,
    MxAuth,
    MxDisplay,
    MxModule
  },
  data() {
    return {
      activeName: 'base',
    };
  },
  methods: {
    hasLicense,
  }
};
</script>

<style scoped>

</style>
