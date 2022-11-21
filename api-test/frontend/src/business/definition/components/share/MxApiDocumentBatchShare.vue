<template>
  <el-popover
    v-if="projectId"
    placement="left"
    width="260"
    @show="batchShareApiDocument"
    v-model="batchSharePopoverVisible"
  >
    <p>{{ shareUrl }}</p>
    <div style="text-align: right; margin: 0">
      <el-button type="primary" size="mini" v-clipboard:copy="shareUrl">{{
        $t('commons.copy')
      }}</el-button>
    </div>
    <i
      class="el-icon-share"
      slot="reference"
      style="margin-right: 10px; cursor: pointer"
    ></i>
  </el-popover>
</template>

<script>
export default {
  name: 'MxApiDocumentBatchShare',
  data() {
    return {
      batchSharePopoverVisible: false,
    };
  },
  props: {
    projectId: String,
    shareUrl: String,
  },
  methods: {
    batchShareApiDocument() {
      this.$emit('shareApiDocument', 'true');
    },
    onBatchCopySuccess: function (e) {
      this.batchSharePopoverVisible = false;
      this.$message({
        message: this.$t('commons.copy_success'),
        type: 'success',
      });
    },
    onBatchCopyError: function (e) {
      this.batchSharePopoverVisible = false;
      this.$message.error(this.$t('commons.error'));
    },
  },
  created() {},
};
</script>

<style scoped></style>
