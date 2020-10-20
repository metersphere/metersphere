<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="list" @create="create" :title="$t('api_test.jar_config.title')"/>
      </template>

      <el-table border :data="items"
                class="adjust-table table-content"
                highlight-current-row>

        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="fileName" :label="$t('api_test.jar_config.jar_file')"  show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip/>
        <el-table-column prop="owner" :label="$t('report.user_name')"  show-overflow-tooltip/>

        <el-table-column :label="$t('commons.operating')" min-width="100">

          <template v-slot:default="scope">
            <ms-table-operator :is-tester-permission="true" @editClick="edit(scope.row)"
                               @deleteClick="handleDelete(scope.row.id)"/>
          </template>
        </el-table-column>

      </el-table>


      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <ms-jar-config-edit @refresh="list"  ref="jarConfigFrom"/>

    </el-card>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {
    listenGoBack,removeGoBackListener
  } from "@/common/js/utils";
  import {DEFAULT, WORKSPACE} from "@/common/js/constants";
  import MsJarConfigEdit from "./edit/JarConfigEdit";

  export default {
    name: "MsJarConfigSetting",
    components: {
      MsJarConfigEdit,
      MsCreateBox,
      MsTablePagination,
      MsTableHeader,
      MsTableOperator,
      MsDialogFooter,
      MsTableOperatorButton
    },
    data() {
      return {
        result: {},
        condition: {},
        items: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
      }
    },
    activated() {
      this.list();
    },
    methods: {
      create() {
        this.$refs.jarConfigFrom.open();
      },
      getJarConfigs() {
        this.result = this.$get("/jar/list/" + this.projectId, response => {
          this.configs = response.data;
        })
      },
      edit(row) {
        this.$refs.jarConfigFrom.open(row);
      },
      list() {
        let url = '/jar/list/' + this.currentPage + '/' + this.pageSize;
        this.result = this.$post(url, this.condition, response => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
        });
      },
      handleDelete(id) {
        this.$confirm(this.$t('删除需重启服务后生效'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get("/jar/delete/" + id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancelled')
          });
        });
      },
    }
  }
</script>

<style scoped>
</style>

