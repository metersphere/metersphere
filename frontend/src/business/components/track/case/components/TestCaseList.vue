<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>

        <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="initTableData" :tip="$t('commons.search_by_name_or_id')"
                         :create-tip="$t('test_track.case.create')" @create="testCaseCreate">
          <template v-slot:title>
            <node-breadcrumb class="table-title" :nodes="selectParentNodes" @refresh="refresh"/>
          </template>
          <template v-slot:button>
            <ms-table-button :is-tester-permission="true" icon="el-icon-download"
                             :content="$t('test_track.case.import.import')" @click="importTestCase"/>
            <ms-table-button :is-tester-permission="true" icon="el-icon-upload2"
                             :content="$t('test_track.case.export.export')" @click="handleBatch('export')"/>
<!--            <ms-table-button :is-tester-permission="true" icon="el-icon-right" :content="$t('test_track.case.move')"-->
<!--                             @click="handleBatch('move')"/>-->
<!--            <ms-table-button :is-tester-permission="true" icon="el-icon-delete" :content="$t('test_track.case.delete')"-->
<!--                             @click="handleBatch('delete')"/>-->
            <!--<test-case-export/>-->
          </template>
        </ms-table-header>

      </template>

      <test-case-import :projectId="currentProject == null? null : currentProject.id" @refresh="refresh"
                        ref="testCaseImport"/>

      <el-table
        border
        :data="tableData"
        @sort-change="sort"
        @filter-change="filter"
        @select-all="handleSelectAll"
        @select="handleSelectionChange"
        @row-click="showDetail"
        row-key="id"
        class="test-content adjust-table">
        <el-table-column
          type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="num"
          sortable="custom"
          :label="$t('commons.id')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="type"
          :filters="typeFilters"
          column-key="type"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <type-table-item :value="scope.row.type"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="method"
          column-key="method"
          :filters="methodFilters"
          :label="$t('test_track.case.method')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <method-table-item :value="scope.row.method"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="nodePath"
          :label="$t('test_track.case.module')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="updateTime"
          sortable="custom"
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <ms-table-operator :is-tester-permission="true" @editClick="handleEdit(scope.row)"
                               @deleteClick="handleDelete(scope.row)">
              <template v-slot:middle>
                <ms-table-operator-button :is-tester-permission="true" :tip="$t('commons.copy')"
                                          icon="el-icon-document-copy"
                                          type="success" @exec="handleCopy(scope.row)"/>
              </template>
            </ms-table-operator>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>
  </div>
</template>

<script>

