<template>

  <div class="card-container" v-loading="result.loading">

    <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="initTableData"
                     :tip="$t('commons.search_by_name_or_id')" title="" :show-create="false"/>
    <el-table
      border
      :data="tableData"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      row-key="id"
      class="test-content adjust-table ms-select-all-fixed"
      ref="table" @row-click="handleEdit">
      <el-table-column
        width="50"
        type="selection"/>

      <ms-table-header-select-popover v-show="total>0"
                                      :page-size="pageSize > total ? total : pageSize"
                                      :total="total"
                                      @selectPageAll="isSelectDataAll(false)"
                                      @selectAll="isSelectDataAll(true)"/>

      <el-table-column width="40" :resizable="false" align="center">
        <template v-slot:default="scope">
          <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
        </template>
      </el-table-column>
      <template v-for="(item, index) in tableLabel">

        <el-table-column
          v-if="item.id == 'num'"
          prop="num"
          sortable="custom"
          :label="$t('commons.id')"
          :key="index"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          v-if="item.id == 'name'"
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip
          :key="index"
        >
        </el-table-column>
        <el-table-column
          v-if="item.id == 'priority'"
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          min-width="100px"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id=='reviewStatus'"
          column-key="reviewStatus"
          min-width="100px"
          :label="$t('test_track.case.status')"
          :key="index">
          <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <review-status :value="scope.row.reviewStatus"/>
            </span>
          </template>
        </el-table-column>
        <el-table-column v-if="item.id=='tags'" prop="tags" :label="$t('commons.tag')" :key="index">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
          </template>
        </el-table-column>

        <el-table-column
          v-if="item.id=='nodePath'"
          prop="nodePath"
          :label="$t('test_track.case.module')"
          min-width="150px"
          show-overflow-tooltip
          :key="index">
        </el-table-column>

        <el-table-column
          v-if="item.id=='updateTime'"
          prop="updateTime"
          sortable="custom"
          :label="$t('commons.update_time')"
          min-width="150px"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </template>
      <el-table-column fixed="right" min-width="150">
        <template slot="header">
          <header-label-operate @exec="customHeader"/>
        </template>
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
      <header-custom ref="headerCustom" :initTableData="initTableData" :optionalFields=headerItems
                     :type=type></header-custom>
    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>

    <batch-move @refresh="refresh" @moveSave="moveSave" ref="testBatchMove"/>
  </div>

</template>

<script>

import MsCreateBox from '../../../settings/CreateBox';
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
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
import {TEST_CASE_CONFIGS} from "../../../common/components/search/search-components";
import ShowMoreBtn from "./ShowMoreBtn";
import BatchEdit from "./BatchEdit";
import {PROJECT_NAME, TEST_CASE_LIST, WORKSPACE_ID} from "@/common/js/constants";
import StatusTableItem from "@/business/components/track/common/tableItems/planview/StatusTableItem";
import TestCaseDetail from "./TestCaseDetail";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import MsTag from "@/business/components/common/components/MsTag";
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  buildBatchParam,
  getLabel,
  getSelectDataCounts,
  initCondition,
  setUnSelectIds,
  toggleAllSelection
} from "@/common/js/tableUtils";
import BatchMove from "./BatchMove";
import {Track_Test_Case} from "@/business/components/common/model/JsonData";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import PlanStatusTableItem from "@/business/components/track/common/tableItems/plan/PlanStatusTableItem";

