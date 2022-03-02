<template>
  <el-dialog
    title=""
    :visible.sync="dialogVisible"
    width="25%">
    <span>{{ $t('test_track.issue.delete_tip') }}{{issue.title}} ?</span>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false" size="medium">{{$t('commons.cancel')}}</el-button>
      <el-button type="primary" @click="confirm" size="medium">{{$t('commons.confirm')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "IssueDeleteConfirm",
  data() {
    return {
      dialogVisible: false,
      issue: {
        title: ""
      }
    }
  },
  methods: {
    open(issue) {
      this.issue = issue;
      this.dialogVisible = true;
    },
    confirm() {
      this.$emit('delete', this.issue);
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
