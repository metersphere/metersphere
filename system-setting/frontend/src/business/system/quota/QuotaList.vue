<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :condition.sync="condition" @search="search"
                       :title="$t('quota.list')" :show-create="false"/>
    </template>

    <ms-table
      :data="tableData"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :operators="operators"
      :screenHeight="tableHeight"
      :field-key="tableHeaderKey"
      :remember-order="true"
      :enable-order-drag="enableOrderDrag"
      row-key="id"
      :fields.sync="fields"
      operator-width="100px"
      @refresh="search"
      :disable-header-config="false"
      :show-select-all="false"
      :enable-selection="false"
      ref="table">

      <span v-for="(item) in fields" :key="item.key">
        <ms-table-column
          v-if="quotaType === QUOTA_TYPE.WORKSPACE"
          :field="item"
          prop="workspaceName"
          :fields-width="fieldsWidth"
          :label="$t('commons.workspace')">
        </ms-table-column>
        <ms-table-column
          v-if="quotaType === QUOTA_TYPE.PROJECT"
          :field="item"
          prop="projectName"
          :fields-width="fieldsWidth"
          :label="$t('commons.project')">
        </ms-table-column>

        <ms-table-column
          prop="api"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.api')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.api"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="performance"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.performance')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.performance"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="member"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.member')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.member"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="project"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.project')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.project"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="maxThreads"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.max_threads')"
          min-width="100"
        >
          <template v-slot:default="scope">
            <quota-value :value="scope.row.maxThreads"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="duration"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.duration')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.duration"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="resourcePool"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.resource_pool')">
          <template v-slot:default="scope">
            <quota-value :value=" resourcePoolNames(scope.row)"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="moduleSetting"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.enable_module')">
          <template v-slot:default="scope">
            <quota-value :value=" moduleName(scope.row)"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="vumTotal"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.vum_total')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.vumTotal"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="vumUsed"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.vum_used')">
          <template v-slot:default="scope">
            <quota-value :value="scope.row.vumUsed" :is-show-value="true"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="useDefault"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('quota.use_default')">
          <template v-slot:default="scope">
            <el-tag size="mini" v-if="scope.row.useDefault">{{ $t('quota.yes') }}</el-tag>
            <el-tag type="info" size="mini" v-else>{{ $t('quota.no') }}</el-tag>
          </template>
        </ms-table-column>
      </span>
    </ms-table>

    <edit-quota :title="title" :quota="quota" :resources="resources" :default-quota="defaultQuota" :modules="modules"
                   @confirm="confirm" :quota-type="quotaType" ref="editQuota"/>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </el-card>
</template>

<script>

import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import QuotaValue from "./QuotaValue";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {getCustomTableHeader, getCustomTableWidth} from "metersphere-frontend/src/utils/tableUtils";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import EditQuota from "./EditQuota";
import {deleteQuota, getQuotaPages, saveQuota} from "../../../api/quota";
import {QUOTA_TABLE_HEADER_KEY, QUOTA_TYPE} from "../../../common/constants";

