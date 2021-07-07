<template>
  <el-form label-position="right" label-width="100px" size="small">
    <el-form-item :label="'Jira 信息'">
      <ms-instructions-icon size="10" :content="'该信息为通过Jira提交缺陷的用户认证信息，若未填写，则使用组织中配置的默认信息'"/>
    </el-form-item>
    <el-form-item :label="'Jira ' + $t('organization.integration.account')" prop="account">
      <el-input v-model="data.jiraAccount" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
    <el-form-item :label="'Jira ' + $t('organization.integration.password')" prop="password">
      <el-input v-model="data.jiraPassword" auto-complete="new-password"
                :placeholder="$t('organization.integration.input_api_password')" show-password/>
    </el-form-item>
    <el-form-item >
      <el-button type="primary" style="float: right" @click="$emit('auth', 'Jira')" size="mini">
        {{$t('commons.validate')}}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
export default {
  name: "JiraUserInfo",
  components: {MsInstructionsIcon},
  props: ['data'],
}
</script>

<style scoped>
.instructions-icon {
  margin-left: -5px;
}
</style>