export default {
  name: "TestCaseList",
  components: {
    PlanStatusTableItem,
    HeaderLabelOperate,
    HeaderCustom,
    BatchMove,
    MsTableHeaderSelectPopover,
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
    BatchEdit,
    StatusTableItem,
    TestCaseDetail,
    ReviewStatus,
    MsTag,
  },
  data() {
    return {
      type: TEST_CASE_LIST,
      headerItems: Track_Test_Case,
      tableLabel: [],
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
      reviewStatusFilters: [
        {text: this.$t('test_track.case.status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.case.status_pass'), value: 'Pass'},
        {text: this.$t('test_track.case.status_un_pass'), value: 'UnPass'},
      ],
      statusFilters: [
        {text: '未开始', value: 'Prepare'},
        {text: '进行中', value: 'Underway'},
        {text: '已完成', value: 'Completed'},
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
      },
      currentCaseId: null,
      selectDataCounts: 0,
      selectDataRange: "all"
    }
  },
  props: {
    treeNodes: {
      type: Array
    },
    trashEnable: {
      type: Boolean,
      default: false,
    }
  },
  computed: {
    projectId() {
      return this.$store.state.projectId
    },
    selectNodeIds() {
      return this.$store.state.testCaseSelectNodeIds;
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    }
  },
  created: function () {
    this.$emit('setCondition', this.condition);
    this.condition.filters = {reviewStatus: ["Prepare", "Pass", "UnPass"]};
    this.initTableData();

  },
  activated() {
    this.condition.filters = {reviewStatus: ["Prepare", "Pass", "UnPass"]};
    this.initTableData();
  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      initCondition(this.condition, false);
      this.initTableData();
    },
    condition() {
      this.$emit('setCondition', this.condition);
    }
  },
  methods: {
    customHeader() {
      this.$refs.headerCustom.open(this.tableLabel)
    },
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      this.selectDataRange = dataType === 'case' ? dataRange : 'all';
    },
    initTableData() {
      this.condition.planId = "";
      this.condition.nodeIds = [];
      //initCondition(this.condition);
      initCondition(this.condition, this.condition.selectAll);
      this.selectDataCounts = 0;
      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        // param.nodeIds = this.selectNodeIds;
        this.condition.nodeIds = this.selectNodeIds;
      }
      getLabel(this, TEST_CASE_LIST);

      this.getData();
    },
    getData() {
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.selectThisWeedRelevanceData = false;
      this.condition.caseCoverage = null;
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'thisWeekRelevanceCount':
          this.condition.selectThisWeedRelevanceData = true;
          break;
        case 'uncoverage':
          this.condition.caseCoverage = 'uncoverage';
          break;
        case 'coverage':
          this.condition.caseCoverage = 'coverage';
          break;
        case 'Prepare':
          this.condition.filters.reviewStatus = [this.selectDataRange];
          break;
        case 'Pass':
          this.condition.filters.reviewStatus = [this.selectDataRange];
          break;
        case 'UnPass':
          this.condition.filters.reviewStatus = [this.selectDataRange];
          break;
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.result = this.$post(this.buildPagePath('/test/case/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          // this.selectIds.clear();
          this.selectRows.clear();
          /*this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          })*/
          this.tableData.forEach((item) => {
            item.tags = JSON.parse(item.tags);
          })

          this.$nextTick(() => {
            if (this.$refs.table) {
              setTimeout(this.$refs.table.doLayout, 200)
            }
            this.checkTableRowIsSelect();
          })
        });
      }
    },
    checkTableRowIsSelect() {
      //如果默认全选的话，则选中应该选中的行
      if (this.condition.selectAll) {
        let unSelectIds = this.condition.unSelectIds;
        this.tableData.forEach(row => {
          if (unSelectIds.indexOf(row.id) < 0) {
            this.$refs.table.toggleRowSelection(row, true);

            //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
            if (!this.selectRows.has(row)) {
              this.$set(row, "showMore", true);
              this.selectRows.add(row);
            }
          } else {
            //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
            if (this.selectRows.has(row)) {
              this.$set(row, "showMore", false);
              this.selectRows.delete(row);
            }
          }
        })
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
      this.$get('test/case/get/' + testCase.id, response => {
        let testCase = response.data;
        this.$emit('testCaseEdit', testCase);
      });
    },
    handleCopy(testCase) {
      this.$get('test/case/get/' + testCase.id, response => {
        let testCase = response.data;
        testCase.name = 'copy_' + testCase.name
        this.$emit('testCaseCopy', testCase);
      });
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
            let param = {};
            param.ids = Array.from(this.selectRows).map(row => row.id);
            param.condition = this.condition;
            this.$post('/test/case/batch/delete', param, () => {
              this.selectRows.clear();
              this.$emit("refresh");
              this.$success(this.$t('commons.delete_success'));
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
      });
    },
    refresh() {
      // this.condition = {components: TEST_CASE_CONFIGS};
      // this.selectIds.clear();
      this.selectRows.clear();
      this.$emit('refresh');
    },
    refreshAll() {
      this.selectRows.clear();
      this.$emit('refreshAll');
    },
    showDetail(row, event, column) {
      this.$emit('testCaseDetail', row);
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    importTestCase() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.testCaseImport.open();
    },
    exportTestCase() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }

      let config = {
        url: '/test/case/export/testcase',
        method: 'post',
        responseType: 'blob',
        data: buildBatchParam(this)
      };

      if (config.data.ids === undefined || config.data.ids.length < 1) {
        this.$warning(this.$t("test_track.case.check_select"));
        return;
      }

      this.result = this.$request(config).then(response => {
        const filename = "Metersphere_case_" + localStorage.getItem(PROJECT_NAME) + ".xlsx";
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
        if (type === 'export') {
          this.exportTestCase();
          return;
        } else {
          this.$warning(this.$t('test_track.plan_view.select_manipulate'));
          return;
        }
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
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;
      param.condition = this.condition;
      this.$post('/test/case/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.refresh();
      });
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
      this.$refs.testBatchMove.open(this.treeNodes, Array.from(this.selectRows).map(row => row.id), this.moduleOptions);
    },
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.valueArr.maintainer = response.data;
      });
    },
    showPopover(row, column, cell) {
      if (column.property === 'name') {
        this.currentCaseId = row.id;
      }
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      toggleAllSelection(this.$refs.table, this.tableData, this.selectRows);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    moveSave(param) {
      param.condition = this.condition;
      this.result = this.$post('/test/case/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.refresh();
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

.el-tag {
  margin-left: 10px;
}

/deep/ .el-table__fixed-body-wrapper {
  top: 60px !important;
}
</style>
