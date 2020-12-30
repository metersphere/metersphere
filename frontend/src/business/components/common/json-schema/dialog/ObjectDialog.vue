<template>
  <el-dialog
    title="基础设置（对象字段）"
    width="600px"
    v-bind="$attrs"
    v-on="$listeners"
    @open="onOpen"
    @close="onClose"
  >
    <el-form ref="elForm" :model="formData" size="small" label-width="100px">
      <el-form-item label-width="0" prop="notEmpty" style="text-align: center">
        <el-radio-group v-model="formData.notEmpty" size="medium">
          <el-radio
            v-for="(item, index) in notEmptyOptions"
            :key="index"
            :label="item.value"
            :disabled="item.disabled"
            >{{ item.label }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { getValidFormVal } from '../utils'
export default {
  name: 'ObjectDialog',
  inheritAttrs: false,
  props: { initData: { type: Object, default: () => ({}) } },

  data() {
    return {
      formData: {
        notEmpty: false,
      },
      notEmptyOptions: [
        {
          label: '可为空',
          value: false,
        },
        {
          label: '不允许为空',
          value: true,
        },
      ],
    }
  },
  created() {},
  methods: {
    onOpen() {
      Object.assign(this.formData, { notEmpty: this.initData.notEmpty })
    },
    onClose() {
      this.$refs['elForm'].resetFields()
    },
    close() {
      this.$emit('update:visible', false)
    },
    handleConfirm() {
      this.$refs['elForm'].validate((valid) => {
        if (!valid) return
        const newData = getValidFormVal(this.formData)
        this.$jsEditorEvent.emit(`schema-update-${this.initData.editorId}`, {
          eventType: 'save-setting',
          ...this.initData, // 之前的参数
          newData, // 设置数据
        })
        this.close()
      })
    },
  },
}
</script>
