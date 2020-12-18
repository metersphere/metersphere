<template>
  <div id="svgBox" style="overflow: auto">
    <div id="svgTop" style="background-color: white">
      <el-card class="card-content">
        <el-input placeholder="搜索" @blur="search" style="float: right ;width: 300px;margin-bottom: 20px;margin-right: 20px" size="small" v-model="condition.name"/>

        <el-table border :data="tableData" row-key="id" class="test-content adjust-table"
                  @select-all="handleSelectAll"
                  @select="handleSelect" :height="screenHeight">
          <el-table-column type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <template v-slot:default="scope">
              <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
            </template>
          </el-table-column>

          <el-table-column prop="name" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>
          <el-table-column
            prop="status"
            column-key="api_status"
            :label="$t('api_test.definition.api_status')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <ms-tag v-if="scope.row.status == 'Prepare'" type="info" effect="plain" :content="$t('test_track.plan.plan_status_prepare')"/>
              <ms-tag v-if="scope.row.status == 'Underway'" type="warning" effect="plain" :content="$t('test_track.plan.plan_status_running')"/>
              <ms-tag v-if="scope.row.status == 'Completed'" type="success" effect="plain" :content="$t('test_track.plan.plan_status_completed')"/>
              <ms-tag v-if="scope.row.status == 'Trash'" type="danger" effect="plain" content="废弃"/>
            </template>
          </el-table-column>

          <el-table-column
            prop="method"
            :label="$t('api_test.definition.api_type')"
            show-overflow-tooltip>
            <template v-slot:default="scope" class="request-method">
              <el-tag size="mini" :style="{'background-color': getColor(true, scope.row.method)}" class="api-el-tag">
                {{ scope.row.method}}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            prop="path"
            :label="$t('api_test.definition.api_path')"
            show-overflow-tooltip/>

          <el-table-column
            prop="userName"
            :label="$t('api_test.definition.api_principal')"
            show-overflow-tooltip/>

          <el-table-column width="160" :label="$t('api_test.definition.api_last_time')" prop="updateTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="caseTotal"
            :label="$t('api_test.definition.api_case_number')"
            show-overflow-tooltip/>

          <el-table-column
            prop="caseStatus"
            :label="$t('api_test.definition.api_case_status')"
            show-overflow-tooltip/>

          <!--          <el-table-column
                      prop="casePassingRate"
                      :label="$t('api_test.definition.api_case_passing_rate')"
                      show-overflow-tooltip/>-->

          <el-table-column :label="$t('commons.operating')" min-width="80" align="center">
            <template v-slot:default="scope">
              <el-button type="text" @click="handleTestCase(scope.row)">用例</el-button>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </div>
    <div id="svgResize"/>
    <div id="svgDown">
      <ms-bottom-container v-bind:enableAsideHidden="isHide">
        <ms-api-case-list @apiCaseClose="apiCaseClose" @refresh="initTable" :visible="visible" :currentRow="currentRow" :api="selectApi"/>
      </ms-bottom-container>
    </div>
  </div>

</template>

