<template>

  <el-dialog title="更改执行结果"
             :visible.sync="statusEditVisible"
             width="30%">

    <test-plan-test-case-status-button
      @statusChange="statusChange"
      :status="status"/>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="statusEditVisible = false"
        @confirm="saveStatus"/>
    </template>

  </el-dialog>

</template>

<script>
  import TestPlanTestCaseStatusButton from '../common/TestPlanTestCaseStatusButton';
  import MsDialogFooter from '../../../common/components/MsDialogFooter'

    export default {
      name: "statusEdit",
      components: {TestPlanTestCaseStatusButton, MsDialogFooter},
      data() {
        return {
          statusEditVisible: false,
          status: ''
        }
      },
      props: {
        selectIds: {
          type: Set
        }
      },
      methods: {
        statusChange(status) {
          this.status = status;
        },
        saveStatus() {
          let param = {};
          if (this.status === '') {
            this.$message('请选择执行结果！');
            return;
          }
          param.status = this.status;
          param.ids = [...this.selectIds];
          this.$post('/test/plan/case/batch/edit' , param, () => {
            this.selectIds.clear();
            this.status = '';
            this.$message.success("保存成功");
            this.statusEditVisible = false;
            this.$emit('refresh');
          });
        },
        openStatusEdit() {
          this.statusEditVisible = true;
        }
      }
    }
</script>

<style scoped>

</style>
