<template>
  <div>
    <span class="kv-description" v-if="description">
      {{ description }}
    </span>
    <div class="kv-row" v-for="(item, index) in parameters" :key="index">
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
      <el-tabs tab-position="top" style="height: 50vh;" @tab-click="selectTab">
        <el-tab-pane :label="$t('api_test.request.parameters_advance_mock')">
          <el-row type="flex" :gutter="20">
            <el-col :span="6" class="col-height">
              <div>
                <el-input size="small" v-model="filterText"
                          :placeholder="$t('api_test.request.parameters_mock_filter_tips')"/>
                <el-tree class="filter-tree" ref="tree" :data="mockFuncs" :props="treeProps"
                         default-expand-all @node-click="selectVariable"
                         :filter-node-method="filterNode"></el-tree>
              </div>
            </el-col>
            <el-col :span="6" v-for="(itemFunc, itemIndex) in mockVariableFuncs" :key="itemIndex">
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
        <el-tab-pane label="变量">
          <el-row>
            <el-col :span="6" class="col-height">
              <div v-if="environment">
                <p>{{ $t('api_test.environment.environment') }}</p>
                <el-tree :data="environmentParams" :props="treeProps" @node-click="selectVariable"></el-tree>
              </div>
              <div v-if="scenario">
                <p>{{ $t('api_test.scenario.scenario') }}</p>
                <el-tree :data="scenarioParams" :props="treeProps" @node-click="selectVariable"></el-tree>
              </div>
              <div v-if="preRequestParams">
                <p>{{ $t('api_test.request.parameters_pre_request') }}</p>
                <el-tree :data="preRequestParams" :props="treeProps" @node-click="selectVariable"></el-tree>
              </div>
            </el-col>
            <el-col :span="6" v-for="(itemFunc, itemIndex) in jmeterVariableFuncs" :key="itemIndex" class="col-height">
              <div>
                <div v-for="(func, funcIndex) in jmeterFuncs"
                     :key="`${itemIndex}-${funcIndex}`">
                  <el-row>
                    <el-radio size="mini" v-model="itemFunc.name" :label="func.name"
                              @change="methodChange(itemFunc, func)"/>
                  </el-row>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
      <el-form>
        <el-form-item>
          <el-input :placeholder="valueText" size="small"
                    v-model="itemValue"/>
        </el-form-item>
      </el-form>
      <div style="padding-top: 10px;">
        <el-row type="flex" align="middle">
          <el-col :span="12">
            <el-button size="small" type="primary" plain @click="saveAdvanced()">
              {{ $t('commons.save') }}
            </el-button>
            <el-button size="small" type="info" plain @click="addFunc()" v-if="currentTab === 0">
              {{ $t('api_test.request.parameters_advance_add_func') }}
            </el-button>
            <el-button size="small" type="success" plain @click="showPreview()" v-if="currentTab === 0">
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
import {HttpRequest, KeyValue, Scenario} from "../model/ScenarioModel";
import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
import {calculate} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsApiVariable",

  props: {
    keyPlaceholder: String,
    valuePlaceholder: String,
    description: String,
    request: HttpRequest,
    environment: Object,
    scenario: Scenario,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    suggestions: Array
  },
  mounted() {
    if (this.request) {
      this.parameters = this.request.parameters;
    }
    if (this.scenario) {
      let variables = this.scenario.variables;
      this.scenarioParams = [
        {
          name: this.scenario.name,
          children: variables.filter(v => v.name).map(v => {
            return {name: v.name, value: '${' + v.name + '}'}
          }),
        }
      ];
      if (this.environment) {
        let variables = JSON.parse(this.environment.variables);
        this.environmentParams = [
          {
            name: this.environment.name,
            children: variables.filter(v => v.name).map(v => {
              return {name: v.name, value: '${' + v.name + '}'}
            }),
          }
        ];
      }
      let i = this.scenario.requests.indexOf(this.request);
      this.preRequests = this.scenario.requests.slice(0, i);
      this.preRequests.forEach(r => {
        let js = r.extract.json.map(v => {
          return {name: v.variable, value: v.value}
        });
        let xs = r.extract.xpath.map(v => {
          return {name: v.variable, value: v.value}
        });
        let rx = r.extract.regex.map(v => {
          return {name: v.variable, value: v.value}
        });
        let vs = [...js, ...xs, ...rx];
        if (vs.length > 0) {
          this.preRequestParams.push({name: r.name, children: vs});
        }
      });
    }

  },
  data() {
    return {
      filterText: '',
      itemValueVisible: false,
      itemValue: null,
      funcs: [
        {name: "md5"},
        {name: "base64"},
        {name: "unbase64"},
        {
          name: "substr",
          params: [{name: "start"}, {name: "length"}]
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
      mockVariableFuncs: [],
      jmeterVariableFuncs: [],
      currentTab: 0,
      mockFuncs: MOCKJS_FUNC.map(f => {
        return {name: f.name, value: f.name}
      }),
      jmeterFuncs: JMETER_FUNC,
      environmentParams: [],
      scenarioParams: [],
      parameters: [],
      preRequests: [],
      preRequestParams: [],
      treeProps: {children: 'children', label: 'name'}
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
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
        this.parameters.push(new KeyValue());
      }
      this.$emit('change', this.parameters);
      // TODO 检查key重复
    },
    isDisable: function (index) {
      return this.parameters.length - 1 === index;
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
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    selectVariable(node) {
      this.itemValue = node.value;
    },
    selectTab(tab) {
      this.currentTab = +tab.index;
      this.itemValue = null;
      this.itemValuePreview = null;
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

      this.mockVariableFuncs.forEach(f => {
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
      let index = this.mockVariableFuncs.indexOf(itemFunc);
      this.mockVariableFuncs = this.mockVariableFuncs.slice(0, index);
      // 这里要用 deep copy
      this.mockVariableFuncs.push(JSON.parse(JSON.stringify(func)));
      this.showPreview();
    },
    addFunc() {
      if (this.mockVariableFuncs.length > 4) {
        this.$info(this.$t('api_test.request.parameters_advance_add_func_limit'));
        return;
      }
      if (this.mockVariableFuncs.length > 0) {
        let func = this.mockVariableFuncs[this.mockVariableFuncs.length - 1];
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
      this.mockVariableFuncs.push({name: '', params: []});
    },
    advanced(item) {
      this.currentItem = item;
      this.itemValueVisible = true;
      this.itemValue = '';
      this.itemValuePreview = null;
      this.mockVariableFuncs = [];
    },
    saveAdvanced() {
      this.currentItem.value = this.itemValue;
      this.itemValueVisible = false;
      this.mockVariableFuncs = [];
    }
  },
  created() {
    if (this.parameters.length === 0) {
      this.parameters.push(new KeyValue());
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

.col-height {
  height: 40vh;
  overflow: auto;
}
</style>
