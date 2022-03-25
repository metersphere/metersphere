<template>
  <el-form label-position="right" label-width="115px" size="small">
    <el-form-item :label="'Jira '+ $t('commons.information')">
      <ms-instructions-icon size="10" :content="$t('organization.integration.jira_prompt_information')"/>
    </el-form-item>
    <el-form-item :label="'Jira ' + $t('organization.integration.account')" prop="account">
      <el-input v-model="data.jiraAccount" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
    <el-form-item label="Token" prop="password">
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
