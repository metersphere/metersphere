<template>
  <div class="jar-config-list">
    <el-card style="height: 100%;">
      <template v-slot:header>
        <ms-table-header :show-create="false" :condition.sync="condition"
                         @search="getJarConfigs"
                         :create-tip="$t('project.create')">
          <template v-slot:button>
            <ms-table-button icon="el-icon-box"
                             v-permission="['PROJECT_FILE:READ+UPLOAD+JAR']"
                             :content="$t('api_test.jar_config.title')" @click="openJarConfig"/>
          </template>
        </ms-table-header>
      </template>

      <el-table border :data="tableData"
                class="adjust-table table-content"
                highlight-current-row
                :height="tableHeight"
                @row-click="handleView">

        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="fileName" :label="$t('api_test.jar_config.jar_file')" show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" :label="$t('report.user_name')" show-overflow-tooltip/>
        <el-table-column prop="modifier" :label="$t('commons.modifier')" show-overflow-tooltip/>

        <el-table-column :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator-button :tip="$t('commons.delete')" icon="el-icon-delete" type="danger"
                                        @exec="handleDelete(scope.row.id)" v-permission="['PROJECT_FILE:READ+DELETE+JAR']"/>
            </div>
          </template>
        </el-table-column>

      </el-table>
      <ms-table-pagination :change="getJarConfigs" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <ms-jar-config ref="jarConfig" @refresh="getJarConfigs"/>
    </el-card>
  </div>
</template>

<script>

import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsJarConfig from "@/business/components/api/test/components/jar/JarConfig";

export default {
  name: "MsJarConfigList",
  components: {
    MsTableOperatorButton,
    MsTablePagination,
    MsMainContainer,
    MsContainer,
    MsTableHeader,
    MsTableButton,
    MsJarConfig
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  created() {
    this.getJarConfigs();
  },
  data() {
    return {
      result: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      tableData: [],
      condition: {},
      tableHeight: 'calc(50vh - 170px)'
    }
  },
  methods: {
    openJarConfig() {
      this.$nextTick(() => {
        this.$refs.jarConfig.open();
      })
    },
    getJarConfigs(isSearchBarQuery) {
      if (isSearchBarQuery) {
        this.isSearchBarQuery = isSearchBarQuery;
      }
      this.result = this.$post("/jar/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
        let data = response.data;
        let {itemCount, listObject} = data;
        this.total = itemCount;
        this.tableData = listObject;
      });
    },
    handleView(row) {
      this.$emit('rowSelect', row);
    },
    handleDelete(id) {
      this.$confirm(this.$t('api_test.jar_config.delete_tip'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get("/jar/delete/" + id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.getJarConfigs();
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

.el-icon-check {
  font-size: 20px;
  font-weight: bold;
  color: green;
  margin-left: 20px;
}
</style>