import MsCreateBox from '../../../settings/CreateBox';
import TestCaseImport from '../components/TestCaseImport';
import TestCaseExport from '../components/TestCaseExport';
import MsTablePagination from '../../../../components/common/pagination/TablePagination';
import NodeBreadcrumb from '../../common/NodeBreadcrumb';
import MsTableHeader from '../../../../components/common/components/MsTableHeader';
import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../common/tableItems/planview/MethodTableItem";
import MsTableOperator from "../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsTableButton from "../../../common/components/MsTableButton";
import {_filter, _sort} from "../../../../../common/js/utils";
import {TEST_CASE_CONFIGS} from "../../../common/components/search/search-components";
import ShowMoreBtn from "./ShowMoreBtn";
import BatchEdit from "./BatchEdit";
import {WORKSPACE_ID} from "../../../../../common/js/constants";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "TestCaseList",
  components: {
    MsTableButton,
    MsTableOperatorButton,
    MsTableOperator,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
    MsCreateBox,
    TestCaseImport,
    TestCaseExport,
    MsTablePagination,
    NodeBreadcrumb,
    MsTableHeader,
    ShowMoreBtn,
    BatchEdit
  },
  data() {
    return {
      result: {},
      deletePath: "/test/case/delete",
      condition: {
        components: TEST_CASE_CONFIGS
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectRows: new Set(),
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodFilters: [
        {text: this.$t('test_track.case.manual'), value: 'manual'},
        {text: this.$t('test_track.case.auto'), value: 'auto'}
      ],
      typeFilters: [
        {text: this.$t('commons.functional'), value: 'functional'},
        {text: this.$t('commons.performance'), value: 'performance'},
        {text: this.$t('commons.api'), value: 'api'}
      ],
      showMore: false,
      buttons: [
        {
          name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit
        }, {
          name: this.$t('test_track.case.batch_move_case'), handleClick: this.handleBatchMove
        }, {
          name: this.$t('test_track.case.batch_delete_case'), handleClick: this.handleDeleteBatch
        }
      ],
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
        {id: 'type', name: this.$t('test_track.case.type')},
        {id: 'method', name: this.$t('test_track.case.method')},
        {id: 'maintainer', name: this.$t('test_track.case.maintainer')},
      ],
      valueArr: {
        priority: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        type: [
          {name: this.$t('commons.functional'), id: 'functional'},
          {name: this.$t('commons.performance'), id: 'performance'},
          {name: this.$t('commons.api'), id: 'api'}
        ],
        method: [
          {name: this.$t('test_track.case.manual'), id: 'manual'},
          {name: this.$t('test_track.case.auto'), id: 'auto'}
        ],
        maintainer: [],
      }
    }
  },
  props: {
    currentProject: {
      type: Object
    },
    selectNodeIds: {
      type: Array
    },
    selectParentNodes: {
      type: Array
    }
  },
  created: function () {
    this.initTableData();
  },
  watch: {
    currentProject() {
      this.initTableData();
    },
    selectNodeIds() {
      this.initTableData();
    }
  },
  methods: {
    initTableData() {
      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        // param.nodeIds = this.selectNodeIds;
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (this.currentProject) {
        this.condition.projectId = this.currentProject.id;
        this.result = this.$post(this.buildPagePath('/test/case/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          // this.selectIds.clear();
          this.selectRows.clear();
        });
      }
    },
    search() {
      this.initTableData();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    testCaseCreate() {
      this.$emit('testCaseEdit');
    },
    handleEdit(testCase) {
      this.$emit('testCaseEdit', testCase);
    },
    handleCopy(testCase) {
      this.$emit('testCaseCopy', testCase);
    },
    handleDelete(testCase) {
      this.$alert(this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'' + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(testCase);
          }
        }
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.case.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            this.$post('/test/case/batch/delete', {ids: ids}, () => {
              this.selectRows.clear();
              this.$emit("refresh");
              this.$success(this.$t('commons.delete_success'));
              // 发送广播，刷新 head 上的最新列表
              TrackEvent.$emit(LIST_CHANGE);
            });
          }
        }
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      this.$post('/test/case/delete/' + testCaseId, {}, () => {
        this.initTableData();
        this.$success(this.$t('commons.delete_success'));
        // 发送广播，刷新 head 上的最新列表
        TrackEvent.$emit(LIST_CHANGE);
      });
    },
    refresh() {
      this.condition = {components: TEST_CASE_CONFIGS};
      // this.selectIds.clear();
      this.selectRows.clear();
      this.$emit('refresh');
    },
    showDetail(row, event, column) {
      this.$emit('testCaseDetail', row);
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        if (selection.length === 1) {
          this.selectRows.add(selection[0]);
        } else {
          this.tableData.forEach(item => {
            this.$set(item, "showMore", true);
            this.selectRows.add(item);
          });
        }
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
    },
    handleSelectionChange(selection, row) {
      // if (this.selectIds.has(row.id)) {
      //   this.selectIds.delete(row.id);
      // } else {
      //   this.selectIds.add(row.id);
      // }
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }

      let arr = Array.from(this.selectRows);

      // 选中1个以上的用例时显示更多操作
      if (this.selectRows.size === 1) {
        this.$set(arr[0], "showMore", false);
      } else if (this.selectRows.size === 2) {
        arr.forEach(row => {
          this.$set(row, "showMore", true);
        })
      }
    },
    importTestCase() {
      this.$refs.testCaseImport.open();
    },
    exportTestCase() {
      let ids = Array.from(this.selectRows).map(row => row.id);
      let config = {
        url: '/test/case/export/testcase',
        method: 'post',
        responseType: 'blob',
        // data: {ids: [...this.selectIds]}
        data: {ids: ids}
      };
      this.result = this.$request(config).then(response => {
        const filename = this.$t('test_track.case.test_case') + ".xlsx";
        const blob = new Blob([response.data]);
        if ("download" in document.createElement("a")) {
          let aTag = document.createElement('a');
          aTag.download = filename;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href)
        } else {
          navigator.msSaveBlob(blob, filename);
        }
      });
    },
    handleBatch(type) {
      if (this.selectRows.size < 1) {
        this.$warning(this.$t('test_track.plan_view.select_manipulate'));
        return;
      }
      if (type === 'move') {
        let ids = Array.from(this.selectRows).map(row => row.id);
        this.$emit('moveToNode', ids);
      } else if (type === 'delete') {
        this.handleDeleteBatch();
      } else {
        this.exportTestCase();
      }
    },
    batchEdit(form) {
      let sign = false;
      let arr = Array.from(this.selectRows);
      // 功能测试的测试方式不能设置为自动
      if (form.type === 'method' && form.value === 'auto') {
        arr.forEach(row => {
          if (row.type === 'functional') {
            sign = true;
            return;
          }
        });
      }

      if (form.type === 'type' && form.value === 'functional') {
        arr.forEach(row => {
          if (row.method === 'auto') {
            sign = true;
            return;
          }
        });
      }

      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;
      if (!sign) {
        this.$post('/test/case/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.refresh();
        });
      } else {
        this.$warning("功能测试的测试方式不能设置为自动！");
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTableData();
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    handleBatchMove() {
      this.$emit("batchMove", Array.from(this.selectRows).map(row => row.id));
    },
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.valueArr.maintainer = response.data;
      });
    }
  }
}
</script>

<style scoped>

.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.operate-button {
  float: right;
}

.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.search {
  margin-left: 10px;
  width: 240px;
}

.el-table {
  cursor: pointer;
}

</style>
