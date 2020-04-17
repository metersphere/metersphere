<template>

  <el-dialog title="更改执行人"
             :visible.sync="executorEditVisible"
             width="20%">
    <el-select v-model="executor" placeholder="请选择活动区域">
      <el-option v-for="item in executorOptions" :key="item.id"
                 :label="item.name" :value="item.name"></el-option>
    </el-select>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="executorEditVisible = false"
        @confirm="saveExecutor"/>
    </template>


  </el-dialog>

</template>

<script>
    import {WORKSPACE_ID} from '../../../../../common/js/constants'
    import MsDialogFooter from '../../../common/components/MsDialogFooter'

    export default {
      name: "executorEdit",
      components: {MsDialogFooter},
      data() {
        return {
          executorEditVisible: false,
          executor: '',
          executorOptions: []
        }
      },
      props: {
        selectIds: {
          type: Set
        }
      },
      methods: {
        setMaintainerOptions() {
          let workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.$post('/user/ws/member/list/all', {workspaceId:workspaceId}, response => {
            this.executorOptions = response.data;
          });
        },
        saveExecutor() {
          let param = {};
          param.executor = this.executor;
          if (this.executor === '') {
            this.$message('请选择执行人！');
            return;
          }
          param.ids = [...this.selectIds];
          this.$post('/test/plan/case/batch/edit' , param, () => {
            this.executor = '';
            this.selectIds.clear();
            this.$message.success("保存成功");
            this.executorEditVisible = false;
            this.$emit('refresh');
          });
        },
        openExecutorEdit() {
          this.executorEditVisible = true;
          this.setMaintainerOptions();
        }
      }
    }
</script>

<style scoped>

</style>
