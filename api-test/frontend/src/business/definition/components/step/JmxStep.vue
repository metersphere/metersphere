<template>
  <div>
    <p>
      <el-select v-model="preOperate" size="mini" class="ms-select-step" v-if="tabType === 'pre'">
        <el-option v-for="item in preOperates" :key="item.id" :label="item.name" :value="item.id"> </el-option>
      </el-select>

      <el-select v-model="postOperate" size="mini" class="ms-select-step" v-else-if="tabType === 'post'">
        <el-option v-for="item in postOperates" :key="item.id" :label="item.name" :value="item.id"> </el-option>
      </el-select>
      <el-button
        size="mini"
        @click="add"
        type="primary"
        v-if="tabType !== 'assertionsRule'"
        style="background-color: var(--primary_color); border-color: var(--primary_color)">
        {{ $t('api_test.request.assertions.add') }}
      </el-button>
    </p>
    <!-- HTTP 请求参数 -->
    <el-tree
      node-key="resourceId"
      :props="props"
      :data="request.hashTree"
      :allow-drop="allowDrop"
      :filter-node-method="filterNode"
      @node-drag-end="allowDrag"
      draggable
      ref="generalSteps"
      class="ms-step-tree-cell">
      <span class="custom-tree-node father" slot-scope="{ node, data }" style="width: calc(100% - 20px)">
        <!--前置脚本-->
        <div v-if="tabType === 'pre'">
          <ms-jsr233-processor
            v-if="data.type === 'JSR223PreProcessor'"
            @remove="remove"
            @copyRow="copyRow"
            :protocol="protocol"
            :draggable="true"
            :title="$t('api_test.definition.request.pre_script')"
            :jsr223-processor="data"
            :key="data.id"
            color="#B8741A"
            background-color="#F9F1EA" />
          <!--前置SQL-->
          <ms-jdbc-processor
            v-if="data.type === 'JDBCPreProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :title="$t('api_test.definition.request.pre_sql')"
            :is-read-only="false"
            :scenarioId="scenarioId"
            :request="data"
            :jdbc-processor="data"
            :draggable="true"
            color="#B8741A"
            background-color="#F9F1EA" />

          <ms-constant-timer
            :inner-step="true"
            :timer="data"
            :node="node"
            :draggable="true"
            @remove="remove"
            @copyRow="copyRow"
            v-if="data.type === 'ConstantTimer'" />
        </div>
        <div v-if="tabType === 'post'">
          <!--后置脚本-->
          <ms-jsr233-processor
            v-if="data.type === 'JSR223PostProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :protocol="protocol"
            :is-read-only="false"
            :title="$t('api_test.definition.request.post_script')"
            :jsr223-processor="data"
            :draggable="true"
            color="#783887"
            :key="data.id"
            background-color="#F2ECF3" />

          <!--后置SQL-->
          <ms-jdbc-processor
            v-if="data.type === 'JDBCPostProcessor'"
            @copyRow="copyRow"
            @remove="remove"
            :title="$t('api_test.definition.request.post_sql')"
            :is-read-only="false"
            :request="data"
            :scenarioId="scenarioId"
            :jdbc-processor="data"
            :draggable="true"
            color="#783887"
            background-color="#F2ECF3" />
          <!--提取规则-->
          <ms-api-extract
            :response="response"
            :is-read-only="data.disabled"
            :extract="data"
            :draggable="true"
            @copyRow="copyRow"
            @remove="remove"
            v-if="data.type === 'Extract'" />
        </div>
        <div v-if="tabType === 'assertionsRule'">
          <!--断言规则-->
          <ms-api-assertions
            v-if="data.type === 'Assertions'"
            @copyRow="copyRow"
            @remove="remove"
            @reload="reloadRule"
            :response="response"
            :request="request"
            :apiId="apiId"
            :draggable="true"
            :is-read-only="request.disabled"
            :assertions="data" />
        </div>
      </span>
    </el-tree>
  </div>
