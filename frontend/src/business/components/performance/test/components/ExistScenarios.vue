<template>
  <el-dialog :title="$t('load_test.scenario_list')"
             :close-on-click-modal="false"
             :destroy-on-close="true"
             width="60%" :visible.sync="loadApiAutomationVisible"
             :modal="true">

    <el-table v-loading="projectLoadingResult.loading" class="basic-config"
              :data="apiScenarios"
              @select-all="handleSelectAll"
              @select="handleSelectionChange">
      <el-table-column type="selection"/>
      <el-table-column
        prop="num"
        label="ID">
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('load_test.scenario_name')">
      </el-table-column>
      <el-table-column prop="tags" min-width="120px"
                       :label="$t('api_test.automation.tag')">
        <template v-slot:default="scope">
          <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                  :content="itemName"
                  style="margin-left: 0px; margin-right: 2px"/>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('load_test.last_modify_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getProjectScenarios" :current-page.sync="currentPage" :page-size.sync="pageSize"
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
import MsTag from "@/business/components/common/components/MsTag";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";

export default {
  name: "ExistScenarios",
  components: {MsTag, MsTablePagination, MsDialogFooter},
  props: {
    fileList: Array,
    tableData: Array,
    uploadList: Array,
    scenarios: Array
  },
  data() {
    return {
      loadApiAutomationVisible: false,
      projectLoadingResult: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      apiScenarios: [],
      selectIds: new Set,
    };
  },
  methods: {
    open() {
      this.loadApiAutomationVisible = true;
      this.getProjectScenarios();
    },
    close() {
      this.loadApiAutomationVisible = false;
      this.selectIds.clear();
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.apiScenarios.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.apiScenarios.forEach(item => {
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
    getProjectScenarios() {
      let condition = {
        projectId: getCurrentProjectID(),
        filters: {status: ["Prepare", "Underway", "Completed"]}
      };
      this.projectLoadingResult = this.$post('/api/automation/list/' + this.currentPage + "/" + this.pageSize, condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.apiScenarios = data.listObject;
        this.apiScenarios.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    handleImport() {
      if (this.selectIds.size === 0) {
        return;
      }
      let rows = this.apiScenarios.filter(f => this.selectIds.has(f.id));
      for (let i = 0; i < rows.length; i++) {
        let row = rows[i];
        if (this.tableData.filter(f => f.name === row.name + ".jmx").length > 0) {
          this.$error(this.$t('load_test.delete_file') + ', name: ' + row.name);
          return;
        }
      }
      let condition = {
        projectId: getCurrentProjectID(),
        ids: [...this.selectIds],
      };
      this.projectLoadingResult = this.$post('api/automation/export/jmx', condition, response => {
        let data = response.data;
        data.forEach(d => {
          let jmxName = d.name + "_" + new Date().getTime() + ".jmx";
          let threadGroups = findThreadGroup(d.jmx, jmxName);
          threadGroups.forEach(tg => {
            tg.options = {};
            this.scenarios.push(tg);
          });
          let file = new File([d.jmx], jmxName);
          this.uploadList.push(file);
          this.tableData.push({
            name: file.name,
            size: (file.size / 1024).toFixed(2) + ' KB',
            type: 'JMX',
            updateTime: file.lastModified,
          });
          // csv 处理
          d.fileMetadataList?.forEach(f => {
            this.fileList.push(f);
            this.tableData.push(f);
          });
        });

        this.$emit('fileChange', this.scenarios);
        this.$success(this.$t('test_track.case.import.success'));
        this.loadApiAutomationVisible = false;
      });
      this.selectIds.clear();
    },
  }
};
</script>

<style scoped>

</style>
