<template>
  <el-form label-position="right" label-width="115px" size="small">
    <el-form-item :label="'Tapd '+ $t('commons.nick_name')">
      <ms-instructions-icon size="10" effect="light">
        <template>
          <img class="jira-image" src="../../../../assets/tapd-user.png"/>
        </template>
      </ms-instructions-icon>
    </el-form-item>
    <el-form-item :label="'Tapd '+ $t('commons.nick_name')" prop="account">
      <el-input v-model="data.tapdUserName" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
  </el-form>
</template>

<script>
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
export default {
  name: "TapdUserInfo",
  components: {MsInstructionsIcon},
  props: ['data'],
}
</script>

<style scoped>
.instructions-icon {
  margin-left: -5px;
}

.el-form {
  margin-bottom: 45px;
}
</style>
