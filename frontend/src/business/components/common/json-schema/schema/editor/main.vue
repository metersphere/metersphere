<template>
  <div class="json-schema-editor">
    <el-row class="row" :gutter="20">
      <el-col :span="8" class="ms-col-name">
        <div :style="{marginLeft:`${10*deep}px`}" class="ms-col-name-c"/>
        <span v-if="pickValue.type==='object'" :class="hidden? 'el-icon-caret-left ms-transform':
            'el-icon-caret-bottom'" @click="hidden = !hidden"/>
        <span v-else style="width:10px;display:inline-block"></span>
        <input class="el-input el-input__inner" style="height: 32px" :disabled="disabled || root" :value="pickKey" @blur="onInputName" size="small"/>

        <el-tooltip v-if="root" :content="$t('schema.checked_all')" placement="top">
          <input type="checkbox" :disabled="disabled || (!isObject && !isArray)" class="ms-col-name-required" @change="onRootCheck"/>
        </el-tooltip>
        <el-tooltip v-else :content="$t('schema.required')" placement="top">
          <input type="checkbox" :disabled="disabled || isItem" :checked="checked" class="ms-col-name-required" @change="onCheck"/>
        </el-tooltip>
      </el-col>
      <el-col :span="4">
        <el-select v-model="pickValue.type" :disabled="disabled||disabledType" class="ms-col-type" @change="onChangeType" size="small">
          <el-option :key="t" :value="t" :label="t" v-for="t in types"/>
        </el-select>
      </el-col>
      <el-col :span="4">
        <ms-mock :disabled="disabled || pickValue.type==='object' || pickKey ==='root' || pickValue.type==='array' || pickValue.type==='null'"
                 :schema="pickValue"
                 :scenario-definition="scenarioDefinition"
                 :show-mock-vars="showMockVars"
                 @editScenarioAdvance="editScenarioAdvance"/>
      </el-col>
      <el-col :span="4">
        <el-input :disabled="disabled" v-model="pickValue.description" class="ms-col-title" :placeholder="$t('schema.description')" size="small"/>
      </el-col>
      <el-col :span="4" class="col-item-setting" v-if="!disabled">
        <el-tooltip class="item" effect="dark" :content="$t('schema.adv_setting')" placement="top">
          <i class="el-icon-setting" @click="onSetting"/>
        </el-tooltip>
        <el-tooltip v-if="isObject || isArray" :content="$t('schema.add_child_node')" placement="top">
          <i class="el-icon-plus" @click="addChild" style="margin-left: 10px"/>
        </el-tooltip>
        <el-tooltip v-if="!root && !isItem" :content="$t('schema.remove_node')" placement="top">
          <i class="el-icon-close" @click="removeNode" style="margin-left: 10px"/>
        </el-tooltip>
      </el-col>
    </el-row>

    <template v-if="!hidden&&pickValue.properties && !isArray && reloadItemOver">
      <json-schema-editor v-for="(item,key,index) in pickValue.properties" :value="{[key]:item}"
                          :parent="pickValue" :key="index" :deep="deep+1" :root="false" class="children"
                          :scenario-definition="scenarioDefinition"
                          :show-mock-vars="showMockVars"
                          :disabled="disabled"
                          @editScenarioAdvance="editScenarioAdvance"
                          :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType" @reloadItems="reloadItems"/>
    </template>
    <template v-if="isArray && reloadItemOver">
      <json-schema-editor v-for="(item,key,index) in pickValue.items" :value="{[key]:item}" :parent="pickValue" :key="index"
                          :deep="deep+1" :root="false" class="children"
                          :scenario-definition="scenarioDefinition"
                          :show-mock-vars="showMockVars"
                          @editScenarioAdvance="editScenarioAdvance"
                          :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType"/>
    </template>
    <!-- 高级设置-->
    <el-dialog append-to-body :close-on-click-modal="true" :title="$t('schema.adv_setting')" :visible.sync="modalVisible" :destroy-on-close="true"
               @close="handleClose">
      <p class="tip">{{ $t("schema.base_setting") }} </p>

      <el-form label-position="left" label-width="100px" v-model="advancedValue" class="ms-advanced-search-form">
        <div :span="8" v-for="(item,key) in advancedValue" :key="key">
          <el-form-item :label="$t('schema.'+key)">
            <el-input-number v-model="advancedValue[key]" v-if="advancedAttr[key].type === 'integer'" style="width:100%" :placeholder="key" size="small"/>
            <el-input-number v-model="advancedValue[key]" v-else-if="advancedAttr[key].type === 'number'" style="width:100%" :placeholder="key" size="small"/>
            <span v-else-if="advancedAttr[key].type === 'boolean'" style="display:inline-block;width:100%">
                <el-switch v-model="advancedValue[key]"/>
                </span>
            <el-select v-else-if="advancedAttr[key].type === 'array'" v-model="advancedValue[key]" style="width:100%" size="small">
              <el-option value="" :label="$t('schema.nothing')"></el-option>
              <el-option :key="t" :value="t" :label="t" v-for="t in advancedAttr[key].enums"/>
            </el-select>
            <el-input v-else-if="advancedAttr[key].type === 'textarea'" :placeholder="advancedAttr[key].description" v-model="advancedValue[key]"
                      type="textarea"
                      :autosize="{ minRows: 2, maxRows: 10}"
                      :rows="2"></el-input>
            <el-input v-model="advancedValue[key]" v-else style="width:100%;" :placeholder="key" size="small"/>

          </el-form-item>
        </div>
      </el-form>
      <p class="tip">{{ $t('schema.preview') }} </p>
      <pre style="width:100%">{{ completeNodeValue }}</pre>

      <span slot="footer" class="dialog-footer">
       <ms-dialog-footer
         @cancel="modalVisible = false"
         @confirm="handleOk"/>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {isNull} from './util'
