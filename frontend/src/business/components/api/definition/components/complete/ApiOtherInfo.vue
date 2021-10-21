<template>
  <div>
    <ms-form-divider :title="$t('test_track.case.other_info')"/>
    <api-info-container>
      <el-form :model="api" ref="api-form" label-width="100px">
        <el-collapse-transition>
          <el-tabs v-model="activeName" style="margin: 20px">
            <el-tab-pane :label="$t('commons.remark')" name="remark" class="pane">
                <form-rich-text-item class="remark-item" :disabled="readOnly && !hasPermissions" :data="api" prop="remark" label-width="0"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('commons.relationship.name')" name="dependencies" class="pane">
              <dependencies-list :read-only="readOnly" :resource-id="api.id" resource-type="API" ref="dependencies"/>
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
import {hasPermissions} from "@/common/js/utils";
export default {
  name: "ApiOtherInfo",
  components: {FormRichTextItem, DependenciesList, ApiInfoContainer, MsFormDivider},
  props: ['api','readOnly'],
  data() {
    return {
      activeName: 'remark'
    }
  },
  computed: {
    hasPermissions() {
      return hasPermissions('PROJECT_API_DEFINITION:READ+EDIT_API');
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
