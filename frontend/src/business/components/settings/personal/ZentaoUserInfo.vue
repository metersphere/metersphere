<template>
  <el-form label-position="right" label-width="115px" size="small">
    <el-form-item :label="$t('organization.integration.zentao_info')">
      <ms-instructions-icon size="10" :content="$t('organization.integration.zentao_prompt_information')"/>
    </el-form-item>
    <el-form-item :label="$t('organization.integration.zentao_account')" prop="account">
      <el-input v-model="data.zentaoUserName" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
    <el-form-item :label="$t('organization.integration.zentao_password')" prop="password">
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
