<template>
  <div>
    <p>
      <el-select v-model="preOperate" size="mini" class="ms-select-step" v-if="tabType === 'pre'">
        <el-option
          v-for="item in preOperates"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>

      <el-select v-model="postOperate" size="mini" class="ms-select-step" v-else-if="tabType === 'post'">
        <el-option
          v-for="item in postOperates"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>
      <el-button size="mini" @click="add" type="primary" v-if="tabType !== 'assertionsRule'">
        {{ $t('api_test.request.assertions.add') }}
      </el-button>
    </p>
    <!-- HTTP 请求参数 -->
    <el-tree node-key="resourceId"
             :props="props"
             :data="request.hashTree"
             :allow-drop="allowDrop"
             :filter-node-method="filterNode"
             @node-drag-end="allowDrag"
             draggable ref="generalSteps" class="ms-step-tree-cell">
       <span class="custom-tree-node father" slot-scope="{node,data}" style="width: calc(100% - 20px);">
        <!--前置脚本-->
         <div v-if="tabType === 'pre'">
           <ms-jsr233-processor
             v-if="data.type==='JSR223PreProcessor'"
             @remove="remove"
             @copyRow="copyRow"
             :protocol="protocol"
             :title="$t('api_test.definition.request.pre_script')"
             :jsr223-processor="data"
             color="#B8741A"
             background-color="#F9F1EA"/>
           <!--前置SQL-->
          <ms-jdbc-processor
            v-if="data.type ==='JDBCPreProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :title="$t('api_test.definition.request.pre_sql')"
            :is-read-only="false"
            :request="data"
            :jdbc-processor="data"
            color="#B8741A"
            background-color="#F9F1EA"/>

           <ms-constant-timer :inner-step="true" :timer="data" :node="node" v-if="data.type ==='ConstantTimer'"
                              @remove="remove" @copyRow="copyRow"/>

         </div>
        <div v-if="tabType ==='post'">
           <!--后置脚本-->
          <ms-jsr233-processor
            v-if="data.type ==='JSR223PostProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :protocol="protocol"
            :is-read-only="false"
            :title="$t('api_test.definition.request.post_script')"
            :jsr223-processor="data"
            color="#783887"
            background-color="#F2ECF3"/>

          <!--后置SQL-->
          <ms-jdbc-processor
            v-if="data.type ==='JDBCPostProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :title="$t('api_test.definition.request.post_sql')"
            :is-read-only="false"
            :request="data"
            :jdbc-processor="data"
            color="#783887"
            background-color="#F2ECF3"/>
          <!--提取规则-->
           <ms-api-extract
             :response="response"
             :is-read-only="data.disabled"
             :extract="data"
             @copyRow="copyRow"
             @remove="remove"
             v-if="data.type==='Extract'"
           />
          </div>
         <div v-if="tabType === 'assertionsRule'">
         <!--断言规则-->
          <ms-api-assertions
            v-if="data.type==='Assertions'"
            @copyRow="copyRow"
            @remove="remove"
            @reload="reloadRule"
            :response="response"
            :request="request"
            :apiId="apiId"
            :is-read-only="data.disabled"
            :assertions="data"/>
         </div>
       </span>
    </el-tree>
  </div>
</template>

<script>
import {REQUEST_HEADERS} from "@/common/js/constants";
import {createComponent} from "../jmeter/components";
import MsApiAssertions from "../assertion/ApiAssertions";
import MsApiExtract from "../extract/ApiExtract";
import {Assertions, Body, ConstantTimer, Extract, KeyValue} from "../../model/ApiTestModel";
import {getUUID} from "@/common/js/utils";
import BatchAddParameter from "../basis/BatchAddParameter";
import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";
import MsConstantTimer from "../../../automation/scenario/component/ConstantTimer";
import MsJdbcProcessor from "@/business/components/api/automation/scenario/component/JDBCProcessor";

