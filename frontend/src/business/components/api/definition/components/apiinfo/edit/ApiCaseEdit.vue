<template>
  <api-test-base-container :show-aside="isEdit">
    <template v-slot:aside>
      <api-base-case-info :api="api" :module-options="moduleOptions" ref="apiBaseInfo"/>
    </template>
    <template v-slot:mainContainer>
      <api-test-info :project-id="projectId" :current-protocol="protocol" :api="api" :is-edit="isEdit"
                     @saveApiAndCase="saveApiAndCase"
                     @saveApi="saveApi"></api-test-info>
    </template>
  </api-test-base-container>
</template>

<script>
import ApiTestBaseContainer from "@/business/components/common/layout/ApiTestBaseContainer";
import ApiBaseInfo from "@/business/components/api/definition/components/apiinfo/edit/ApiBaseInfo";
import ApiTestInfo from "@/business/components/api/definition/components/apiinfo/edit/ApiTestInfo";
import ApiBaseCaseInfo from "@/business/components/api/definition/components/apiinfo/edit/ApiBaseCaseInfo";

export default {
  name: "ApiCaseEdit",
  components: {
    ApiBaseCaseInfo,
    ApiTestBaseContainer, ApiBaseInfo, ApiTestInfo
  },
  props: {
    api: {},
    moduleOptions: [],
    projectId: String,
    protocol: String,
    isEdit:Boolean,
  },
  methods: {
    saveApi(apiStruct) {
      let baseInfoCheck = this.$refs.apiBaseInfo.validateForm();
      if(baseInfoCheck){
        this.$emit("saveApi", apiStruct);
      }
    },
    saveApiAndCase(apiStruct){
      this.$emit("saveApiAndCase",apiStruct);
    }
  }
}
</script>

<style scoped>

</style>
