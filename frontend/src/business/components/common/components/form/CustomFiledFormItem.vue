<template>
  <div>
    <el-row v-for="(i) in (customFieldRowNums)" :key="i">
    <span class="custom-item" v-for="(item, j) in issueTemplate.customFields" :key="j">
      <span v-if="j >= (i - 1)*3 && j < (i - 1)*3+3">
        <el-col :span="8" v-if="item.type !== 'richText'">
          <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                        :label-width="formLabelWidth">
            <custom-filed-component :data="item" :form="form" prop="defaultValue" :disabled="isPublic"/>
          </el-form-item>
        </el-col>
        <div v-else>
          <el-col :span="24">
            <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                          :label-width="formLabelWidth">
               <custom-filed-component :data="item" :form="form" prop="defaultValue"/>
            </el-form-item>
          </el-col>
        </div>
      </span>
    </span>
    </el-row>
  </div>
</template>

<script>
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import CustomFiledComponent from "@/business/components/settings/workspace/template/CustomFiledComponent";

export default {
  name: "CustomFiledFormItem",
  components: {CustomFiledComponent},
  props: {
    issueTemplate: {
      type: Object,
      default() {
        return {}
      }
    },
    formLabelWidth: String,
    form: Object,
    isPublic: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  computed: {
    customFieldRowNums() {
      let size = this.issueTemplate.customFields ? this.issueTemplate.customFields.length : 0
      let val = parseInt(size / 3);
      return size % 3 == 0 ? val : (val + 1);
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
  }
}
</script>

<style scoped>

</style>
