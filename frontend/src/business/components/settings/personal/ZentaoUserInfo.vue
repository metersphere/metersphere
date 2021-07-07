<template>
  <el-form label-position="right" label-width="100px" size="small">
    <el-form-item :label="'禅道信息'">
      <ms-instructions-icon size="10" :content="'该信息为通过禅道提交缺陷的的用户名、密码，若未填写，则使用组织中配置的默认信息'"/>
    </el-form-item>
    <el-form-item :label="'禅道' + $t('organization.integration.account')" prop="account">
      <el-input v-model="data.zentaoUserName" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
    <el-form-item :label="'禅道' + $t('organization.integration.password')" prop="password">
      <el-input v-model="data.zentaoPassword" auto-complete="new-password"
                :placeholder="$t('organization.integration.input_api_password')" show-password/>
    </el-form-item>
    <el-form-item >
      <el-button type="primary" style="float: right" @click="$emit('auth', 'Zentao')" size="mini">
        {{$t('commons.validate')}}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
export default {
  name: "ZentaoUserInfo",
  components: {MsInstructionsIcon},
  props: ['data'],
}
</script>

<style scoped>
.instructions-icon {
  margin-left: -5px;
}
</style>
