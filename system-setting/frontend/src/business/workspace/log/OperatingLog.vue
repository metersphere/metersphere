<template>
  <div>
    <el-card class="table-card">
      <template v-slot:header>
        <div style="font-size: 16px;margin-bottom: 20px;margin-left: 10px">
          {{ $t('operating_log.title') }}
        </div>
        <div>
          <el-form :model="condition" label-position="right" label-width="auto" size="small" ref="basicForm">
            <el-row>
              <el-col :span="8">
                <el-form-item :label="$t('operating_log.time')" prop="times">
                  <el-date-picker
                    size="small"
                    v-model="condition.times"
                    type="datetimerange"
                    value-format="timestamp"
                    :range-separator="$t('commons.date.range_separator')"
                    :start-placeholder="$t('schedule.cron.start')"
                    :end-placeholder="$t('variables.end')" style="width: 95%">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="$t('operating_log.user')" prop="user">
                  <el-select
                    v-model="condition.operUser"
                    filterable
                    remote
                    clearable
                    size="small"
                    style="width: 95%"
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

              <el-col :span="8" v-if="isSystem">
                <el-form-item :label="$t('commons.workspace')" prop="workspace">
                  <el-select size="small" v-model="condition.workspaceId" @change="initTableData" clearable filterable
                             style="width: 100%">
                    <el-option v-for="o in workspaceList" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="8" v-if="!isSystem">
                <el-form-item :label="$t('commons.project')" prop="project">
                  <el-select size="small" v-model="condition.projectId" @change="initTableData" clearable
                             style="width: 100%">
                    <el-option v-for="o in items" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

            </el-row>
            <el-row>
              <el-col :span="8">
                <el-form-item :label="$t('operating_log.type')" prop="type">
                  <el-select size="small" v-model="condition.operType" clearable @change="initTableData"
                             style="width: 95%">
                    <el-option v-for="o in LOG_TYPE" :key="o.id" :label="$t(o.label)" :value="o.id"/>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="8">
                <el-form-item :label="$t('operating_log.object')" prop="module">
                  <el-cascader v-model="condition.operModules"
                               show-all-levels filterable
                               :options="sysList"
                               :placeholder="$t('operating_log.object')"
                               :props="props"
                               class="ms-case"
                               clearable
                               @change="initTableData" ref="cascade" style="width: 95%"/>
                </el-form-item>
              </el-col>

              <el-col :span="8">
                <div style="float: right">
                  <el-button type="primary" size="small" @click="search">
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
      <el-table border class="adjust-table" :data="tableData" ref="operLog" :height="screenHeight" v-loading="loading">
        <el-table-column prop="operTime" :label="$t('operating_log.time')" width="200" show-overflow-tooltip>
          <template v-slot="scope">
            <span>{{ scope.row.operTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" width="110" :label="$t('operating_log.user')" show-overflow-tooltip/>
        <el-table-column prop="workspaceName" v-if="isSystem"
                         :label="$t('commons.workspace')"
                         show-overflow-tooltip
                         width="150"/>
        <el-table-column prop="projectName" :label="$t('commons.project')" width="140" show-overflow-tooltip/>
        <el-table-column prop="operType" :label="$t('operating_log.type')" width="90" show-overflow-tooltip>
          <template v-slot="scope">
            <span>{{ getType(scope.row.operType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operModule" :label="$t('operating_log.object')" show-overflow-tooltip>
          <template v-slot="scope">
            <span>{{ getLogModule(scope.row.operModule) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operTitle" :label="$t('operating_log.name')" show-overflow-tooltip>
          <template v-slot="scope">
            <el-link v-if="isLink(scope.row)" style="color: #409EFF" @click="clickResource(scope.row)">
              {{ scope.row.operTitle }}
            </el-link>
            <span v-else>{{ scope.row.operTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('report.test_log_details')" width="100">
          <template v-slot="scope">
            <el-link style="color: #409EFF" @click="openDetail(scope.row)">{{ $t('operating_log.info') }}</el-link>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination
        :change="initTableData"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </el-card>

    <ms-log-detail ref="logDetail" :title="$t('report.test_log_details')"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUrl, LOG_MODULE_MAP, LOG_TYPE, LOG_TYPE_MAP, SYSLIST, WORKSYSLIST} from "../../common/config";
import MsLogDetail from "../../common/LogDetail";
import {getProjectById, getProjectsByWorkspaceId} from "../../../api/project";
import {getUserListByResourceUrl} from "../../../api/user";
import {modifyUserByResourceId} from "metersphere-frontend/src/api/user";
import {getOperatingLogPages} from "../../../api/operating-log";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

export default {
  name: "OperatingLog",
  components: {
    MsTablePagination,
    MsTableOperator,
    MsLogDetail
  },
  data() {
    let getModules = function () {
      let license = hasLicense();
      let data = new WORKSYSLIST();
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
      form: {},
      currentPage: 0,
      pageSize: 10,
      total: 0,
      items: [],
      workspaceList: [],
      isSystem: false,
      condition: {
        times: [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()],
      },
      tableData: [],
      userList: [],
      screenHeight: 'calc(100vh - 230px)',
      LOG_TYPE: new LOG_TYPE(this),
      LOG_TYPE_MAP: new LOG_TYPE_MAP(this),
      LOG_MODULE_MAP: new LOG_MODULE_MAP(this),
      sysList: getModules(),
      loading: false,
      options: [],
      selectLoading: false
    }
  },
  watch: {
    '$route.path': {
      handler(toPath) {
        if (toPath === '/setting/operatingLog/workspace') {
          this.isSystem = false;
          this.condition.workspaceId = getCurrentWorkspaceId();
          this.initProject();
          this.getMember('/user/ws/current/member/list');
        }
      },
      deep: true,
      immediate: true
    },
  },
  methods: {
    isLink(row) {
      let uri = getUrl(row, this);
      return (row.operType === 'UPDATE' || row.operType === 'CREATE' || row.operType === 'EXECUTE' || row.operType === 'DEBUG')
        && uri !== "/#";
    },
    clickResource(resource) {
      let uri = getUrl(resource, this);
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
        if (!this.isSystem) {
          let user = getCurrentUser();
          let permission = user.userGroups.filter(ug => ug.sourceId === resource.projectId);
          if (!permission || (Array.isArray(permission) && permission.length === 0)) {
            this.$warning(this.$t("commons.no_operation_permission"));
            return;
          }
        }
        modifyUserByResourceId(resourceId).then(() => {
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
    getMember(url) {
      if (!url) {
        url = '/user/list';
      }
      getUserListByResourceUrl(url).then(res => {
        this.userList = res.data;
      })
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
      if (this.isSystem) {
        this.condition.projectIds = [];
      } else {
        this.condition.workspaceIds = [];
      }
      this.condition.level = 'workspace';
      this.loading = getOperatingLogPages(this.currentPage, this.pageSize, this.condition).then(res => {
        let {listObject, itemCount} = res.data;
        this.tableData = listObject;
        this.total = itemCount;
      });
    },
    reset() {
      let projectIds = this.condition.projectIds;
      this.condition = {
        projectIds: projectIds,
        times: [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()]
      };
      this.initTableData();
    },
    initProject() {
      this.loading = getProjectsByWorkspaceId(getCurrentWorkspaceId()).then(res => {
        let projects = res.data;
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
      this.loading = getProjectById(getCurrentProjectID()).then(res => {
        let project = res.data;
        this.items = [{id: project.id, label: project.name}];
        this.condition.projectIds = [project.id];
        this.condition.projectId = project.id;
        this.initTableData();
      });
    },
    getType(type) {
      return this.LOG_TYPE_MAP.get(type);
    },
    getLogModule(val) {
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
