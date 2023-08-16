<template>
  <div class="json-schema-editor" style="padding: 0 10px">
    <el-row class="row" :gutter="10" v-if="reloadSelfOver">
      <el-col :style="{ minWidth: `${200 - 10 * deep}px` }" class="ms-col-name">
        <div :style="{ marginLeft: `${10 * deep}px` }" class="ms-col-name-c" />
        <span
          v-if="pickValue.type === 'object' || pickValue.type === 'array'"
          :class="hidden ? 'el-icon-caret-left ms-transform' : 'el-icon-caret-bottom'"
          @click="hidden = !hidden" />
        <span v-else style="width: 10px; display: inline-block"></span>
        <input
          class="el-input el-input__inner"
          style="height: 32px"
          :disabled="disabled || root"
          :value="pickKey"
          @blur="onInputName"
          size="small" />

        <el-tooltip v-if="root" :content="$t('schema.checked_all')" placement="top">
          <input
            type="checkbox"
            :disabled="disabled || (!isObject && !isArray(pickValue))"
            class="ms-col-name-required"
            @change="onRootCheck" />
        </el-tooltip>
        <el-tooltip v-else :content="$t('schema.required')" placement="top">
          <input
            type="checkbox"
            :disabled="disabled || isItem"
            :checked="checked"
            class="ms-col-name-required"
            @change="onCheck" />
        </el-tooltip>
      </el-col>

      <el-col style="width: 120px; padding: 0 5px" class="ms-col-name">
        <el-select
          v-model="pickValue.type"
          :disabled="disabled || disabledType"
          class="ms-col-type"
          @change="onChangeType"
          style="width: 110px"
          size="small">
          <el-option :key="t" :value="t" :label="t" v-for="t in types" />
        </el-select>
      </el-col>
      <el-col class="ms-col-name" style="min-width: 200px; padding: 0 5px">
        <ms-mock
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          :schema="pickValue"
          :scenario-definition="scenarioDefinition"
          :show-mock-vars="showMockVars"
          :need-mock="needMock"
          style="width: 100%"
          @editScenarioAdvance="editScenarioAdvance" />
      </el-col>
      <el-col v-if="showColumns('MIX_LENGTH')" class="item kv-select ms-col-name" style="width: 150px; padding: 0 5px">
        <el-input-number
          :min="0"
          :controls="false"
          v-model="pickValue.minLength"
          :placeholder="$t('schema.minLength')"
          size="small"
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          style="width: 140px" />
      </el-col>

      <el-col v-if="showColumns('MAX_LENGTH')" class="item kv-select ms-col-name" style="width: 150px; padding: 0 5px">
        <el-input-number
          :min="0"
          :controls="false"
          v-model="pickValue.maxLength"
          :placeholder="$t('schema.maxLength')"
          size="small"
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          style="width: 140px" />
      </el-col>

      <el-col v-if="showColumns('DEFAULT')" class="item kv-select ms-col-name" style="min-width: 200px; padding: 0 5px">
        <custom-input
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          v-model="pickValue.default"
          class="ms-col-title"
          :placeholder="$t('schema.default')"
          style="width: 100%"
          size="small" />
      </el-col>
      <el-col v-if="showColumns('PATTERN')" class="ms-col-name" style="min-width: 200px; padding: 0 5px">
        <custom-input
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          v-model="pickValue.pattern"
          class="ms-col-title"
          :placeholder="$t('schema.pattern')"
          size="small" />
      </el-col>
      <el-col v-if="showColumns('FORMAT')" class="ms-col-name" style="min-width: 120px; padding: 0 5px">
        <div v-if="advancedAttr.format">
          <el-select
            :disabled="
              disabled ||
              pickValue.type === 'object' ||
              pickKey === 'root' ||
              pickValue.type === 'array' ||
              pickValue.type === 'null'
            "
            v-model="pickValue.format"
            style="width: 100%"
            size="small"
            @change="formatChange">
            <el-option value="" :label="$t('schema.nothing')"></el-option>
            <el-option :key="t" :value="t" :label="t" v-for="t in advancedAttr.format.enums" />
          </el-select>
        </div>
        <div v-else>
          <el-input :disabled="true" size="small" :placeholder="$t('schema.format')"></el-input>
        </div>
      </el-col>
      <el-col v-if="showColumns('ENUM')" class="ms-col-name" style="min-width: 300px; padding: 0 5px">
        <custom-textarea
          :disabled="
            disabled ||
            pickValue.type === 'object' ||
            pickKey === 'root' ||
            pickValue.type === 'array' ||
            pickValue.type === 'null'
          "
          v-model="pickValue.enum"
          class="ms-col-title"
          :placeholder="$t('schema.enum')"
          />
      </el-col>
      <el-col v-if="showColumns('DESCRIPTION')" class="ms-col-name" style="min-width: 300px; padding: 0 5px">
        <custom-input
          :disabled="disabled"
          v-model="pickValue.description"
          class="ms-col-title"
          :placeholder="$t('schema.description')"
          size="small" />
      </el-col>
      <!--其余操作-->
      <el-col style="width: 220px" class="col-item-setting ms-col-name" v-if="!disabled">
        <div style="width: 80px">
          <el-tooltip class="item" effect="dark" :content="$t('schema.adv_setting')" placement="top">
            <i class="el-icon-setting" @click="onSetting" />
          </el-tooltip>
          <el-tooltip v-if="isObject || isArray(pickValue)" :content="$t('schema.add_child_node')" placement="top">
            <i class="el-icon-plus" @click="addChild" style="margin-left: 10px" />
          </el-tooltip>
          <el-tooltip v-if="!root && !isItem" :content="$t('schema.remove_node')" placement="top">
            <i class="el-icon-close" @click="removeNode" style="margin-left: 10px" />
          </el-tooltip>
        </div>
      </el-col>
    </el-row>

    <template v-if="!hidden && pickValue.properties && !isArray(pickValue)">
      <json-schema-editor
        v-for="(item, key, index) in pickValue.properties"
        :value="{ [key]: item }"
        :parent="pickValue"
        :expand-all-params="expandAllParams"
        :key="index"
        :deep="deep + 1"
        :root="false"
        class="children"
        :param-columns="paramColumns"
        :scenario-definition="scenarioDefinition"
        :show-mock-vars="showMockVars"
        :disabled="disabled"
        @editScenarioAdvance="editScenarioAdvance"
        :lang="lang"
        :custom="custom"
        @changeAllItemsType="changeAllItemsType"
        :need-mock="needMock"
        @reloadItems="reloadItems" />
    </template>
    <template v-if="!hidden && isArray(pickValue)">
      <json-schema-editor
        v-for="(item, key, index) in pickValue.items"
        :value="{ [key]: item }"
        :parent="pickValue"
        :expand-all-params="expandAllParams"
        :key="index"
        :deep="deep + 1"
        :root="false"
        class="children"
        :param-columns="paramColumns"
        :scenario-definition="scenarioDefinition"
        :show-mock-vars="showMockVars"
        :disabled="disabled"
        :need-mock="needMock"
        @editScenarioAdvance="editScenarioAdvance"
        :lang="lang"
        :custom="custom"
        @changeAllItemsType="changeAllItemsType"
        @reloadItems="reloadItems" />
    </template>
    <!-- 高级设置-->
    <el-dialog
      append-to-body
      :close-on-click-modal="true"
      :title="$t('schema.adv_setting')"
      :visible.sync="modalVisible"
      :destroy-on-close="true"
      @close="handleClose"
      v-if="modalVisible">
      <p class="tip">{{ $t('schema.base_setting') }}</p>

      <el-form label-position="left" label-width="100px" v-model="advancedValue" class="ms-advanced-search-form">
        <div :span="8" v-for="(item, key) in advancedValue" :key="key">
          <el-form-item :label="$t('schema.' + key)">
            <el-input-number
              v-model="advancedValue[key]"
              v-if="advancedAttr[key].type === 'integer'"
              style="width: 100%"
              :placeholder="key"
              size="small" />
            <el-input-number
              v-model="advancedValue[key]"
              v-else-if="advancedAttr[key].type === 'number'"
              style="width: 100%"
              :placeholder="key"
              size="small" />
            <span v-else-if="advancedAttr[key].type === 'boolean'" style="display: inline-block; width: 100%">
              <el-switch v-model="advancedValue[key]" />
            </span>
            <el-select
              v-else-if="advancedAttr[key].type === 'array'"
              v-model="advancedValue[key]"
              style="width: 100%"
              size="small">
              <el-option value="" :label="$t('schema.nothing')"></el-option>
              <el-option :key="t" :value="t" :label="t" v-for="t in advancedAttr[key].enums" />
            </el-select>
            <el-input
              v-else-if="advancedAttr[key].type === 'textarea'"
              :placeholder="advancedAttr[key].description"
              v-model="advancedValue[key]"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 10 }"
              :rows="2"></el-input>
            <el-input v-model="advancedValue[key]" v-else style="width: 100%" :placeholder="key" size="small" />
          </el-form-item>
        </div>
      </el-form>
      <p class="tip">{{ $t('schema.preview') }}</p>
      <pre style="width: 100%; white-space: pre-wrap">{{ completeNodeValue }}</pre>

      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer @cancel="modalVisible = false" @confirm="handleOk" />
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { isNull } from './util';
import { TYPE, TYPE_NAME, TYPES } from './type/type';
import MsMock from './mock/MockComplete';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import { getUUID } from 'metersphere-frontend/src/utils';
import CustomInput from '../custom-input/index';
import CustomTextarea from '../custom-textarea/index';

