<template>
  <div class="json-schema-editor">
    <el-row class="row" :gutter="20">
      <el-col :span="8" class="ms-col-name">
        <div :style="{marginLeft:`${10*deep}px`}" class="ms-col-name-c"/>
        <span v-if="pickValue.type === 'object' || pickValue.type === 'array'" :class="hidden ? 'el-icon-caret-left ms-transform':
            'el-icon-caret-bottom'" @click="hidden = !hidden"/>
        <span v-else style="width:10px;display:inline-block"></span>
        <input class="el-input el-input__inner" style="height: 32px" :disabled="disabled || root" :value="pickKey"
               @blur="onInputName" size="small"/>
      </el-col>
      <el-col :span="4">
        <el-select v-model="pickValue.type" :disabled="disabled || disabledType" class="ms-col-type"
                   @change="onChangeType" size="small">
          <el-option :key="t" :value="t" :label="t" v-for="t in types"/>
        </el-select>
      </el-col>
      <el-col :span="5">
        <ms-mock
          :disabled="disabled || pickValue.type === 'object' || pickKey === 'root' || pickValue.type === 'array' || pickValue.type === 'null'"
          :schema="pickValue"
          :scenario-definition="scenarioDefinition"
          :show-mock-vars="showMockVars"
          @editScenarioAdvance="editScenarioAdvance"/>
      </el-col>
      <el-col :span="5">
        <el-input :disabled="disabled" v-model="pickValue.description" class="ms-col-title"
                  :placeholder="$t('schema.description')" size="small"/>
      </el-col>
      <el-col :span="2">
        <div v-if="hasAdvancedSetting">
          <el-link @click="changeCollapseStatus">{{ getCollapseOption() }}</el-link>
        </div>
      </el-col>
    </el-row>
    <div>
      <el-collapse-transition>
        <div v-show="collapseStatus && hasAdvancedSetting" :style="{marginLeft:`${10*deep+10}px`}">
          <json-advanced-setting :json-data=" pickValue"/>
        </div>
      </el-collapse-transition>
    </div>

    <template v-if="!hidden && pickValue.properties && !isArray && reloadItemOver">
      <json-schema-panel v-for="(item,key,index) in pickValue.properties" :value="{[key]:item}"
                         :parent="pickValue" :key="index" :deep="deep + 1" :root="false" class="children"
                         :scenario-definition="scenarioDefinition"
                         :show-mock-vars="showMockVars"
                         :disabled="disabled"
                         :expand-all-params="expandAllParams"
                         @editScenarioAdvance="editScenarioAdvance"
                         :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType"/>
    </template>
    <template v-if="!hidden && isArray && reloadItemOver">
      <json-schema-panel v-for="(item,key,index) in pickValue.items" :value="{[key]:item}" :parent="pickValue"
                         :key="index"
                         :deep="deep+1" :root="false" class="children"
                         :scenario-definition="scenarioDefinition"
                         :show-mock-vars="showMockVars"
                         :expand-all-params="expandAllParams"
                         @editScenarioAdvance="editScenarioAdvance"
                         :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType"/>
    </template>
  </div>
</template>
<script>
import {getUUID} from "@/common/js/utils";
import {TYPE, TYPE_NAME, TYPES} from "@/business/components/common/json-schema/schema/editor/type/type";
import MsMock from "@/business/components/common/json-schema/schema/editor/mock/MockComplete";
import JsonAdvancedSetting
  from "@/business/components/api/definition/components/document/components/plugin/JsonAdvancedSetting";

