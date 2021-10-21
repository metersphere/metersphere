<template>
  <div class="json-schema-editor">
    <el-row id="rowId" class="row" :gutter="20">
      <div class="box" v-if="pickOpt"/>
      <el-col :span="8" class="ms-col-name">
        <div :style="{marginLeft:`${10*deep}px`}" class="ms-col-name-c"/>
        <span v-if="pickValue.type==='object'" :class="hidden? 'el-icon-caret-left ms-transform':
            'el-icon-caret-bottom'" @click="hidden = !hidden"/>
        <span v-else style="width:10px;display:inline-block"/>
        <input class="el-input el-input__inner ms-input-css" :style="{'background':getBg()}" :disabled="disabled" :value="pickKey" @blur="onInputName" size="small"/>

        <el-tooltip v-if="root" :content="$t('schema.checked_all')" placement="top">
          <input type="checkbox" :disabled="disabled" class="ms-col-name-required" :style="{'background':getBg()}" @change="onRootCheck"/>
        </el-tooltip>
        <el-tooltip v-else :content="$t('schema.required')" placement="top">
          <input type="checkbox" :disabled="disabled" :checked="checked" :style="{'background-color':getBg()}" class="ms-col-name-required" @change="onCheck"/>
        </el-tooltip>
      </el-col>
      <el-col :span="4">
        <input v-model="pickValue.type" :disabled="disabled" class="el-input el-input__inner ms-input-css" size="small" :style="{'background-color':getBg()}"/>
      </el-col>
      <el-col :span="6">
        <input v-if="pickValue && pickValue.mock" v-model="pickValue.mock.mock" :disabled="disabled" class="el-input el-input__inner ms-input-css" size="small" :style="{'background-color':getBg()}"/>
        <input v-else v-model="defaultValue" :disabled="disabled" class="el-input el-input__inner ms-input-css" size="small" :style="{'background-color':getBg()}"/>
      </el-col>
      <el-col :span="6">
        <input v-model="pickValue.description" :disabled="disabled" class="el-input el-input__inner ms-input-css" size="small" :style="{'background-color':getBg()}"/>
      </el-col>
    </el-row>

    <template v-if="!hidden && pickValue.properties && !isArray && reloadItemOver">
      <compared-editor v-for="(item,key,index) in pickValue.properties"
                       :value="{[key]:item}" :parent="pickValue" :key="index" :deep="deep+1" :root="false" class="children" :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType" @reloadItems="reloadItems"/>
    </template>
    <template v-if="isArray && reloadItemOver">
      <compared-editor v-for="(item,key,index) in pickValue.items" :value="{[key]:item}" :parent="pickValue" :key="index" :deep="deep+1" :root="false" class="children" :lang="lang" :custom="custom" @changeAllItemsType="changeAllItemsType"/>
    </template>

  </div>
</template>
<script>
import {isNull} from './util'
import {TYPE_NAME, TYPE} from './type/type'
import {getUUID} from "@/common/js/utils";

export default {
  name: 'ComparedEditor',
  components: {},
  props: {
    value: {
      type: Object,
      required: true
    },
    disabled: { //name不可编辑，根节点name不可编辑,数组元素name不可编辑
      type: Boolean,
      default: true
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
    }
  },
  computed: {
    pickValue() {
      return Object.values(this.value)[0]
    },
    pickOpt() {
      let value = Object.keys(this.value)[0];
      if (value && value.indexOf("--") !== -1) {
        return true;
      }
      return false;
    },
    pickKey() {
      let value = Object.keys(this.value)[0];
      if (value && value.indexOf("--") !== -1) {
        return value.substr(2);
      } else if (value && value.indexOf("++") !== -1) {
        return value.substr(2);
      }
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
      customing: false,
      defaultValue: "",
    }
  },
  methods: {
    getBg() {
      let value = Object.keys(this.value)[0];
      if (value && value.indexOf("--") !== -1) {
        return "#F3E6E7";
      } else if (value && value.indexOf("++") !== -1) {
        return "#E2ECDC";
      } else {
        return "";
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
    }
  }
}
</script>
<style scoped>

.row-add {
  background: #E2ECDC;
}

.row-del {
  text-decoration: none;
  text-decoration-color: red;
  background: #F3E6E7;
}

.row-update {
  background: #E2ECDC;
}

.json-schema-editor .row {
  display: flex;
  margin: 2px;
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

.ms-input-css {
  height: 32px;
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

.box {
  position: absolute;
  width: 100%;
  height: 1px;
  top: 16px;
  z-index: 999;
  border-color: red;
  background: red;
}

</style>
