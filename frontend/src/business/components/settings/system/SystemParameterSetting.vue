<template>
  <div>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane label="邮箱设置" name="email">
        <email-setting/>
      </el-tab-pane>
      <el-tab-pane label="LDAP设置" name="ldap">
        <ldap-setting/>
      </el-tab-pane>
    </el-tabs>
  </div>
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
