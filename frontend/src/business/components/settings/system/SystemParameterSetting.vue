<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane :label="$t('system_parameter_setting.mailbox_service_settings')" name="email">
        <email-setting/>
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.ldap_setting')" name="ldap">
        <ldap-setting/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import EmailSetting from "./EmailSetting";
import LdapSetting from "./LdapSetting";

export default {
  name: "SystemParameterSetting",
  components: {
    EmailSetting, LdapSetting
  },
  data() {
    return {
      activeName: 'email'
    }
  }
}
</script>

<style scoped>

</style>
