<template>
  <div>

      <el-input :placeholder="$t('commons.search_by_name_or_id')" @blur="initTable"
                @keyup.enter.native="initTable" class="search-input" size="small" v-model="condition.name"/>

      <ms-table v-loading="result.loading" :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
                :total="total"
                :showSelectAll="false"
                :screenHeight="screenHeight"
                @refresh="initTable"
                ref="table">

        <ms-table-column
          prop="num"
          label="ID"
          width="100px"
          sortable=true>
        </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('test_track.case.name')"/>

        <ms-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </ms-table-column>

<!--        <ms-table-column-->
<!--          prop="path"-->
<!--          width="180px"-->
<!--          :label="'API'+ $t('api_test.definition.api_path')"/>-->

<!--        <ms-table-column-->
<!--          sortable="custom"-->
<!--          prop="casePath"-->
<!--          width="180px"-->
<!--          :label="$t('api_test.definition.request.case')+ $t('api_test.definition.api_path')"/>-->


      </ms-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    <table-select-count-bar :count="selectRows.size"/>

  </div>

</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import TableSelectCountBar from "@/business/components/api/automation/scenario/api/TableSelectCountBar";

export default {
  name: "TestCaseRelateApiList",
  components: {
    TableSelectCountBar,
    MsTablePagination,
    PriorityTableItem,
    MsTable,
    MsTableColumn
  },
  data() {
    return {
      condition: {},
      selectCase: {},
      result: {},
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodColorMap: new Map(API_METHOD_COLOUR),
      screenHeight: '600px',//屏幕高度
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
    }
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    projectId: String,
  },
  created: function () {
    this.initTable();
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    currentProtocol() {
      this.initTable();
    },
    projectId() {
      this.initTable();
    }
  },
  computed: {
    selectRows() {
      if (this.$refs.table) {
        return this.$refs.table.getSelectRows();
      } else {
        return new Set();
      }
    }
  },
  methods: {
    initTable(projectId) {
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      let url = '/test/case/relevance/api/list/';
      this.result = this.$post(this.buildPagePath(url), this.condition, response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
      });
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    getSelectIds() {
      return this.$refs.table.selectIds;
    },
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
      }
    },
  },
}
</script>

<style scoped>
.search-input {
  float: right;
  width: 300px;
  /*margin-bottom: 20px;*/
  margin-right: 20px;
}
</style>