</template>

<script>
import { REQUEST_HEADERS } from 'metersphere-frontend/src/utils/constants';
import { createComponent } from '../jmeter/components';
import MsApiAssertions from '../assertion/ApiAssertions';
import MsApiExtract from '../extract/ApiExtract';
import { Assertions, Body, ConstantTimer, Extract, KeyValue } from '../../model/ApiTestModel';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import BatchAddParameter from '../basis/BatchAddParameter';
import MsJsr233Processor from '../../../automation/scenario/component/Jsr233Processor';
import MsConstantTimer from '../../../automation/scenario/component/ConstantTimer';
import MsJdbcProcessor from '@/business/automation/scenario/component/JDBCProcessor';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';
import { useApiStore } from '@/store';

const store = useApiStore();
export default {
  name: 'MsJmxStep',
  components: {
    MsJdbcProcessor,
    MsJsr233Processor,
    BatchAddParameter,
    MsApiExtract,
    MsApiAssertions,
    MsConstantTimer,
  },
  props: {
    request: {},
    tabType: String,
    response: {},
    apiId: String,
    scenarioId: String,
    showScript: {
      type: Boolean,
      default: true,
    },
    headers: {
      type: Array,
      default() {
        return [];
      },
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
      default: false,
    },
  },
  watch: {
    // 接口/用例 右上角公共环境监听
    storeUseEnvironment: function () {
      if (this.request.hashTree && this.request.hashTree.length > 0 && !this.scenarioId) {
        this.setSubset(this.request.hashTree, store.useEnvironment);
      }
    },
  },
  computed: {
    storeUseEnvironment() {
      return store.useEnvironment;
    },
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
        label: 'name',
        children: 'hashTree',
      },
      preOperate: 'script',
      postOperate: 'extract',
      preOperates: [
        {
          id: 'script',
          name: this.$t('api_test.definition.request.pre_script'),
        },
        { id: 'sql', name: this.$t('api_test.definition.request.pre_sql') },
        {
          id: 'wait_controller',
          name: this.$t('api_test.automation.wait_controller'),
        },
      ],
      postOperates: [
        {
          id: 'script',
          name: this.$t('api_test.definition.request.post_script'),
        },
        { id: 'sql', name: this.$t('api_test.definition.request.post_sql') },
        {
          id: 'extract',
          name: this.$t('api_test.definition.request.extract_param'),
        },
      ],
      activeName: 'headers',
      loaded: true,
      rules: {
        name: [
          {
            max: 300,
            message: this.$t('commons.input_limit', [1, 300]),
            trigger: 'blur',
          },
        ],
        url: [
          {
            max: 500,
            required: true,
            message: this.$t('commons.input_limit', [1, 500]),
            trigger: 'blur',
          },
          { validator: validateURL, trigger: 'blur' },
        ],
        path: [
          {
            max: 500,
            message: this.$t('commons.input_limit', [0, 500]),
            trigger: 'blur',
          },
        ],
      },
      headerSuggestions: REQUEST_HEADERS,
      isReloadData: false,
      isBodyShow: true,
      dialogVisible: false,
      environments: [],
    };
  },
  created() {
    this.init();
  },

  methods: {
    initAssertions() {
      let ruleSize = 0;
      this.request.hashTree.forEach((item) => {
        if (item.type === 'Assertions') {
          ruleSize++;
        }
      });
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
      // 继承请求中的环境和数据源
      if (this.request.hashTree && this.request.hashTree.length > 0) {
        this.setOwnEnvironment(this.request.hashTree);
      }
      this.sort();
      this.reload();
    },
    setSubset(scenarioDefinition, env) {
      for (let i in scenarioDefinition) {
        let typeArray = ['JDBCPostProcessor', 'JDBCSampler', 'JDBCPreProcessor'];
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          // 找到原始数据源名称
          this.getTargetSource(scenarioDefinition[i]);
          scenarioDefinition[i].environmentId = env;
          this.setSameSourceId(env, scenarioDefinition[i]);
        }
        if (scenarioDefinition[i].hashTree && scenarioDefinition[i].hashTree.length > 0) {
          this.setSubset(scenarioDefinition[i].hashTree, env);
        }
      }
    },
    getEnvs() {
      if (!this.scenarioId) {
        let projectId = this.request.projectId ? this.request.projectId : getCurrentProjectID();
        this.result = getEnvironmentByProjectId(projectId).then((response) => {
          this.environments = response.data;
          this.environments.forEach((environment) => {
            parseEnvironment(environment);
          });
        });
      }
    },
    getTargetSource(obj) {
      this.environments.forEach((environment) => {
        // 找到原始环境和数据源名称
        if (environment.id === obj.environmentId) {
          if (environment.config && environment.config.databaseConfigs) {
            environment.config.databaseConfigs.forEach((item) => {
              if (item.id === obj.dataSourceId) {
                obj.targetDataSourceName = item.name;
              }
            });
          }
        }
      });
    },
    setSameSourceId(envId, obj) {
      let currentEnvironment;
      for (let i in this.environments) {
        if (this.environments[i].id === envId) {
          currentEnvironment = this.environments[i];
          break;
        }
      }
      let isSame = false;
      if (currentEnvironment && currentEnvironment.config && currentEnvironment.config.databaseConfigs) {
        currentEnvironment.config.databaseConfigs.forEach((item) => {
          // 按照名称匹配
          if (item.name === obj.targetDataSourceName) {
            obj.dataSourceId = item.id;
            isSame = true;
          }
        });
        if (!isSame && currentEnvironment.config.databaseConfigs.length > 0) {
          obj.dataSourceId = currentEnvironment.config.databaseConfigs[0].id;
        }
      }
    },
    setOwnEnvironment(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = ['JDBCPostProcessor', 'JDBCSampler', 'JDBCPreProcessor'];
        if (
          typeArray.indexOf(scenarioDefinition[i].type) !== -1 &&
          this.request.environmentId &&
          !scenarioDefinition[i].environmentId
        ) {
          scenarioDefinition[i].environmentId = this.request.environmentId;
          if (this.request.dataSourceId && !scenarioDefinition[i].dataSourceId) {
            scenarioDefinition[i].dataSourceId = this.request.dataSourceId;
            scenarioDefinition[i].originalDataSourceId = scenarioDefinition[i].dataSourceId;
            scenarioDefinition[i].originalEnvironmentId = scenarioDefinition[i].environmentId;
          }
        }
      }
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
          vars = ['JSR223PreProcessor', 'JDBCPreProcessor', 'ConstantTimer'];
        } else if (this.tabType === 'post') {
          vars = ['JSR223PostProcessor', 'JDBCPostProcessor', 'Extract'];
        } else {
          vars = ['Assertions'];
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
      let jsr223PreProcessor = createComponent('JSR223PreProcessor');
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
      let jsr223PostProcessor = createComponent('JSR223PostProcessor');
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
      let jdbcPreProcessor = createComponent('JDBCPreProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jdbcPreProcessor.label = 'SCENARIO-REF-STEP';
      }
      jdbcPreProcessor.projectId = this.request.projectId;
      jdbcPreProcessor.active = false;
      this.request.hashTree.push(jdbcPreProcessor);
      this.sort();
      this.reload();
    },
    addPostSql() {
      let jdbcPostProcessor = createComponent('JDBCPostProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      if (this.request.disabled) {
        jdbcPostProcessor.label = 'SCENARIO-REF-STEP';
      }
      jdbcPostProcessor.projectId = this.request.projectId;
      jdbcPostProcessor.active = false;
      this.request.hashTree.push(jdbcPostProcessor);
      this.sort();
      this.reload();
    },
    addWait() {
      let constant = new ConstantTimer({ delay: 1000 });
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
      let assertions = new Assertions({ id: getUUID() });
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
      let jsonPostProcessor = new Extract({ id: getUUID() });
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
      const index = this.request.hashTree.findIndex((d) => d.id === row.id);
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
      if (dropType !== 'inner') {
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
        store.forceRerenderIndex = getUUID();
      });
    },
    reload() {
      this.isReloadData = true;
      this.$nextTick(() => {
        this.isReloadData = false;
      });
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
      this.getEnvs();
    },
    sort() {
      let index = 1;
      for (let i in this.request.hashTree) {
        let step = this.request.hashTree[i];
        if (
          this.tabType === 'pre' &&
          (step.type === 'JSR223PreProcessor' || step.type === 'JDBCPreProcessor' || step.type === 'ConstantTimer')
        ) {
          step.index = Number(index);
          index++;
        } else if (
          this.tabType === 'post' &&
          (step.type === 'JSR223PostProcessor' || step.type === 'JDBCPostProcessor' || step.type === 'Extract')
        ) {
          step.index = Number(index);
          index++;
        } else if (this.tabType === 'assertionsRule' && step.type === 'Assertions') {
          step.index = Number(index);
          index++;
        }
        // 兼容历史数据
        if (!step.clazzName) {
          step.clazzName = TYPE_TO_C.get(step.type);
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
        let params = data.split(/[\r\n]+/);
        let keyValues = [];
        params.forEach((item) => {
          let line = item.split(/，|,/);
          let required = false;
          if (line[1] === '必填' || line[1] === 'Required' || line[1] === 'true') {
            required = true;
          }
          keyValues.push(
            new KeyValue({
              name: line[0],
              required: required,
              value: line[2],
              description: line[3],
              type: 'text',
              valid: false,
              file: false,
              encode: true,
              enable: true,
              contentType: 'text/plain',
            })
          );
        });
        keyValues.forEach((item) => {
          switch (this.activeName) {
            case 'parameters':
              this.request.arguments.unshift(item);
              break;
            case 'rest':
              this.request.rest.unshift(item);
              break;
            case 'headers':
              this.request.headers.unshift(item);
              break;
            default:
              break;
          }
        });
      }
    },
  },
};
</script>