export default {
  name: "MsJmxStep",
  components: {
    MsJdbcProcessor,
    MsJsr233Processor,
    BatchAddParameter,
    MsApiExtract,
    MsApiAssertions,
    MsConstantTimer
  },
  props: {
    request: {},
    tabType: String,
    response: {},
    apiId: String,
    showScript: {
      type: Boolean,
      default: true,
    },
    headers: {
      type: Array,
      default() {
        return [];
      }
    },
    referenced: {
      type: Boolean,
      default: false,
    },
    isShowEnable: Boolean,
    jsonPathList: Array,
    protocol: String,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    let validateURL = (rule, value, callback) => {
      try {
        new URL(this.addProtocol(this.request.url));
      } catch (e) {
        callback(this.$t('api_test.request.url_invalid'));
      }
    };
    return {
      props: {
        label: "name",
        children: "hashTree"
      },
      preOperate: "script",
      postOperate: "extract",
      preOperates: [
        {id: 'script', name: this.$t('api_test.definition.request.pre_script')},
        {id: 'sql', name: this.$t('api_test.definition.request.pre_sql')},
        {id: 'wait_controller', name: this.$t('api_test.automation.wait_controller')}
      ],
      postOperates: [
        {id: 'script', name: this.$t('api_test.definition.request.post_script')},
        {id: 'sql', name: this.$t('api_test.definition.request.post_sql')},
        {id: 'extract', name: this.$t('api_test.definition.request.extract_param')}
      ],
      activeName: "headers",
      loaded: true,
      rules: {
        name: [
          {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
        ],
        url: [
          {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
          {validator: validateURL, trigger: 'blur'}
        ],
        path: [
          {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
        ]
      },
      headerSuggestions: REQUEST_HEADERS,
      isReloadData: false,
      isBodyShow: true,
      dialogVisible: false,
    }
  },
  created() {
    this.init();
  },

  methods: {
    initAssertions() {
      let ruleSize = 0;
      this.request.hashTree.forEach(item => {
        if (item.type === "Assertions") {
          ruleSize++;
        }
      })
      if (ruleSize === 0) {
        this.addAssertions();
      }
    },
    reloadRule() {
      this.$emit('reload');
    },
    add() {
      this.request.active = true;
      if (this.tabType === 'pre') {
        if (this.preOperate === 'script') {
          this.addPre();
        } else if (this.preOperate === 'sql') {
          this.addPreSql();
        } else {
          this.addWait();
        }
      } else if (this.tabType === 'post') {
        if (this.postOperate === 'script') {
          this.addPost();
        } else if (this.postOperate === 'sql') {
          this.addPostSql();
        } else if (this.postOperate === 'extract') {
          this.addExtract();
        } else {
          this.addWait();
        }
      } else {
        this.addAssertions();
      }
      this.sort();
      this.reload();
    },
    filterNode(value, data) {
      if (data.type && value.indexOf(data.type) !== -1) {
        return true;
      }
      return false;
    },
    filter() {
      this.$nextTick(() => {
        let vars = [];
        if (this.tabType === 'pre') {
          vars = ["JSR223PreProcessor", "JDBCPreProcessor", "ConstantTimer"];
        } else if (this.tabType === 'post') {
          vars = ["JSR223PostProcessor", "JDBCPostProcessor", "Extract"];
        } else {
          vars = ["Assertions"];
        }
        this.$nextTick(() => {
          if (this.$refs.generalSteps && this.$refs.generalSteps.filter) {
            this.$refs.generalSteps.filter(vars);
          }
        });
        this.sort();
      });
    },
    addPre() {
      let jsr223PreProcessor = createComponent("JSR223PreProcessor");
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jsr223PreProcessor.label = 'SCENARIO-REF-STEP';
      }
      this.request.hashTree.push(jsr223PreProcessor);
      this.sort();
      this.reload();
    },
    addPost() {
      let jsr223PostProcessor = createComponent("JSR223PostProcessor");
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jsr223PostProcessor.label = 'SCENARIO-REF-STEP';
      }
      this.request.hashTree.push(jsr223PostProcessor);
      this.sort();
      this.reload();
    },
    addPreSql() {
      let jdbcPreProcessor = createComponent("JDBCPreProcessor");
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jdbcPreProcessor.label = 'SCENARIO-REF-STEP';
      }
      jdbcPreProcessor.active = false;
      this.request.hashTree.push(jdbcPreProcessor);
      this.sort();
      this.reload();
    },
    addPostSql() {
      let jdbcPostProcessor = createComponent("JDBCPostProcessor");
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jdbcPostProcessor.label = 'SCENARIO-REF-STEP';
      }
      jdbcPostProcessor.active = false;
      this.request.hashTree.push(jdbcPostProcessor);
      this.sort();
      this.reload();
    },
    addWait() {
      let constant = new ConstantTimer({delay: 1000});
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        constant.label = 'SCENARIO-REF-STEP';
      }
      this.request.hashTree.push(constant);
      this.sort();
      this.reload();
    },
    addAssertions() {
      let assertions = new Assertions({id: getUUID()});
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        assertions.label = 'SCENARIO-REF-STEP';
      }
      this.request.hashTree.push(assertions);
      this.sort();
      this.reload();
    },
    addExtract() {
      let jsonPostProcessor = new Extract({id: getUUID()});
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jsonPostProcessor.label = 'SCENARIO-REF-STEP';
      }
      this.request.hashTree.push(jsonPostProcessor);
      this.sort();
      this.reload();
    },
    remove(row) {
      let index = this.request.hashTree.indexOf(row);
      this.request.hashTree.splice(index, 1);
      this.sort();
      this.reload();
      this.forceRerender();
    },
    copyRow(row) {
      let obj = JSON.parse(JSON.stringify(row));
      obj.id = getUUID();
      obj.resourceId = getUUID();
      const index = this.request.hashTree.findIndex(d => d.id === row.id);
      if (index !== -1) {
        this.request.hashTree.splice(index, 0, obj);
      } else {
        this.request.hashTree.push(obj);
      }
      this.sort();
      this.reload();
      this.forceRerender();
    },
    allowDrop(draggingNode, dropNode, dropType) {
      // 增加插件权限控制
      if (dropType !== "inner") {
        return true;
      }
      return false;
    },
    allowDrag(draggingNode, dropNode, dropType) {
      if (dropNode && draggingNode && dropType) {
        this.reload();
        this.filter();
        this.forceRerender();
      }
    },
    forceRerender() {
      this.$nextTick(() => {
        this.$store.state.forceRerenderIndex = getUUID();
      });
    },
    reload() {
      this.isReloadData = true
      this.$nextTick(() => {
        this.isReloadData = false
      })
    },
    init() {
      if (!this.request.body) {
        this.request.body = new Body();
      }
      if (!this.request.body.kvs) {
        this.request.body.kvs = [];
      }
      if (!this.request.rest) {
        this.request.rest = [];
      }
      if (!this.request.arguments) {
        this.request.arguments = [];
      }
      this.sort();
    },
    sort() {
      let index = 1;
      for (let i in this.request.hashTree) {
        if (this.tabType === 'pre' && (this.request.hashTree[i].type === 'JSR223PreProcessor' ||
          this.request.hashTree[i].type === 'JDBCPreProcessor' || this.request.hashTree[i].type === 'ConstantTimer')) {
          this.request.hashTree[i].index = Number(index);
          index++;
        } else if (this.tabType === 'post' && (this.request.hashTree[i].type === 'JSR223PostProcessor' ||
          this.request.hashTree[i].type === 'JDBCPostProcessor' ||
          this.request.hashTree[i].type === 'Extract')) {
          this.request.hashTree[i].index = Number(index);
          index++;
        } else if (this.tabType === 'assertionsRule' && this.request.hashTree[i].type === 'Assertions') {
          this.request.hashTree[i].index = Number(index);
          index++;
        }
      }
    },
    // 解决修改请求头后 body 显示错位
    reloadBody() {
      this.isBodyShow = false;
      this.$nextTick(() => {
        this.isBodyShow = true;
      });
    },
    batchAdd() {
      this.$refs.batchAddParameter.open();
    },
    batchSave(data) {
      if (data) {
        let params = data.split("\n");
        let keyValues = [];
        params.forEach(item => {
          let line = item.split(/，|,/);
          let required = false;
          if (line[1] === '必填' || line[1] === 'Required' || line[1] === 'true') {
            required = true;
          }
          keyValues.push(new KeyValue({
            name: line[0],
            required: required,
            value: line[2],
            description: line[3],
            type: "text",
            valid: false,
            file: false,
            encode: true,
            enable: true,
            contentType: "text/plain"
          }));
        })
        keyValues.forEach(item => {
          switch (this.activeName) {
            case "parameters":
              this.request.arguments.unshift(item);
              break;
            case "rest":
              this.request.rest.unshift(item);
              break;
            case "headers":
              this.request.headers.unshift(item);
              break;
            default:
              break;
          }
        })
      }
    }
  }
}
</script>

