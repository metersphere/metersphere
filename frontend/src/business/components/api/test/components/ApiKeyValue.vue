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
               width="70%">
      <el-tabs tab-position="left" style="height: 40vh;">
        <el-tab-pane :label="$t('api_test.request.parameters_advance_mock')">
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
            <el-col :span="6" v-for="(itemFunc, itemIndex) in itemFuncs" :key="itemIndex">
              <div v-for="(func, funcIndex) in funcs"
                   :key="`${itemIndex}-${funcIndex}`">
                <el-row>
                  <el-col :span="12">
                    <el-radio size="mini" v-model="itemFunc.name" :label="func.name"
                              @change="methodChange(itemFunc, func)"/>
                  </el-col>
                  <el-col :span="12" v-if="itemFunc.name === func.name">
                    <div v-for="(p, pIndex) in itemFunc.params" :key="`${itemIndex}-${funcIndex}-${pIndex}`">
                      <el-input :placeholder="p.name" size="mini" v-model="p.value" @change="showPreview"/>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="变量"></el-tab-pane>
      </el-tabs>

      <div style="padding-top: 10px;">
        <el-row type="flex" align="middle">
          <el-col :span="12">
            <el-button size="small" type="primary" plain @click="saveAdvanced()">
              {{ $t('commons.save') }}
            </el-button>
            <el-button size="small" type="info" plain @click="addFunc()">
              {{ $t('api_test.request.parameters_advance_add_func') }}
            </el-button>
            <el-button size="small" type="success" plain @click="showPreview()">
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
    showPreview() {
      // 找到变量本身
      if (!this.itemValue) {
        return;
      }
      let index = this.itemValue.indexOf("|");
      if (index > -1) {
        this.itemValue = this.itemValue.substring(0, index).trim();
      }

      this.itemFuncs.forEach(f => {
        if (!f.name) {
          return;
        }
        this.itemValue += "|" + f.name;
        if (f.params) {
          this.itemValue += ":" + f.params.map(p => p.value).join(",");
        }
      });

      this.itemValuePreview = calculate(this.itemValue);
    },
    methodChange(itemFunc, func) {
      let index = this.itemFuncs.indexOf(itemFunc);
      this.itemFuncs = this.itemFuncs.slice(0, index);
      // 这里要用 deep copy
      this.itemFuncs.push(JSON.parse(JSON.stringify(func)));
      this.showPreview();
    },
    addFunc() {
      if (this.itemFuncs.length > 4) {
        this.$info(this.$t('api_test.request.parameters_advance_add_func_limit'));
        return;
      }
      if (this.itemFuncs.length > 0) {
        let func = this.itemFuncs[this.itemFuncs.length - 1];
        if (!func.name) {
          this.$warning(this.$t('api_test.request.parameters_advance_add_func_error'));
          return;
        }
        if (func.params) {
          for (let j = 0; j < func.params.length; j++) {
            if (!func.params[j].value) {
              this.$warning(this.$t('api_test.request.parameters_advance_add_param_error'));
              return;
            }
          }
        }
      }
      this.itemFuncs.push({name: '', params: []});
    },
    advanced(item) {
      this.currentItem = item;
      this.itemValueVisible = true;
      this.itemValue = '';
      this.itemValuePreview = null;
      this.itemFuncs = [];
    },
    saveAdvanced() {
      this.currentItem.value = this.itemValue;
      this.itemValueVisible = false;
      this.itemFuncs = [];
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

.el-row {
  margin-bottom: 5px;
}

</style>
