<template>
  <div>
    <el-input :placeholder="$t('test_track.module.search')" v-model="condition.filterText" size="small">
      <template v-slot:append>
        <el-dropdown v-if="!isReadOnly" size="small" split-button type="primary" class="ms-api-button" @click="handleCommand('add-api')"

                     @command="handleCommand">
          <el-button icon="el-icon-folder-add" @click="addScenario"></el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="import">{{ $t('api_test.api_import.label') }}</el-dropdown-item>
            <el-dropdown-item command="export">{{ $t('report.export') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>

    </el-input>
    <module-trash-button v-if="!isReadOnly" :condition="condition" :exe="enableTrash"/>

    <api-import :model="'scenario'" ref="apiImport" :moduleOptions="moduleOptions" @refresh="$emit('refresh')"/>

  </div>
</template>
<script>
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";
import ModuleTrashButton from "@/business/components/api/definition/components/module/ModuleTrashButton";
import ApiImport from "@/business/components/api/definition/components/import/ApiImport";

export default {
  name: "ApiScenarioModuleHeader",
  components: {ApiImport, ModuleTrashButton},
  data() {
    return {
      moduleOptions: {}
    }
  },
  props: {
    condition: {
      type: Object,
      default() {
        return {}
      }
    },
    currentModule: {
      type: Object,
      default() {
        return {}
      }
    },
    isReadOnly: {
      type: Boolean,
      default() {
        return false
      }
    },
    projectId: String
  },
  methods: {
    handleCommand(e) {
      switch (e) {
        case "import":
          if (!this.projectId) {
            this.$warning(this.$t('commons.check_project_tip'));
            return;
          }
          this.result = this.$get("/api/automation/module/list/" + this.projectId + "/", response => {
            if (response.data != undefined && response.data != null) {
              this.data = response.data;
              let moduleOptions = [];
              this.data.forEach(node => {
                buildNodePath(node, {path: ''}, moduleOptions);
              });
              this.moduleOptions = moduleOptions
            }
          });
          this.$refs.apiImport.open(this.currentModule);
          break;
        default:
          if (!this.projectId) {
            this.$warning(this.$t('commons.check_project_tip'));
            return;
          }
          this.$emit('exportAPI');
          break;
      }
    },
    addScenario() {
      this.$emit('addScenario');
    },
    refresh() {
      this.$emit('refresh');
    },
    enableTrash() {
      this.condition.trashEnable = true;
    }
  }
}
</script>

<style scoped>

.read-only {
  width: 150px !important;
}

.filter-input {
  width: 174px;
  padding-left: 3px;
}

</style>
