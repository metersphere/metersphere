<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card class="table-card">
          <template v-slot:header>
            <div>
              <el-form :model="condition" label-position="right" label-width="75px" size="small" ref="basicForm"
                       style="margin-right: 20px">
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
                          <span class="ws-member-name">{{ scope.item.name }}</span>
                          <span class="ws-member-email">{{ scope.item.email }}</span>
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
                        {{ $t('commons.adv_search.reset') }}
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
            <el-table-column prop="operType" :label="$t('operating_log.type')" width="100px">
              <template v-slot:default="scope">
                <span>{{ getType(scope.row.operType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operModule" :label="$t('operating_log.object')" show-overflow-tooltip width="120px"/>
            <el-table-column prop="operTitle" :label="$t('operating_log.name')" :show-overflow-tooltip="true" width="180px">
              <template v-slot:default="scope">
                <el-link v-if="isLink(scope.row)" style="color: #409EFF" @click="clickResource(scope.row)">
                  {{ scope.row.operTitle }}
                </el-link>
                <span v-else>{{ scope.row.operTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('report.test_log_details')" width="100px">
              <template v-slot:default="scope">
                <el-link style="color: #409EFF" @click="openDetail(scope.row)">{{ $t('operating_log.info') }}</el-link>
              </template>
            </el-table-column>
          </el-table>
          <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                               :total="total"/>
        </el-card>

        <ms-log-detail ref="logDetail" :title="$t('report.test_log_details')"/>
      </div>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import {getCurrentProjectID} from "@/common/js/utils";
import {getUrl, LOG_TYPE, LOG_TYPE_MAP, sysList} from "@/business/components/settings/operatinglog/config";
import MsLogDetail from "@/business/components/settings/operatinglog/LogDetail";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";

export default {
  name: "Log",
  components: {
    MsMainContainer,
    MsTablePagination, MsTableOperator, MsLogDetail,
    MsContainer
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
      LOG_TYPE: new LOG_TYPE(this),
      LOG_TYPE_MAP: new LOG_TYPE_MAP(this),
      sysList: sysList,
    }
  },
  mounted() {
    this.getProject();
    this.getMember();
  },
  methods: {
    isLink(row) {
      let uri = getUrl(row);
      if ((row.operType === 'UPDATE' || row.operType === 'CREATE' || row.operType === 'EXECUTE' || row.operType === 'DEBUG') && uri !== "/#") {
        return true;
      }
      return false;
    },
    clickResource(resource) {
      let uri = getUrl(resource);
      if (!resource.sourceId) {
        this.toPage(uri);
      }
      let operModule = resource.operModule;
      if (operModule === "系统-系统参数设置" || operModule === "系统-系統參數設置" || operModule === "System parameter setting") {
        this.toPage(uri);
      } else {
        let resourceId = resource.sourceId;
        if (resourceId && resourceId.startsWith("\"" || resourceId.startsWith("["))) {
          resourceId = JSON.parse(resource.sourceId);
        }
        if (resourceId instanceof Array) {
          resourceId = resourceId[0];
        }
        this.$get('/user/update/currentByResourceId/' + resourceId, () => {
          this.toPage(uri);
        });
      }
    },
    toPage(uri) {
      let id = "new_a";
      let a = document.createElement("a");
      a.setAttribute("href", uri);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    },
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
      this.condition.projectIds = [getCurrentProjectID()];
      this.condition.projectId = getCurrentProjectID();
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
      this.condition = {projectIds: projectIds, projectId: getCurrentProjectID()};
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
