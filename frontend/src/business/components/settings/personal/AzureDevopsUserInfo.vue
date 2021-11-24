<template>
  <el-form label-position="right" label-width="175px" size="small">
    <el-form-item :label="$t('project.azureDevops_info')">
      <ms-instructions-icon size="10" :content="$t('project.azureDevops_tip')"/>
    </el-form-item>
    <el-form-item :label="$t('organization.integration.azure_pat')" prop="pat">
      <el-input v-model="data.azureDevopsPat" :placeholder="$t('organization.integration.input_azure_pat')"/>
    </el-form-item>
    <el-form-item >
      <el-button type="primary" style="float: right" @click="$emit('auth', 'AzureDevops')" size="mini">
        {{$t('commons.validate')}}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
  export default {
    name: "AzureDevopsUserInfo",
    components: {MsInstructionsIcon},
    props: ['data'],
  }
</script>

<style scoped>
  .instructions-icon {
    margin-left: -5px;
  }
</style>
