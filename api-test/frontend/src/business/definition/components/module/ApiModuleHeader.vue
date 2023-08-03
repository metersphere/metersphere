<template>
  <div>
    <el-row :gutter="8">
      <el-col :span="8">
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
      <el-col :span="16">
        <ms-search-bar :show-operator="showOperator && !isTrashData" :condition="condition" :commands="operators" />
      </el-col>
    </el-row>

    <module-trash-button v-if="showTrashNode" :condition="condition" :total="total" :exe="enableTrash" />

    <ms-add-basis-api
      :current-protocol="condition.protocol"
      :module-options="moduleOptions"
      @saveAsEdit="saveAsEdit"
      @refresh="refresh"
      ref="basisApi" />
    <api-import
      :protocol="condition.protocol"
      ref="apiImport"
      :moduleOptions="moduleOptions"
      @refresh="$emit('refresh')" />
  </div>
</template>

<script>
import { OPTIONS } from '../../model/JsonData';
import MsAddBasisApi from '../basis/AddBasisApi';
import ApiImport from '../import/ApiImport';
import ModuleTrashButton from './ModuleTrashButton';
import MsSearchBar from 'metersphere-frontend/src/components/search/MsSearchBar';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';

export default {
  name: 'ApiModuleHeader',
  components: { MsSearchBar, ModuleTrashButton, ApiImport, MsAddBasisApi },
  data() {
    return {
      httpOperators: [
        {
          label: this.$t('api_test.definition.request.title'),
          callback: this.addApi,
          permissions: ['PROJECT_API_DEFINITION:READ+CREATE_API'],
        },
        {
          label: this.$t('api_test.definition.request.fast_debug'),
          callback: () => {
            this.$emit('debug');
          },
          permissions: ['PROJECT_API_DEFINITION:READ+DEBUG'],
        },
        {
          label: this.$t('api_test.api_import.timing_synchronization'),
          callback: () => {
            this.$emit('schedule');
          },
          permissions: ['PROJECT_API_DEFINITION:READ+TIMING_SYNC'],
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_API_DEFINITION:READ+IMPORT_API'],
        },
        {
          label: this.$t('report.export'),
          children: [
            {
              label: this.$t('report.export_to_ms_format'),
              permissions: ['PROJECT_API_DEFINITION:READ+EXPORT_API'],
              callback: () => {
                if (!this.projectId) {
                  this.$warning(this.$t('commons.check_project_tip'));
                  return;
                }
                this.$emit('exportAPI', 'MS');
              },
            },
            {
              label: this.$t('report.export_to_swagger3_format'),
              permissions: ['PROJECT_API_DEFINITION:READ+EXPORT_API'],
              callback: () => {
                if (!this.projectId) {
                  this.$warning(this.$t('commons.check_project_tip'));
                  return;
                }
                this.$emit('exportAPI', 'Swagger');
              },
            },
          ],
        },
      ],
      operators: [],
      otherOperators: [
        {
          label: this.$t('api_test.definition.request.title'),
          callback: this.addApi,
          permissions: ['PROJECT_API_DEFINITION:READ+CREATE_API'],
        },
        {
          label: this.$t('api_test.definition.request.fast_debug'),
          callback: () => {
            this.$emit('debug');
          },
          permissions: ['PROJECT_API_DEFINITION:READ+DEBUG'],
        },
        {
          label: this.$t('api_test.api_import.timing_synchronization'),
          callback: () => {
            this.$emit('schedule');
          },
          permissions: ['PROJECT_API_DEFINITION:READ+TIMING_SYNC'],
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_API_DEFINITION:READ+IMPORT_API'],
        },
        {
          label: this.$t('report.export'),
          children: [
            {
              label: this.$t('report.export_to_ms_format'),
              permissions: ['PROJECT_API_DEFINITION:READ+EXPORT_API'],
              callback: () => {
                if (!this.projectId) {
                  this.$warning(this.$t('commons.check_project_tip'));
                  return;
                }
                this.$emit('exportAPI', 'MS');
              },
            },
          ],
        },
      ],
    };
  },
  props: {
    condition: {
      type: Object,
      default() {
        return {};
      },
    },
    showOperator: Boolean,
    moduleOptions: Array,
    total: Number,
    currentModule: {
      type: Object,
      default() {
        return {};
      },
    },
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      },
    },
    isTrashData: {
      type: Boolean,
      default() {
        return false;
      },
    },
    options: {
      type: Array,
      default() {
        return OPTIONS;
      },
    },
    selectProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      },
    },
  },
  computed: {
    projectId() {
      if (this.selectProjectId) {
        return this.selectProjectId;
      } else {
        return getCurrentProjectID();
      }
    },
    showTrashNode() {
      return !this.isReadOnly && !this.isTrashData;
    },
  },
  watch: {
    'condition.protocol'() {
      if (this.condition.protocol === 'HTTP') {
        this.operators = this.httpOperators;
      } else {
        this.operators = this.otherOperators;
      }
    },
  },
  created() {
    this.operators = this.httpOperators;
  },
  methods: {
    handleImport() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.protocol = 'HTTP';
      this.$refs.apiImport.open(this.currentModule.id ? this.currentModule : this.moduleOptions);
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
    },
  },
};
</script>

<style scoped>
.protocol-select {
  width: 100%;
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
