<template>
  <api-test-base-container :show-aside="isEdit">
    <template v-slot:aside>
      <api-base-info :api="api" :module-options="moduleOptions" :current-protocol="protocol" ref="apiBaseInfo"></api-base-info>
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

export default {
  name: "ApiDefinitionEdit",
  components: {
    ApiTestBaseContainer, ApiBaseInfo, ApiTestInfo
  },
  props: {
    api: {},
    moduleOptions: [],
    projectId: String,
    protocol: String,
    isEdit:Boolean,
    isLoading:Boolean,
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
