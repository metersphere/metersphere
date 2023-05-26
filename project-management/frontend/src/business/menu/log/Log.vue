<template>
  <ms-container>
    <ms-main-container>
      <div>
        <el-card class="table-card" v-loading="cardLoading">
          <template v-slot:header>
            <div>
              <el-form :model="condition" label-position="right" label-width="75px" size="small" ref="basicForm"
                       style="margin-right: 20px">
                <el-row>
                  <el-col :span="6">
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
                  <el-col :span="6">
                    <el-form-item :label="$t('operating_log.user')" prop="user">
                      <el-select
                        v-model="condition.operUser"
                        filterable
                        remote
                        clearable
                        size="small"
                        style="width: 100%"
                        @visible-change="visibleChange"
                        reserve-keyword
                        :placeholder="$t('member.input_id_or_email_or_name')"
                        :remote-method="querySearch"
                        @change="initTableData"
                        :loading="selectLoading">
                        <el-option
                          v-for="item in options"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id">
                          <span class="ws-member-name">{{ item.name }} &nbsp;&nbsp;</span>
                          <span class="ws-member-email">{{ item.email }}</span>
                        </el-option>
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
                                   clearable
                                   @change="initTableData(condition.operModules)" ref="cascade"/>
                    </el-form-item>
                  </el-col>

                  <el-col :span="4">
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
                <span>{{ scope.row.operTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="userName" :label="$t('operating_log.user')"/>
            <el-table-column prop="projectName" :label="$t('commons.project')"/>
            <el-table-column prop="operType" :label="$t('operating_log.type')" width="100px">
              <template v-slot:default="scope">
                <span>{{ getType(scope.row.operType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operModule" :label="$t('operating_log.object')" show-overflow-tooltip width="120px">
              <template v-slot:default="scope">
                <span>{{ getModule(scope.row.operModule) }}</span>
              </template>
            </el-table-column>
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
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {
  getPermissionUrl,
  LOG_MODULE_MAP,
  LOG_TYPE,
  LOG_TYPE_MAP,
  PROJECTSYSLIST
} from "./config";
import MsLogDetail from "./LogDetail";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import {getCurrentWsMembers} from "../../../api/user";
import {modifyUserByResourceId} from "metersphere-frontend/src/api/user";
import {getOperatingLogPages} from "../../../api/log";
import {getProject} from "../../../api/project";
import {hasLicense, hasPermissions} from "metersphere-frontend/src/utils/permission";

export default {
  name: "Log",
  components: {
    MsMainContainer,
    MsTablePagination, MsTableOperator, MsLogDetail,
    MsContainer
  },
  data() {
    let getModules = function () {
      let license = hasLicense();
      let data = new PROJECTSYSLIST();
      return data.filter(item => {
        if (!item.license) {
          if (item.children) {
            item.children = item.children.filter(child => {
              if (!child.license || (child.license && license)) {
                return true;
              }
            })
          }
          return true;
        } else {
          if (license) {
            return true;
          }
        }
      });
    };
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
      condition: {
        times: [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()],
      },
      tableData: [],
      userList: [],
      screenHeight: 'calc(100vh - 175px)',
      LOG_TYPE: new LOG_TYPE(this),
      LOG_TYPE_MAP: new LOG_TYPE_MAP(this),
      LOG_MODULE_MAP: new LOG_MODULE_MAP(this),
      sysList: getModules(),
      options: [],
      selectLoading: false,
      cardLoading: false
    }
  },
  mounted() {
    this.getProject();
    this.getMember();
  },
  methods: {
    isLink(row) {
      let res = getPermissionUrl(row, this);
      let permissions = undefined;
      let uri = "/#";
      if (res && res instanceof Array) {
        uri = res[0];
        permissions = res[1];
      }
      if ((row.operType === 'UPDATE' || row.operType === 'CREATE' || row.operType === 'EXECUTE' || row.operType === 'DEBUG') && uri !== "/#") {
        if (permissions && permissions instanceof Array) {
          return hasPermissions(...permissions);
        }
        return true;
      }
      return false;
    },
    clickResource(resource) {
      let res = getPermissionUrl(resource, this);
      let uri = res[0];
      if (!resource.sourceId) {
        this.toPage(uri);
      }
      let operModule = resource.operModule;
      let module = this.getLogModule(operModule);
      if (module === "系统-系统参数设置" || module === "系统-系統參數設置" || module === "System parameter setting") {
        this.toPage(uri);
      } else {
        let resourceId = resource.sourceId;
        if (resourceId && resourceId.startsWith("\"" || resourceId.startsWith("["))) {
          resourceId = JSON.parse(resource.sourceId);
        }
        if (resourceId instanceof Array) {
          resourceId = resourceId[0];
        }
        modifyUserByResourceId(resourceId).then(() => {
          this.toPage(uri);
        });
      }
    },
    getLogModule(val) {
      return this.LOG_MODULE_MAP.get(val) ? this.LOG_MODULE_MAP.get(val) : val;
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
      this.cardLoading = getCurrentWsMembers().then(response => {
        this.userList = response.data;
      });
    },
    createFilter(queryString) {
      return (user) => {
        return (user.email.indexOf(queryString.toLowerCase()) === 0
          || user.id.indexOf(queryString.toLowerCase()) === 0
          || (user.name && user.name.indexOf(queryString) === 0));
      };
    },
    querySearch(query) {
      if (query !== '') {
        this.selectLoading = true;
        setTimeout(() => {
          this.selectLoading = false;
          this.options = this.userList.filter(this.createFilter(query));
        }, 300);
      } else {
        this.options = [];
      }
    },
    visibleChange(val) {
      if (!val) {
        this.querySearch('');
      }
    },
    initTableData() {
      if (this.condition.operModules && this.condition.operModules.length > 0) {
        this.condition.operModule = this.condition.operModules[1];
      } else {
        this.condition.operModule = undefined;
      }
      this.condition.projectIds = [getCurrentProjectID()];
      this.condition.projectId = getCurrentProjectID();
      this.cardLoading = getOperatingLogPages(this.currentPage, this.pageSize, this.condition).then(response => {
        this.tableData = response.data.listObject;
        this.total = response.data.itemCount;
      });
    },
    reset() {
      let projectIds = this.condition.projectIds;
      this.condition = {projectIds: projectIds, projectId: getCurrentProjectID(), times: [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()]};
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
      this.cardLoading = getProject(getCurrentProjectID()).then(response => {
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
    getModule(val) {
      return this.LOG_MODULE_MAP.get(val) ? this.LOG_MODULE_MAP.get(val) : val;
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
