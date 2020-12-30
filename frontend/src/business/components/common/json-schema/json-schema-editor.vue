<template>
  <div style="min-height: 400px;">
    <el-button
      v-if="showRaw"
      type="primary"
      size="mini"
      style="margin-bottom: 10px"
      @click="handleReqBodyRaw"
      >RAW查看</el-button
    >
    <div class="json-schema-vue-editor">
      <el-row type="flex" align="middle">
        <el-col :span="8" class="col-item name-item col-item-name">
          <el-row type="flex" justify="space-around" align="middle">
            <el-col :span="2" class="down-style-col">
              <span
                v-if="schemaData.type === 'object'"
                class="down-style"
                @click="handleClickIcon"
              >
                <i v-if="show" class="el-icon-caret-bottom icon-object"></i>
                <i v-if="!show" class="el-icon-caret-right icon-object"></i>
              </span>
            </el-col>
            <el-col :span="20">
              <el-input disabled value="root" size="small" />
            </el-col>
            <el-col :span="2" style="text-align: center">
              <el-tooltip placement="top" content="全选">
                <el-checkbox
                  :checked="checked"
                  :disabled="disabled"
                  @change="changeCheckBox"
                />
              </el-tooltip>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="3" class="col-item col-item-type">
          <el-select
            :value="schemaData.type"
            :disabled="schemaData.disabled && !schemaData.canChangeType"
            class="type-select-style"
            size="small"
            @change="handleChangeType2($event)"
          >
            <el-option
              v-for="item in schemaTypes"
              :key="item"
              :value="item"
              :label="item"
            ></el-option>
          </el-select>
        </el-col>
        <el-col v-if="isMock" :span="3" class="col-item col-item-mock">
          <MockSelect
            :schema="schemaData"
            @showEdit="handleShowEdit"
            @change="handleChangeMock"
          />
        </el-col>
        <el-col
          v-if="showTitle"
          :span="isMock ? 4 : 5"
          class="col-item col-item-mock"
        >
          <el-input
            v-model="schemaData.title"
            placeholder="标题"
            :disabled="schemaData.disabled"
            size="small"
          >
            <i
              slot="append"
              class="el-icon-edit"
              @click="
                handleSchemaUpdateEvent({
                  eventType: 'show-edit',
                  field: 'title',
                  prefix: ['properties'],
                  isRoot: true,
                })
              "
            ></i>
          </el-input>
        </el-col>
        <el-col
          v-if="!showTitle && showDefaultValue"
          :span="isMock ? 4 : 5"
          class="col-item col-item-mock"
        >
          <el-input
            v-model="schemaData.default"
            placeholder="默认值"
            size="small"
            :disabled="
              schemaData.type === 'object' ||
              schemaData.type === 'array' ||
              schemaData.disabled
            "
          >
            <i
              slot="append"
              class="el-icon-edit"
              @click="
                handleSchemaUpdateEvent({
                  eventType: 'show-edit',
                  field: 'default',
                  prefix: ['properties'],
                  isRoot: true,
                })
              "
            ></i>
          </el-input>
        </el-col>

        <el-col :span="isMock ? 4 : 5" class="col-item col-item-desc">
          <el-input
            v-model="schemaData.description"
            placeholder="备注"
            size="small"
            :disabled="schemaData.disabled"
          >
            <i
              slot="append"
              class="el-icon-edit"
              @click="
                handleSchemaUpdateEvent({
                  eventType: 'show-edit',
                  field: 'description',
                  prefix: ['properties'],
                  isRoot: true,
                })
              "
            ></i>
          </el-input>
        </el-col>
        <el-col :span="2" class="col-item col-item-setting">
          <span
            class="adv-set"
            @click="
              handleSchemaUpdateEvent({
                eventType: 'setting',
                schemaType: schemaData.type,
                prefix: ['properties'],
                isRoot: true,
              })
            "
          >
            <el-tooltip placement="top" content="高级设置">
              <i class="el-icon-setting"></i>
            </el-tooltip>
          </span>

          <span
            v-if="schemaData.type === 'object'"
            @click="
              handleSchemaUpdateEvent({
                eventType: 'add-field',
                isChild: false,
                prefix: ['properties'],
              })
            "
          >
            <el-tooltip placement="top" content="添加子节点">
              <i class="el-icon-plus plus"></i>
            </el-tooltip>
          </span>
        </el-col>
      </el-row>
      <schema-json
        v-if="show"
        :data="schemaData"
        :is-mock="isMock"
        :show-title="showTitle"
        :show-default-value="showDefaultValue"
        :editor-id="editorId"
      />
      <!-- RAW弹窗 -->
      <RawDialog
        v-if="showRaw"
        :visible.sync="rawDialogVisible"
        :schema="schemaData"
      />
      <!-- 高级设置弹窗 -->
      <BasicDialog
        :visible.sync="basicDialogVisible"
        :init-data="basicModalData"
      />
      <StringDialog
        :visible.sync="settingDialogVisible.string"
        :init-data="settingModalData"
      />
      <NumberDialog
        :visible.sync="settingDialogVisible.number"
        :init-data="settingModalData"
      />
      <NumberDialog
        :visible.sync="settingDialogVisible.integer"
        :init-data="settingModalData"
      />
      <ArrayDialog
        :visible.sync="settingDialogVisible.array"
        :init-data="settingModalData"
      />
      <BooleanDialog
        :visible.sync="settingDialogVisible.boolean"
        :init-data="settingModalData"
      />
      <ObjectDialog
        :visible.sync="settingDialogVisible.object"
        :init-data="settingModalData"
      />
    </div>
  </div>