export default {
  name: 'JsonSchemaEditor',
  components: { MsMock, MsDialogFooter, CustomInput, CustomTextarea },
  props: {
    value: {
      type: Object,
      required: true,
    },
    expandAllParams: {
      type: Boolean,
      default() {
        return false;
      },
    },
    paramColumns: Array,
    showMockVars: {
      type: Boolean,
      default() {
        return false;
      },
    },
    disabled: {
      //name不可编辑，根节点name不可编辑,数组元素name不可编辑
      type: Boolean,
      default: false,
    },
    disabledType: {
      //禁用类型选择
      type: Boolean,
      default: false,
    },
    isItem: {
      //是否数组元素
      type: Boolean,
      default: false,
    },
    deep: {
      // 节点深度，根节点deep=0
      type: Number,
      default: 0,
    },
    root: {
      //是否root节点
      type: Boolean,
      default: true,
    },
    parent: {
      //父节点
      type: Object,
      default: null,
    },
    custom: {
      //enable custom properties
      type: Boolean,
      default: false,
    },
    lang: {
      // i18n language
      type: String,
      default: 'zh_CN',
    },
    scenarioDefinition: Array,
    needMock: {
      type: Boolean,
      default: true,
    },
  },
  computed: {
    pickValue() {
      return Object.values(this.value)[0];
    },
    pickKey() {
      return Object.keys(this.value)[0];
    },
    isObject() {
      return this.pickValue.type === 'object';
    },
    checked() {
      return this.parent && this.parent.required && this.parent.required.indexOf(this.pickKey) >= 0;
    },
    advanced() {
      return TYPE[this.pickValue.type];
    },
    types() {
      return TYPES(this.pickKey);
    },
    advancedAttr() {
      return TYPE[this.pickValue.type].attr;
    },
    advancedNotEmptyValue() {
      const jsonNode = Object.assign({}, this.advancedValue);
      for (let key in jsonNode) {
        isNull(jsonNode[key]) && delete jsonNode[key];
      }
      return jsonNode;
    },
    completeNodeValue() {
      const t = {};
      for (const item of this.customProps) {
        t[item.key] = item.value;
      }
      return Object.assign({}, this.pickValue, this.advancedNotEmptyValue, t);
    },
  },
  data() {
    return {
      TYPE_NAME,
      hidden: false,
      countAdd: 1,
      modalVisible: false,
      reloadSelfOver: true,
      advancedValue: {},
      addProp: {}, // 自定义属性
      customProps: [],
      customing: false,
    };
  },
  created() {
    if (this.expandAllParams) {
      this.hidden = false;
    } else {
      if (this.pickValue) {
        if (this.pickValue.hidden === undefined) {
          this.hidden = !this.root;
        } else {
          this.hidden = this.root ? false : this.pickValue.hidden;
        }
      } else {
        this.hidden = true;
      }
    }
  },
  watch: {
    expandAllParams() {
      if (this.expandAllParams) {
        this.hidden = false;
      } else {
        if (!this.root) {
          this.hidden = true;
        }
      }
    },
  },
  methods: {
    showColumns(columns) {
      if (!this.paramColumns) {
        return false;
      }
      return this.paramColumns.indexOf(columns) >= 0;
    },
    isArray(data) {
      let isDataArray = data.type === 'array';
      if (isDataArray) {
        //如果item不是array类型，去掉。
        let itemsIsArray = Array.isArray(data.items);
        if (!itemsIsArray) {
          data.items = [];
        }
      }
      return isDataArray;
    },
    onInputName(e) {
      const val = e.target.value;
      const p = {};
      for (let key in this.parent.properties) {
        if (key != this.pickKey) {
          p[key] = this.parent.properties[key];
        } else {
          p[val] = this.parent.properties[key];
          delete this.parent.properties[key];
        }
      }
      this.$set(this.parent, 'properties', p);
    },
    onChangeType() {
      if (this.parent && this.parent.type === 'array') {
        this.$emit('changeAllItemsType', this.pickValue.type);
      } else {
        delete this.pickValue['properties'];
        delete this.pickValue['items'];
        delete this.pickValue['required'];
        delete this.pickValue['mock'];
        if (this.isArray(this.pickValue)) {
          this.$set(this.pickValue, 'items', [{ type: 'string', mock: { mock: '' } }]);
        }
        if (this.pickValue.type === 'null') {
          this.$set(this.pickValue, 'mock', { mock: '' });
          this.reloadItems();
        }
        // 解决数组对象下拉框切换类型，不刷新的问题
        if (this.parent && this.parent.type === 'object') {
          this.reloadSelf();
        }
      }
    },
    changeAllItemsType(changeType) {
      if (this.isArray(this.pickValue) && this.pickValue.items && this.pickValue.items.length > 0) {
        this.pickValue.items.forEach((item) => {
          delete item['properties'];
          delete item['items'];
          delete item['required'];
          delete item['mock'];
          this.$set(item, 'type', changeType);
          if (changeType === 'array') {
            this.$set(item, 'items', [{ type: 'string', mock: { mock: '' } }]);
          }
        });
        this.$nextTick(() => {
          this.reloadSelf();
          this.reloadItems();
        });
      }
    },
    onCheck(e) {
      this._checked(e.target.checked, this.parent);
    },
    onRootCheck(e) {
      const checked = e.target.checked;
      this._deepCheck(checked, this.pickValue);
    },
    _deepCheck(checked, node) {
      if (node.type === 'object' && node.properties) {
        checked ? this.$set(node, 'required', Object.keys(node.properties)) : delete node['required'];
        Object.keys(node.properties).forEach((key) => this._deepCheck(checked, node.properties[key]));
      } else if (node.type === 'array' && node.items.type === 'object') {
        checked ? this.$set(node.items, 'required', Object.keys(node.items.properties)) : delete node.items['required'];
        Object.keys(node.items.properties).forEach((key) => this._deepCheck(checked, node.items.properties[key]));
      }
    },
    _checked(checked, parent) {
      let required = parent.required;
      if (checked) {
        required || this.$set(this.parent, 'required', []);

        required = this.parent.required;
        required.indexOf(this.pickKey) === -1 && required.push(this.pickKey);
      } else {
        const pos = required.indexOf(this.pickKey);
        pos >= 0 && required.splice(pos, 1);
      }
      required.length === 0 && delete parent['required'];
    },
    addChild() {
      const node = this.pickValue;
      if (this.isArray(node)) {
        let childObj = { type: 'string', mock: { mock: '' } };
        if (node.items && node.items.length > 0) {
          childObj.type = node.items[0].type;
          node.items.push(childObj);
        } else {
          this.$set(this.pickValue, 'items', [childObj]);
        }
        this.reloadItems();
      } else {
        const name = this._joinName();
        const type = 'string';
        node.properties || this.$set(node, 'properties', {});
        const props = node.properties;
        this.$set(props, name, { type: type, mock: { mock: '' } });
        this.reloadItems();
      }
    },
    addCustomNode() {
      this.$set(this.addProp, 'key', this._joinName());
      this.$set(this.addProp, 'value', '');
      this.customing = true;
    },
    confirmAddCustomNode() {
      this.customProps.push(this.addProp);
      this.addProp = {};
      this.customing = false;
    },
    removeNode() {
      if (this.parent.type && this.parent.type === 'object') {
        const { properties, required } = this.parent;
        delete properties[this.pickKey];
        if (required) {
          const pos = required.indexOf(this.pickKey);
          pos >= 0 && required.splice(pos, 1);
          required.length === 0 && delete this.parent.required;
        }
      } else if (this.parent.type && this.parent.type === 'array') {
        const { items, required } = this.parent;
        items.splice(this.pickKey, 1);
        if (required) {
          const pos = required.indexOf(this.pickKey);
          pos >= 0 && required.splice(pos, 1);
          required.length === 0 && delete this.parent.required;
        }
      }
      this.parentReloadItems();
    },
    _joinName() {
      return `field_${this.deep}_${this.countAdd++}_${getUUID().substring(0, 5)}`;
    },
    onSetting() {
      this.modalVisible = true;
      this.advancedValue = {};
      this.advancedValue = this.advanced.value;
      for (const k in this.advancedValue) {
        this.advancedValue[k] = this.pickValue[k];
      }
    },
    handleClose() {
      this.modalVisible = false;
    },
    handleOk() {
      this.modalVisible = false;
      for (const key in this.advancedValue) {
        if (isNull(this.advancedValue[key])) {
          delete this.pickValue[key];
        } else {
          this.$set(this.pickValue, key, this.advancedValue[key]);
        }
      }
      for (const item of this.customProps) {
        this.$set(this.pickValue, item.key, item.value);
      }
    },
    parentReloadItems() {
      this.$emit('reloadItems');
    },
    reloadItems() {
      if (!this.hidden) {
        this.hidden = !this.hidden;
        this.$nextTick(() => {
          this.hidden = !this.hidden;
        });
      }
    },

    reloadSelf() {
      this.reloadSelfOver = false;
      this.$nextTick(() => {
        this.reloadSelfOver = true;
      });
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
    formatChange(value) {
      this.pickValue.format = value;
      this.$nextTick(() => {
        this.reloadSelf();
      });
    },
  },
};
</script>
<style scoped>
.json-schema-editor .row {
  display: flex;
  margin: 12px;
}

.json-schema-editor .row .ms-col-name {
  display: flex;
  align-items: center;
}

.json-schema-editor .row .ms-col-name .ms-col-name-c {
  display: flex;
  align-items: center;
}

.json-schema-editor .row .ms-col-name .ms-col-name-required {
  flex: 0 0 30px;
  text-align: center;
}

.json-schema-editor .row .ms-col-type {
  width: 100%;
}

.json-schema-editor .row .ms-col-setting {
  display: inline-block;
}

.json-schema-editor .row .setting-icon {
  color: rgba(0, 0, 0, 0.45);
  border: none;
}

.json-schema-editor .row .plus-icon {
  border: none;
}

.json-schema-editor .row .close-icon {
  color: #888;
  border: none;
}

.json-schema-editor-advanced-modal {
  color: rgba(0, 0, 0, 0.65);
  min-width: 600px;
}

.json-schema-editor-advanced-modal pre {
  font-family: monospace;
  height: 100%;
  overflow-y: auto;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  padding: 12px;
  width: 50%;
}

.json-schema-editor-advanced-modal h3 {
  display: block;
  border-left: 3px solid #1890ff;
  padding: 0 8px;
}

.json-schema-editor-advanced-modal .ms-advanced-search-form {
  display: flex;
}

.json-schema-editor-advanced-modal .ms-advanced-search-form .ms-form-item .ms-form-item-control-wrapper {
  flex: 1;
}

.col-item-setting {
  padding-top: 8px;
  cursor: pointer;
}

.ms-transform {
  transform: rotate(-180deg);
  transition: 0ms;
}
</style>
