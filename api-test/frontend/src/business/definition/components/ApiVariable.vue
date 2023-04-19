<template>
  <div style="margin-bottom: 20px">
    <div style="overflow: auto; width: 100%">
      <span class="kv-description" v-if="description">
        {{ description }}
      </span>
      <el-table @cell-dblclick="clickRow" border :show-header="true" row-key="id" :data="parameters" ref="expandTable">
        <el-table-column
          v-for="item in tableColumnArr"
          :key="item.id"
          :prop="item.prop"
          :label="item.label"
          :min-width="getTableMinWidth(item.prop)"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <div v-show="!scope.row.isEdit">
              <div v-if="item.prop === 'required' || item.prop === 'urlEncode'">
                <span class="param-span" v-if="scope.row[item.prop]">{{ $t('commons.yes') }}</span>
                <span class="param-span" v-else>{{ $t('commons.no') }}</span>
              </div>
              <div v-else-if="item.prop === 'value' && isActive && scope.row.type === 'file'">
                <ms-api-body-file-upload :parameter="scope.row" :id="id" :is-read-only="true" :disabled="true" />
              </div>
              <span v-else>
                {{ scope.row[item.prop] }}
              </span>
            </div>
            <div v-show="scope.row.isEdit">
              <div v-if="item.prop === 'type'">
                <el-select
                  v-if="type === 'body'"
                  :disabled="isReadOnly"
                  v-model="scope.row.type"
                  size="mini"
                  @change="typeChange(scope.row)">
                  <el-option value="text" />
                  <el-option value="file" />
                  <el-option value="json" />
                </el-select>
              </div>
              <div v-else-if="item.prop === 'name'">
                <el-input
                  v-if="!suggestions"
                  :disabled="isReadOnly"
                  v-model="scope.row.name"
                  size="small"
                  maxlength="200"
                  @change="change"
                  :placeholder="keyText">
                </el-input>
                <el-autocomplete
                  :disabled="isReadOnly"
                  v-if="suggestions"
                  v-model="scope.row.name"
                  size="small"
                  :fetch-suggestions="querySearch"
                  @change="change"
                  :placeholder="keyText"
                  show-word-limit />
              </div>
              <div v-else-if="item.prop === 'required'">
                <el-select v-model="scope.row.required" size="small" style="width: 99%">
                  <el-option v-for="req in requireds" :key="req.id" :label="req.name" :value="req.id" />
                </el-select>
              </div>
              <div v-else-if="item.prop === 'value'">
                <div v-if="isActive && scope.row.type !== 'file'">
                  <el-autocomplete
                    :disabled="isReadOnly"
                    size="small"
                    class="input-with-autocomplete"
                    v-model="scope.row.value"
                    :fetch-suggestions="funcSearch"
                    :placeholder="valueText"
                    value-key="name"
                    highlight-first-item
                    @select="change">
                    <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced(scope.row)"></i>
                  </el-autocomplete>
                </div>
                <div v-else-if="isActive && scope.row.type === 'file'">
                  <ms-api-body-file-upload :parameter="scope.row" :id="id" :is-read-only="isReadOnly" />
                </div>
                <div v-else class="param-div-show">
                  <span class="param-span">{{ item.value }}</span>
                </div>
              </div>
              <div v-else-if="item.prop === 'contentType'">
                <el-input
                  :disabled="isReadOnly"
                  v-model="scope.row.contentType"
                  size="small"
                  @change="change"
                  :placeholder="$t('api_test.request.content_type')"
                  show-word-limit />
              </div>
              <div v-else-if="item.prop === 'min'">
                <el-input-number
                  :min="0"
                  v-model="scope.row.min"
                  :controls="false"
                  :placeholder="$t('schema.minLength')"
                  size="small"
                  style="width: 99%" />
              </div>
              <div v-else-if="item.prop === 'max'">
                <el-input-number
                  :min="0"
                  v-model="scope.row.max"
                  :controls="false"
                  :placeholder="$t('schema.maxLength')"
                  size="small"
                  style="width: 99%" />
              </div>
              <div v-else-if="item.prop === 'description'">
                <el-input
                  v-model="scope.row.description"
                  size="small"
                  maxlength="200"
                  :placeholder="$t('commons.description')"
                  show-word-limit>
                </el-input>
              </div>
              <div v-else-if="item.prop === 'urlEncode'">
                <el-select v-model="scope.row.urlEncode" size="small" clearable style="width: 100px">
                  <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"></el-option>
                </el-select>
              </div>
              <div v-else>
                {{ item.prop }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="uuid" :label="$t('commons.operating')" width="140px" show-overflow-tooltip>
          <template v-slot:default="scope">
            <el-switch
              size="mini"
              style="margin-right: 5px"
              :disabled="!(isShowEnable && !isDisable(scope.$index)) || isReadOnly"
              v-model="scope.row.enable" />
            <el-button
              size="mini"
              class="el-icon-delete-solid"
              style="margin-right: 5px"
              circle
              @click="remove(scope.$index)"
              :disabled="isDisable(scope.$index) || isReadOnly" />
            <i
              class="el-icon-top"
              v-show="!(isDisable(scope.$index) || isReadOnly)"
              style="cursor: pointer"
              @click="moveTop(scope.$index)" />
            <i
              class="el-icon-bottom"
              v-show="!(isDisable(scope.$index) || isReadOnly)"
              style="cursor: pointer"
              @click="moveBottom(scope.$index)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <ms-api-variable-advance
      ref="variableAdvance"
      :environment="environment"
      :scenario="scenario"
      :append-to-body="appendDialogToBody"
      :parameters="parameters"
      :current-item="currentItem"
      :scenario-definition="scenarioDefinition"
      @advancedRefresh="reload" />

    <ms-api-variable-json :append-to-body="appendDialogToBody" ref="variableJson" @callback="callback" />

    <api-variable-setting :append-to-body="appendDialogToBody" :suggestions="suggestions" ref="apiVariableSetting" />
  </div>
</template>

<script>
import { KeyValue, Scenario } from '../model/ApiTestModel';
import { JMETER_FUNC, MOCKJS_FUNC } from 'metersphere-frontend/src/utils/constants';
import MsApiVariableAdvance from './ApiVariableAdvance';
import MsApiVariableJson from './ApiVariableJson';
import MsApiBodyFileUpload from './body/ApiBodyFileUpload';
import Vue from 'vue';
import ApiVariableSetting from '@/business/definition/components/ApiVariableSetting';
import { getShowFields } from 'metersphere-frontend/src/utils/custom_field';

export default {
  name: 'MsApiVariable',
  components: {
    ApiVariableSetting,
    MsApiBodyFileUpload,
    MsApiVariableAdvance,
    MsApiVariableJson,
  },
  props: {
    id: String,
    urlEncode: {
      type: Boolean,
      default: false,
    },
    keyPlaceholder: String,
    valuePlaceholder: String,
    description: String,
    parameters: {
      type: Array,
      default() {
        return [];
      },
    },
    rest: Array,
    environment: Object,
    scenario: Scenario,
    type: {
      type: String,
      default: '',
    },
    paramType: {
      type: String,
      default: 'request',
    },
    appendDialogToBody: {
      type: Boolean,
      default() {
        return true;
      },
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    isShowEnable: {
      type: Boolean,
      default: true,
    },
    suggestions: Array,
    withMoreSetting: Boolean,
    scenarioDefinition: Array,
  },
  data() {
    return {
      currentItem: null,
      requireds: [
        { name: this.$t('commons.yes'), id: true },
        { name: this.$t('commons.no'), id: false },
      ],
      isSelectAll: true,
      isActive: true,
      paramColumns: [],
      storageKey: 'API_PARAMS_SHOW_FIELD',
      editRowIndex: -1,
      tableColumnArr: [],
      options: [
        {
          value: true,
          label: this.$t('commons.yes'),
        },
        {
          value: false,
          label: this.$t('commons.no'),
        },
      ],
    };
  },
  watch: {
    isSelectAll: function (to, from) {
      if (from == false && to == true) {
        this.selectAll();
      } else if (from == true && to == false) {
        this.invertSelect();
      }
    },

    paramColumns: {
      handler(val) {
        this.reload();
      },
      deep: true,
    },
  },
  computed: {
    keyText() {
      return this.keyPlaceholder || this.$t('api_test.key');
    },
    valueText() {
      return this.valuePlaceholder || this.$t('api_test.value');
    },
  },
  methods: {
    closeAllTableDataEdit() {
      this.parameters.forEach((item) => {
        item.isEdit = false;
      });
    },
    getTableMinWidth(col) {
      if (col === 'name') {
        return '200px';
      } else if (col === 'value') {
        return '300px';
      } else if (col === 'description') {
        return '200px';
      } else {
        return '120px';
      }
    },
    clickRow(row, column, event) {
      this.closeAllTableDataEdit();
      if (!this.isReadOnly) {
        this.$nextTick(() => {
          this.$set(row, 'isEdit', true);
        });
      }
    },
    initTableColumn() {
      this.tableColumnArr = [];
      if (this.type === 'body') {
        this.tableColumnArr.push({
          id: 0,
          prop: 'type',
          label: this.$t('api_test.definition.document.request_param') + this.$t('commons.type'),
        });
      }
      this.tableColumnArr.push({ id: 1, prop: 'name', label: this.$t('api_definition.document.name') });
      this.tableColumnArr.push({ id: 3, prop: 'required', label: this.$t('api_definition.document.is_required') });
      this.tableColumnArr.push({ id: 4, prop: 'value', label: this.$t('api_definition.document.value') });
      if (this.type === 'body') {
        this.tableColumnArr.push({ id: 2, prop: 'contentType', label: this.$t('api_definition.document.type') });
      }
      if (this.paramColumns) {
        this.paramColumns.forEach((item) => {
          let tableColumn = {};
          if (item === 'MIX_LENGTH') {
            tableColumn.id = 5;
            tableColumn.prop = 'min';
            tableColumn.label = this.$t('schema.minLength');
          } else if (item === 'MAX_LENGTH') {
            tableColumn.id = 6;
            tableColumn.prop = 'max';
            tableColumn.label = this.$t('schema.maxLength');
          } else if (item === 'ENCODE') {
            tableColumn.id = 7;
            tableColumn.prop = 'urlEncode';
            tableColumn.label = this.$t('commons.encode');
          } else if (item === 'DESCRIPTION') {
            tableColumn.id = 8;
            tableColumn.prop = 'description';
            tableColumn.label = this.$t('commons.description');
          } else {
            tableColumn = null;
          }
          if (tableColumn) {
            this.tableColumnArr.push(tableColumn);
          }
        });
      }
    },
    showColumns(columns) {
      return this.paramColumns.indexOf(columns) >= 0;
    },
    moveBottom(index) {
      if (this.parameters.length < 2 || index === this.parameters.length - 2) {
        return;
      }
      let thisRow = this.parameters[index];
      let nextRow = this.parameters[index + 1];
      Vue.set(this.parameters, index + 1, thisRow);
      Vue.set(this.parameters, index, nextRow);
    },
    moveTop(index) {
      if (index === 0) {
        return;
      }
      let thisRow = this.parameters[index];
      let lastRow = this.parameters[index - 1];
      Vue.set(this.parameters, index - 1, thisRow);
      Vue.set(this.parameters, index, lastRow);
    },
    remove: function (index) {
      // 移除整行输入控件及内容
      this.parameters.splice(index, 1);
      this.$emit('change', this.parameters);
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndexArr = [];
      this.parameters.forEach((item, index) => {
        if (
          (!item.name || item.name === '') &&
          (!item.value || item.value === '') &&
          (!item.files || item.files.length === 0)
        ) {
          // 多余的空行
          removeIndexArr.push(index);
        }
      });
      if (removeIndexArr.length > 0) {
        for (let i = removeIndexArr.length - 1; i >= 0; i--) {
          this.remove(removeIndexArr[i]);
        }
      }
      let removeIndex = -1;
      this.parameters.forEach((item, index) => {
        if (
          (!item.name || item.name === '') &&
          (!item.value || item.value === '') &&
          (!item.files || item.files.length === 0)
        ) {
          // 多余的空行
          if (index !== this.parameters.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });

      if (isNeedCreate) {
        this.parameters.push(
          new KeyValue({
            contentType: 'text/plain',
            description: null,
            enable: true,
            file: false,
            files: null,
            isEdit: false,
            max: 0,
            min: 0,
            required: false,
            type: 'text',
            urlEncode: this.urlEncode,
            uuid: this.uuid(),
            valid: false,
            value: null,
          })
        );
      }
      this.$emit('change', this.parameters);
      // TODO 检查key重复
    },
    isDisable: function (index) {
      return this.parameters.length - 1 == index;
    },
    querySearch(queryString, cb) {
      let suggestions = this.suggestions;
      let results = queryString ? suggestions.filter(this.createFilter(queryString)) : suggestions;
      cb(results);
    },
    createFilter(queryString) {
      return (restaurant) => {
        return restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0;
      };
    },
    funcSearch(queryString, cb) {
      let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
      let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    funcFilter(queryString) {
      return (func) => {
        return func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1;
      };
    },
    uuid: function () {
      return (((1 + Math.random()) * 0x100000) | 0).toString(16).substring(1);
    },
    advanced(item) {
      if (item.type === 'json') {
        this.appendDialogToBody = true;
        this.$refs.variableJson.open(item);
        this.currentItem = item;
      } else {
        this.currentItem = item;
        // 场景编辑参数设置冒泡，调用父组件的参数设置打开方法
        if (this.scenarioDefinition != undefined) {
          this.$emit('editScenarioAdvance', this.currentItem);
        } else {
          this.$refs.variableAdvance.open();
        }
      }
    },
    typeChange(item) {
      if (item.type === 'file') {
        this.$set(item, 'contentType', 'application/octet-stream');
      } else if (item.type === 'text') {
        this.$set(item, 'contentType', 'text/plain');
      } else {
        this.$set(item, 'contentType', 'application/json');
      }
      this.reload();
    },
    selectAll() {
      this.parameters.forEach((item) => {
        item.enable = true;
      });
    },
    invertSelect() {
      this.parameters.forEach((item) => {
        item.enable = false;
      });
    },
    reload() {
      this.isActive = false;
      this.$nextTick(() => {
        this.isActive = true;
      });
    },
    openApiVariableSetting(item) {
      this.$refs.apiVariableSetting.open(item);
    },
    callback(item) {
      this.currentItem.value = item;
      this.currentItem = null;
    },
  },
  created() {
    if (this.paramType === 'response') {
      this.storageKey = 'API_RESPONSE_PARAMS_SHOW_FIELD';
    }
    if (this.parameters.length === 0 || this.parameters[this.parameters.length - 1].name) {
      //如果参数最后没有空白行，需要加入一个空白行
      this.parameters.push(
        new KeyValue({
          contentType: 'text/plain',
          description: null,
          enable: true,
          file: false,
          files: null,
          isEdit: false,
          max: 0,
          min: 0,
          required: false,
          type: 'text',
          urlEncode: this.urlEncode,
          uuid: this.uuid(),
          valid: false,
          value: null,
        })
      );
    } else {
      //检查最后一个空白行是否缺少参数，缺少参数会影响数据反显.(多存在于旧数据）
      if (!this.parameters[this.parameters.length - 1].max) {
        this.$set(this.parameters[this.parameters.length - 1], 'max', 0);
      }
      if (!this.parameters[this.parameters.length - 1].min) {
        this.$set(this.parameters[this.parameters.length - 1], 'min', 0);
      }
      if (!this.parameters[this.parameters.length - 1].value) {
        this.$set(this.parameters[this.parameters.length - 1], 'value', null);
      }
      if (!this.parameters[this.parameters.length - 1].uuid) {
        this.$set(this.parameters[this.parameters.length - 1], 'uuid', this.uuid());
      }
      if (!this.parameters[this.parameters.length - 1].description) {
        this.$set(this.parameters[this.parameters.length - 1], 'description', null);
      }
      this.$set(this.parameters[this.parameters.length - 1], 'files', null);
    }
    let savedApiParamsShowFields = getShowFields(this.storageKey);
    if (savedApiParamsShowFields) {
      this.paramColumns = savedApiParamsShowFields;
    }
    this.parameters.forEach((item) => {
      this.$set(item, 'isEdit', false);
    });
    this.initTableColumn();
  },
  activated() {
    this.initTableColumn();
  },
  mounted() {
    this.initTableColumn();
  },
};
</script>

<style scoped>
.kv-description {
  font-size: 13px;
}

.kv-row {
  margin-top: 10px;
}

.kv-delete {
  width: 60px;
}

.kv-select {
  width: 30%;
}

.el-autocomplete {
  width: 100%;
}

.kv-checkbox {
  width: 20px;
  margin-right: 10px;
}

.advanced-item-value :deep(.el-dialog__body) {
  padding: 15px 25px;
}

.el-row {
  margin-bottom: 5px;
}

.kv-type {
  width: 70px;
}

.pointer {
  cursor: pointer;
  color: #1e90ff;
}

.kv-setting {
  width: 40px;
  padding: 0px !important;
}

.param-header-span {
  margin-bottom: 5px;
  font-weight: 600;
}

.param-div-show {
  min-height: 16px;
}
</style>
