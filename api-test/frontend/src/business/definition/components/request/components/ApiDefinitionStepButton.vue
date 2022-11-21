<template>
  <el-col :span="3" class="ms-left-cell">
    <el-button class="ms-left-button" size="small" @click="addPre"
      >+{{ $t('api_test.definition.request.pre_script') }}
    </el-button>
    <br />
    <el-button class="ms-left-button" size="small" @click="addPost">
      +{{ $t('api_test.definition.request.post_script') }}
    </el-button>
    <br />
    <el-button class="ms-left-button" size="small" @click="addPreSql"
      >+{{ $t('api_test.definition.request.pre_sql') }}
    </el-button>
    <br />
    <el-button class="ms-left-button" size="small" @click="addPostSql">
      +{{ $t('api_test.definition.request.post_sql') }}
    </el-button>
    <br />
    <el-button class="ms-left-button" size="small" @click="addAssertions">
      +{{ $t('api_test.definition.request.assertions_rule') }}
    </el-button>
    <br />
    <el-button class="ms-left-button" size="small" @click="addExtract">
      +{{ $t('api_test.definition.request.extract_param') }}
    </el-button>
  </el-col>
</template>

<script>
import { createComponent } from '../../jmeter/components';
import { Assertions, Extract } from '../../../model/ApiTestModel';
import { getUUID } from 'metersphere-frontend/src/utils';

export default {
  name: 'ApiDefinitionStepButton',
  props: {
    request: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  methods: {
    addPre() {
      let jsr223PreProcessor = createComponent('JSR223PreProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(jsr223PreProcessor);
    },
    addPost() {
      let jsr223PostProcessor = createComponent('JSR223PostProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(jsr223PostProcessor);
    },
    addPreSql() {
      let jdbcPreProcessor = createComponent('JDBCPreProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(jdbcPreProcessor);
    },
    addPostSql() {
      let jdbcPostProcessor = createComponent('JDBCPostProcessor');
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(jdbcPostProcessor);
    },
    addAssertions() {
      let assertions = new Assertions({ id: getUUID() });
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(assertions);
    },
    addExtract() {
      let jsonPostProcessor = new Extract({ id: getUUID() });
      if (!this.request.hashTree) {
        this.request.hashTree = [];
      }
      this.request.hashTree.push(jsonPostProcessor);
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
  margin-top: 30px;
}

.ms-left-button {
  margin: 6px 0px 8px 30px;
}
</style>
