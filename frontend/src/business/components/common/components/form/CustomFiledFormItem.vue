<template>
  <div>
    <el-row v-for="(i) in (customFieldRowNums)" :key="i">
    <span class="custom-item" v-for="(item, j) in sortCustomFields" :key="j">
      <span v-if="j >= (i - 1)*colNum && j < (i - 1)*colNum+colNum">
        <el-col :span=colSpan v-if="item.type !== 'richText'">
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
    },
    colNum: {
      type: Number,
      default() {
        return 3;
      }
    },
    colSpan: {
      type: Number,
      default() {
        return 8;
      }
    }
  },
  computed: {
    customFieldRowNums() {
      let size = this.issueTemplate.customFields ? this.issueTemplate.customFields.length : 0
      let val = parseInt(size / this.colNum);
      return size % this.colNum == 0 ? val : (val + 1);
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    sortCustomFields() {
      let total = 0;//定义total用于控制循环结束
      let customFields = this.issueTemplate.customFields;
      for (let i = 0; total < customFields.length; total++) {
        if (customFields[i].type === 'richText') {
          //循环到是0的位置就删除该元素0并且在arr末尾push进这个元素0，由于splice删除了该位置元素，所以i不用+1，下次循环仍然检查i位置的元素
          customFields.push(customFields.splice(i, 1)[0]);
        } else {
          i++;//循环到不是0的位置就继续往后循环
        }
      }
      return customFields;
    },
  }
}
</script>

<style scoped>

</style>
