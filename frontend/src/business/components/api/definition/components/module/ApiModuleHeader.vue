<template>
  <div>
    <el-row>
      <el-col class="protocol-col" :span="9">
        <el-select class="protocol-select" size="small" v-model="condition.protocol">
          <el-option
            v-for="item in options"
            :key="item.value"
            :name="item.name"
            :value="item.value"
            :disabled="item.disabled">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="15">
        <ms-search-bar
          :condition="condition"
          :commands="operators"/>
      </el-col>
    </el-row>

    <module-trash-button v-if="!isReadOnly" :condition="condition" :exe="enableTrash"/>

    <ms-add-basis-api
      :current-protocol="condition.protocol"
      @saveAsEdit="saveAsEdit"
      @refresh="refresh"
      ref="basisApi"/>
    <api-import :propotal="condition.protocol" ref="apiImport" :moduleOptions="moduleOptions" @refresh="$emit('refresh')"/>
  </div>
</template>

<script>
import {OPTIONS} from "../../model/JsonData";
import MsAddBasisApi from "../basis/AddBasisApi";
import ApiImport from "../import/ApiImport";
import ModuleTrashButton from "./ModuleTrashButton";
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";
import TemplateComponent from "../../../../track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
import MsSearchBar from "@/business/components/common/components/search/MsSearchBar";

export default {
  name: "ApiModuleHeader",
  components: {MsSearchBar, TemplateComponent, ModuleTrashButton, ApiImport, MsAddBasisApi},
  data() {
    return {
      options: OPTIONS,
      moduleOptions: {},
      operators:  [
        {
          label: this.$t('api_test.definition.request.title'),
          callback: this.addApi
        },
        {
          label: this.$t('api_test.definition.request.fast_debug'),
          callback: () => {this.$emit('debug')}
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport
        },
        {
          label: this.$t('report.export'),
          children: [
            {
              label: this.$t('report.export_to_ms_format') ,
              callback: () => {
                if (!this.projectId) {
                  this.$warning(this.$t('commons.check_project_tip'));
                  return;
                }
                this.$emit('exportAPI', 'MS');
              }
            },
            {
              label: this.$t('report.export_to_swagger3_format'),
              callback: () => {
                if (!this.projectId) {
                  this.$warning(this.$t('commons.check_project_tip'));
                  return;
                }
                this.$emit('exportAPI', 'Swagger');
              }
            }
          ]
        }
      ]
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
  },
  computed: {
    projectId() {
      return this.$store.state.projectId
    },
  },
  methods: {
    handleImport() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.protocol = "HTTP";
      this.result = this.$get("/api/module/list/" + this.projectId + "/" + this.condition.protocol, response => {
        if (response.data != undefined && response.data != null) {
          this.data = response.data;
          let moduleOptions = [];
          this.data.forEach(node => {
            buildNodePath(node, {path: ''}, moduleOptions);
          });
          this.moduleOptions = moduleOptions
        }
        this.$refs.apiImport.open(this.moduleOptions);
      });
    },
    addApi() {
      if (!this.projectId) {
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

.protocol-col {
  min-width: 93px;
}

.read-only {
  width: 150px !important;
}

.filter-input {
  width: 174px;
  padding-left: 3px;
}
</style>
