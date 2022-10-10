<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="loading">
        <el-card class="table-card">
          <template v-slot:header>
            <ms-table-header :show-create="false" :condition.sync="condition"
                             @search="init" :tip="$t('project.code_segment.search')">
              <template v-slot:button>
                <ms-table-button icon="el-icon-circle-plus-outline" :content="$t('project.code_segment.create')" @click="handleCreate"
                                 v-permission="['PROJECT_CUSTOM_CODE:READ+CREATE']"/>
              </template>
            </ms-table-header>
          </template>
          <el-table border class="adjust-table" :data="data" style="width: 100%"
                    @sort-change="sort"
                    @filter-change="filter"
                    :height="screenHeight" ref="table">
            <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
            <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
              <template v-slot:default="scope">
                <pre>{{ scope.row.description }}</pre>
              </template>
            </el-table-column>
            <el-table-column prop="tags" :label="$t('api_test.automation.tag')">
              <template v-slot:default="scope">
                <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                        :content="itemName" style="margin-left: 0; margin-right: 2px">
                </ms-tag>
                <span></span>
              </template>
            </el-table-column>
            <el-table-column prop="type" column-key="type" :label="$t('project.code_segment.language')" show-overflow-tooltip :filters="languages"/>
            <el-table-column prop="createTime"
                             sortable
                             :label="$t('commons.create_time')"
                             show-overflow-tooltip>
              <template v-slot:default="scope">
                <span>{{ scope.row.createTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')">
              <template v-slot:default="scope">
                <div>
                  <ms-table-operator @editClick="handleEdit(scope.row)" @deleteClick="handleDelete(scope.row)"
                                     :edit-permission="['PROJECT_CUSTOM_CODE:READ+EDIT']" :delete-permission="['PROJECT_CUSTOM_CODE:READ+DELETE']">
                    <template v-slot:middle>
                      <ms-table-operator-button :tip="$t('commons.copy')" icon="el-icon-copy-document" type="info"
                                                @exec="handleCopy(scope.row)" v-permission="['PROJECT_CUSTOM_CODE:READ+COPY']"/>
                    </template>
                  </ms-table-operator>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <ms-table-pagination :change="init" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
        </el-card>

        <edit-function @refresh="init" ref="editFunction"/>
        <ms-delete-confirm :title="$t('project.code_segment.delete')" @delete="_handleDel" ref="deleteConfirm"/>
      </div>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import EditFunction from "./EditFunction";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {copyCodeSnippetById, deleteCodeSnippetById, getCodeSnippetPages} from "../../../api/custom-func";

export default {
  name: "CustomFunction",
  components: {
    MsMainContainer,
    MsContainer,
    EditFunction,
    MsTableHeader,
    MsTableButton,
    MsTablePagination,
    MsTag,
    MsTableOperator,
    MsTableOperatorButton,
    MsDeleteConfirm
  },
  data() {
    return {
      condition: {},
      result: {},
      loading: false,
      data: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 155px)',
      languages: [
        {text: 'beanshell', value: 'beanshell'},
        {text: 'python', value: 'python'},
        {text: 'groovy', value: 'groovy'},
        {text: 'javascript', value: 'javascript'},
      ],
    }
  },
  activated() {
    this.init();
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.condition.projectId = getCurrentProjectID();
      this.loading = getCodeSnippetPages(this.currentPage, this.pageSize, this.condition).then(res => {
        let tableData = res.data;
        const {itemCount, listObject} = tableData;
        this.total = itemCount;
        this.data = listObject;
        this.data.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        })
      });
    },
    handleCreate() {
      this.$refs.editFunction.open({});
    },
    sort(column) {
      _sort(column, this.condition);
      this.init();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.init();
    },
    handleEdit(row) {
      this.$refs.editFunction.open(row);
    },
    handleDelete(row) {
      this.$refs.deleteConfirm.open(row);
    },
    _handleDel(row) {
      deleteCodeSnippetById(row.id).then(() => {
        this.init();
        this.$success(this.$t('commons.delete_success'));
      });
    },
    handleCopy(row) {
      copyCodeSnippetById(row.id).then(() => {
        this.init();
        this.$success(this.$t('commons.copy_success'));
      });
    }
  }
}
</script>


<style scoped>
  pre {
    margin: 0 0;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  }
</style>