import {TYPE_NAME, TYPE, TYPES} from './type/type'
import MsMock from './mock/MockComplete'
import MsDialogFooter from '../../../components/MsDialogFooter'
import {getUUID} from "@/common/js/utils";

export default {
  name: 'JsonSchemaEditor',
  components: {MsMock, MsDialogFooter},
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
    types(){
      return TYPES(this.pickKey);
    },
    advancedAttr() {
      return TYPE[this.pickValue.type].attr
    },
    advancedNotEmptyValue() {
      const jsonNode = Object.assign({}, this.advancedValue);
      for (let key in jsonNode) {
        isNull(jsonNode[key]) && delete jsonNode[key]
      }
      return jsonNode
    },
    completeNodeValue() {
      const t = {}
      for (const item of this.customProps) {
        t[item.key] = item.value
      }
      return Object.assign({}, this.pickValue, this.advancedNotEmptyValue, t)
    }
  },
  data() {
    return {
      TYPE_NAME,
      hidden: false,
      countAdd: 1,
      modalVisible: false,
      reloadItemOver: true,
      advancedValue: {},
      addProp: {},// 自定义属性
      customProps: [],
      customing: false
    }
  },
  methods: {
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
    onCheck(e) {
      this._checked(e.target.checked, this.parent)
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
    _checked(checked, parent) {
      let required = parent.required
      if (checked) {
        required || this.$set(this.parent, 'required', [])

        required = this.parent.required
        required.indexOf(this.pickKey) === -1 && required.push(this.pickKey)
      } else {
        const pos = required.indexOf(this.pickKey)
        pos >= 0 && required.splice(pos, 1)
      }
      required.length === 0 && this.$delete(parent, 'required')
    },
    addChild() {
      const node = this.pickValue;
      if (this.isArray) {
        let childObj = {type: 'string', mock: {mock: ""}}
        if (node.items && node.items.length > 0) {
          childObj.type = node.items[0].type;
          node.items.push(childObj);
        } else {
          this.$set(this.pickValue, 'items', [childObj]);
        }

      } else {
        const name = this._joinName()
        const type = 'string'
        node.properties || this.$set(node, 'properties', {})
        const props = node.properties
        this.$set(props, name, {type: type, mock: {mock: ""}})
      }
    },
    addCustomNode() {
      this.$set(this.addProp, 'key', this._joinName())
      this.$set(this.addProp, 'value', '')
      this.customing = true
    },
    confirmAddCustomNode() {
      this.customProps.push(this.addProp)
      this.addProp = {}
      this.customing = false
    },
    removeNode() {
      if (this.parent.type && this.parent.type === 'object') {
        const {properties, required} = this.parent
        this.$delete(properties, this.pickKey)
        if (required) {
          const pos = required.indexOf(this.pickKey)
          pos >= 0 && required.splice(pos, 1)
          required.length === 0 && this.$delete(this.parent, 'required')
        }
      } else if (this.parent.type && this.parent.type === 'array') {
        const {items, required} = this.parent
        this.$delete(items, this.pickKey)
        if (required) {
          const pos = required.indexOf(this.pickKey)
          pos >= 0 && required.splice(pos, 1)
          required.length === 0 && this.$delete(this.parent, 'required')
        }
      }
      this.parentReloadItems();
    },
    _joinName() {
      return `feild_${this.deep}_${this.countAdd++}_${getUUID().substring(0, 5)}`
    },
    onSetting() {
      this.modalVisible = true;
      this.advancedValue = {};
      this.advancedValue = this.advanced.value
      for (const k in this.advancedValue) {
        this.advancedValue[k] = this.pickValue[k]
      }
    },
    handleClose() {
      this.modalVisible = false;
    },
    handleOk() {
      this.modalVisible = false
      for (const key in this.advancedValue) {
        if (isNull(this.advancedValue[key])) {
          this.$delete(this.pickValue, key)
        } else {
          this.$set(this.pickValue, key, this.advancedValue[key])
        }
      }
      for (const item of this.customProps) {
        this.$set(this.pickValue, item.key, item.value)
      }
    },
    parentReloadItems() {
      this.$emit("reloadItems");
    },
    reloadItems() {
      this.reloadItemOver = false;
      this.$nextTick(() => {
        this.reloadItemOver = true;
      })
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
