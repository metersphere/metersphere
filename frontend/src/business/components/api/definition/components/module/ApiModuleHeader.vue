<template>
  <div>
    <el-select class="protocol-select" size="small" v-model="condition.protocol">
      <el-option
        v-for="item in options"
        :key="item.value"
        :name="item.name"
        :value="item.value"
        :disabled="item.disabled">
      </el-option>
    </el-select>
    <el-input class="filter-input" :class="{'read-only': isReadOnly}" :placeholder="$t('test_track.module.search')" v-model="condition.filterText"
              size="small">
      <template v-slot:append>
        <el-dropdown v-if="!isReadOnly" size="small" split-button type="primary" class="ms-api-button" @click="handleCommand('add-api')"
                     v-tester
                     @command="handleCommand">
          <el-button icon="el-icon-folder-add" @click="addApi"></el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="add-api">{{ $t('api_test.definition.request.title') }}</el-dropdown-item>
            <el-dropdown-item command="debug">{{ $t('api_test.definition.request.fast_debug') }}</el-dropdown-item>
            <el-dropdown-item command="import">{{ $t('api_test.api_import.label') }}</el-dropdown-item>
            <el-dropdown-item command="export">{{ $t('report.export') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
    </el-input>

    <module-trash-button v-if="!isReadOnly" :condition="condition" :exe="enableTrash"/>

    <ms-add-basis-api
      :current-protocol="condition.protocol"
      @saveAsEdit="saveAsEdit"
      @refresh="refresh"
      ref="basisApi"/>
    <api-import ref="apiImport" @refresh="$emit('refresh')"/>
  </div>
</template>

<script>
import {OPTIONS} from "../../model/JsonData";
import MsAddBasisApi from "../basis/AddBasisApi";
import ApiImport from "../import/ApiImport";
import ModuleTrashButton from "./ModuleTrashButton";
import {getCurrentProjectID} from "../../../../../../common/js/utils";

export default {
  name: "ApiModuleHeader",
  components: {ModuleTrashButton, ApiImport, MsAddBasisApi},
  data() {
    return {
      options: OPTIONS,
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
    }
  },
  methods: {
    handleCommand(e) {
      switch (e) {
        case "debug":
          this.$emit('debug');
          break;
        case "add-api":
          this.addApi();
          break;
        case "add-module":
          break;
        case "import":
          if (!getCurrentProjectID()) {
            this.$warning(this.$t('commons.check_project_tip'));
            return;
          }
          this.$refs.apiImport.open(this.currentModule);
          break;
        default:
          if (!getCurrentProjectID()) {
            this.$warning(this.$t('commons.check_project_tip'));
            return;
          }
          this.$emit('exportAPI');
          break;
      }
    },
    addApi() {
      if (!getCurrentProjectID()) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.basisApi.open(this.currentModule);
    },
    saveAsEdit(data) {
      this.$emit('saveAsEdit', data);
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

.protocol-select {
  width: 92px;
  height: 30px;
}

.read-only {
  width: 150px !important;
}

.filter-input {
  width: 175px;
  padding-left: 3px;
}

</style>
