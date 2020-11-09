<template>

  <div class="card-container">
    <el-card class="card-content">
      <template v-slot:header>
        <ms-table-header :showCreate="false" :condition.sync="condition" @search="search"
                         :title="$t('api_test.delimit.api_title')"/>
      </template>
      <el-table border :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @select="handleSelect">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

        <el-table-column prop="name" :label="$t('api_test.delimit.api_name')" show-overflow-tooltip/>
        <el-table-column
          prop="status"
          column-key="api_status"
          :label="$t('api_test.delimit.api_status')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <ms-tag v-if="scope.row.status == 'Prepare'" type="info"
                    :content="$t('test_track.plan.plan_status_prepare')"/>
            <ms-tag v-if="scope.row.status == 'Underway'" type="primary"
                    :content="$t('test_track.plan.plan_status_running')"/>
            <ms-tag v-if="scope.row.status == 'Completed'" type="success"
                    :content="$t('test_track.plan.plan_status_completed')"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          :label="$t('api_test.delimit.api_type')"
          show-overflow-tooltip>
          <template v-slot:default="scope" class="request-method">
            <el-tag size="mini"
                    :style="{'background-color': getColor(true, scope.row.path)}" class="api-el-tag"> {{ scope.row.path
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="url"
          :label="$t('api_test.delimit.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="userName"
          :label="$t('api_test.delimit.api_principal')"
          show-overflow-tooltip/>

        <el-table-column width="200" :label="$t('api_test.delimit.api_last_time')" prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="caseTotal"
          :label="$t('api_test.delimit.api_case_number')"
          show-overflow-tooltip/>

        <el-table-column
          prop="caseStatus"
          :label="$t('api_test.delimit.api_case_status')"
          show-overflow-tooltip/>

        <el-table-column
          prop="casePassingRate"
          :label="$t('api_test.delimit.api_case_passing_rate')"
          show-overflow-tooltip/>


        <el-table-column
          :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <el-button type="text" @click="editApi(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleTestCase(scope.row)">用例</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initApiTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>

    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" :api="selectApi" :current-project="currentProject"/>
    </ms-bottom-container>

  </div>
</template>

<script>

  import MsTableHeader from '../../../../components/common/components/MsTableHeader';
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../common/components/MsTableButton";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../common/pagination/TablePagination";
  import MsTag from "../../../common/components/MsTag";
  import MsApiCaseList from "./ApiCaseList";
  import MsContainer from "../../../common/components/MsContainer";
  import MsBottomContainer from "./BottomContainer";
  import ShowMoreBtn from "../../../../components/track/case/components/ShowMoreBtn";
  import {API_METHOD_COLOUR} from "../model/JsonData";

  export default {
    name: "ApiList",
    components: {
      MsTableButton,
      MsTableOperatorButton,
      MsTableOperator,
      MsTableHeader,
      MsTablePagination,
      MsTag,
      MsApiCaseList,
      MsContainer,
      MsBottomContainer,
      ShowMoreBtn
    },
    data() {
      return {
        condition: {},
        isHide: true,
        selectApi: {},
        moduleId: "",
        deletePath: "/test/case/delete",
        selectRows: new Set(),
        buttons: [{name: this.$t('api_test.delimit.request.batch_delete'), handleClick: this.handleDeleteBatch}],
        methodColorMap: new Map(API_METHOD_COLOUR),
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
      }
    },
    props: {
      currentProject: Object,
      currentModule: Object
    },
    created: function () {
      this.initApiTable();
    },
    watch: {
      currentProject() {
        this.initApiTable();
      },
      currentModule() {
        this.initApiTable();
      }
    },
    methods: {
      initApiTable() {
        if (this.currentModule != null) {
          if (this.currentModule.id == "root") {
            this.condition.moduleIds = [];
          } else {
            this.condition.moduleIds = this.currentModule.ids;
          }
        }
        if (this.currentProject != null) {
          this.condition.projectId = this.currentProject.id;
        }
        this.result = this.$post("/api/delimit/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      },
      handleSelect(selection, row) {
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
      search() {
        this.initApiTable();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      editApi(row) {
        this.$emit('editApi', row);
      },
      handleDeleteBatch() {
        this.$alert(this.$t('api_test.delimit.request.delete_confirm') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let ids = Array.from(this.selectRows).map(row => row.id);
              this.$post('/api/delimit/deleteBatch/', ids, () => {
                this.selectRows.clear();
                this.initApiTable();
                this.$success(this.$t('commons.delete_success'));
              });
            }
          }
        });
      },
      handleTestCase(testCase) {
        this.selectApi = testCase;
        this.isHide = false;
      },
      handleDelete(testCase) {
        this.$get('/api/delimit/delete/' + testCase.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.initApiTable();
        });
      },
      apiCaseClose() {
        this.isHide = true;
      },
      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
    }
  }
</script>

<style scoped>
  .operate-button > div {
    display: inline-block;
    margin-left: 10px;
  }

  .request-method {
    padding: 0 5px;
    color: #1E90FF;
  }

  .api-el-tag {
    color: white;
  }
</style>
