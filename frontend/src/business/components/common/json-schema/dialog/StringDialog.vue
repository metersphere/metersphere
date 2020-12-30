<template>
  <div>
    <el-dialog
      title="基础设置（字符串）"
      width="700px"
      v-bind="$attrs"
      v-on="$listeners"
      @open="onOpen"
      @close="onClose"
    >
      <el-row :gutter="15">
        <el-form
          ref="elForm"
          :model="formData"
          :rules="rules"
          size="small">
          <el-col :span="24">
            <el-form-item label="默认值：" prop="default">
              <el-input
                v-model="formData.default"
                placeholder="请输入默认值"
                :maxlength="200"
                clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小长度：" prop="minLength">
              <el-input-number
                v-model="formData.minLength"
                placeholder="请输入"
                :step="2"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大长度:" prop="maxLength">
              <el-input-number
                v-model="formData.maxLength"
                placeholder="请输入"
                :step="2"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-col :span="3">
                <div>
                  <label for>枚举</label>
                  <el-checkbox v-model="enableEnum">:</el-checkbox>
                </div>
              </el-col>
              <el-col :span="21">
                <el-form-item label-width="0" prop="enum">
                  <el-input
                    v-model="formData.enum"
                    type="textarea"
                    placeholder="请输入枚举，一行一个"
                    :maxlength="120"
                    :disabled="!enableEnum"
                    :autosize="{ minRows: 2, maxRows: 4 }"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
          <el-col v-if="enableEnum" :span="24">
            <el-form-item label="枚举描述：" prop="enumDesc">
              <el-input
                v-model="formData.enumDesc"
                type="textarea"
                placeholder="请输入枚举描述"
                :maxlength="100"
                :autosize="{ minRows: 2, maxRows: 4 }"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <div slot="footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import compact from 'lodash/compact'
import { getValidFormVal } from '../utils'
export default {
  name: 'StringDialog',
  inheritAttrs: false,
  props: { initData: { type: Object, default: () => ({}) } },
  data() {
    return {
      enableEnum: false,
      formData: {
        default: undefined,
        minLength: undefined,
        maxLength: undefined,
        enum: undefined,
        enumDesc: undefined,
      },
      rules: {
        default: [],
        minLength: [],
        maxLength: [],
        innerScope: [],
        enum: [],
        enumDesc: [],
      },
    }
  },
  methods: {
    onOpen() {
      const { minLength, maxLength, enumDesc } = this.initData
      let enumValue = this.initData.enum
      if (enumValue) {
        try {
          enumValue = enumValue.join('\n')
          this.enableEnum = true
        } catch (e) {
          this.$message({ text: '枚举数据格式不对，将丢失', type: 'warning' })
          enumValue = ''
        }
      }
      Object.assign(
        this.formData,
        { minLength, maxLength, enumDesc },
        { default: this.initData.default, enum: enumValue }
      )
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
        if (newData.enum) {
          newData.enum = compact(newData.enum.split('\n'))
        }
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
