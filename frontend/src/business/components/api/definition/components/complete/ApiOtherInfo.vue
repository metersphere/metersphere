<template>
  <div>
    <ms-form-divider :title="'其他信息'"/>
    <api-info-container>
      <el-form :model="api" ref="api-form" label-width="100px">
        <el-collapse-transition>
          <el-tabs v-model="activeName" style="margin: 20px">
            <el-tab-pane :label="'备注'" name="remark" class="pane">
                <form-rich-text-item class="remark-item" :disabled="readOnly" :data="api" prop="remark" label-width="0"/>
            </el-tab-pane>
            <el-tab-pane :label="'依赖关系'" name="dependencies" class="pane">
              <dependencies-list :resource-id="api.id" resource-type="API" ref="dependencies"/>
            </el-tab-pane>
          </el-tabs>
        </el-collapse-transition>
      </el-form>
    </api-info-container>
  </div>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiInfoContainer from "@/business/components/api/definition/components/complete/ApiInfoContainer";
import DependenciesList from "@/business/components/common/components/graph/DependenciesList";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
export default {
  name: "ApiOtherInfo",
  components: {FormRichTextItem, DependenciesList, ApiInfoContainer, MsFormDivider},
  props: ['api','readOnly'],
  data() {
    return {
      activeName: 'remark'
    }
  },
  watch: {
    activeName() {
     if (this.activeName === 'dependencies') {
        this.$refs.dependencies.open();
      }
    }
  },
}
</script>

<style scoped>

.remark-item {
  padding: 15px 15px;
}

</style>
