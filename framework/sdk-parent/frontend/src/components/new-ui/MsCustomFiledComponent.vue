<template>
  <span>
    <el-select v-if="data.type === 'select' || data.type === 'multipleSelect'"
                :loading="loading"
                @click.native="clickPane"
                :disabled="disabled"
                :multiple="data.type === 'multipleSelect'"
                :class="{'ms-select-tag' : data.type === 'multipleSelect'}"
                @change="handleChange"
                @clear="handleClear"
                clearable
                filterable
                v-model="data[prop]"
                :filter-method="data.inputSearch ? handleSelectInput : null"
                :remote="data.inputSearch"
                :placeholder="$t('commons.default')">
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
      @change="handleChange" :precision="0" :step="1"/>

    <el-input-number
      v-else-if="data.type === 'float'"
      @click.native="clickPane"
      :disabled="disabled"
      @input.native="changeInput($event)"
      @change="handleChange"
      v-model="data[prop]" :step="0.1"/>

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
               :class="{'ms-select-tag' : data.type === 'multipleMember'}"
               @change="handleChange"
               clearable
               :disabled="disabled"
               filterable
               v-model="data[prop]"
               :placeholder="$t('commons.default')">
       <el-option
         v-for="(item) in data.options ? data.options : []"
         :key="item.value"
         :label="item.text + (item.email ? ' (' + item.email + ')' : '')"
         :value="item.value">
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
import {getProjectMemberById, getProjectMemberOption} from "../../api/user";
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
    'isTemplateEdit',
    'projectId'
  ],
  data() {
    return {
      originOptions: null,
      loading: false
    };
  },
  mounted() {
    this.clearDeletedOption();
    this.setFormData();
    this.getMemberOptions();
  },
  watch: {
    form() {
      this.setFormData();
    }
  },
  methods: {
    clearDeletedOption() {
      // 如果选项没有当前值，置空
      if (['select', 'multipleSelect', 'checkbox', 'radio'].indexOf(this.data.type) > -1 && this.data.options) {
        if (['multipleSelect', 'checkbox'].indexOf(this.data.type) > -1) {
         this.clearDeletedMultipleOption();
        } else {
          this.clearDeletedSingleOption();
        }
      }
    },
    clearDeletedMultipleOption() {
      let values = this.data[this.prop];
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
    },
    clearDeletedSingleOption() {
      if (!this.data.options.find(item => item.value === this.data[this.prop])) {
        // 没有选项则清空
        this.data[this.prop] = '';
      }
    },
    getMemberOptions() {
      if (['member', 'multipleMember'].indexOf(this.data.type) < 0) {
        return;
      }
      if (this.projectId) {
        getProjectMemberById(this.projectId)
          .then((r) => {
            this.handleMemberOptions(r.data);
          });
      } else {
        getProjectMemberOption()
          .then((r) => {
            this.handleMemberOptions(r.data);
          });
      }
    },
    handleMemberOptions(data) {
      this.data.options = data;
      this.data.options.forEach(item => {
        item.value = !isNaN(item.id) ? Number(item.id) : item.id;
        item.text = item.name;
      });
      if (this.data.name === '责任人' && this.data.system && this.isTemplateEdit) {
        this.data.options.unshift({id: 'CURRENT_USER', name: '创建人', email: ''});
      }
      if ('multipleMember' === this.data.type) {
        this.clearDeletedMultipleOption();
      } else {
        this.clearDeletedSingleOption();
      }
    },
    clickPane(){
      this.$emit("onClick");
    },
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    },
    changeInput(e) {
      // 默认浮点输入最多不超过10位
      if (e.target.value.indexOf('.') >= 0) {
        e.target.value = e.target.value.substring(0, e.target.value.indexOf('.') + 11);
      }
    },
    handleChange() {
      if (this.form) {
        this.$set(this.form, this.data.name, this.data[this.prop]);
      }
      this.$emit('change', this.data.name);
      this.$forceUpdate();
    },
    handleSelectInput(val) {
      this.loading = true;
      if (!this.originOptions) {
        this.originOptions = this.data.options;
      }
      if (!val) {
        // 置空搜索时，恢复回原始选项
        this.data.options = this.originOptions;
      }
      this.$emit('inputSearch', this.data, val);
    },
    handleClear() {
      if (this.originOptions && this.data.inputSearch) {
        // 置空搜索时，恢复回原始选项
        this.data.options = this.originOptions;
      }
    },
    stopLoading() {
      this.loading = false;
    },
    setFormData() {
      if (this.form && this.data && this.data[this.prop]) {
        this.$set(this.form, this.data.name, this.data[this.prop]);
      }
    }
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

:deep(.ms-select-tag.el-select span.el-tag--light) {
  width: 100%;
}
:deep(.ms-select-tag.el-select span.el-select__tags-text) {
  width: 90%;
}
</style>