<style scoped>
.ms-left-cell .el-button:nth-of-type(1) {
  color: #b8741a;
  background-color: #f9f1ea;
  border: #f9f1ea;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #a30014;
  background-color: #f7e6e9;
  border: #f7e6e9;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #015478;
  background-color: #e6eef2;
  border: #e6eef2;
}

.ms-tree :deep(.el-tree-node__expand-icon.expanded) {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree :deep(.el-icon-caret-right:before) {
  content: '\e723';
  font-size: 20px;
}

.ms-tree :deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}

.ms-tree :deep(.el-tree-node__expand-icon) {
  color: #7c3985;
}

.ms-tree :deep(.el-tree-node__expand-icon.expanded.el-icon-caret-right:before) {
  color: #7c3985;
  content: '\e722';
  font-size: 20px;
}

:deep(.el-tree-node__content) {
  height: 100%;
  margin-top: 6px;
  vertical-align: center;
}

.ms-step-button {
  margin: 6px 0px 8px 30px;
}

.ms-left-cell .el-button:nth-of-type(1) {
  color: #b8741a;
  background-color: #f9f1ea;
  border: #f9f1ea;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #fe6f71;
  background-color: #f9f1ea;
  border: #ebf2f2;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #1483f6;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(5) {
  color: #a30014;
  background-color: #f7e6e9;
  border: #f7e6e9;
}

.ms-left-cell .el-button:nth-of-type(6) {
  color: #015478;
  background-color: #e6eef2;
  border: #e6eef2;
}

.ms-left-cell {
  margin-top: 0px;
}

.ms-step-tree-cell :deep(.el-tree-node__expand-icon) {
  padding: 0px;
}

.ms-select-step {
  margin-left: 10px;
  margin-right: 10px;
  width: 200px;
}
</style>