</template>
<script>
import set from 'lodash/set'
import get from 'lodash/get'
import unset from 'lodash/unset'
import cloneDeep from 'lodash/cloneDeep'
import SchemaJson from './schema'
import MockSelect from './mock'
import './jsonschema.scss'
import {
  BasicDialog,
  StringDialog,
  NumberDialog,
  ArrayDialog,
  BooleanDialog,
  ObjectDialog,
  RawDialog,
} from './dialog'
import {
  SCHEMA_TYPE,
  log,
  JSONPATH_JOIN_CHAR,
  defaultSchema,
  uuid,
  defaultInitSchemaData,
  handleSchemaRequired,
  cloneObject,
  deleteData,
} from './utils'
export default {
  name: 'JsonSchemaEditor',
  components: {
    MockSelect,
    SchemaJson,
    BasicDialog,
    StringDialog,
    NumberDialog,
    ArrayDialog,
    BooleanDialog,
    ObjectDialog,
    RawDialog,
  },
  props: {
    schema: { type: Object, default: () => {} },
    isMock: { type: Boolean, default: false },
    showTitle: { type: Boolean, default: false },
    showDefaultValue: { type: Boolean, default: false },
    showRaw: { type: Boolean, default: false },
  },
  data() {
    const visibleObj = {}
    SCHEMA_TYPE.map((type) => {
      visibleObj[type] = false
    })
    const initSchema = this.schema || defaultInitSchemaData
    return {
      editorId: uuid(),
      checked: false,
      disabled: false,
      show: true,
      schemaTypes: SCHEMA_TYPE,
      schemaData: initSchema,
      rawDialogVisible: false,
      basicDialogVisible: false,
      basicModalData: { title: '', value: '' },
      settingDialogVisible: visibleObj,
      settingModalData: {},
    }
  },
  watch: {
    schemaData: {
      handler(newVal) {
        log(this, 'watch', newVal)
      },
      deep: true,
    },
  },
  mounted() {
    log(this, this.schemaData)
    this.$jsEditorEvent.on(
      `schema-update-${this.editorId}`,
      this.handleSchemaUpdateEvent
    )
  },
  beforeDestroy() {
    this.$jsEditorEvent.off(
      `schema-update-${this.editorId}`,
      this.handleSchemaUpdateEvent
    )
  },
  methods: {
    handleSchemaUpdateEvent(options) {
      const { eventType, ...opts } = options
      switch (eventType) {
        case 'add-field':
          this.addFieldAction(opts)
          break
        case 'delete-field':
          this.deleteFieldAction(opts)
          break
        case 'update-field-name':
          this.updateFieldNameAction(opts)
          break
        case 'schema-type':
          this.handleChangeType(opts)
          break
        case 'show-edit':
          this.handleShowEdit(opts)
          break
        case 'save-showedit':
          this.handleSaveShowEdit(opts)
          break
        case 'setting':
          this.handleSettingAction(opts)
          break
        case 'save-setting':
          this.handleSaveSetting(opts)
          break
        case 'toggle-required':
          this.enableRequireAction(opts)
          break
        default:
          break
      }
    },
    handleClickIcon() {
      this.show = !this.show
    },
    changeCheckBox(e) {
      this.requireAllAction({ required: e, value: this.schemaData })
    },
    requireAllAction(opts) {
      const { value, required } = opts
      const cloneSchema = cloneObject(value)
      handleSchemaRequired(cloneSchema, required)
      this.forceUpdate(cloneSchema)
      this.handleEmitChange(cloneSchema)
    },
    enableRequireAction(opts) {
      const { prefix, name, required } = opts
      const prefixCopy = cloneDeep(prefix)
      prefixCopy.pop()
      const parentKeys = [...prefixCopy]
      const parentPrefix = parentKeys.join(JSONPATH_JOIN_CHAR)
      const cloneSchema = cloneDeep(this.schemaData)
      let parentData = null
      if (!parentPrefix) {
        // 一级属性
        parentData = cloneSchema
      } else {
        parentData = get(cloneSchema, parentPrefix)
      }
      const requiredData = [].concat(parentData.required || [])
      const index = requiredData.indexOf(name)
      // 取消必填
      if (!required && index >= 0) {
        requiredData.splice(index, 1)
        parentKeys.push('required')
        if (requiredData.length === 0) {
          deleteData(cloneSchema, parentKeys)
        } else {
          set(cloneSchema, parentKeys, requiredData)
        }
      } else if (required && index === -1) {
        // 必填
        requiredData.push(name)
        parentKeys.push('required')
        set(cloneSchema, parentKeys, requiredData)
      }
      this.forceUpdate(cloneSchema)
      this.handleEmitChange(cloneSchema)
    },
    /**
     * 处理新增字段
     * @param isChild 新增子节点
     * @param action 字段和路径
     */
    addFieldAction(opts) {
      log(this, opts)
      const { isChild, name, prefix } = opts
      let parentPrefix = ''
      let requirePrefix = []
      if (isChild) {
        const tempArr = [].concat(prefix, name)
        parentPrefix = tempArr.concat('properties').join(JSONPATH_JOIN_CHAR)
        requirePrefix = [...tempArr]
      } else {
        parentPrefix = prefix.join(JSONPATH_JOIN_CHAR)
        const tempPrefix = [].concat(prefix)
        tempPrefix.pop()
        requirePrefix = tempPrefix
      }
      log('addFieldAction>>>', parentPrefix, '\n\t')
      let newPropertiesData = {}
      const ranName = 'field_' + uuid()
      const propertiesData = get(this.schemaData, parentPrefix)
      newPropertiesData = Object.assign({}, propertiesData)
      newPropertiesData[ranName] = defaultSchema.string
      const cloneSchema = cloneDeep(this.schemaData)
      set(cloneSchema, parentPrefix, newPropertiesData)

      // add required
      let pRequiredData = null
      if (!requirePrefix.length) {
        // 一级属性
        pRequiredData = cloneSchema
      } else {
        pRequiredData = get(cloneSchema, requirePrefix)
      }
      const requiredData = [].concat(pRequiredData.required || [])
      requiredData.push(ranName)
      requirePrefix.push('required')
      set(cloneSchema, requirePrefix, requiredData)
      // update schema
      this.schemaData = cloneSchema
      this.forceUpdate(cloneSchema)
      this.handleEmitChange(cloneSchema)
    },

    // 删除字段
    deleteFieldAction(opts) {
      const { name, prefix } = opts
      const curFieldPath = [].concat(prefix, name).join(JSONPATH_JOIN_CHAR)
      // console.log(curFieldPath)
      const cloneSchema = cloneDeep(this.schemaData)
      unset(cloneSchema, curFieldPath)
      this.schemaData = cloneSchema
      this.forceUpdate()
      this.handleEmitChange(cloneSchema)
    },

    // 更新字段名称
    updateFieldNameAction(opts) {
      log(this, opts)
      const { value, name, prefix } = opts
      let requirePrefix = []
      const prefixCopy = cloneDeep(prefix)
      prefixCopy.pop()
      requirePrefix = prefixCopy // 上级 required路径
      const parentPrefix = prefix.join(JSONPATH_JOIN_CHAR)
      const curFieldPath = prefix.concat(name).join(JSONPATH_JOIN_CHAR)
      const cloneSchema = cloneDeep(this.schemaData)
      const propertiesData = get(cloneSchema, curFieldPath) // 原来的值
      unset(cloneSchema, curFieldPath) // 移除

      set(cloneSchema, `${parentPrefix}.${value}`, propertiesData) // 添加

      // update required name
      let pRequiredData = null
      if (!requirePrefix.length) {
        // 一级属性
        pRequiredData = cloneSchema
      } else {
        pRequiredData = get(cloneSchema, requirePrefix)
      }
      let requiredData = [].concat(pRequiredData.required || [])
      requiredData = requiredData.map((item) => {
        if (item === name) return value
        return item
      })
      requirePrefix.push('required')
      set(cloneSchema, requirePrefix, requiredData)

      this.schemaData = cloneSchema
      this.forceUpdate()
      this.handleEmitChange(cloneSchema)
    },
    // root
    handleChangeType2(value) {
      this.schemaData.type = value
      const parentDataItem = this.schemaData.description
        ? { description: this.schemaData.description }
        : {}
      const newParentDataItem = defaultSchema[value]
      const newParentData = Object.assign({}, newParentDataItem, parentDataItem)
      this.schemaData = newParentData
      this.handleEmitChange(this.schemaData)
    },
    // schema 类型变化
    handleChangeType(opts) {
      log(this, opts, 2)
      const { value, name, prefix } = opts
      const parentPrefix = [].concat(prefix, name)
      const cloneSchema = cloneDeep(this.schemaData)
      const parentData = get(cloneSchema, parentPrefix)
      const newParentDataItem = defaultSchema[value] // 重置当前 schema 为默认值
      // 保留备注信息
      const parentDataItem = parentData.description
        ? { description: parentData.description }
        : {}

      const newParentData = Object.assign({}, newParentDataItem, parentDataItem)
      set(cloneSchema, parentPrefix, newParentData)
      this.schemaData = cloneSchema
      this.forceUpdate()
      this.handleEmitChange(cloneSchema)
    },
    // title & description 编辑
    handleShowEdit(opts) {
      const { field, name, prefix, isRoot } = opts
      log(this, 'handleShowEdit', name, prefix)

      let parentData
      if (isRoot) {
        parentData = this.schemaData
      } else {
        const parentPrefix = [].concat(prefix, name)
        parentData = get(this.schemaData, parentPrefix)
      }
      // disable 的时候，return事件处理
      if (
        (field === 'default' && parentData.type === 'array') ||
        parentData.type === 'object'
      ) {
        return
      }

      this.basicDialogVisible = true

      Object.assign(this.basicModalData, {
        title:
          field === 'title' ? '标题' : field === 'default' ? '默认值' : '描述',
        value: parentData[field],
        editorId: this.editorId,
        ...opts,
      })
    },
    handleSaveShowEdit(opts) {
      const { value, field, name, prefix, isRoot } = opts
      // console.log(field, value)
      let parentPrefix
      const cloneSchema = cloneDeep(this.schemaData)
      if (isRoot) {
        cloneSchema[field] = value
      } else {
        parentPrefix = [].concat(prefix, name, field)
        set(cloneSchema, parentPrefix, value)
      }

      this.schemaData = cloneSchema
      this.forceUpdate()
      this.handleEmitChange(cloneSchema)
    },
    // 高级设置
    handleSettingAction(opts) {
      const { schemaType, name, prefix, isRoot } = opts
      // console.log(schemaType)
      this.settingDialogVisible[schemaType] = true

      let parentData
      if (isRoot) {
        parentData = this.schemaData
      } else {
        const parentPrefix = [].concat(prefix, name)
        parentData = get(this.schemaData, parentPrefix)
      }

      this.settingModalData = {
        schemaType,
        name,
        isRoot,
        prefix,
        editorId: this.editorId,
        ...parentData,
      }
    },
    // 高级设置更新 schema
    handleSaveSetting(opts) {
      const { name, prefix, newData, isRoot } = opts
      const cloneSchema = cloneDeep(this.schemaData)
      if (isRoot) {
        Object.assign(cloneSchema, { ...newData })
      } else {
        const parentPrefix = [].concat(prefix, name)
        const oldData = get(cloneSchema, parentPrefix)
        set(cloneSchema, parentPrefix, { ...oldData, ...newData })
      }
      this.schemaData = cloneSchema
      this.forceUpdate()
      this.handleEmitChange(cloneSchema)
    },
    handleChangeMock() {},
    handleReqBodyRaw() {
      this.rawDialogVisible = true
      this.forceUpdate()
    },
    // 解决嵌套对象属性无法刷新页面问题
    forceUpdate(data) {
      const temp = data || this.schemaData
      this.schemaData = {}
      this.$nextTick(() => {
        this.schemaData = temp
      })
    },
    handleEmitChange(schema) {
      // console.log(schema)
      this.$emit('schema-change', schema)
      this.$emit('update:schema', schema)
    },
  },
}
</script>
