<template>
  <div class="jar-config-list">
    <el-card class="table-card">

      <template v-slot:header>
        <ms-table-header
          :showCreate="false"
          :create-permission="['SYSTEM_USER:READ+CREATE']"
          :condition.sync="condition"
          :import-tip="$t('test_track.case.import.click_upload')"
          :tip="$t('commons.search_by_name_or_id')"
          :create-tip="$t('user.create')"
          :title="$t('plugin.title')"
          @search="initPlugins"
          @import="importJar"
          :show-import="true" :upload-permission="['SYSTEM_PLUGIN:UPLOAD']"/>
      </template>

      <el-table
        border
        :data="tableData"
        row-key="id"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        class="adjust-table table-content">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="sourceName" :label="$t('api_test.jar_config.jar_file')" show-overflow-tooltip/>
        <el-table-column prop="pluginId" :label="$t('plugin.plugin_id')" show-overflow-tooltip/>
        <el-table-column prop="createUserId" :label="$t('report.user_name')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="license" :label="$t('license.licenseVersion')">
          <template v-slot:default="scope">
            <span v-if="scope.row.license">{{ $t('commons.enterprise_edition') }}</span>
            <span v-else>{{ $t('commons.open_source_version') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" min-width="30">
          <template v-slot:default="scope">
            <div v-if="scope.row.name === scope.row.sourceName">
              <ms-table-operator-button
                :tip="$t('commons.delete')"
                icon="el-icon-delete"
                type="danger"
                @exec="handleDelete(scope.row.id)" v-permission="['SYSTEM_PLUGIN:DEL']"/>
            </div>
            <div v-else>
              <ms-table-operator-button
                :tip="$t('plugin.script_view')"
                icon="el-icon-view"
                @exec="handleView(scope.row)" v-permission="['SYSTEM_PLUGIN:READ']"/>
            </div>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
    <el-dialog :title="$t('commons.import')" :visible.sync="dialogVisible" @close="close" destroy-on-close>
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
  components: {MsTableOperatorButton, MsTableHeader, MsJarConfig, MsScriptView},
  props: {},
  data() {
    return {
      result: {},
      condition: {},
      tableData: [],
      dialogVisible: false,
      dataMap: new Map(),
    }
  },
  created() {
    this.initPlugins();
  },
  methods: {
    importJar() {
      this.dialogVisible = true;
    },
    initPlugins() {
      let url = "/plugin/list";
      let name = this.condition.name;
      if (name) {
        url = "/plugin/list?name=" +name;
      }
      this.tableData = [];
      this.$get(url, response => {
        if (response.data) {
          this.format(response.data);
          this.dataMap.forEach((values, key) => {
            let obj = {id: key, license: values[0].license, name: values[0].sourceName, sourceName: values[0].sourceName, pluginId: key, createUserId: values[0].createUserId, updateTime: values[0].updateTime};
            obj.children = values;
            this.tableData.push(obj);
          })
        }
      });
    },
    format(data) {
      this.dataMap = new Map();
      this.tableData = [];
      data.forEach(item => {
        if (this.dataMap.has(item.pluginId)) {
          this.dataMap.get(item.pluginId).push(item);
        } else {
          this.dataMap.set(item.pluginId, [item]);
        }
      })
    },
    close() {
      this.dialogVisible = false;
      this.initPlugins();
    },
    handleView(row) {
      this.$refs.scriptView.open(row.scriptId);
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
