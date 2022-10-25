<template>
  <div>
    <el-row v-for="(i) in (customFieldRowNums)" :key="i">
    <span class="custom-item" v-for="(item, j) in sortCustomFields" :key="j">
      <span v-if="j >= (i - 1) * 3 && j < (i - 1) * 3 + 3 ">
        <el-col :span="24">
          <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                        :label-width="formLabelWidth">
            <custom-filed-component
              :data="item"
              :form="form"
              prop="defaultValue"
              :disabled="(item.type !== 'richText' && isPublic) || disabled"
              :default-open="defaultOpen"
            />
          </el-form-item>
        </el-col>
      </span>
    </span>
    </el-row>
  </div>
</template>

<script>
import {SYSTEM_FIELD_NAME_MAP} from "../../utils/table-constants";
import CustomFiledComponent from "../template/CustomFiledComponent";
import {sortCustomFields} from "../../utils/custom_field";

export default {
  name: "CustomFiledFormRow",
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
    },
    disabled: {
      type: Boolean,
      default() {
        return false;
      }
    },
    defaultOpen: {
      type: String,
      default() {
        return 'preview';
      }
    },
  },
  computed: {
    customFieldRowNums() {
      let size = this.issueTemplate.customFields ? this.issueTemplate.customFields.length : 0
      let val = parseInt(size / 3);
      return size % 3 === 0 ? val : (val + 1);
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    sortCustomFields() {
      return sortCustomFields(this.issueTemplate.customFields);
    },
  }
}
</script>

<style scoped>

</style>
