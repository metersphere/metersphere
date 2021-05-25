<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <div style="font-size: 16px;margin-bottom: 20px;margin-left: 10px">
          {{$t('operating_log.title')}}
        </div>
        <div>
          <el-form :model="condition" label-position="right" label-width="80px" size="small" ref="basicForm" style="margin-right: 20px">
            <el-row>
              <el-col :span="6">
                <el-form-item :label="$t('operating_log.time')" prop="times">
                  <el-date-picker
                    size="small"
                    v-model="condition.times"
                    type="datetimerange"
                    value-format="timestamp"
                    :range-separator="$t('commons.date.range_separator')"
                    :start-placeholder="$t('commons.date.start_date')"
                    :end-placeholder="$t('commons.date.end_date')" style="width: 100%">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item :label="$t('operating_log.user')" prop="user">
                  <el-input size="small" v-model="condition.operUser"/>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item :label="$t('commons.project')" prop="project">
                  <el-select size="small" v-model="condition.projectId" clearable>
                    <el-option v-for="o in items" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item :label="$t('operating_log.type')" prop="type">
                  <el-select size="small" v-model="condition.operType" clearable>
                    <el-option v-for="o in LOG_TYPE" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="3">
                <el-form-item :label="$t('operating_log.object')" prop="module">
                  <el-input size="small" v-model="condition.operModule"/>
                </el-form-item>
              </el-col>

              <el-col :span="3">
                <div style="width: 140px">
                  <el-button type="primary" size="small" style="float: right" @click="search">
                    {{ $t('commons.adv_search.search') }}
                  </el-button>
                  <el-button size="small" @click="reset">{{$t('commons.adv_search.reset')}}</el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </div>

      </template>
      <el-table border class="adjust-table" :data="tableData" ref="operLog"
                :height="screenHeight">
        <el-table-column prop="operTime" :label="$t('operating_log.time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.operTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" :label="$t('operating_log.user')"/>
        <el-table-column prop="projectName" :label="$t('commons.project')"/>
        <el-table-column prop="operType" :label="$t('operating_log.type')">
          <template v-slot:default="scope">
            <span>{{ getType(scope.row.operType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operModule" :label="$t('operating_log.object')" show-overflow-tooltip width="120px"/>
        <el-table-column prop="operTitle" :label="$t('operating_log.name')" show-overflow-tooltip width="150px"/>
        <el-table-column :label="$t('report.test_log_details')">
          <template v-slot:default="scope">
            <el-link style="color: #409EFF" @click="openDetail(scope.row)">{{$t('operating_log.info')}}</el-link>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <ms-log-detail ref="logDetail" :title="$t('report.test_log_details')"/>
  </div>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import {getCurrentProjectID, getCurrentUser, hasRoles} from "@/common/js/utils";
  import {PROJECT_ID, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER, WORKSPACE_ID} from "@/common/js/constants";
  import MsLogDetail from "./LogDetail";

  export default {
    name: "OperatingLog",
    components: {
      MsTablePagination, MsTableOperator, MsLogDetail
    },
    data() {
      return {
        result: {},
        form: {},
        currentPage: 0,
        pageSize: 10,
        total: 0,
        items: [],
        condition: {},
        tableData: [],
        screenHeight: 'calc(100vh - 275px)',
        LOG_TYPE: [
          {id: 'CREATE', label: this.$t('api_test.definition.request.create_info')},
          {id: 'DELETE', label: this.$t('commons.delete')},
          {id: 'UPDATE', label: this.$t('commons.update')},
          {id: 'IMPORT', label: this.$t('api_test.api_import.label')},
          {id: 'EXPORT', label: this.$t('commons.export')},
          {id: 'ASSOCIATE_CASE', label: this.$t('test_track.review_view.relevance_case')},
          {id: 'ASSOCIATE_ISSUE', label: this.$t('test_track.case.relate_issue')},
          {id: 'UN_ASSOCIATE_CASE', label: this.$t('test_track.case.unlink')},
          {id: 'REVIEW', label: this.$t('test_track.review_view.start_review')},
          {id: 'COPY', label: this.$t('commons.copy')},
          {id: 'EXECUTE', label: this.$t('api_test.automation.execute')},
          {id: 'CREATE_PRE_TEST', label: this.$t('api_test.create_performance_test')},
          {id: 'SHARE', label: this.$t('operating_log.share')},
          {id: 'LOGIN', label: this.$t('commons.login')},
          {id: 'RESTORE', label: this.$t('commons.reduction')},
          {id: 'DEBUG', label: this.$t('api_test.request.debug')},
          {id: 'GC', label: this.$t('api_test.automation.trash')},
          {id: 'BATCH_DEL', label: this.$t('api_test.definition.request.batch_delete')},
          {id: 'BATCH_UPDATE', label: this.$t('api_test.definition.request.batch_edit')},
          {id: 'BATCH_ADD', label: this.$t('commons.batch_add')},
          {id: 'BATCH_RESTORE', label: "批量恢复"},
          {id: 'BATCH_GC', label: "批量回收"}
        ],
        LOG_TYPE_MAP: new Map([
          ['CREATE', this.$t('api_test.definition.request.create_info')],
          ['DELETE', this.$t('commons.delete')],
          ['UPDATE', this.$t('commons.update')],
          ['IMPORT', this.$t('api_test.api_import.label')],
          ['EXPORT', this.$t('commons.export')],
          ['ASSOCIATE_CASE', this.$t('test_track.review_view.relevance_case')],
          ['ASSOCIATE_ISSUE', this.$t('test_track.case.relate_issue')],
          ['UN_ASSOCIATE_CASE', this.$t('test_track.case.unlink')],
          ['REVIEW', this.$t('test_track.review_view.start_review')],
          ['COPY', this.$t('commons.copy')],
          ['EXECUTE', this.$t('api_test.automation.execute')],
          ['CREATE_PRE_TEST', this.$t('api_test.create_performance_test')],
          ['SHARE', this.$t('operating_log.share')],
          ['LOGIN', this.$t('commons.login')],
          ['RESTORE', this.$t('commons.reduction')],
          ['DEBUG', this.$t('api_test.request.debug')],
          ['GC', this.$t('api_test.automation.trash')],
          ['BATCH_DEL', this.$t('api_test.definition.request.batch_delete')],
          ['BATCH_UPDATE', this.$t('api_test.definition.request.batch_edit')],
          ['BATCH_ADD', this.$t('commons.batch_add')],
          ['BATCH_RESTORE', "批量恢复"],
          ['BATCH_GC', "批量回收"],
        ])
      }
    },
    created() {
      this.initTableData();
      this.initProject();
    },
    methods: {
      initTableData() {
        let url = "/operating/log/list/" + this.currentPage + "/" + this.pageSize;
        this.result.loading = true;
        this.$post(url, this.condition, response => {
          this.tableData = response.data.listObject;
          this.total = response.data.itemCount;
          this.result.loading = false;
        })
      },
      reset() {
        this.condition = {};
      },
      initProject() {
        if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.result = this.$get("/project/listAll", response => {
            let projects = response.data;
            if (projects) {
              this.items = [];
              projects.forEach(item => {
                let data = {id: item.id, label: item.name};
                this.items.push(data);
              })
            }
          })
        }
      },
      getType(type) {
        return this.LOG_TYPE_MAP.get(type);
      },
      search() {
        this.initTableData();
      },
      openDetail(row) {
        this.$refs.logDetail.open(row.id);
      },
    }
  }
</script>

<style scoped>

</style>
