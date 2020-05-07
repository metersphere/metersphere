<template>

  <el-dialog :title="$t('test_track.case.move')"
             :visible.sync="dialogVisible"
             :before-close="close"
             width="20%">

    <el-select v-model.trim="module"
               :placeholder="$t('test_track.case.move')"
               filterable>
      <el-option v-for="item in moduleOptions" :key="item.id"
                 :label="item.path" :value="item.id"></el-option>
    </el-select>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="save"/>
    </template>


  </el-dialog>

</template>

<script>
  import MsDialogFooter from '../../../common/components/MsDialogFooter';

  export default {
    name: "TestCaseMove",
    components: {MsDialogFooter},
    data() {
      return {
        dialogVisible: false,
        module: '',
        moduleOptions: [],
        selectIds: []
      }
    },
    methods: {
      save() {
        if (this.module === '') {
          this.$warning(this.$t('test_track.plan_view.select_execute_result'));
          return;
        }
        let param = {};
        param.nodeId = this.module;
        this.moduleOptions.forEach(item => {
          if (item.id === this.module) {
            param.nodePath = item.path;
          }
        });

        if (this.module === '') {
          this.$warning(this.$t('test_track.plan_view.select_executor'));
          return;
        }
        param.ids = [...this.selectIds];
        this.$post('/test/case/batch/edit' , param, () => {
          this.$success(this.$t('commons.save_success'));
          this.close();
          this.$emit('refresh');
        });

      },
      open(moduleOptions, selectIds) {
        this.moduleOptions = moduleOptions;
        this.selectIds = selectIds;
        this.dialogVisible = true;
      },
      close() {
        this.module = '';
        this.selectIds = [];
        this.dialogVisible = false;
      }
    }
  }
</script>

<style scoped>

</style>