export default {
  name: 'JsonSchemaPanel',
  components: {MsMock, JsonAdvancedSetting},
  props: {
    value: {
      type: Object,
      required: true
    },
    showMockVars: {
      type: Boolean,
      default() {
        return false;
      }
    },
    disabled: { //name不可编辑，根节点name不可编辑,数组元素name不可编辑
      type: Boolean,
      default: false
    },
    disabledType: { //禁用类型选择
      type: Boolean,
      default: false
    },
    isItem: { //是否数组元素
      type: Boolean,
      default: false
    },
    deep: { // 节点深度，根节点deep=0
      type: Number,
      default: 0
    },
    root: { //是否root节点
      type: Boolean,
      default: true
    },
    parent: { //父节点
      type: Object,
      default: null
    },
    custom: { //enable custom properties
      type: Boolean,
      default: false
    },
    lang: { // i18n language
      type: String,
      default: 'zh_CN'
    },
    expandAllParams: {
      type: Boolean,
      default() {
        return false;
      }
    },
    scenarioDefinition: Array,
  },
  computed: {
    pickValue() {
      return Object.values(this.value)[0]
    },
    pickKey() {
      return Object.keys(this.value)[0]
    },
    isObject() {
      return this.pickValue.type === 'object'
    },
    isArray() {
      return this.pickValue.type === 'array'
    },
    checked() {
      return this.parent && this.parent.required && this.parent.required.indexOf(this.pickKey) >= 0
    },
    advanced() {
      return TYPE[this.pickValue.type]
    },
    types() {
      return TYPES(this.pickKey);
    },
    hasAdvancedSetting() {
      if (this.isNotEmptyValue(this.pickValue["default"]) || this.isNotEmptyValue(this.pickValue["minLength"]) || this.isNotEmptyValue(this.pickValue["maxLength"])
        || this.isNotEmptyValue(this.pickValue["format"]) || this.isNotEmptyValue(this.pickValue["description"]) || this.isNotEmptyValue(this.pickValue["pattern"])
        || this.isNotEmptyValue(this.pickValue["enum"])) {
        return true;
      } else {
        return false;
      }
    },
  },
  data() {
    return {
      TYPE_NAME,
      collapseStatus: false,
      hidden: false,
      countAdd: 1,
      reloadItemOver: true,
      advancedValue: {},
      addProp: {},// 自定义属性
      customing: false
    }
  },
  created() {
    if (this.pickValue) {
      if (this.pickValue.hidden === undefined) {
        this.hidden = this.root ? false : true;
      } else {
        this.hidden = this.root ? false : this.pickValue.hidden;
      }
    } else {
      this.hidden = true;
    }
  },
  watch: {
    expandAllParams() {
      this.collapseStatus = this.expandAllParams;
    }
  },
  methods: {
    isNotEmptyValue(value) {
      return value && value !== '';
    },
    changeCollapseStatus() {
      this.collapseStatus = !this.collapseStatus;
    },
    getCollapseOption() {
      if (this.collapseStatus) {
        return this.$t('api_test.definition.document.close');
      } else {
        return this.$t('api_test.definition.document.open');
      }
    },
    onInputName(e) {
      const val = e.target.value
      const p = {};
      for (let key in this.parent.properties) {
        if (key != this.pickKey) {
          p[key] = this.parent.properties[key]
        } else {
          p[val] = this.parent.properties[key]
          delete this.parent.properties[key]
        }
      }
      this.$set(this.parent, 'properties', p)
    },
    onChangeType() {
      if (this.parent && this.parent.type === 'array') {
        this.$emit('changeAllItemsType', this.pickValue.type);
      } else {
        this.$delete(this.pickValue, 'properties')
        this.$delete(this.pickValue, 'items')
        this.$delete(this.pickValue, 'required')
        this.$delete(this.pickValue, 'mock')
        if (this.isArray) {
          this.$set(this.pickValue, 'items', [{type: 'string', mock: {mock: ""}}]);
        }
      }
    },
    changeAllItemsType(changeType) {
      if (this.isArray && this.pickValue.items && this.pickValue.items.length > 0) {
        this.pickValue.items.forEach(item => {
          item.type = changeType;
          this.$delete(item, 'properties')
          this.$delete(item, 'items')
          this.$delete(item, 'required')
          this.$delete(item, 'mock')
          if (changeType === 'array') {
            this.$set(item, 'items', [{type: 'string', mock: {mock: ""}}]);
          }
        });
      }
    },
    onRootCheck(e) {
      const checked = e.target.checked
      this._deepCheck(checked, this.pickValue)
    },
    _deepCheck(checked, node) {
      if (node.type === 'object' && node.properties) {
        checked ? this.$set(node, 'required', Object.keys(node.properties)) : this.$delete(node, 'required')
        Object.keys(node.properties).forEach(key => this._deepCheck(checked, node.properties[key]))
      } else if (node.type === 'array' && node.items.type === 'object') {
        checked ? this.$set(node.items, 'required', Object.keys(node.items.properties)) : this.$delete(node.items, 'required')
        Object.keys(node.items.properties).forEach(key => this._deepCheck(checked, node.items.properties[key]))
      }
    },
    confirmAddCustomNode() {
      this.customProps.push(this.addProp)
      this.addProp = {}
      this.customing = false
    },
    _joinName() {
      return `feild_${this.deep}_${this.countAdd++}_${getUUID().substring(0, 5)}`
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
  }
}
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