<script>

  import MsTableHeader from '../../../../../components/common/components/MsTableHeader';
  import MsTableOperator from "../../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../../common/components/MsTableButton";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../../common/pagination/TablePagination";
  import MsTag from "../../../../common/components/MsTag";
  import MsApiCaseList from "./ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsBottomContainer from "../../../definition/components/BottomContainer";
  import ShowMoreBtn from "../../../../../components/track/case/components/ShowMoreBtn";
  import {API_METHOD_COLOUR} from "../../../definition/model/JsonData";
  import {getCurrentProjectID} from "@/common/js/utils";

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
        buttons: [{name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch}],
        methodColorMap: new Map(API_METHOD_COLOUR),
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        projectId: "",
        screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
      }
    },
    props: {
      currentProtocol: String,
      currentModule: Object,
      visible: {
        type: String,
      },
      currentRow: {
        type: Object,
      }
    },
    created: function () {
      this.projectId = getCurrentProjectID();
      this.initTable();
    },
    mounted() {
      this.dragControllerDiv();
    },
    watch: {
      currentModule() {
        this.initTable();
        this.apiCaseClose();
      },
      visible() {
        this.initTable();
        this.apiCaseClose();
      },
      currentProtocol() {
        this.initTable();
        this.apiCaseClose();
      },
    },
    methods: {
      initTable() {
        this.selectRows = new Set();
        this.condition.filters = ["Prepare", "Underway", "Completed"];
        if (this.currentModule) {
          this.condition.moduleIds = [this.currentModule.id];
        }
        if (this.trashEnable) {
          this.condition.filters = ["Trash"];
          this.condition.moduleIds = [];
        }
        if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }
        if (this.currentProtocol != null) {
          this.condition.protocol = this.currentProtocol;
        }
        this.result = this.$post("/api/definition/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      },
      handleSelect(selection, row) {
        row.hashTree = [];
        if (this.selectRows.has(row)) {
          this.$set(row, "showMore", false);
          this.selectRows.delete(row);
        } else {
          this.$set(row, "showMore", true);
          this.selectRows.add(row);
        }
        let arr = Array.from(this.selectRows);
        if (this.currentRow) {
          this.currentRow.apis = arr;
        }
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
            selection.hashTree = [];
            this.selectRows.add(selection[0]);
          } else {
            this.tableData.forEach(item => {
              item.hashTree = [];
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
        if (this.currentRow) {
          let arr = Array.from(this.selectRows);
          this.currentRow.apis = arr;
        }
      },
      search() {
        this.initTable();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      editApi(row) {
        this.$emit('editApi', row);
      },
      reductionApi(row) {
        row.status = 'Underway';
        row.request = null;
        row.response = null;
        this.$fileUpload("/api/definition/update", null, [], row, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      },
      handleDeleteBatch() {
        if (this.currentModule != undefined && this.currentModule.id == "gc") {
          this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/deleteBatch/', ids, () => {
                  this.selectRows.clear();
                  this.initTable();
                  this.$success(this.$t('commons.delete_success'));
                });
              }
            }
          });
        } else {
          this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/removeToGc/', ids, () => {
                  this.selectRows.clear();
                  this.initTable();
                  this.$success(this.$t('commons.delete_success'));
                });
              }
            }
          });
        }
      },
      handleTestCase(testCase) {
        let h = window.screen.height;
        let svgTop = document.getElementById("svgTop");
        svgTop.style.height = h / 2 - 200 + "px";

        let svgDown = document.getElementById("svgDown");
        svgDown.style.height = h / 2 + "px";

        this.selectApi = testCase;
        let request = JSON.parse(testCase.request);
        this.selectApi.url = request.path;
        this.isHide = false;
      },
      handleDelete(api) {
        if (this.currentModule != undefined && this.currentModule.id == "gc") {
          this.$get('/api/definition/delete/' + api.id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.initTable();
          });
          return;
        }
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + api.name + " ？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let ids = [api.id];
              this.$post('/api/definition/removeToGc/', ids, () => {
                this.$success(this.$t('commons.delete_success'));
                this.initTable();
              });
            }
          }
        });
      },
      apiCaseClose() {
        this.selectApi = {};
        let h = window.screen.height;
        let svgTop = document.getElementById("svgTop");
        svgTop.style.height = h - 200 + "px";

        let svgDown = document.getElementById("svgDown");
        svgDown.style.height = 0 + "px";
        this.isHide = true;
      },
      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
      dragControllerDiv: function () {
        let svgResize = document.getElementById("svgResize");
        let svgTop = document.getElementById("svgTop");
        let svgDown = document.getElementById("svgDown");
        let svgBox = document.getElementById("svgBox");
        svgResize.onmousedown = function (e) {
          let startY = e.clientY;
          svgResize.top = svgResize.offsetTop;
          document.onmousemove = function (e) {
            let endY = e.clientY;
            let moveLen = svgResize.top + (endY - startY);
            let maxT = svgBox.clientHeight - svgResize.offsetHeight;
            if (moveLen < 30) moveLen = 30;
            if (moveLen > maxT - 30) moveLen = maxT - 30;
            svgResize.style.top = moveLen;
            svgTop.style.height = moveLen + "px";
            svgDown.style.height = (svgBox.clientHeight - moveLen - 5) + "px";
          }
          document.onmouseup = function (evt) {
            document.onmousemove = null;
            document.onmouseup = null;
            svgResize.releaseCapture && svgResize.releaseCapture();
          }
          svgResize.setCapture && svgResize.setCapture();
          return false;
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

  #svgBox {
    width: 100%;
    height: 100%;
    position: relative;
    overflow: hidden;
  }

  #svgTop {
    height: calc(30% - 5px);
    width: 100%;
    float: left;
    overflow: auto;
  }

  #svgResize {
    position: relative;
    height: 5px;
    width: 100%;
    cursor: s-resize;
    float: left;
  }

  #svgDown {
    height: 70%;
    width: 100%;
    float: left;
    overflow: hidden;
  }
</style>
