<template>
    <el-dialog v-loading="result.loading"
               :visible.sync="dialogVisible"
               :close-on-click-modal="false"
    >
      <el-table
        :data="tableData"
        highlight-current-row
        @current-change="handleCurrentChange"
        style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
          <template v-slot:default="scope">
            {{ scope.row.description }}
          </template>
        </el-table-column>
        <el-table-column
          sortable
          prop="createTime"
          :label="$t('commons.create_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template v-slot:footer>
        <div class="dialog-footer">
          <ms-dialog-footer
            @cancel="dialogVisible = false"
            @confirm="submit()"/>
        </div>
      </template>
    </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../common/components/MsDialogFooter";

  export default {
    name: "SwitchProject",
    components: {MsDialogFooter},
    data() {
      return {
        tableData: [],
        result: {},
        dialogVisible: false,
        projectId: ''
      }
    },
    methods: {
      open(planId) {
        this.dialogVisible = true;
        this.initData(planId);
      },
      initData(planId) {
        this.result = this.$get("/test/plan/project/" + planId,res => {
          this.tableData = res.data;
        })
      },
      handleCurrentChange(currentRow) {
        // initData 改变表格数据会触发此方法
        if (currentRow) {
          this.projectId = currentRow.id;
        }
      },
      submit() {
        this.$emit('getProjectNode', this.projectId);
        this.dialogVisible = false;
      }
    }
  }
</script>

<style scoped>

</style>
