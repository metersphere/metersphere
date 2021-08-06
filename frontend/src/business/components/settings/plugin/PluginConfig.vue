<template>
  <div class="jar-config-list">
    <el-card class="table-card">

      <template v-slot:header>
        <ms-table-header
          :showCreate="false"
          :create-permission="['SYSTEM_USER:READ+CREATE']"
          :condition.sync="condition" @search="search"
          :import-tip="$t('test_track.case.import.click_upload')"
          :tip="$t('commons.search_by_name_or_id')"
          :create-tip="$t('user.create')"
          :title="$t('plugin.title')"
          @import="importJar"
          :show-import="true"/>
      </template>

      <el-table border :data="tableData" class="adjust-table table-content">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="sourceName" :label="$t('api_test.jar_config.jar_file')" show-overflow-tooltip/>
        <el-table-column prop="pluginId" :label="$t('plugin.plugin_id')" show-overflow-tooltip/>
        <el-table-column prop="createUserId" :label="$t('report.user_name')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator-button
                :tip="$t('commons.delete')"
                icon="el-icon-delete"
                type="danger"
                @exec="handleDelete(scope.row.id)"/>

              <ms-table-operator-button
                :tip="$t('plugin.script_view')"
                icon="el-icon-view"
                @exec="handleView(scope.row)"/>
            </div>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
    <el-dialog :title="$t('commons.import')" :visible.sync="dialogVisible" @close="close">
      <ms-jar-config @close="close"/>
    </el-dialog>
    <ms-script-view ref="scriptView"/>
  </div>
</template>

<script>

import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsJarConfig from "./JarConfig";
import MsScriptView from "./ScriptView";

export default {
  name: "PluginConfig",
  components: {MsTableOperatorButton, MsTableHeader, MsJarConfig,MsScriptView},
  props: {},
  data() {
    return {
      result: {},
      condition: {},
      tableData: [],
      dialogVisible: false,
    }
  },
  created() {
    this.initPlugins();
  },
  methods: {
    search() {

    },
    importJar() {
      this.dialogVisible = true;
    },
    initPlugins() {
      let url = "/plugin/list";
      this.$get(url, response => {
        this.tableData = response.data;
      });
    },
    close() {
      this.dialogVisible = false;
      this.initPlugins();
    },
    handleView(row){
      this.$refs.scriptView.open(row.pluginId);
    },
    handleDelete(id) {
      this.$confirm(this.$t('api_test.jar_config.delete_tip'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get("/plugin/delete/" + id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.initPlugins();
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