export default {
  name: "QuotaList",
  components: {
    MsTablePagination,
    MsTableOperators,
    MsTableHeader,
    MsTableColumn,
    MsTable,
    EditQuota,
    QuotaValue,
  },
  props: {
    resources: Array,
    defaultQuota: Object,
    quotaType: {
      type: String,
      default() {
        return QUOTA_TYPE.WORKSPACE;
      }
    },
    modules: Array,
  },
  computed: {
    // id => name
    resourcePoolNames() {
      return row => {
        if (!row.resourcePool) return "";
        let ids = row.resourcePool.split(",");
        let names = [];
        ids.forEach(id => {
          for (let resource of this.resources) {
            if (resource.id === id) {
              names.push(resource.name);
            }
          }
        });
        return names.join(",");
      };
    },
    moduleName() {
      return row => {
        if (!row.moduleSetting) return "";
        let ids = row.moduleSetting.split(",");
        let names = [];
        ids.forEach(id => {
          for (let module of this.modules) {
            if (module.id === id) {
              names.push(module.name);
            }
          }
        });
        return names.join(",");
      };
    },
    tableHeaderKey() {
      return this.quotaType === QUOTA_TYPE.WORKSPACE ?
        QUOTA_TABLE_HEADER_KEY.WORKSPACE : QUOTA_TABLE_HEADER_KEY.PROJECT;
    },
    fieldsWidth() {
      return this.quotaType === QUOTA_TYPE.WORKSPACE ?
        getCustomTableWidth(QUOTA_TABLE_HEADER_KEY.WORKSPACE) : getCustomTableWidth(QUOTA_TABLE_HEADER_KEY.PROJECT);
    }
  },
  data() {
    return {
      result: {loading: false},
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      enableOrderDrag: false,
      QUOTA_TYPE,
      QUOTA_TABLE_HEADER_KEY,
      operators: [
        {
          tip: this.$t('quota.modify'), icon: "el-icon-edit",
          exec: this.edit,
          permissions: this.quotaType === QUOTA_TYPE.WORKSPACE ? ['SYSTEM_QUOTA:READ+EDIT'] : ['WORKSPACE_QUOTA:READ+EDIT']
        }, {
          tip: this.$t('commons.adv_search.reset'), icon: "el-icon-refresh-left", type: "danger",
          exec: this.delete,
          permissions: this.quotaType === QUOTA_TYPE.WORKSPACE ? ['SYSTEM_QUOTA:READ+EDIT'] : ['WORKSPACE_QUOTA:READ+EDIT']
        }
      ],
      quota: {},
      title: "",
      tableHeight: 'calc(100vh - 350px)',
      fields: this.quotaType === QUOTA_TYPE.WORKSPACE ?
        getCustomTableHeader(QUOTA_TABLE_HEADER_KEY.WORKSPACE) : getCustomTableHeader(QUOTA_TABLE_HEADER_KEY.PROJECT)
    };
  },
  watch: {
    defaultQuota: {
      handler(val) {
        this.handleUseDefault(val);
      },
      deep: true
    }
  },
  methods: {
    search() {
      let param = {name: this.condition.name};
      if (this.quotaType === QUOTA_TYPE.PROJECT) {
        let workspaceId = getCurrentWorkspaceId();
        param = {name: this.condition.name, workspaceId};
      }
      this.result.loading = true;
      getQuotaPages(this.quotaType.toLowerCase(), this.currentPage, this.pageSize, param)
        .then(res => {
          this.result.loading = false;
          let {itemCount, listObject} = res.data;
          this.total = itemCount;
          this.tableData = listObject;
          this.handleUseDefault(this.defaultQuota);
        })
        .catch(() => {
          this.result.loading = false;
        });
    },
    edit(row) {
      this.quota = row;
      let title = this.quotaType === QUOTA_TYPE.WORKSPACE ? row.workspaceName : row.projectName;
      this.title = this.$t('quota.edit_quota_title', [title]);
      this.$refs.editQuota.openDialog();
    },
    delete(row) {
      if (!row || !row.id) {
        this.$success(this.$t("organization.integration.successful_operation"));
        return;
      }
      deleteQuota(row).then(() => {
        this.$success(this.$t("organization.integration.successful_operation"));
        this.search();
      });
    },
    confirm(quota) {
      saveQuota(quota).then(() => {
        this.$success(this.$t("commons.save_success"));
        this.refresh();
      }).catch(() => {
        this.refresh();
      });
    },
    refresh() {
      this.$emit("refresh");
      this.search();
    },
    handleUseDefault(val) {
      this.tableData.forEach(quota => {
        if (quota.useDefault) {
          quota.api = val.api;
          quota.performance = val.performance;
          quota.maxThreads = val.maxThreads;
          quota.duration = val.duration;
          quota.resourcePool = val.resourcePool;
          quota.member = val.member;
          quota.project = val.project;
          quota.vumTotal = val.vumTotal;
          quota.moduleSetting = val.moduleSetting;
        }
      });
    }
  },
  mounted() {
    this.search();
  },
  activated() {
    this.search();
  }
};
</script>

<style scoped>

</style>
