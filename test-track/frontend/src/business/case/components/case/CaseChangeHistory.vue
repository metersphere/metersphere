<template>
  <!-- <el-dialog
    :close-on-click-modal="false"
    :title="$t('operating_log.change_history')"
    :visible.sync="infoVisible"
    width="900px"
    :destroy-on-close="true"
    append-to-body
    @close="handleClose"
    v-loading="loading"
  >

  </el-dialog> -->
  <div class="wrap">
    <ms-table :data="details" :enableSelection="false">
      <ms-table-column
        prop="operTime"
        :label="$t('operating_log.time')"
        :sortable="true"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.operTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>
      <ms-table-column prop="userName" :label="$t('operating_log.user')" />
      <ms-table-column
        prop="columnTitle"
        :label="$t('operating_log.change_field')"
        v-if="showChangeField"
      >
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">
              {{ detail.columnTitle }}
            </div>
          </div>
        </template>
      </ms-table-column>
      <ms-table-column
        prop="originalValue"
        :label="$t('case.content_before_change')"
      >
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">
              <div v-if="linkDatas.indexOf(detail.columnName) !== -1">
                <el-link
                  style="color: #409eff"
                  @click="openDetail(scope.row, detail)"
                  >{{ $t("operating_log.info") }}</el-link
                >
              </div>
              <el-tooltip :content="detail.originalValue" v-else>
                <div class="current-value">
                  {{
                    detail.originalValue
                      ? detail.originalValue
                      : $t("case.empty_tip")
                  }}
                </div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </ms-table-column>
      <ms-table-column prop="newValue" :label="$t('case.content_after_change')">
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">
              <div v-if="linkDatas.indexOf(detail.columnName) !== -1">
                <el-link
                  style="color: #409eff"
                  @click="openDetail(scope.row, detail)"
                  >{{ $t("operating_log.info") }}
                </el-link>
              </div>
              <el-tooltip :content="detail.newValue" v-else>
                <div class="current-value">
                  {{ detail.newValue ? detail.newValue : $t("case.empty_tip") }}
                </div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </ms-table-column>
    </ms-table>
    <!-- <ms-table-pagination
      :change="getDetails"
      :current-page.sync="goPage"
      :page-size.sync="pageSize"
      :total="totalCount"
    /> -->
    <home-pagination
      class="change-his-pagination"
      :change="getDetails"
      :current-page.sync="goPage"
      :page-size.sync="pageSize"
      :total="totalCount"
      layout="total, prev, pager, next, sizes, jumper"
    />

    <ms-history-detail ref="historyDetail" />
    <ms-tags-history-detail ref="tagsHistoryDetail" />
  </div>
</template>
<script>
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import MsHistoryDetail from "metersphere-frontend/src/components/history/HistoryDetail";
import MsTagsHistoryDetail from "metersphere-frontend/src/components/history//tags/TagsHistoryDetail";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import { getOperatingLogForResource } from "metersphere-frontend/src//api/history";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";

export default {
  name: "CaseChangeHistory",
  components: {
    MsHistoryDetail,
    MsTagsHistoryDetail,
    MsTablePagination,
    MsTable,
    MsTableColumn,
    HomePagination,
  },
  props: {
    title: String,
    caseId: String,
  },
  data() {
    return {
      infoVisible: false,
      loading: false,
      details: [],
      linkDatas: [
        "prerequisite",
        "steps",
        "remark",
        "request",
        "config",
        "response",
        "scenarioDefinition",
        "tags",
        "loadConfiguration",
        "advancedConfiguration",
      ],
      showChangeField: true,
      pageSize: 10,
      goPage: 1,
      totalCount: 0,
      id: String,
      module: String,
    };
  },
  methods: {
    openHit(){
      this.open(this.caseId, [
        "测试用例",
        "測試用例",
        "Test case",
        "TRACK_TEST_CASE",
      ]);
    },
    handleClose() {
      this.infoVisible = false;
    },
    getDetails(id, modules) {
      id = this.id;
      modules = this.module;
      getOperatingLogForResource(this.goPage, this.pageSize, {
        sourceId: id,
        modules: modules,
      }).then((response) => {
        let data = response.data.listObject;
        this.totalCount = response.data.itemCount;
        if (data) {
          //过滤接口定义请求参数为空的数据
          if (modules.length > 0 && modules[0] === "接口定义") {
            for (let i = 0; i < data.length; i++) {
              if (
                data[i].details.columns &&
                data[i].details.columns.length > 0
              ) {
                let columns = data[i].details.columns;
                for (let j = 0; j < columns.length; j++) {
                  if (
                    columns[j].columnName === "request" &&
                    (columns[j].diffValue === null ||
                      columns[j].diffValue === "")
                  ) {
                    data[i].details.columns.splice(j, 1);
                  }
                }
              }
            }
          }
          // 过滤非全局脚本历史变更数据
          if (modules.length > 0 && modules[0] === "项目-环境设置") {
            // 环境设置不显示变更字段
            this.showChangeField = false;
            // 不显示的节点 id
            let ids = [];
            for (let i = 0; i < data.length; i++) {
              if (
                data[i].details.columns.findIndex(
                  (d) => d.diffValue === null || d.diffValue === ""
                ) !== -1
              ) {
                ids.push(data[i].id);
                continue;
              }
            }
            if (ids.length > 0) {
              ids.forEach((row) => {
                const index = data.findIndex((d) => d.id === row);
                data.splice(index, 1);
              });
            }
          } else {
            this.showChangeField = true;
          }
          this.details = data;
        }
      });
    },
    open(id, modules) {
      this.infoVisible = true;
      this.id = id;
      this.module = modules;
      this.getDetails(id, modules);
    },
    openDetail(row, value) {
      value.createUser = row.details.createUser;
      value.operTime = row.operTime;
      if (value.columnName === "tags") {
        this.$refs.tagsHistoryDetail.open(value);
      } else if (
        (value.columnName === "request" || value.columnName === "response") &&
        (row.operModule === "接口定义" ||
          row.operModule === "接口定義" ||
          row.operModule === "Api definition" ||
          row.operModule === "API_DEFINITION")
      ) {
        this.$refs.apiHistoryDetail.open(value);
      } else if (
        row.operModule === "项目-环境设置" ||
        row.operModule === "項目-環境設置" ||
        row.operModule === "PROJECT_ENVIRONMENT_SETTING"
      ) {
        this.$refs.environmentHistoryDetail.open(value);
      } else {
        try {
          value.newValue = JSON.parse(value.newValue);
          value.originalValue = JSON.parse(value.originalValue);
        } catch (e) {
          // console.info(e);
        }
        this.$refs.historyDetail.open(value);
      }
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.current-value {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 120px;
}
.wrap {
  padding: 24px 0 0 0;
}
.wrap :deep(.ms-table) {
  height: px2rem(350) !important;
  max-height: px2rem(536) !important;
}

.wrap .change-his-pagination {
  padding-top: 24px!important;
}
</style>
