<template>
  <div style="margin-bottom: 20px">
    <div style="overflow: auto; width: 100%">
      <span class="kv-description" v-if="description">
        {{ description }}
      </span>
      <el-row>
        <el-checkbox v-model="isSelectAll" v-if="parameters.length > 1" />
      </el-row>

      <!--      数据-->
      <div class="item kv-row" v-for="(item, index) in parameters" :key="index" style="width: 99%">
        <el-row type="flex" :gutter="20" justify="space-between" align="middle">
          <el-col class="kv-checkbox" v-if="isShowEnable">
            <el-checkbox v-if="!isDisable(index)" v-model="item.enable" :disabled="isReadOnly" />
          </el-col>
          <span style="margin-left: 10px" v-else></span>
          <i class="el-icon-top" style="cursor: pointer" @click="moveTop(index)" />
          <i class="el-icon-bottom" style="cursor: pointer" @click="moveBottom(index)" />

          <el-col class="item" style="min-width: 200px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ keyText }}</span>
            </el-row>
            <el-input
              v-if="!suggestions"
              :disabled="isReadOnly"
              v-model="item.name"
              size="small"
              maxlength="200"
              @change="change"
              :placeholder="keyText"
              show-word-limit>
              <template v-slot:prepend>
                <el-select
                  v-if="type === 'body'"
                  :disabled="isReadOnly"
                  class="kv-type"
                  v-model="item.type"
                  @change="typeChange(item)">
                  <el-option value="text" />
                  <el-option value="file" />
                  <el-option value="json" />
                </el-select>
              </template>
            </el-input>

            <el-autocomplete
              :disabled="isReadOnly"
              v-if="suggestions"
              v-model="item.name"
              size="small"
              :fetch-suggestions="querySearch"
              @change="change"
              :placeholder="keyText"
              show-word-limit />
          </el-col>

          <el-col class="item kv-select" style="width: 130px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0">
                {{ $t('api_test.definition.document.table_coloum.is_required') }}</span
              >
            </el-row>
            <el-select v-model="item.required" size="small" style="width: 120px">
              <el-option v-for="req in requireds" :key="req.id" :label="req.name" :value="req.id" />
            </el-select>
          </el-col>

          <el-col class="item" v-if="isActive && item.type !== 'file'" style="min-width: 200px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ valueText }}</span>
            </el-row>
            <el-autocomplete
              :disabled="isReadOnly"
              size="small"
              class="input-with-autocomplete"
              v-model="item.value"
              :fetch-suggestions="funcSearch"
              :placeholder="valueText"
              value-key="name"
              highlight-first-item
              @select="change">
              <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced(item)"></i>
            </el-autocomplete>
          </el-col>

          <el-col v-if="isActive && item.type === 'file'" class="item" style="min-width: 200px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ valueText }}</span>
            </el-row>
            <ms-api-body-file-upload :parameter="item" :id="id" :is-read-only="isReadOnly" />
          </el-col>

          <el-col v-if="type === 'body'" class="item kv-select" style="min-width: 160px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ $t('api_test.request.content_type') }}</span>
            </el-row>
            <el-input
              :disabled="isReadOnly"
              v-model="item.contentType"
              size="small"
              @change="change"
              :placeholder="$t('api_test.request.content_type')"
              show-word-limit>
            </el-input>
          </el-col>

          <el-col v-if="showColumns('MIX_LENGTH')" class="item kv-select" style="width: 150px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ $t('schema.minLength') }}</span>
            </el-row>
            <el-input-number
              :min="0"
              v-model="item.min"
              :placeholder="$t('schema.minLength')"
              size="small"
              style="width: 140px" />
          </el-col>

          <el-col v-if="showColumns('MAX_LENGTH')" class="item kv-select" style="width: 150px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ $t('schema.maxLength') }}</span>
            </el-row>
            <el-input-number
              :min="0"
              v-model="item.max"
              :placeholder="$t('schema.maxLength')"
              size="small"
              style="width: 140px" />
          </el-col>

          <el-col v-if="showColumns('ENCODE')" class="item kv-select" style="width: 130px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ $t('commons.encode') }}</span>
            </el-row>
            <el-select v-model="item.urlEncode" size="small" clearable style="width: 100px">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-col>

          <el-col class="item" v-if="showColumns('DESCRIPTION')" style="min-width: 300px; padding: 0 5px">
            <el-row>
              <span class="param-header-span" v-if="index === 0"> {{ $t('commons.description') }}</span>
            </el-row>
            <el-input
              v-model="item.description"
              size="small"
              maxlength="200"
              :placeholder="$t('commons.description')"
              show-word-limit>
            </el-input>
          </el-col>

          <el-col v-if="withMoreSetting" class="item kv-setting">
            <el-tooltip effect="dark" :content="$t('schema.adv_setting')" placement="top">
              <i class="el-icon-setting" @click="openApiVariableSetting(item)" />
            </el-tooltip>
          </el-col>

          <el-col class="item kv-delete">
            <el-button
              size="mini"
              class="el-icon-delete-solid"
              circle
              @click="remove(index)"
              :disabled="isDisable(index) || isReadOnly" />
          </el-col>
        </el-row>
      </div>
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
        { name: this.$t('commons.selector.required'), id: true },
        { name: this.$t('commons.selector.not_required'), id: false },
      ],
      isSelectAll: true,
      isActive: true,
      paramColumns: [],
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
            type: 'text',
            enable: true,
            urlEncode: this.urlEncode,
            uuid: this.uuid(),
            required: false,
            contentType: 'text/plain',
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
        item.contentType = 'application/octet-stream';
      } else if (item.type === 'text') {
        item.contentType = 'text/plain';
      } else {
        item.contentType = 'application/json';
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
    if (this.parameters.length === 0 || this.parameters[this.parameters.length - 1].name) {
      this.parameters.push(
        new KeyValue({
          type: 'text',
          enable: true,
          required: false,
          urlEncode: this.urlEncode,
          uuid: this.uuid(),
          contentType: 'text/plain',
        })
      );
    }
    let savedApiParamsShowFields = getShowFields('API_PARAMS_SHOW_FIELD');
    if (savedApiParamsShowFields) {
      this.paramColumns = savedApiParamsShowFields;
    }
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
</style>
