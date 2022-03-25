<template>
  <el-dialog :title="$t('api_test.request.parameters_advance')"
             :visible.sync="itemValueVisible"
             :append-to-body="appendToBody"
             :fullscreen="dialogVisible"
             :modal="false"
             class="advanced-item-value"
             width="100%">

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
      <el-tab-pane :label="$t('api_test.automation.scenario_total')" name="variable"
                   v-if="scenarioDefinition != undefined">
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

      <!--前置返回-->
      <el-tab-pane :label="$t('api_test.definition.request.pre_return')" v-if="scenarioDefinition != undefined">
        <ms-container :class="{'maximize-container': !asideHidden}" v-outside-click="outsideClick">
          <ms-aside-container @setAsideHidden="setAsideHidden" style="padding-top: 0px">
            <div v-loading="loading" v-show="!asideHidden">
              <el-tree node-key="resourceId"
                       highlight-current
                       ref="preTree"
                       v-show="!asideHidden"
                       :props="props"
                       :data="scenarioDefinition"
                       :default-expanded-keys="expandedNode"
                       :expand-on-click-node="false"
                       @node-click="nodeClick"
                       @node-expand="nodeExpand"
                       @node-collapse="nodeCollapse">
                  <span class="custom-tree-node father" slot-scope="{node, data}">
                     <!-- 步骤组件-->
                     <ms-component-config
                       :isMax="true"
                       :node="node"
                       :project-list="projectList"
                       :if-from-variable-advance="ifFromVariableAdvance"
                       :type="data.type"
                       :scenario="data"
                       :env-map="projectEnvMap"
                     />
                  </span>
              </el-tree>
            </div>
          </ms-aside-container>

          <!--右边元素-->
          <ms-main-container>
            <div>
              <!-- 第一层当前节点内容-->
              <ms-component-config
                :if-from-variable-advance="ifFromVariableAdvance"
                :type="selectedTreeNode.type"
                :scenario="selectedTreeNode"
                :node="selectedNode"
                :env-map="projectEnvMap"
                :draggable="false"
                @savePreParams="savePreParams"
                v-if="selectedTreeNode && selectedNode"/>
              <!-- 请求下还有的子步骤-->
              <div v-if="selectedTreeNode && selectedTreeNode.hashTree && showNode(selectedTreeNode)">
                <div v-for="item in selectedTreeNode.hashTree" :key="item.id" class="ms-col-one">
                  <ms-component-config
                    :if-from-variable-advance="ifFromVariableAdvance"
                    :type="item.type"
                    :scenario="item"
                    :env-map="projectEnvMap"
                    :response="response"
                    :draggable="false"
                    @savePreParams="savePreParams"
                    @suggestClick="suggestClick"
                    v-if="selectedTreeNode && selectedNode && filterSonNode(item)"/>
                </div>
              </div>
            </div>
            <div v-if="scenarioPreRequestParams.length > 0" style="margin-bottom: 10px">
              <p>{{ $t('api_test.definition.request.extract_params') }}</p>
              <div v-for="(item, index) in scenarioPreRequestParams" :key="index" class="kv-row item">
                <el-row type="flex" :gutter="20" justify="space-between" align="middle">
                  <el-col class="item">
                    <el-input v-model="item.name" size="small" :readonly="true"
                              @click.native="savePreParams(item.name)"/>
                  </el-col>
                  <el-col class="item">
                    <el-input v-model="item.exp" size="small" :readonly="true"
                              @click.native="savePreParams(item.name)"/>
                  </el-col>
                </el-row>
              </div>
            </div>
          </ms-main-container>
        </ms-container>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.request.jmeter_func')">
        <el-row>
          <el-col :span="18" class="col-height">
            <div>
              <h1>{{ $t('api_test.request.jmeter_func') }}</h1>
              <el-table border :data="jmeterFuncs"
                        class="adjust-table table-content"
                        height="400"
                        @row-click="handleRowClick">
                <el-table-column prop="type" label="Type" width="150"/>
                <el-table-column prop="name" label="Functions" width="250"/>
                <el-table-column prop="description" label="Description"/>
              </el-table>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.definition.document.request_info')" v-if="showMockVars">
        <el-row>
          <el-col :span="18" class="col-height">
            <div>
              <h1>{{ $t('api_test.definition.document.request_info') }}</h1>
              <el-table border :data="requestValues"
                        class="adjust-table table-content"
                        @row-click="handleRowClick">
                <el-table-column prop="type" :label="$t('commons.name')" width="150"/>
                <el-table-column prop="name" :label="$t('api_test.value')" width="250"/>
              </el-table>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    <div style="padding-top: 10px;">
      <el-row type="flex" align="bottom">
        <el-col :span="16">
          <div style="position: fixed; bottom: 0px; z-index: 5;">
            <el-form :inline="true" class="demo-form-inline">
              <el-form-item>
                <el-input :placeholder="valueText" size="small" v-model="itemValue"/>
              </el-form-item>
              <el-form-item>
                <el-button size="small" type="primary" plain @click="saveAdvanced()">
                  {{ $t('commons.save') }}
                </el-button>
                <el-button size="small" type="info" plain @click="addFunc()" v-if="currentTab === 0">
                  {{ $t('api_test.request.parameters_advance_add_func') }}
                </el-button>
                <el-button size="small" type="success" plain @click="showPreview()" v-if="currentTab === 0">
                  {{ $t('api_test.request.parameters_preview') }}
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
      </el-row>
      <div> {{ itemValuePreview }}</div>
    </div>
  </el-dialog>
