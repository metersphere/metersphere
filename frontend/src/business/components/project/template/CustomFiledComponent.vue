<template>
  <span>
     <el-select v-if="data.type === 'select' || data.type === 'multipleSelect'"
                :disabled="disabled"
                :multiple="data.type === 'multipleSelect'"
                @change="handleChange"
                filterable v-model="data[prop]" :placeholder="$t('commons.default')">
      <el-option
          v-for="(item,index) in data.options ? data.options : []"
          :key="index"
          :label="getTranslateOption(item)"
          :value="item.value"
          @change="handleChange">
      </el-option>
    </el-select>

    <el-cascader
        v-else-if="data.type === 'cascadingSelect'"
        v-model="data[prop]"
        :options="data.options"
        :props="{label: 'text'}"
        expand-trigger="hover"
        @change="handleChange">
    </el-cascader>

    <el-input
        v-else-if="data.type === 'textarea'"
        v-model="data[prop]"
        :disabled="disabled"
        :placeholder="$t('commons.input_content')"
        :rows="2"
        class="custom-with"
        type="textarea"
        @change="handleChange">
    </el-input>

    <el-checkbox-group
        v-else-if="data.type === 'checkbox'"
        v-model="data[prop]"
        :disabled="disabled">
      <el-checkbox v-for="(item, index) in data.options ? data.options : []"
                   :key="index"
                   @change="handleChange"
                   :label="item.value">
        {{ getTranslateOption(item) }}
      </el-checkbox>
    </el-checkbox-group>

    <el-radio
        v-for="(item,index) in data.options ? data.options : []"
        v-else-if="data.type === 'radio'"
        :key="index"
        v-model="data[prop]"
        :disabled="disabled"
        :label="item.value"
        @change="handleChange">
      {{ getTranslateOption(item) }}
    </el-radio>

    <el-input-number
        v-else-if="data.type === 'int'"
        v-model="data[prop]"
        :disabled="disabled"
        @change="handleChange"/>

    <el-input-number
        v-else-if="data.type === 'float'"
        v-model="data[prop]"
        :disabled="disabled"
        :precision="2" :step="0.1" @change="handleChange"/>

     <el-date-picker
         v-else-if="data.type === 'date' || data.type === 'datetime'"
         v-model="data[prop]"
         :disabled="disabled"
         :placeholder="$t('commons.select_date')"
         :type="data.type === 'date' ? 'date' : 'datetime'"
         :value-format="data.type === 'date' ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm:ss'"
         class="custom-with"
         @change="handleChange">
    </el-date-picker>

    <el-select v-else-if="data.type === 'member' || data.type === 'multipleMember'"
               :multiple="data.type === 'multipleMember'"
               @change="handleChange"
               :disabled="disabled"
               filterable v-model="data[prop]" :placeholder="$t('commons.default')">
       <el-option
           v-for="(item) in memberOptions"
           :key="item.id"
           :label="item.name + (item.email ? ' (' + item.email + ')' : '')"
           :value="item.id">
       </el-option>
    </el-select>

    <ms-input-tag v-else-if="data.type === 'multipleInput'"
                  @input="handleChange"
                  :read-only="disabled" :currentScenario="data" :prop="prop"/>

    <ms-mark-down-text v-else-if="data.type === 'richText'"
                       :prop="prop"
                       @change="handleChange"
                       :default-open="defaultOpen"
                       :data="data" :disabled="disabled"/>

    <el-input class="custom-with"
              @input="handleChange"
              :disabled="disabled"
              v-else v-model="data[prop]" maxlength="450" show-word-limit/>

  </span>

</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
import {getProjectMemberOption} from "@/network/user";

export default {
  name: "CustomFiledComponent",
  components: {MsMarkDownText, MsInputTag, MsTableColumn},
  props: [
    'data',
    'prop',
    'form',
    'disabled',
    'defaultOpen',
    'isTemplateEdit'
  ],
  data() {
    return {
      memberOptions: [],
    };
  },
  mounted() {
    if (['select', 'multipleSelect', 'checkbox', 'radio'].indexOf(this.data.type) > -1 && this.data.options) {
      let values = this.data[this.prop];
      if (['multipleSelect', 'checkbox'].indexOf(this.data.type) > -1) {
        if (values && values instanceof Array) {
          for (let i = values.length - 1; i >= 0; i--) {
            if (!this.data.options.find(item => item.value === values[i])) {
              // 删除已删除的选项
              values.splice(i, 1);
            }
          }
        } else {
          // 不是数组类型，改成空数组
          this.data[this.prop] = [];
        }
      } else {
        if (!this.data.options.find(item => item.value === values)) {
          // 没有选项则清空
          this.data[this.prop] = '';
        }
      }
    }
    if (this.data.type === 'member' || this.data.type === 'multipleMember') {
      getProjectMemberOption((data) => {
        this.memberOptions = data;
        if (this.data.name === '责任人' && this.data.system && this.isTemplateEdit) {
          this.memberOptions.unshift({id: 'CURRENT_USER', name: '创建人', email: ''});
        }
      });
    }
  },
  methods: {
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    },
    handleChange() {
      if (this.form) {
        this.$set(this.form, this.data.name, this.data[this.prop]);
      }
      this.$forceUpdate();
    },
  }
};
</script>

<style scoped>
.el-select {
  width: 100%;
}

.el-date-editor.el-input {
  width: 100%;
}

.custom-with >>> .el-input__inner {
  height: 32px;
}

>>> .el-input--suffix .el-input__inner {
  height: 32px;
}
</style>
