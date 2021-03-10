<template>
  <el-dialog :close-on-click-modal="false"
             :destroy-on-close="true"
             :title="$t('load_test.exist_jmx')" width="70%"
             :visible.sync="loadFileVisible">

    <el-table v-loading="projectLoadingResult.loading"
              class="basic-config"
              :data="existFiles"
              @select-all="handleSelectAll"
              @select="handleSelectionChange">

      <el-table-column type="selection"/>
      <el-table-column
        prop="testName"
        :label="$t('load_test.test')">
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('load_test.file_name')">
      </el-table-column>
      <el-table-column
        prop="type"
        :label="$t('load_test.file_type')">
      </el-table-column>
      <el-table-column
        :label="$t('load_test.last_modify_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleImport"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {getCurrentProjectID} from "@/common/js/utils";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";

export default {
  name: "ExistFiles",
  components: {MsTablePagination, MsDialogFooter},
  props: {
    fileList: Array,
    tableData: Array,
    uploadList: Array,
    scenarios: Array
  },
  data() {
    return {
      loadFileVisible: false,
      projectLoadingResult: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loadType: 'jmx',
      existFiles: [],
      selectIds: new Set,
    }
  },
  methods: {
    open(loadType) {
      this.loadFileVisible = true;
      this.loadType = loadType;
      this.getProjectFiles();
    },
    close() {
      this.loadFileVisible = false;
      this.selectIds.clear();
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.existFiles.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.existFiles.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
          }
        });
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectIds.has(row.id)) {
        this.selectIds.delete(row.id);
      } else {
        this.selectIds.add(row.id);
      }
    },
    getProjectFiles() {
      this.projectLoadingResult = this.$get('/performance/project/' + this.loadType + '/' + getCurrentProjectID() + "/" + this.currentPage + "/" + this.pageSize, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.existFiles = data.listObject;
      })
    },
    handleImport() {
      if (this.selectIds.size === 0) {
        return;
      }

      let rows = this.existFiles.filter(f => this.selectIds.has(f.id));
      for (let i = 0; i < rows.length; i++) {
        let row = rows[i];
        if (this.tableData.filter(f => f.name === row.name).length > 0) {
          this.$error(this.$t('load_test.delete_file'));
          this.selectIds.clear();
          return;
        }
      }

      if (this.loadType === 'resource') {
        rows.forEach(row => {
          this.fileList.push(row);
          this.tableData.push({
            name: row.name,
            size: (row.size / 1024).toFixed(2) + ' KB',
            type: 'JMX',
            updateTime: row.lastModified,
          });
        })
        this.$success(this.$t('test_track.case.import.success'));
        this.loadFileVisible = false;
        this.selectIds.clear();
        return;
      }

      this.projectLoadingResult = this.$post('/performance/export/jmx', [...this.selectIds], (response) => {
        let data = response.data;
        if (!data) {
          return;
        }
        data.forEach(d => {
          let threadGroups = findThreadGroup(d.jmx, d.name);
          threadGroups.forEach(tg => {
            tg.options = {};
            this.scenarios.push(tg);
          });
          let file = new File([d.jmx], d.name);
          this.uploadList.push(file);
          this.tableData.push({
            name: file.name,
            size: (file.size / 1024).toFixed(2) + ' KB',
            type: 'JMX',
            updateTime: file.lastModified,
          });
        });

        this.$emit('fileChange', this.scenarios);
        this.$success(this.$t('test_track.case.import.success'));
        this.loadFileVisible = false;
        this.selectIds.clear();
      });

    },
  }
}
</script>

<style scoped>

</style>
