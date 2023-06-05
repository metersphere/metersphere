<template>
  <div>
    <el-row v-for="(i) in (customFieldRowNums)" :key="i">
    <span class="custom-item" v-for="(item, j) in sortCustomFields" :key="j">
      <span v-if="j >= (i - 1)*3 && j < (i - 1)*3+3">
        <el-col :span="8" v-if="item.type !== 'richText'">
          <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item[formProp]"
                        :label-width="formLabelWidth">
            <custom-filed-component
              prop="defaultValue"
              :data="item"
              :form="form"
              :default-open="defaultOpen"
              :disabled="isPublic"
              :form-prop="formProp"
              @inputSearch="handleInputSearch"
              ref="customFiled"
            />
          </el-form-item>
        </el-col>
        <div v-else>
          <el-col :span="24">
            <el-form-item
              :label="item.system ? $t(systemNameMap[item.name]) : item.name"
              :prop="item[formProp]"
              :default-open="defaultOpen"
              :label-width="formLabelWidth">
               <custom-filed-component
                 :data="item"
                 :form="form"
                 :form-prop="formProp"
                 prop="defaultValue"
                 @inputSearch="handleInputSearch"
                 ref="customFiled"/>
            </el-form-item>
          </el-col>
        </div>
      </span>
    </span>
    </el-row>
  </div>
</template>

<script>
import CustomFiledComponent from "../template/CustomFiledComponent";
import {SYSTEM_FIELD_NAME_MAP} from "../../utils/table-constants";

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
      },
    },
    formProp: {
      type: String,
      default: 'name'
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
      return size % 3 == 0 ? val : (val + 1);
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
    }
  },
  methods: {
    handleInputSearch(data, query) {
      this.$emit('inputSearch', data, query);
    },
    stopLoading() {
      this.$refs.customFiled.forEach(item => {
        item.stopLoading();
      });
    },
  }
}
</script>

<style scoped>

</style>
