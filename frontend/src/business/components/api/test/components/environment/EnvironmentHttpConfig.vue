<template>
  <el-form :model="condition" :rules="rules" ref="httpConfig">
    <el-form-item prop="socket">
      <span class="ms-env-span">{{$t('api_test.environment.socket')}}</span>
      <el-input v-model="httpConfig.socket" style="width: 80%" :placeholder="$t('api_test.request.url_description')" clearable size="small">
        <template v-slot:prepend>
          <el-select v-model="httpConfig.protocol" class="request-protocol-select" size="small">
            <el-option label="http://" value="http"/>
            <el-option label="https://" value="https"/>
          </el-select>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item prop="enable">
      <span class="ms-env-span">{{$t('api_test.environment.condition_enable')}}</span>
      <el-radio-group v-model="condition.type" @change="typeChange">
        <el-radio label="no">{{ $t('api_test.definition.document.data_set.none') }}</el-radio>
        <el-radio label="module">{{$t('test_track.module.module')}}</el-radio>
        <el-radio label="path">{{$t('api_test.definition.api_path')}}</el-radio>
      </el-radio-group>
      <el-button type="primary" style="float: right" size="mini" @click="add">{{$t('commons.add')}}</el-button>
      <div v-if="condition.type === 'module'">
        <ms-select-tree size="small" :data="moduleOptions" :default-key="condition.value" @getValue="setModule" :obj="moduleObj" clearable checkStrictly multiple/>
      </div>
      <div v-if="condition.type === 'path'">
        <el-input v-model="pathDetails.name" :placeholder="$t('api_test.value')" clearable size="small">
          <template v-slot:prepend>
            <el-select v-model="pathDetails.value" class="request-protocol-select" size="small">
              <el-option :label="$t('api_test.request.assertions.contains')" value="contains"/>
              <el-option :label="$t('commons.adv_search.operators.equals')" value="equals"/>
            </el-select>
          </template>
        </el-input>
      </div>
    </el-form-item>
    <div class="el-form-item">
      <el-table :data="httpConfig.conditions" style="width: 100%">
        <el-table-column prop="domain" :label="$t('load_test.domain')" width="180">
        </el-table-column>
        <el-table-column prop="type"
                         :label="$t('api_test.environment.condition_enable')"
                         show-overflow-tooltip
                         min-width="120px">
          <template v-slot:default="{row}">
            {{getName(row)}}
          </template>
        </el-table-column>
        <el-table-column prop="details"
                         show-overflow-tooltip
                         min-width="120px"
                         :label="$t('api_test.value')">
          <template v-slot:default="{row}">
            {{getDetails(row)}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="100px">
          <template v-slot:default="{row}">
            <ms-table-operator-button
              :tip="$t('api_test.automation.copy')"
              icon="el-icon-document-copy"
              @exec="copy(row)"/>
            <ms-table-operator-button
              :tip="$t('api_test.automation.remove')"
              icon="el-icon-delete"
              @exec="remove(row)"
              type="danger"
              v-tester/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <span>{{$t('api_test.request.headers')}}</span>
    <ms-api-key-value :items="httpConfig.headers" :isShowEnable="true" :suggestions="headerSuggestions"/>
  </el-form>
</template>

<script>
  import {HttpConfig} from "../../model/EnvironmentModel";
  import MsApiKeyValue from "../ApiKeyValue";
  import {REQUEST_HEADERS} from "../../../../../../common/js/constants";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";
  import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
  import {getUUID} from "@/common/js/utils";
  import {KeyValue} from "../../../definition/model/ApiTestModel";

  export default {
    name: "MsEnvironmentHttpConfig",
    components: {MsApiKeyValue, MsSelectTree, MsTableOperatorButton},
    props: {
      httpConfig: new HttpConfig(),
      projectId: String,
    },
    created() {
      this.list();
    },
    data() {
      let socketValidator = (rule, value, callback) => {
        if (!this.validateSocket(value)) {
          callback(new Error(this.$t('commons.formatErr')));
          return false;
        } else {
          callback();
          return true;
        }
      }
      return {
        headerSuggestions: REQUEST_HEADERS,
        rules: {
          socket: [{required: false, validator: socketValidator, trigger: 'blur'}],
        },
        moduleOptions: [],
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        pathDetails: new KeyValue({name: "", value: "contains"}),
        condition: {type: 'no', details: [new KeyValue({name: "", value: "contains"})], domain: ""},
      }
    },
    watch: {
      projectId() {
        this.list();
      }
    },
    methods: {
      getName(row) {
        switch (row.type) {
          case 'no':
            return this.$t('api_test.definition.document.data_set.none');
          case 'module':
            return this.$t('test_track.module.module');
          case 'path':
            return this.$t('api_test.definition.api_path');
        }
      },
      getDetails(row) {
        if (row && row.type === 'module') {
          if (row.details && row.details instanceof Array) {
            let value = "";
            row.details.forEach(item => {
              value += item.name + ",";
            })
            if (value.endsWith(",")) {
              value = value.substr(0, value.length - 1);
            }
            return value;
          }
        } else if (row && row.type === 'path' && row.details.length > 0 && row.details[0].name) {
          return row.details[0].value === 'equals' ? this.$t('commons.adv_search.operators.equals')
            : this.$t('api_test.request.assertions.contains') + "/" + row.details[0].name;
        }
        else {
          return "";
        }
      },
      typeChange() {
        switch (this.condition.type) {
          case 'no':
            this.condition.details = [];
            break;
          case 'module':
            this.condition.details = [];
            break;
          case 'path':
            this.pathDetails = new KeyValue({name: "", value: "contains"});
            break;
        }
      },
      list() {
        let url = "/api/automation/module/list/" + this.projectId;
        this.result = this.$get(url, response => {
          if (response.data !== undefined && response.data !== null) {
            this.moduleOptions = response.data;
          }
        });
      },
      setModule(id, data) {
        if (data && data.length > 0) {
          this.condition.details = [];
          data.forEach(item => {
            this.condition.details.push(new KeyValue({name: item.name, value: item.id}));
          })
        }
      },
      add() {
        let obj = {id: getUUID(), type: this.condition.type, domain: this.httpConfig.socket};
        if (this.condition.type === 'path') {
          obj.details = [JSON.parse(JSON.stringify(this.pathDetails))];
        } else {
          obj.details = this.condition.details ? JSON.parse(JSON.stringify(this.condition.details)) : this.condition.details;
        }
        this.httpConfig.conditions.push(obj);
      },
      remove(row) {
        const index = this.httpConfig.conditions.findIndex(d => d.id === row.id);
        this.httpConfig.conditions.splice(index, 1);
      },
      copy(row) {
        const index = this.httpConfig.conditions.findIndex(d => d.id === row.id);
        let obj = {id: getUUID(), type: row.type, domain: row.domain, details: row.details};
        if (index != -1) {
          this.httpConfig.conditions.splice(index + 1, 0, obj);
        } else {
          this.httpConfig.conditions.push(obj);
        }
      },
      validateSocket(socket) {
        if (!socket) return true;
        let urlStr = this.httpConfig.protocol + '://' + socket;
        let url = {};
        try {
          url = new URL(urlStr);
        } catch (e) {
          return false;
        }
        this.httpConfig.domain = decodeURIComponent(url.hostname);

        this.httpConfig.port = url.port;
        let path = url.pathname === '/' ? '' : url.pathname;
        if (url.port) {
          this.httpConfig.socket = this.httpConfig.domain + ':' + url.port + path;
        } else {
          this.httpConfig.socket = this.httpConfig.domain + path;
        }
        return true;
      },
      validate() {
        let isValidate = false;
        this.$refs['httpConfig'].validate((valid) => {
          isValidate = valid;
        });
        return isValidate;
      }
    }
  }
</script>

<style scoped>

  .request-protocol-select {
    width: 90px;
  }

  .ms-env-span {
    margin-right: 10px;
  }

  /deep/ .el-form-item {
    margin-bottom: 10px;
  }
</style>