</template>

<script>
  import {calculate, Scenario} from "../model/ApiTestModel";
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import {STEP} from "../../automation/scenario/Setting";
  import MsMainContainer from "../../../common/components/MsMainContainer";
  import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
  import MsContainer from "../../../common/components/MsContainer";
  import OutsideClick from "@/common/js/outside-click";

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
      showMockVars: {
        type: Boolean,
        default() {
          return false;
        }
      },
      variables: Array,
      scenarioDefinition: Array,
    },
    components: {
      MsMainContainer,
      MsAsideContainer,
      MsContainer,
      MsComponentConfig: () => import("../../automation/scenario/component/ComponentConfig"),
    },
    data() {
      return {
        itemValueVisible: false,
        filterText: '',
        environmentParams: [],
        scenarioParams: [],
        preRequests: [],
        preRequestParams: [],
        scenarioPreRequestParams: [],
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
          return {
            name: f.name + " " + f.des + " " + this.$t('api_test.request.parameters_filter_example') + "：" + f.ex,
            value: f.name
          }
        }),
        jmeterFuncs: JMETER_FUNC,
        mockVariableFuncs: [],
        jmeterVariableFuncs: [],
        dialogVisible: true,
        requestValues: [
          {
            type: this.$t('api_test.request.address'),
            name: "${address}"
          },
          {
            type: "Header " + this.$t('api_test.definition.document.request_param'),
            name: "${header.param}"
          },
          {
            type: this.$t('api_test.request.body') + this.$t('api_test.variable'),
            name: "${body.param}"
          },
          {
            type: this.$t('api_test.request.body') + this.$t('api_test.variable') + " (Raw)",
            name: "${bodyRaw}"
          },
          {
            type: "Query " + this.$t('api_test.definition.document.request_param'),
            name: "${query.param}"
          },
          {
            type: "Rest " + this.$t('api_test.definition.document.request_param'),
            name: "${rest.param}"
          },
        ],

        // 自定义变量相关
        defineVariable: "",
        searchType: "",
        selection: [],
        loading: false,
        types: new Map([
          ['CONSTANT', this.$t('api_test.automation.constant')],
          ['LIST', this.$t('test_track.case.list')],
          ['CSV', 'CSV'],
          ['COUNTER', this.$t('api_test.automation.counter')],
          ['RANDOM', this.$t('api_test.automation.random')]
        ]),

        // 前置返回相关
        props: {
          label: "label",
          children: "hashTree"
        },
        expandedNode: [],
        stepFilter: new STEP,
        operatingElements: [],
        selectedTreeNode: undefined,
        selectedNode: undefined,
        projectList: [],
        projectEnvMap: new Map,
        ifFromVariableAdvance: false,
        asideHidden: false,
        scenarioRootTree: undefined,
        insideClick: false,
        response: {},
      }
    },
    computed: {
      valueText() {
        return this.valuePlaceholder || this.$t("api_test.value");
      },
    },
    directives: {OutsideClick},
    mounted() {
      this.prepareData();
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
    },
    methods: {
      created() {
        this.operatingElements = this.stepFilter.get("ALL");
      },
      open() {
        if (this.scenarioDefinition != undefined) {
          // 标识为场景编辑入口进入
          this.ifFromVariableAdvance = true;
        }
        this.itemValueVisible = true;
        // 关闭页面重新进入需要再做过滤
        if (this.ifFromVariableAdvance && this.$refs.preTree != undefined && this.currentTab == 3) {
          this.componentActive(this.$refs.preTree.root);
        }
      },
      prepareData(data) {
        if (data != undefined || data != null) {
          this.scenario = data;
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

      // 获取该节点及所有子节点下的前置提取参数 key/value
      getExtractDataByNode(data, node) {
        if (!node.isLeaf) {
          if (node.childNodes.length > 0) {
            for (let i = 0; i < node.childNodes.length; i++) {
              if (node.childNodes[i].isLeaf) { //是叶子节点
                if (node.childNodes[i].data.type === 'Extract') { //叶子节点的数据的类型是 提取
                  let extractJsonParams = (node.childNodes[i].data.json).map(v => {
                    return {name: v.variable, value: v.value, exp: v.expression}
                  });
                  let extractRegexParams = (node.childNodes[i].data.regex).map(v => {
                    return {name: v.variable, value: v.value, exp: v.expression}
                  });
                  let extractXpathParams = (node.childNodes[i].data.xpath).map(v => {
                    return {name: v.variable, value: v.value, exp: v.expression}
                  });
                  let vs = [...extractJsonParams, ...extractRegexParams, ...extractXpathParams];
                  if (vs.length > 0) {
                    //数组合并
                    this.scenarioPreRequestParams = this.scenarioPreRequestParams.concat(extractJsonParams, extractRegexParams, extractXpathParams);
                  }
                }
                continue;
              } else {
                this.getExtractDataByNode(node.childNodes[i].data, node.childNodes[i]);
              }
            }
          }
        }
      },
      componentActive(node) {
        if (this.ifFromVariableAdvance) {
          this.setLeafNodeUnVisible(node);
        }
      },
      // 递归设置不需要显示的叶子节点
      setLeafNodeUnVisible(node) {
        if (!node.isLeaf) {
          if (node.childNodes.length > 0) {
            for (let i = 0; i < node.childNodes.length; i++) {
              // 提取参数不需要隐藏
              if (node.childNodes[i].isLeaf && node.childNodes[i].level > 1) {
                node.childNodes[i].visible = false;
                if (node.childNodes[i].data.type === 'Extract' && node.data.type !== 'HTTPSamplerProxy') {
                  node.childNodes[i].visible = true;
                }
              } else {
                // 等待控制器不显示
                if (node.childNodes[i].level == 1 && node.childNodes[i].data.type === 'ConstantTimer') {
                  node.childNodes[i].visible = false;
                }
                this.setLeafNodeUnVisible(node.childNodes[i]);
              }
            }
          }
        }
      },

      getAllExtractDataByNode() {
        if (this.ifFromVariableAdvance) {
          this.selectedNode = undefined;
          this.selectedTreeNode = undefined;
          this.scenarioPreRequestParams = [];
          if (this.$refs.preTree != undefined) {
            this.getExtractDataByNode(null, this.$refs.preTree.root);
          }
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

        if (this.ifFromVariableAdvance && this.currentTab === 3) {
          // 前置提取屏蔽部分叶子节点
          this.componentActive(this.$refs.preTree.root);
        }
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
          let valindex = this.itemValue.indexOf('|' + func.name);
          this.itemValue = this.itemValue.slice(0, valindex);
        } else {
          this.methodChange(itemFunc, func);
        }
      },
      addFunc() {
        if (this.itemValue == undefined || this.itemValue == null) {
          this.$warning(this.$t('api_test.request.parameters_advance_add_mock_error'));
          return;
        }
        if (this.itemValue.indexOf('@') == -1) {
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
        if (this.itemValue != null && this.itemValue != undefined
          && this.itemValue.indexOf('@') == -1
          && this.itemValue.indexOf('$') == -1) {
          this.$set(this.currentItem, 'value', '@' + this.itemValue);
        } else {
          this.$set(this.currentItem, 'value', this.itemValue);
          if (this.currentItem.mock != undefined) {
            this.$set(this.currentItem, 'mock', this.itemValue);
          }
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
      },
      savePreParams(data) {
        this.itemValue = '${' + data + '}';
      },
      handleRowClick(row) {
        if (row && row.name) {
          this.itemValue = row.name;
        }
      },

      // 前置返回
      nodeExpand(data) {
        if (data.resourceId) {
          this.expandedNode.push(data.resourceId);
        }
      },
      nodeCollapse(data) {
        if (data.resourceId) {
          this.expandedNode.splice(this.expandedNode.indexOf(data.resourceId), 1);
        }
      },
      nodeClick(data, node) {
        if (data.referenced != 'REF' && data.referenced != 'Deleted' && !data.disabled) {
          this.operatingElements = this.stepFilter.get(data.type);
        } else {
          this.operatingElements = [];
        }
        if (!this.operatingElements) {
          this.operatingElements = this.stepFilter.get("ALL");
        }
        this.selectedTreeNode = data;
        this.selectedNode = node;
        this.$store.state.selectStep = data;
        this.reload();

        // 计算前置提取变量
        this.scenarioPreRequestParams = [];
        this.getExtractDataByNode(data, node);
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      setAsideHidden(data) {
        this.asideHidden = data;
      },

      showNode(node) {
        for (let i = 0; i < node.hashTree.length; i++) {
          // 右边展示如果包含了前置提取表单,且是 HTTPSamplerProxy 类型，则不需要显示提取参数列表
          if (node.hashTree[i].type == 'Extract' && node.type == 'HTTPSamplerProxy'
            && this.scenarioPreRequestParams.length > 0) {
            this.scenarioPreRequestParams = [];
            break;
          }
        }
        node.active = true;
        if (node && this.stepFilter.get("AllSamplerProxy").indexOf(node.type) != -1) {
          return true;
        }
        return false;
      },
      filterSonNode(item) {
        if (item.type == 'Assertions' || item.type == 'ConstantTimer'
          || item.type == 'JDBCPreProcessor' || item.type == 'JDBCPostProcessor') {
          return false;
        }
        return true;
      },
      outsideClick(e) {
        // 获取全部前置提取
        this.getAllExtractDataByNode();
      },
      // 获取响应结果，后续 jsonpath 引用
      suggestClick(node) {
        this.response = {};
        if (node && node.parent && node.parent.data.requestResult) {
          this.response = node.parent.data.requestResult[0];
        } else if (this.selectedNode) {
          this.response = this.selectedNode.data.requestResult[0];
        }
      },
    }
  }
</script>

<style scoped>

  .col-height {
    height: 40vh;
    overflow: auto;
  }

  .maximize-container .ms-aside-container {
    min-width: 680px;
  }

  .ms-aside-container {
    height: calc(100vh - 50px) !important;
  }

  .ms-open-btn-left {
    margin-left: 35px;
  }

  .father .child {
    display: none;
  }

  .father:hover .child {
    display: block;
  }

  .ms-col-one {
    margin-top: 10px;
  }

  .custom-tree-node {
    width: 1000px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .extract-add {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

  .extract-item {
    width: 100%;
  }
</style>
