<template>
  <div style="margin-bottom: 20px">
    <span class="kv-description" v-if="description">
      {{ description }}
    </span>
    <div class="item kv-row" v-for="(item, index) in parameters" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">
        <span style="margin-left: 10px"></span>
        <i class="el-icon-top" style="cursor:pointer" @click="moveTop(index)"/>
        <i class="el-icon-bottom" style="cursor:pointer;" @click="moveBottom(index)"/>

        <el-col class="item">
          <el-input v-if="!suggestions" :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200"
                    @change="change" :placeholder="keyText" show-word-limit>
            <template v-slot:prepend>
              <el-select v-if="type === 'body'" :disabled="isReadOnly" class="kv-type" v-model="item.type"
                         @change="typeChange(item)">
                <el-option value="text"/>
                <el-option value="file"/>
                <el-option value="json"/>
              </el-select>
            </template>
          </el-input>
          <el-autocomplete :disabled="isReadOnly" v-if="suggestions" v-model="item.name" size="small"
                           :fetch-suggestions="querySearch" @input="change" @change="change" :placeholder="keyText" show-word-limit>
            <template v-slot:prepend>
              <el-select v-if="type === 'body'" :disabled="isReadOnly" class="kv-type" v-model="item.type"
                         @change="typeChange(item)">
                <el-option value="text"/>
                <el-option value="file"/>
                <el-option value="json"/>
              </el-select>
            </template>
          </el-autocomplete>

        </el-col>

        <el-col class="item" v-if="isActive && item.type !== 'file'">
          <el-select :disabled="isReadOnly" :maxlength="100" v-model="item.rangeType" size="small" style="width: 100%">
            <el-option
              v-for="item in rangeTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
          </el-select>
        </el-col>
        <el-col class="item" v-if="isActive && item.type !== 'file'">
          <el-input-number v-if="item.rangeType === 'length_eq' || item.rangeType === 'length_not_eq' || item.rangeType === 'length_large_than' || item.rangeType === 'length_shot_than'"
                           v-model="item.value" size="small" :placeholder="valueText" show-word-limit />
          <el-autocomplete
            v-else
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

        <el-col class="item">
          <el-input v-model="item.description" size="small" maxlength="200"
                    :placeholder="$t('commons.description')" show-word-limit>
          </el-input>
        </el-col>

        <el-col v-if="isActive && item.type === 'file'" class="item">
          <ms-api-body-file-upload :parameter="item"/>
        </el-col>

        <el-col v-if="type === 'body'" class="item kv-select">
          <el-input :disabled="isReadOnly" v-model="item.contentType" size="small"
                    @change="change" :placeholder="$t('api_test.request.content_type')" show-word-limit>
          </el-input>
        </el-col>

        <el-col class="item kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index) || isReadOnly"/>
        </el-col>

      </el-row>
    </div>
    <ms-api-variable-advance ref="variableAdvance" :environment="environment" :scenario="scenario"
                             :append-to-body="appendDialogToBody"
                             :parameters="parameters"
                             :current-item="currentItem"/>
    <ms-api-variable-json :append-to-body="appendDialogToBody" ref="variableJson" @callback="callback"/>

    <api-variable-setting :append-to-body="appendDialogToBody"
                          ref="apiVariableSetting"/>

  </div>
</template>

<script>
import {KeyValue, Scenario} from "@/business/components/api/definition/model/ApiTestModel";
import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
import MsApiVariableAdvance from "@/business/components/api/definition/components/ApiVariableAdvance";
import MsApiVariableJson from "@/business/components/api/definition/components/ApiVariableJson";
import MsApiBodyFileUpload from "@/business/components/api/definition/components/body/ApiBodyFileUpload";
import Vue from 'vue';
import ApiVariableSetting from "@/business/components/api/definition/components/ApiVariableSetting";

