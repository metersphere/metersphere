<template>
  <span>
     <el-select v-if="data.type === 'select' || data.type === 'multipleSelect'"
                @click.native="clickPane"
                :disabled="disabled"
                :multiple="data.type === 'multipleSelect'"
                @change="handleChange"
                clearable
                filterable v-model="data[prop]" :placeholder="$t('commons.default')">
      <el-option
        v-for="(item,index) in data.options ? data.options : []"
        :key="index"
        @change="handleChange"
        :label="getTranslateOption(item)"
        :value="item.value">
      </el-option>
    </el-select>

    <el-cascader
      v-else-if="data.type === 'cascadingSelect'"
      @click.native="clickPane"
      expand-trigger="hover"
      @change="handleChange"
      :props="{label: 'text'}"
      :options="data.options"
      v-model="data[prop]">
    </el-cascader>

    <el-input
      v-else-if="data.type === 'textarea'"
      @click.native="clickPane"
      type="textarea"
      @change="handleChange"
      :rows="2"
      :disabled="disabled"
      :placeholder="$t('commons.input_content')"
      class="custom-with"
      v-model="data[prop]">
    </el-input>

    <el-checkbox-group
      v-else-if="data.type === 'checkbox'"
      @click.native="clickPane"
      :disabled="disabled"
      v-model="data[prop]">
      <el-checkbox v-for="(item, index) in data.options ? data.options : []"
                   :key="index"
                   @change="handleChange"
                   :label="item.value">
        {{ getTranslateOption(item) }}
      </el-checkbox>
    </el-checkbox-group>

    <el-radio
      v-else-if="data.type === 'radio'"
      @click.native="clickPane"
      v-model="data[prop]"
      :disabled="disabled"
      v-for="(item,index) in data.options ? data.options : []"
      :key="index"
      @change="handleChange"
      :label="item.value">
      {{ getTranslateOption(item) }}
    </el-radio>

    <el-input-number
      v-else-if="data.type === 'int'"
      @click.native="clickPane"
      v-model="data[prop]"
      :disabled="disabled"
      @change="handleChange"/>

    <el-input-number
      v-else-if="data.type === 'float'"
      @click.native="clickPane"
      :disabled="disabled"
      @change="handleChange"
      v-model="data[prop]" :precision="2" :step="0.1"/>

     <el-date-picker
       class="custom-with"
       @click.native="clickPane"
       @change="handleChange"
       v-else-if="data.type === 'date' || data.type === 'datetime'"
       :value-format="data.type === 'date' ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm:ss'"
       :disabled="disabled"
       v-model="data[prop]"
       :type="data.type === 'date' ? 'date' : 'datetime'"
       :placeholder="$t('commons.select_date')">
    </el-date-picker>

    <el-select v-else-if="data.type === 'member' || data.type === 'multipleMember'"
               @click.native="clickPane"
               :multiple="data.type === 'multipleMember'"
               @change="handleChange"
               clearable
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
                  @click.native="clickPane"
                  @input="handleChange"
                  :read-only="disabled" :currentScenario="data" :prop="prop"/>

    <ms-mark-down-text v-else-if="data.type === 'richText'"
                       @click.native="clickPane"
                       :prop="prop"
                       @change="handleChange"
                       :default-open="defaultOpen"
                       :data="data" :disabled="disabled"/>

      <el-input v-else-if="data.type === 'password'"
                @click.native="clickPane"
                v-model="data[prop]"
                class="custom-with"
                auto-complete="new-password"
                show-password
                :disabled="disabled"
                @input="handleChange"/>

    <el-input v-else
              @click.native="clickPane"
              v-model="data[prop]"
              class="custom-with"
              maxlength="450"
              show-word-limit
              :disabled="disabled"
              @input="handleChange"/>

  </span>

</template>

<script>
import MsTableColumn from "../table/MsTableColumn";
import MsInputTag from "../MsInputTag";
import {getProjectMemberOption} from "../../api/user";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";

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
    if (['member', 'multipleMember'].indexOf(this.data.type) < 0) {
      return;
    }
    getProjectMemberOption()
      .then((r) => {
        this.memberOptions = r.data;
        if (this.data.name === '责任人' && this.data.system && this.isTemplateEdit) {
          this.memberOptions.unshift({id: 'CURRENT_USER', name: '创建人', email: ''});
        }
      });
  },
  methods: {
    clickPane(){
      this.$emit("onClick");
    },
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    },
    handleChange() {
      if (this.form) {
        this.$set(this.form, this.data.name, this.data[this.prop]);
      }
      this.$emit('change', this.data.name);
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

.custom-with :deep( .el-input__inner) {
  height: 32px;
}

:deep( .el-input--suffix .el-input__inner) {
  height: 32px;
}
</style>
