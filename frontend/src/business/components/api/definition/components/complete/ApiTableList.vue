<template>
  <div>
    <slot name="header"></slot>
    <el-input :placeholder="$t('commons.search_by_name_or_id')" @blur="initTable" class="search-input" size="small"
              @keyup.enter.native="initTable" v-model="condition.name"/>
    <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                             v-if="condition.components !== undefined && condition.components.length > 0"
                             @search="initTable"/>

    <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
              :total="total" enableSelection
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

  </div>

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
  import ApiListContainer from "../../../definition/components/list/ApiListContainer";
  import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
  import MsEnvironmentSelect from "../../../definition/components/case/MsEnvironmentSelect";
  import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
  import {getProtocolFilter} from "@/business/components/api/definition/api-definition";
  import {getProjectMember} from "@/network/user";
  import TableSelectCountBar from "@/business/components/api/automation/scenario/api/TableSelectCountBar";

  export default {
    name: "ApiTableList",
    components: {
      TableSelectCountBar,
      MsEnvironmentSelect,
      PriorityTableItem,
      ApiListContainer,
      MsTableOperatorButton,
      MsTableOperator,
      MsTablePagination,
      MsTag,
      MsBottomContainer,
      ShowMoreBtn,
      MsBatchEdit,
      MsTable,
      MsTableColumn,
      MsTableAdvSearchBar
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
        userFilters: []
      }
    },
    props: {
      currentProtocol: String,
      selectNodeIds: Array,
      result: Object,
      tableData: Array,
      condition: Object,
      currentPage: Number,
      pageSize: Number,
      screenHeight: {
        type: [Number, String],
        default() {
          return  'calc(100vh - 400px)';
        }
      }
    },
    created: function () {
      getProjectMember((data) => {
        this.userFilters = data;
      });
      this.getProtocolFilter();
    },
    watch: {
      currentProtocol() {
        this.getProtocolFilter();
      }
    },
    mounted() {
      if (this.$refs.apitable) {
        this.$emit('setSelectRow', this.$refs.apitable.getSelectRows());
      } else {
        this.$emit('setSelectRow', new Set());
      }
    },
    computed: {
      getApiRequestTypeName(){
        if(this.currentProtocol === 'TCP'){
          return this.$t('api_test.definition.api_agreement');
        }else{
          return this.$t('api_test.definition.api_type');
        }
      },
      total() {
        return this.tableData ? this.tableData.length : 0;
      },
    },
    methods: {
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
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
    },
  }
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
    width: 30%;
    margin-bottom: 20px;
    margin-right: 20px;
  }

  .adv-search-bar {
    float: right;
    margin-top: 5px;
    margin-right: 10px;
  }

</style>
