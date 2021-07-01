<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <div style="font-size: 16px;margin-bottom: 20px;margin-left: 10px">
          {{$t('operating_log.title')}}
        </div>
        <div>
          <el-form :model="condition" label-position="right" label-width="75px" size="small" ref="basicForm" style="margin-right: 20px">
            <el-row>
              <el-col :span="5">
                <el-form-item :label="$t('operating_log.time')" prop="times">
                  <el-date-picker
                    size="small"
                    v-model="condition.times"
                    type="datetimerange"
                    value-format="timestamp"
                    :range-separator="$t('commons.date.range_separator')"
                    :start-placeholder="$t('schedule.cron.start')"
                    :end-placeholder="$t('variables.end')" style="width: 100%">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item :label="$t('operating_log.user')" prop="user">
                  <el-autocomplete
                    class="input-with-autocomplete"
                    v-model="condition.operUser"
                    :placeholder="$t('member.input_id_or_email')"
                    :trigger-on-focus="false"
                    :fetch-suggestions="querySearch"
                    size="small"
                    highlight-first-item
                    value-key="email"
                    @select="handleSelect">
                    <template v-slot:default="scope">
                      <span class="ws-member-name">{{scope.item.name}}</span>
                      <span class="ws-member-email">{{scope.item.email}}</span>
                    </template>
                  </el-autocomplete>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item :label="$t('commons.project')" prop="project">
                  <el-select size="small" v-model="condition.projectId" @change="initTableData" clearable>
                    <el-option v-for="o in items" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item :label="$t('operating_log.type')" prop="type">
                  <el-select size="small" v-model="condition.operType" clearable @change="initTableData">
                    <el-option v-for="o in LOG_TYPE" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item :label="$t('operating_log.object')" prop="module">
                  <el-cascader v-model="condition.operModules"
                               show-all-levels filterable
                               :options="sysList"
                               :placeholder="$t('operating_log.object')"
                               :props="props"
                               class="ms-case"
                               @change="initTableData" ref="cascade"/>
                </el-form-item>
              </el-col>

              <el-col :span="3">
                <div style="width: 140px">
                  <el-button type="primary" size="small" style="float: right" @click="search">
                    {{ $t('commons.adv_search.search') }}
                  </el-button>
                  <el-button size="small" @click="reset">
                    {{$t('commons.adv_search.reset')}}
                  </el-button>
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
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
    </el-card>

    <ms-log-detail ref="logDetail" :title="$t('report.test_log_details')"/>
  </div>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId, hasRoles} from "@/common/js/utils";
  import {PROJECT_ID, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER, WORKSPACE_ID} from "@/common/js/constants";
  import MsLogDetail from "./LogDetail";

  export default {
    name: "OperatingLog",
    components: {
      MsTablePagination, MsTableOperator, MsLogDetail
    },
    data() {
      return {
        props: {
          multiple: false,
        },
        result: {},
        form: {},
        currentPage: 0,
        pageSize: 10,
        total: 0,
        items: [],
        condition: {},
        tableData: [],
        userList: [],
        screenHeight: 'calc(100vh - 215px)',
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
        ]),
        sysList: [
          {
            label: "测试跟踪", value: "测试跟踪", children: [
              {label: "测试用例", value: "测试用例", leaf: true},
              {label: "用例评审", value: "用例评审", leaf: true},
              {label: "测试计划", value: "测试计划", leaf: true},
              {label: "缺陷管理", value: "缺陷管理", leaf: true},
              {label: "报告", value: "报告", leaf: true}]
          },
          {
            label: "接口测试", value: "api", children: [
              {label: "接口定义", value: "接口定义", leaf: true},
              {label: "接口自动化", value: "接口自动化", leaf: true},
              {label: "测试报告", value: "测试报告", leaf: true}]
          },
          {
            label: "性能测试", value: "性能测试", children: [
              {label: "性能测试", value: "性能测试", leaf: true},
              {label: "性能测试报告", value: "性能测试报告", leaf: true}]
          },
          {
            label: "系统设置", value: "系统设置", children: [
              {label: "系统-用户", value: "系统-用户", leaf: true},
              {label: "系统-组织", value: "系统-组织", leaf: true},
              {label: "工作空间", value: "工作空间", leaf: true},
              {label: "系统-测试资源池", value: "系统-测试资源池", leaf: true},
              {label: "系统-系统参数设置", value: "系统-系统参数设置", leaf: true},
              {label: "系统-配额管理", value: "系统-配额管理", leaf: true},
              {label: "系统-授权管理", value: "系统-授权管理", leaf: true},
              {label: "组织-成员", value: "组织-成员", leaf: true},
              {label: "组织-服务集成", value: "组织-服务集成", leaf: true},
              {label: "组织-消息设置", value: "组织-消息设置", leaf: true},

              {label: "工作空间-成员", value: "工作空间-成员", leaf: true},
              {label: "项目-项目管理", value: "项目-项目管理", leaf: true},
              {label: "工作空间-模版设置", value: "工作空间-模版设置", leaf: true},
              {label: "工作空间-项目管理", value: "工作空间-项目管理", leaf: true},
              {label: "项目-项目管理", value: "项目-项目管理", leaf: true},
              {label: "项目-成员", value: "项目-成员", leaf: true},
              {label: "工作空间-成员", value: "工作空间-成员", leaf: true},

              {label: "項目-JAR包管理", value: "項目-JAR包管理", leaf: true},
              {label: "项目-环境设置", value: "项目-环境设置", leaf: true},
              {label: "项目-文件管理", value: "项目-文件管理", leaf: true},
              {label: "个人信息-个人设置", value: "个人信息-个人设置", leaf: true},
              {label: "个人信息-API Keys", value: "个人信息-API Keys", leaf: true}
            ]
          },
        ],
      }
    },
    mounted() {
      switch (this.$route.name) {
        case "system":
          this.initProject("/project/listAll");
          this.getMember();
          break;
        case "organization":
          this.initProject("/project/listAll/" + getCurrentWorkspaceId());
          this.getMember();
          break;
        case "workspace":
          this.initProject("/project/listAll/" + getCurrentWorkspaceId());
          this.getMember();
          break;
        case "project":
          this.getProject();
          this.getMember();
          break;
      }
    },
    watch: {
      '$route'(to, from) {
        switch (to.name) {
          case "system":
            this.initProject("/project/listAll");
            this.getMember();
            break;
          case "organization":
            this.initProject("/project/listAll/" + getCurrentWorkspaceId());
            this.getMember();
            break;
          case "workspace":
            this.initProject("/project/listAll/" + getCurrentWorkspaceId());
            this.getMember();
            break;
          case "project":
            this.getProject();
            this.getMember();
            break;
        }
      }
    },
    methods: {
      handleSelect(item) {
        this.$set(this.condition, "operUser", item.id);
      },
      getMember() {
        this.result = this.$get('/user/list/', response => {
          this.userList = response.data;
        });
      },
      createFilter(queryString) {
        return (user) => {
          return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
        };
      },
      querySearch(queryString, cb) {
        let userList = this.userList;
        let results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      initTableData() {
        if (this.condition.operModules && this.condition.operModules.length > 0) {
          this.condition.operModule = this.condition.operModules[1];
        }
        let url = "/operating/log/list/" + this.currentPage + "/" + this.pageSize;
        this.result.loading = true;
        this.$post(url, this.condition, response => {
          this.tableData = response.data.listObject;
          this.total = response.data.itemCount;
          this.result.loading = false;
        })

      },

      reset() {
        let projectIds = this.condition.projectIds;
        this.condition = {projectIds: projectIds};
        this.initTableData();
      },
      initProject(url) {
        this.condition = {};
        this.result = this.$get(url, response => {
          let projects = response.data;
          let projectIds = [];
          if (projects) {
            this.items = [];
            projects.forEach(item => {
              let data = {id: item.id, label: item.name};
              this.items.push(data);
              projectIds.push(item.id);
            })
          }
          this.condition.projectIds = projectIds;
          this.initTableData();
        })
      },
      getProject() {
        this.condition.projectIds = [];
        this.result = this.$get("/project/get/" + getCurrentProjectID(), response => {
          let project = response.data;
          this.items = [{id: project.id, label: project.name}];
          this.condition.projectIds = [project.id];
          this.condition.projectId = project.id;
          this.initTableData();
        });
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
