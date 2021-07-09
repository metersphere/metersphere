<template>
  <el-form label-position="right" label-width="100px" size="small">
    <el-form-item :label="'Tapd 昵称'">
      <ms-instructions-icon size="10" effect="light">
        <template>
          <img class="jira-image" src="../../../../assets/tapd-user.png"/>
        </template>
      </ms-instructions-icon>
    </el-form-item>
    <el-form-item :label="'Tapd 昵称'" prop="account">
      <el-input v-model="data.tapdUserName" :placeholder="$t('organization.integration.input_api_account')"/>
    </el-form-item>
    <el-form-item >
      <el-button type="primary" style="float: right" @click="$emit('auth', 'Tapd')" size="mini">
        {{$t('commons.validate')}}
      </el-button>
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
</style>
