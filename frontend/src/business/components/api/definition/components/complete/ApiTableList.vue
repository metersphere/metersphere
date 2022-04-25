<template>
  <span>
    <slot name="header"></slot>
    <ms-search
      :condition.sync="condition"
      @search="initTable">
    </ms-search>

    <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
              :total="total" enableSelection @selectCountChange="selectCountChange"
              :screenHeight="screenHeight"
              operator-width="170px"
              @refresh="initTable"
              ref="apitable">
      <ms-table-column
        prop="num"
        label="ID"
        min-width="80px"
        sortable>

      </ms-table-column>
      <ms-table-column
        prop="name"
        :label="$t('api_test.definition.api_name')"
        sortable
        width="120px"/>

      <ms-table-column
        prop="method"
        sortable="custom"
        column-key="method"
        :filters="methodFilters"
        :label="getApiRequestTypeName"
        width="120px">
        <template v-slot:default="scope">
          <el-tag size="mini"
                  :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}"
                  class="api-el-tag">
            {{ scope.row.method }}
          </el-tag>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="userName"
        sortable="custom"
        :filters="userFilters"
        column-key="user_id"
        :label="$t('api_test.definition.api_principal')"
        width="100px"/>

      <ms-table-column
        prop="path"
        width="120px"
        :label="$t('api_test.definition.api_path')"/>

      <ms-table-column
        prop="tags"
        :label="$t('commons.tag')"
        width="120px">
        <template v-slot:default="scope">
          <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                  :show-tooltip="true" :content="itemName"
                  style="margin-left: 0px; margin-right: 2px"/>
        </template>
      </ms-table-column>

      <ms-table-column
        v-if="versionEnable"
        :label="$t('project.version.name')"
        :filters="versionFilters"
        min-width="100px"
        prop="versionId">
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        width="160"
        :label="$t('api_test.definition.api_last_time')"
        sortable="custom"
        prop="updateTime">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="caseTotal"
        width="80px"
        :label="$t('api_test.definition.api_case_number')"/>

    </ms-table>
    <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </span>

</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTag from "../../../../common/components/MsTag";
import MsBottomContainer from "../../../definition/components/BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../../../definition/components/basis/BatchEdit";
import {API_METHOD_COLOUR} from "../../../definition/model/JsonData";
import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
import MsEnvironmentSelect from "../../../definition/components/case/MsEnvironmentSelect";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import {getProtocolFilter} from "@/business/components/api/definition/api-definition";
import {getProjectMember} from "@/network/user";
import TableSelectCountBar from "@/business/components/api/automation/scenario/api/TableSelectCountBar";
import {hasLicense} from "@/common/js/utils";
import MsSearch from "@/business/components/common/components/search/MsSearch";

export default {
  name: "ApiTableList",
  components: {
    TableSelectCountBar,
    MsEnvironmentSelect,
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    MsSearch
  },
  data() {
    return {
      moduleId: "",
      deletePath: "/test/case/delete",
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodColorMap: new Map(API_METHOD_COLOUR),
      methodFilters: [],
      userFilters: [],
      currentPage: 1,
      pageSize: 10,
      versionEnable: false,
    };
  },
  props: {
    currentProtocol: String,
    projectId: String,
    selectNodeIds: Array,
    result: Object,
    tableData: Array,
    condition: Object,
    total: Number,
    versionFilters: Array,
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 400px)';
      }
    }
  },
  created: function () {
    getProjectMember((data) => {
      this.userFilters = data;
    });
    this.getProtocolFilter();
    this.checkVersionEnable();
  },
  watch: {
    currentProtocol() {
      this.getProtocolFilter();
    },
    projectId() {
      this.checkVersionEnable();
    }
  },
  mounted() {
    if (this.$refs.apitable) {
      this.$emit('setSelectRow', this.$refs.apitable.getSelectRows());
    } else {
      this.$emit('setSelectRow', new Set());
    }

    if (this.$refs.apitable) {
      this.$refs.apitable.doLayout();
    }
  },
  computed: {
    getApiRequestTypeName() {
      if (this.currentProtocol === 'TCP') {
        return this.$t('api_test.definition.api_agreement');
      } else {
        return this.$t('api_test.definition.api_type');
      }
    },
  },
  methods: {
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    selectCountChange(value) {
      this.$emit('selectCountChange', value)
    },
    getColor(flag, method) {
      return this.methodColorMap.get(method);
    },
    getProtocolFilter() {
      this.methodFilters = getProtocolFilter(this.currentProtocol);
    },
    getSelectIds() {
      return this.$refs.apitable.selectIds;
    },
    initTable() {
      this.$emit('refreshTable');
    },
    clear() {
      if (this.$refs.apitable) {
        this.$refs.apitable.clear();
      }
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
        });
      }
    }
  },
};
</script>

<style scoped>

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 200px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}

</style>
