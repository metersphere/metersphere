<template>
  <div class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <el-table border :data="tableData" class="adjust-table table-content" height="260px">
      <el-table-column prop="num" :label="$t('api_test.home_page.new_case_list.table_coloum.index')" width="100"
                       show-overflow-tooltip>
        <template v-slot:default="{row}">
          <span type="num" @click="redirect(row.apiType,row.id)">
            {{ row.num }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="$t('api_test.home_page.new_case_list.table_coloum.api_name')"
                       show-overflow-tooltip width="170">
        <template v-slot:default="{row}">
          <span type="name" @click="redirect(row.apiType,row.id)">
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="$t('api_test.home_page.new_case_list.table_coloum.path')" width="170"
                       show-overflow-tooltip>

      </el-table-column>
      <el-table-column prop="status" :label="$t('api_test.home_page.new_case_list.table_coloum.api_status')">
        <template v-slot:default="scope">
          <span class="el-dropdown-link">
            <api-status :value="scope.row.status"/>
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('api_test.home_page.new_case_list.table_coloum.update_time')" width="170">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="caseTotal" :label="$t('api_test.home_page.new_case_list.table_coloum.relation_case')"
                       width="100">
        <template v-slot:default="{row}">
          <el-link type="info" @click="redirect(row.caseType,row.id)">
            {{ row.caseTotal }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="scenarioTotal"
                       :label="$t('api_test.home_page.new_case_list.table_coloum.relation_scenario')"
                       width="100">
        <template v-slot:default="{row}">
          <el-link type="info" @click="redirect(row.scenarioType,row.ids)">
            {{ row.scenarioTotal }}
          </el-link>
        </template>
      </el-table-column>

    </el-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </div>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import {API_DEFINITION_CONFIGS} from "@/business/components/common/components/search/search-components";
import {API_STATUS} from "@/business/components/api/definition/model/JsonData";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import MsTablePagination from "../../../common/pagination/TablePagination";
import {initCondition} from "@/common/js/tableUtils";

export default {
  name: "MsApiNewTestCaseList",

  components: {
    MsTag, ApiStatus, MsTablePagination
  },

  data() {
    return {
      result: {},
      tableData: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      condition: {
        components: API_DEFINITION_CONFIGS,
      },
      status: API_STATUS,
      currentProtocol: "HTTP",
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    search(currentProtocol) {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
      initCondition(this.condition, this.condition.selectAll);
      this.selectDataCounts = 0;
      if (!this.trashEnable) {
        this.condition.moduleIds = this.selectNodeIds;
      }
      this.condition.projectId = this.projectId;
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      this.enableOrderDrag = (this.condition.orders && this.condition.orders.length) > 0 ? false : true;

      //检查是否只查询本周数据
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.apiCaseCoverage = null;
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'uncoverage':
          this.condition.apiCaseCoverage = 'uncoverage';
          break;
        case 'coverage':
          this.condition.apiCaseCoverage = 'coverage';
          break;
        case 'Prepare':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Completed':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Underway':
          this.condition.filters.status = [this.selectDataRange];
          break;
      }
      if (currentProtocol) {
        this.condition.moduleIds = [];
      }
      if (this.condition.projectId) {
        this.result = this.$post("/api/definition/week/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      }
    },
    genProtocalFilter(protocalType) {
      if (protocalType === "HTTP") {
        this.methodFilters = [
          {text: 'GET', value: 'GET'},
          {text: 'POST', value: 'POST'},
          {text: 'PUT', value: 'PUT'},
          {text: 'PATCH', value: 'PATCH'},
          {text: 'DELETE', value: 'DELETE'},
          {text: 'OPTIONS', value: 'OPTIONS'},
          {text: 'HEAD', value: 'HEAD'},
          {text: 'CONNECT', value: 'CONNECT'},
        ];
      } else if (protocalType === "TCP") {
        this.methodFilters = [
          {text: 'TCP', value: 'TCP'},
        ];
      } else if (protocalType === "SQL") {
        this.methodFilters = [
          {text: 'SQL', value: 'SQL'},
        ];
      } else if (protocalType === "DUBBO") {
        this.methodFilters = [
          {text: 'DUBBO', value: 'DUBBO'},
          {text: 'dubbo://', value: 'dubbo://'},
        ];
      } else {
        this.methodFilters = [
          {text: 'GET', value: 'GET'},
          {text: 'POST', value: 'POST'},
          {text: 'PUT', value: 'PUT'},
          {text: 'PATCH', value: 'PATCH'},
          {text: 'DELETE', value: 'DELETE'},
          {text: 'OPTIONS', value: 'OPTIONS'},
          {text: 'HEAD', value: 'HEAD'},
          {text: 'CONNECT', value: 'CONNECT'},
          {text: 'DUBBO', value: 'DUBBO'},
          {text: 'dubbo://', value: 'dubbo://'},
          {text: 'SQL', value: 'SQL'},
          {text: 'TCP', value: 'TCP'},
        ];
      }
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (dataType === 'api') {
        this.selectDataRange = dataRange;
      } else {
        this.selectDataRange = 'all';
      }
    },
    redirect(pageType, param) {
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      switch (pageType) {
        case "api":
          this.$router.push('/api/definition/' + uuid + '?resourceId=' + param)
          break;
        case "apiCase":
          this.$emit('redirectPage', 'api', 'apiTestCase', 'singleList:' + param);
          break;
        case "scenario":
          if (param) {
            this.$emit('redirectPage', 'scenario', 'scenario', 'list:' + param);
            break;
          }
      }
    },
  },
    created() {
      this.search();
    },
    activated() {
      this.search();
    }
  }
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

</style>
