<template>
  <div>
    <el-dialog :title="initData.title" :visible.sync="visible" width="30%">
      <el-input
        v-model="data"
        type="textarea"
        :rows="3"
        placeholder="请输入内容"
        style="margin-bottom: 15px"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="close">取 消</el-button>
        <el-button type="primary" @click="handleOk">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'BasicDialog',
  props: {
    visible: { type: Boolean, default: false },
    initData: {
      type: Object,
      default: () => ({
        title: '提示',
        value: '',
      }),
    },
  },
  data() {
    return {
      dialogVisible: false,
      data: '',
    }
  },

  watch: {
    initData: {
      handler() {
        this.data = this.initData.value
      },
      deep: true,
    },
  },
  created() {},
  methods: {
    close() {
      this.$emit('update:visible', false)
    },
    handleOk() {
      this.initData.value = this.data
      this.$jsEditorEvent.emit(`schema-update-${this.initData.editorId}`, {
        eventType: 'save-showedit',
        ...this.initData,
      })
      this.close()
    },
  },
}
</script>

<style lang="scss" scoped>
</style>
