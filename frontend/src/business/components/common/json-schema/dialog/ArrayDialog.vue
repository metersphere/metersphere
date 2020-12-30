<template>
  <el-dialog
    title="基础设置（数组字段）"
    width="700px"
    v-bind="$attrs"
    v-on="$listeners"
    @open="onOpen"
    @close="onClose"
  >
    <el-row :gutter="15">
      <el-form ref="elForm" :model="formData" size="small">
        <el-col :span="12">
          <el-form-item label="最小元素个数" prop="minItems">
            <el-input-number
              v-model="formData.minItems"
              placeholder="请输入"
              :min="0"
              :step="1"
            ></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大元素个数" prop="maxItems">
            <el-input-number
              v-model="formData.maxItems"
              placeholder="请输入"
              :max="100000"
              :step="1"
            ></el-input-number>
          </el-form-item>
        </el-col>
      </el-form>
    </el-row>
    <div slot="footer">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getValidFormVal } from '../utils'
export default {
  name: 'ArrayDialog',
  inheritAttrs: false,
  props: { initData: { type: Object, default: () => ({}) } },

  data() {
    return {
      formData: {
        minItems: undefined,
        maxItems: undefined,
      },
    }
  },
  created() {},
  methods: {
    onOpen() {
      const { minItems, maxItems } = this.initData
      Object.assign(this.formData, { minItems, maxItems })
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