<style scoped>
.ms-left-cell .el-button:nth-of-type(1) {
  color: #B8741A;
  background-color: #F9F1EA;
  border: #F9F1EA;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #F2ECF3;
  border: #F2ECF3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #A30014;
  background-color: #F7E6E9;
  border: #F7E6E9;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #015478;
  background-color: #E6EEF2;
  border: #E6EEF2;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree >>> .el-icon-caret-right:before {
  content: '\e723';
  font-size: 20px;
}

.ms-tree >>> .el-tree-node__expand-icon.is-leaf {
  color: transparent;
}

.ms-tree >>> .el-tree-node__expand-icon {
  color: #7C3985;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded.el-icon-caret-right:before {
  color: #7C3985;
  content: "\e722";
  font-size: 20px;
}

/deep/ .el-tree-node__content {
  height: 100%;
  margin-top: 6px;
  vertical-align: center;
}

.ms-step-button {
  margin: 6px 0px 8px 30px;
}

.ms-left-cell .el-button:nth-of-type(1) {
  color: #B8741A;
  background-color: #F9F1EA;
  border: #F9F1EA;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #F2ECF3;
  border: #F2ECF3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #FE6F71;
  background-color: #F9F1EA;
  border: #EBF2F2;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #1483F6;
  background-color: #F2ECF3;
  border: #F2ECF3;
}

.ms-left-cell .el-button:nth-of-type(5) {
  color: #A30014;
  background-color: #F7E6E9;
  border: #F7E6E9;
}

.ms-left-cell .el-button:nth-of-type(6) {
  color: #015478;
  background-color: #E6EEF2;
  border: #E6EEF2;
}

.ms-left-cell {
  margin-top: 0px;
}

.ms-step-tree-cell >>> .el-tree-node__expand-icon {
  padding: 0px;
}

.ms-select-step {
  margin-left: 10px;
  margin-right: 10px;
  width: 200px;
}
</style>
