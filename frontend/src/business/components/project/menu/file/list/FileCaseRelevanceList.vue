<template>
  <div>
    <ms-table
      class="basic-config"
      :batch-operators="buttons"
      :data="tableData"
      :condition="condition"
      :hidePopover="true"

      :total="total"
      enableSelection
      showSelectAll

      @refresh="selectData" ref="table">
      <ms-table-column
        label="ID"
        :min-width="120"
        prop="caseId">
      </ms-table-column>

      <ms-table-column
        :label="$t('api_test.home_page.failed_case_list.table_coloum.case_name')"
        :min-width="120"
        prop="caseName">
      </ms-table-column>

      <ms-table-column
        :label="$t('commons.type')"
        :min-width="120"
        prop="caseType">
        <template v-slot="scope">
            <span v-if="scope.row.caseType === 'API'">
              {{ $t('workstation.table_name.api_definition') }}
            </span>
          <span v-else-if="scope.row.caseType === 'CASE'">
              {{ $t('commons.api_case') }}
            </span>
          <span v-else-if="scope.row.caseType === 'SCENARIO'">
              {{ $t('commons.scenario_case') }}
            </span>
          <span v-else-if="scope.row.caseType === 'TEST_CASE'">
              {{ $t('test_track.case.test_case') }}
            </span>
          <span v-else-if="scope.row.caseType === 'LOAD_CASE'">
              {{ $t('commons.performance') }}
            </span>
        </template>
      </ms-table-column>


      <ms-table-column
        :label="$t('project.project_file.repository.file_version')"
        :min-width="120"
        prop="commitId">
      </ms-table-column>

      <el-table-column :label="$t('commons.operating')" fixed="right" :width="130">
        <template v-slot:default="scope">
          <el-button size="mini" @click="updateFileVersion(scope.row)" style="padding: 4px;font-size: 12px">PULL
          </el-button>
        </template>
      </el-table-column>
    </ms-table>
    <table-pagination
      :change="selectData"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"/>
  </div>
</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import TablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";

export default {
  name: "FileCaseRelevanceList",
  components: {
    MsTable, MsTableColumn, MsTableOperatorButton, TablePagination, MsTableHeaderSelectPopover
  },
  data() {
    return {
      tableData: [],
      selectNodeIds: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      metadataArr: [],
      fileNumLimit: 10,
      condition: {},
      buttons: [
        {
          name: 'PULL',
          handleClick: this.batchUpdateFileVersion
        },
      ],
    };
  },
  props: {
    fileMetadataRefId: String,
  },
  watch: {
    fileMetadataRefId() {
      this.selectData();
    }
  },
  created() {
    this.selectData();
  },
  methods: {
    selectData() {
      this.$post('/file/metadata/file/relevance/case/' + this.fileMetadataRefId + "/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let returnData = res.data;
        this.total = returnData.itemCount;
        this.tableData = returnData.listObject;
      });
    },
    updateFileVersion(row) {

      if (row.caseType === 'LOAD_CASE') {
        this.condition.loadCaseFileIdMap = {};
        this.$set(this.condition.loadCaseFileIdMap, row.caseId, row.id);
      } else {
        this.condition.ids = [row.id];
      }
      this.$post('/file/metadata/case/version/update/' + this.fileMetadataRefId, this.condition, res => {
        this.$success('Pull ' + this.$t('variables.end'));
        this.condition.ids = [];
        this.condition.loadCaseFileIdMap = {};
        this.selectData();
      });

    },
    batchUpdateFileVersion() {
      let selectRows = this.$refs.table.selectRows;
      this.condition.ids = [];
      this.condition.loadCaseFileIdMap = {};
      this.$refs.table.selectRows.forEach(row => {
        if (row.caseType === 'LOAD_CASE') {
          this.$set(this.condition.loadCaseFileIdMap, row.caseId, row.id);
        } else {
          this.condition.ids.push(row.id);
        }
      });
      this.$post('/file/metadata/case/version/update/' + this.fileMetadataRefId, this.condition, res => {
        this.condition.ids = [];
        this.condition.loadCaseFileIdMap = {};
        this.$success('Pull ' + this.$t('variables.end'));
        this.selectData();
      });
    },
  },
}
</script>

<style scoped>

</style>
