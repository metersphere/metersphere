<template>
  <div>
    <span class="kv-description" v-if="description">
      {{ description }}
    </span>
    <div class="kv-row" v-for="(item, index) in items" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">
        <el-col>
          <el-input v-if="!suggestions" :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200"
                    @change="change"
                    :placeholder="keyText" show-word-limit/>
          <el-autocomplete :disabled="isReadOnly" :maxlength="200" v-if="suggestions" v-model="item.name" size="small"
                           :fetch-suggestions="querySearch" @change="change" :placeholder="keyText"
                           show-word-limit/>

        </el-col>
        <el-col>
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
            <i slot="suffix" class="el-input__icon el-icon-edit" style="cursor: pointer;" @click="advanced(item)"></i>
          </el-autocomplete>
        </el-col>
        <el-col class="kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index) || isReadOnly"/>
        </el-col>
      </el-row>
    </div>

    <el-dialog :title="$t('api_test.request.parameters_advance')"
               :visible.sync="itemValueVisible"
               class="advanced-item-value"
               width="50%">
      <!--      <el-form>
              <el-form-item>
                <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" :placeholder="valueText"
                          v-model="itemValue"/>
              </el-form-item>
            </el-form>
            <div>
              <el-row type="flex" align="middle">
                <el-col :span="6">
                  <el-button size="small" type="primary" plain @click="saveAdvanced()">
                    {{ $t('commons.save') }}
                  </el-button>
                  <el-button size="small" type="success" plain @click="showPreview(itemValue)">
                    {{ $t('api_test.request.parameters_preview') }}
                  </el-button>
                </el-col>
                <el-col>
                  <div> {{ itemValuePreview }}</div>
                </el-col>
              </el-row>
            </div>

            <div class="format-tip">
              <div>
                <p>{{ $t('api_test.request.parameters_filter') }}：
                  <el-tag size="mini" v-for="func in funcs" :key="func" @click="appendFunc(func)"
                          style="margin-left: 2px;cursor: pointer;">
                    <span>{{ func }}</span>
                  </el-tag>
                </p>
              </div>
              <div>
                <span>{{ $t('api_test.request.parameters_filter_desc') }}：
                  <el-link href="http://mockjs.com/examples.html" target="_blank">http://mockjs.com/examples.html</el-link>
                </span>
                <p>{{ $t('api_test.request.parameters_filter_example') }}：@string(10) | md5 | substr: 1, 3</p>
                <p>{{ $t('api_test.request.parameters_filter_example') }}：@integer(1, 5) | concat:_metersphere</p>
                <p><strong>{{ $t('api_test.request.parameters_filter_tips') }}</strong></p>
              </div>
            </div>-->
      <el-tabs tab-position="left" style="height: 60vh;">
        <el-tab-pane label="Mock 数据">
          <el-row type="flex" :gutter="20" style="overflow-x: auto;">
            <el-col :span="6">
              <el-autocomplete
                :disabled="isReadOnly"
                size="small"
                class="input-with-autocomplete"
                v-model="itemValue"
                :fetch-suggestions="funcSearch"
                :placeholder="valueText"
                value-key="name"
                highlight-first-item
                @select="change">
              </el-autocomplete>
            </el-col>
            <el-col :span="6">
              <el-row v-for="(func, index) in funcs" :key="index">
                <div @click="addFunc(func)" style="padding-top: 5px;">
                  <el-col :span="12">
                    <el-radio v-model="currentFunc" :label="func.name">{{ func.name }}</el-radio>
                  </el-col>
                  <el-col :span="12">
                    <el-input size="mini" :placeholder="param.name" v-model="param.value"
                              v-for="(param, index) in func.params" :key="index"/>
                  </el-col>
                </div>
              </el-row>
            </el-col>
            <!--<el-col v-for="(f, index) in itemFuncs" :key="index">
              <el-menu>
                <el-menu-item v-for="(func, index) in funcs.slice(0, 5)" :key="index" @click="addFunc(func)">
                  {{ func.func }}
                </el-menu-item>
              </el-menu>
            </el-col>-->
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="变量"></el-tab-pane>
      </el-tabs>

      <div style="padding-top: 10px;">
        <el-row type="flex" align="middle">
          <el-col :span="6">
            <el-button size="small" type="primary" plain @click="saveAdvanced()">
              {{ $t('commons.save') }}
            </el-button>
            <el-button size="small" type="success" plain @click="showPreview(itemValue)">
              {{ $t('api_test.request.parameters_preview') }}
            </el-button>
          </el-col>
          <el-col>
            <div> {{ itemValuePreview }}</div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {KeyValue} from "../model/ScenarioModel";
import {MOCKJS_FUNC} from "@/common/js/constants";
import {calculate} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsApiKeyValue",

  props: {
    keyPlaceholder: String,
    valuePlaceholder: String,
    description: String,
    items: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    suggestions: Array
  },

  data() {
    return {
      itemValueVisible: false,
      itemValue: null,
      funcs: [
        {name: "md5"},
        {name: "base64"},
        {name: "unbase64"},
        {
          name: "substr",
          params: [{name: "start"}, {name: "end"}]
        },
        {
          name: "concat",
          params: [{name: "suffix"}]
        },
        {name: "lconcat", params: [{name: "prefix"}]},
        {name: "sha1"},
        {name: "sha224"},
        {name: "sha256"},
        {name: "sha384"},
        {name: "sha512"},
        {name: "lower"},
        {name: "upper"},
        {name: "length"},
        {name: "number"}
      ],
      itemValuePreview: null,
      itemFuncs: [],
      currentFunc: ""
    }
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
    remove: function (index) {
      this.items.splice(index, 1);
      this.$emit('change', this.items);
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndex = -1;
      this.items.forEach((item, index) => {
        if (!item.name && !item.value) {
          // 多余的空行
          if (index !== this.items.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (isNeedCreate) {
        this.items.push(new KeyValue());
      }
      this.$emit('change', this.items);
      // TODO 检查key重复
    },
    isDisable: function (index) {
      return this.items.length - 1 === index;
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
      let funcs = MOCKJS_FUNC;
      let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    funcFilter(queryString) {
      return (func) => {
        return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
      };
    },
    showPreview(itemValue) {
      this.itemValuePreview = calculate(itemValue);
    },
    addFunc(func) {
      this.itemFuncs.push(func);
    },
    appendFunc(func) {
      if (this.itemValue) {
        this.itemValue += " | " + func;
      } else {
        this.$warning(this.$t("api_test.request.parameters_preview_warning"));
      }
    },
    advanced(item) {
      this.currentItem = item;
      this.itemValueVisible = true;
      this.itemValue = item.value;
      this.itemValuePreview = null;
    },
    saveAdvanced() {
      this.currentItem.value = this.itemValue;
      this.itemValueVisible = false;
    }
  },
  created() {
    if (this.items.length === 0) {
      this.items.push(new KeyValue());
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

.el-autocomplete {
  width: 100%;
}

.advanced-item-value >>> .el-dialog__body {
  padding: 15px 25px;
}

.format-tip {
  background: #EDEDED;
}

.format-tip {
  border: solid #E1E1E1 1px;
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
}

.func-background {
  background: #2395f1;
}

</style>