export default {
  name: "MsApiVariable",
  components: {ApiVariableSetting, MsApiBodyFileUpload, MsApiVariableAdvance, MsApiVariableJson},
  props: {
    keyPlaceholder: String,
    valuePlaceholder: String,
    description: String,
    parameters: Array,
    rest: Array,
    environment: Object,
    scenario: Scenario,
    type: {
      type: String,
      default: ''
    },
    appendDialogToBody: {
      type: Boolean,
      default() {
        return false;
      }
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isShowEnable: {
      type: Boolean,
      default: true
    },
    suggestions: Array,
    withMorSetting: Boolean
  },
  data() {
    return {
      currentItem: null,
      requireds: [
        {name: this.$t('commons.selector.required'), id: true},
        {name: this.$t('commons.selector.not_required'), id: false}
      ],
      isSelectAll: true,
      isActive: true,
      rangeTypeOptions: [
        {
          value: "",
          label: this.$t("commons.please_select"),
        },
        {
          value: "value_eq",
          label: this.$t("api_test.mock.range_type.value_eq"),
        },
        {
          value: "value_not_eq",
          label: this.$t("api_test.mock.range_type.value_not_eq"),
        },
        {
          value: "value_contain",
          label: this.$t("api_test.mock.range_type.value_contain"),
        },
        {
          value: "length_eq",
          label: this.$t("api_test.mock.range_type.length_eq"),
        },
        {
          value: "length_not_eq",
          label: this.$t("api_test.mock.range_type.length_not_eq"),
        },
        {
          value: "length_large_than",
          label: this.$t("api_test.mock.range_type.length_large_than"),
        },
        {
          value: "length_shot_than",
          label: this.$t("api_test.mock.range_type.length_shot_than"),
        },
        {
          value: "regular_match",
          label: this.$t("api_test.mock.range_type.regular_match"),
        },
      ],
    }
  },
  watch: {
    isSelectAll: function (to, from) {
      if (from == false && to == true) {
        this.selectAll();
      } else if (from == true && to == false) {
        this.invertSelect();
      }
    },
  },
  computed: {
    keyText() {
      return this.keyPlaceholder || this.$t("api_test.key");
    },
    valueText() {
      return this.valuePlaceholder || this.$t("api_test.value");
    }
  },
  methods: {
    moveBottom(index) {
      if (this.parameters.length < 2 || index === this.parameters.length - 2) {
        return;
      }
      let thisRow = this.parameters[index];
      let nextRow = this.parameters[index + 1];
      Vue.set(this.parameters, index + 1, thisRow);
      Vue.set(this.parameters, index, nextRow)
    },
    moveTop(index) {
      if (index === 0) {
        return;
      }
      let thisRow = this.parameters[index];
      let lastRow = this.parameters[index - 1];
      Vue.set(this.parameters, index - 1, thisRow);
      Vue.set(this.parameters, index, lastRow)

    },
    remove: function (index) {
      // 移除整行输入控件及内容
      this.parameters.splice(index, 1);
      this.$emit('change', this.parameters);
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndex = -1;
      this.parameters.forEach((item, index) => {
        if (!item.name && !item.value) {
          // 多余的空行
          if (index !== this.parameters.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (isNeedCreate) {
        this.parameters.push(new KeyValue({
          type: 'text',
          enable: true,
          uuid: this.uuid(),
          contentType: 'text/plain'
        }));
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
        return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
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
        return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
      };
    },
    uuid: function () {
      return (((1 + Math.random()) * 0x100000) | 0).toString(16).substring(1);
    },
    advanced(item) {
      if (item.type === 'json') {
        this.$refs.variableJson.open(item);
        this.currentItem = item;
      } else {
        this.$refs.variableAdvance.open();
        this.currentItem = item;
      }

    },
    typeChange(item) {
      if (item.type === 'file') {
        item.contentType = 'application/octet-stream';
      } else if (item.type === 'text') {
        item.contentType = 'text/plain';
      } else {
        item.contentType = 'application/json'
      }
      this.reload();
    },
    selectAll() {
      this.parameters.forEach(item => {
        item.enable = true;
      });
    },
    invertSelect() {
      this.parameters.forEach(item => {
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
    }
  },
  created() {
    if (this.parameters.length === 0 || this.parameters[this.parameters.length - 1].name) {
      this.parameters.push(new KeyValue({
        type: 'text',
        enable: true,
        required: true,
        uuid: this.uuid(),
        contentType: 'text/plain'
      }));
    }
  }
}
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
  width: 50%;
}

.el-autocomplete {
  width: 100%;
}

.kv-checkbox {
  width: 20px;
  margin-right: 10px;
}

.advanced-item-value >>> .el-dialog__body {
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
  color: #1E90FF;
}

.kv-setting {
  width: 40px;
  padding: 0px !important;
}
</style>
