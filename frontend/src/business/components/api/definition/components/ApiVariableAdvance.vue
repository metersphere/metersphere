<template>
  <el-dialog :title="$t('api_test.request.parameters_advance')"
             :visible.sync="itemValueVisible"
             :append-to-body="appendToBody"
             class="advanced-item-value"
             width="100%"
             :fullscreen="dialogVisible">

    <el-tabs tab-position="top" style="height: 50vh;" @tab-click="selectTab">
      <el-tab-pane :label="$t('api_test.request.parameters_advance_mock')">
        <el-row type="flex" :gutter="20">
          <el-col :span="10" class="col-height">
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
                            @change="methodChange(itemFunc, func)" @click.native.prevent="radioClick(itemFunc, func)"/>
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

      <!--场景自定义变量-->
      <el-tab-pane :label="$t('api_test.automation.scenario_total')" name="variable">
        <div>
          <el-row style="margin-bottom: 10px">
            <div style="float: left">
              <el-input :placeholder="$t('commons.search_by_name')" v-model="defineVariable" size="small"
                        @change="filter"
                        @keyup.enter="filter">
                <el-select v-model="searchType" slot="prepend" :placeholder="$t('test_resource_pool.type')"
                           style="width: 90px" @change="filter">
                  <el-option value="CONSTANT" :label="$t('api_test.automation.constant')"></el-option>
                  <el-option value="LIST" :label="$t('test_track.case.list')"></el-option>
                  <el-option value="CSV" label="CSV"></el-option>
                  <el-option value="COUNTER" :label="$t('api_test.automation.counter')"></el-option>
                  <el-option value="RANDOM" :label="$t('api_test.automation.random')"></el-option>
                </el-select>
              </el-input>
            </div>
          </el-row>
          <el-row>
            <el-col :span="12">
              <div style="border:1px #DCDFE6 solid; min-height: 325px;border-radius: 4px ;width: 100% ;">
                <el-table ref="table" border :data="variables" class="adjust-table"
                          :row-class-name="tableRowClassName"
                          @select-all="select"
                          @select="select"
                          @row-click="edit"
                          v-loading="loading" height="325px">
                  <el-table-column type="selection" width="38"/>
                  <el-table-column prop="num" label="ID" sortable width="60"/>
                  <el-table-column prop="name" :label="$t('api_test.variable_name')" sortable
                                   show-overflow-tooltip/>
                  <el-table-column prop="type" :label="$t('test_track.case.type')" width="70">
                    <template v-slot:default="scope">
                      <span>{{ types.get(scope.row.type) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="value" :label="$t('api_test.value')" show-overflow-tooltip/>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.variable')">
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
          <el-col :span="18" class="col-height">
            <div>
              <h1>{{ $t('api_test.request.jmeter_func') }}</h1>
              <el-table border :data="jmeterFuncs" class="adjust-table table-content" height="400">
                <el-table-column prop="type" label="Type" width="150"/>
                <el-table-column prop="name" label="Functions" width="250"/>
                <el-table-column prop="description" label="Description"/>
              </el-table>
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
</template>

<script>
  import {calculate, Scenario} from "../model/ApiTestModel";
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import {buttons} from "../../automation/scenario/menu/Menu";
  import {ENV_TYPE} from "@/common/js/constants";

  export default {
    name: "MsApiVariableAdvance",
    props: {
      parameters: Array,
      environment: Object,
      scenario: Scenario,
      currentItem: Object,
      appendToBody: {
        type: Boolean,
        default() {
          return false;
        }
      },
    },
    data() {
      return {
        itemValueVisible: false,
        filterText: '',
        environmentParams: [],
        scenarioParams: [],
        preRequests: [],
        preRequestParams: [],
        treeProps: {children: 'children', label: 'name'},
        currentTab: 0,
        itemValue: null,
        itemValuePreview: null,
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
        mockFuncs: MOCKJS_FUNC.map(f => {
          return {name: f.name + " " + f.des + " " + this.$t('api_test.request.parameters_filter_example') + "：" + f.ex, value: f.name}
        }),
        jmeterFuncs: JMETER_FUNC,
        mockVariableFuncs: [],
        jmeterVariableFuncs: [],
        dialogVisible: true,

        // 自定义变量相关
        defineVariable: "",
        searchType: "",
        variables: [],
        selection: [],
        loading: false,
        types: new Map([
          ['CONSTANT', this.$t('api_test.automation.constant')],
          ['LIST', this.$t('test_track.case.list')],
          ['CSV', 'CSV'],
          ['COUNTER', this.$t('api_test.automation.counter')],
          ['RANDOM', this.$t('api_test.automation.random')]
        ])
      }
    },
    computed: {
      valueText() {
        return this.valuePlaceholder || this.$t("api_test.value");
      }
    },
    mounted() {
      this.prepareData();
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
    },
    methods: {
      open() {
        this.itemValueVisible = true;
        if(this.$store.state.scenarioMap.get != undefined
          && this.$store.state.scenarioMap.get("currentScenarioId") != undefined){
          this.variables = this.$store.state.scenarioMap.get("currentScenarioId");
        }
      },
      prepareData() {
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
            let variables = this.environment.config.commonConfig.variables;
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
      radioClick(itemFunc, func) {
        if (itemFunc.name === func.name) {
          let index = this.mockVariableFuncs.indexOf(itemFunc);
          this.mockVariableFuncs = this.mockVariableFuncs.slice(0, index);
          this.mockVariableFuncs.push({name: '', params: []});
          let valindex = this.itemValue.indexOf('|'+func.name);
          this.itemValue = this.itemValue.slice(0,valindex);
        }else {
          this.methodChange(itemFunc, func);
        }
      },
      addFunc() {
        if(this.itemValue.indexOf('@') == -1){
          this.itemValue = '@' + this.itemValue;
        } else {
          this.itemValue = this.itemValue;
        }
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
      saveAdvanced() {
        if(this.itemValue != null && this.itemValue != undefined
          && this.itemValue.indexOf('@') == -1
          && this.itemValue.indexOf('$') == -1){
          this.currentItem.value = '@' + this.itemValue;
        } else {
          this.currentItem.value = this.itemValue;
        }
        this.itemValueVisible = false;
        this.mockVariableFuncs = [];
        this.$emit('advancedRefresh', this.itemValue);
      },

      // 自定义变量
      filter() {
        let datas = [];
        this.variables.forEach(item => {
          if (this.searchType && this.searchType != "" && this.defineVariable && this.defineVariable != "") {
            if ((item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1) || (item.name && item.name.toLowerCase().indexOf(this.defineVariable.toLowerCase()) == -1)) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else if (this.defineVariable && this.defineVariable != "") {
            if (item.name && item.name.toLowerCase().indexOf(this.defineVariable.toLowerCase()) == -1) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else if (this.searchType && this.searchType != "") {
            if (item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else {
            item.hidden = undefined;
          }
          datas.push(item);
        });
        this.variables = datas;
      },
      tableRowClassName(row) {
        if (row.row.hidden) {
          return 'ms-variable-hidden-row';
        }
        return '';
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
      },
      edit(row) {
        this.selection = [row.id];
        this.itemValue = '${' + row.name + '}';
      }
    }
  }
</script>

<style scoped>
  .col-height {
    height: 40vh;
    overflow: auto;
  }
</style>
