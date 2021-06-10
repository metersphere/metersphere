<template>

  <el-form :model="condition" :rules="rules" ref="httpConfig" class="ms-el-form-item__content" :disabled="isReadOnly">
    <div class="ms-border">
      <el-form-item prop="socket">
        <span class="ms-env-span">{{$t('api_test.environment.socket')}}</span>
        <el-input v-model="condition.socket" style="width: 80%" :placeholder="$t('api_test.request.url_description')" clearable size="small">
          <template slot="prepend">
            <el-select v-model="condition.protocol" class="request-protocol-select" size="small">
              <el-option label="http://" value="http"/>
              <el-option label="https://" value="https"/>
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="enable">
        <span class="ms-env-span">{{$t('api_test.environment.condition_enable')}}</span>
        <el-radio-group v-model="condition.type" @change="typeChange">
          <el-radio label="NONE">{{ $t('api_test.definition.document.data_set.none') }}</el-radio>
          <el-radio label="MODULE">{{$t('test_track.module.module')}}</el-radio>
          <el-radio label="PATH">{{$t('api_test.definition.api_path')}}</el-radio>
        </el-radio-group>
        <div v-if="condition.type === 'MODULE'" style="margin-top: 6px">
          <ms-select-tree size="small" :data="moduleOptions" :default-key="condition.ids" @getValue="setModule" :obj="moduleObj" clearable checkStrictly multiple v-if="!loading"/>
        </div>
        <div v-if="condition.type === 'PATH'" style="margin-top: 6px">
          <el-input v-model="pathDetails.name" :placeholder="$t('api_test.value')" clearable size="small">
            <template v-slot:prepend>
              <el-select v-model="pathDetails.value" class="request-protocol-select" size="small">
                <el-option :label="$t('api_test.request.assertions.contains')" value="contains"/>
                <el-option :label="$t('commons.adv_search.operators.equals')" value="equals"/>
              </el-select>
            </template>
          </el-input>
        </div>

        <p>{{$t('api_test.request.headers')}}</p>
        <ms-api-key-value :items="condition.headers" :isShowEnable="true" :suggestions="headerSuggestions"/>
        <div style="margin-top: 20px">
          <el-button v-if="!condition.id" type="primary" style="float: right" size="mini" @click="add">{{$t('commons.add')}}</el-button>
          <div v-else>
            <el-button type="primary" style="float: right;margin-left: 10px" size="mini" @click="clear">{{$t('commons.clear')}}</el-button>
            <el-button type="primary" style="float: right" size="mini" @click="update">{{$t('commons.update')}}</el-button>
          </div>
        </div>
      </el-form-item>
    </div>
    <div class="ms-border">
      <el-table :data="httpConfig.conditions" highlight-current-row @current-change="selectRow">
        <el-table-column prop="socket" :label="$t('load_test.domain')" show-overflow-tooltip width="180">
          <template v-slot:default="{row}">
            {{getUrl(row)}}
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('api_test.environment.condition_enable')" show-overflow-tooltip min-width="100px">
          <template v-slot:default="{row}">
            {{getName(row)}}
          </template>
        </el-table-column>
        <el-table-column prop="details" show-overflow-tooltip min-width="120px" :label="$t('api_test.value')">
          <template v-slot:default="{row}">
            {{getDetails(row)}}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" show-overflow-tooltip min-width="120px" :label="$t('commons.create_time')">
          <template v-slot:default="{row}">
            <span>{{ row.time | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="100px">
          <template v-slot:default="{row}">
            <div>
              <ms-table-operator-button :tip="$t('api_test.automation.copy')"
                                        icon="el-icon-document-copy" @exec="copy(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')"
                                        icon="el-icon-delete" @exec="remove(row)" type="danger"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-form>
</template>

<script>
  import {HttpConfig} from "../../model/EnvironmentModel";
  import MsApiKeyValue from "../ApiKeyValue";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";
  import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
  import {getUUID} from "@/common/js/utils";
  import {KeyValue} from "../../../definition/model/ApiTestModel";
  import Vue from "vue";

  export default {
    name: "MsEnvironmentHttpConfig",
    components: {MsApiKeyValue, MsSelectTree, MsTableOperatorButton},
    props: {
      httpConfig: new HttpConfig(),
      projectId: String,
      isReadOnly: {
        type: Boolean,
        default: false
      },
    },
    created() {
      this.list();
    },
    data() {
      let socketValidator = (rule, value, callback) => {
        if (!this.validateSocket(value)) {
          callback(new Error(this.$t("commons.formatErr")));
          return false;
        } else {
          callback();
          return true;
        }
      };
      return {
        headerSuggestions: REQUEST_HEADERS,
        rules: {
          socket: [{required: false, validator: socketValidator, trigger: "blur"}],
        },
        moduleOptions: [],
        moduleObj: {
          id: "id",
          label: "name",
        },
        loading: false,
        pathDetails: new KeyValue({name: "", value: "contains"}),
        condition: {type: "NONE", details: [new KeyValue({name: "", value: "contains"})], protocol: "http", socket: "", domain: "", port: 0, headers: [new KeyValue()]},
        beforeCondition: {}
      };
    },
    watch: {
      projectId() {
        this.list();
      },
      httpConfig: function (o) {
        // 历史数据处理
        if (this.httpConfig && this.httpConfig.socket && this.httpConfig.conditions && this.httpConfig.conditions.length === 0) {
          this.condition.type = "NONE";
          this.condition.socket = this.httpConfig.socket;
          this.condition.protocol = this.httpConfig.protocol;
          this.condition.port = this.httpConfig.port;
          this.condition.domain = this.httpConfig.domain;
          this.condition.time = new Date().getTime();
          this.condition.headers = this.httpConfig.headers;
          this.add();
        }
        this.condition = {id: undefined, type: "NONE", details: [new KeyValue({name: "", value: "contains"})], protocol: "http", socket: "", domain: "", port: 0, headers: [new KeyValue()]};
      },
    },
    methods: {
      getUrl(row) {
        return row.protocol + "://" + row.socket;
      },
      getName(row) {
        switch (row.type) {
          case "NONE":
            return this.$t("api_test.definition.document.data_set.none");
          case "MODULE":
            return this.$t("test_track.module.module");
          case "PATH":
            return this.$t("api_test.definition.api_path");
        }
      },
      clearHisData() {
        this.httpConfig.socket = undefined;
        this.httpConfig.protocol = undefined;
        this.httpConfig.port = undefined;
        this.httpConfig.domain = undefined;
      },
      getDetails(row) {
        if (row && row.type === "MODULE") {
          if (row.details && row.details instanceof Array) {
            let value = "";
            row.details.forEach((item) => {
              value += item.name + ",";
            });
            if (value.endsWith(",")) {
              value = value.substr(0, value.length - 1);
            }
            return value;
          }
        } else if (row && row.type === "PATH" && row.details.length > 0 && row.details[0].name) {
          return row.details[0].value === "equals" ? this.$t("commons.adv_search.operators.equals") + row.details[0].name : this.$t("api_test.request.assertions.contains") + row.details[0].name;
        } else {
          return "";
        }
      },
      selectRow(row) {
        this.condition = {type: "NONE", details: [new KeyValue({name: "", value: "contains"})], protocol: "http", socket: "", domain: "", port: 0, headers: [new KeyValue()]};
        if (row) {
          this.httpConfig.socket = row.socket;
          this.httpConfig.protocol = row.protocol;
          this.httpConfig.port = row.port;
          this.condition = row;
          if (!this.condition.headers) {
            this.condition.headers = [new KeyValue()];
          }
          if (row.type === "PATH" && row.details.length > 0) {
            this.pathDetails = JSON.parse(JSON.stringify(row.details[0]));
          } else if (row.type === "MODULE" && row.details.length > 0) {
            this.condition.ids = [];
            row.details.forEach((item) => {
              this.condition.ids.push(item.value);
            });
          }
        }
        this.beforeCondition = JSON.parse(JSON.stringify(this.condition));
        this.reload();
      },
      typeChange() {
        if (this.condition.type === "NONE" && this.condition.id  &&  this.checkNode(this.condition.id)) {
          this.condition.type = this.beforeCondition.type;
          this.$warning("启用条件为 '无' 的域名已经存在！");
          return;
        }
        switch (this.condition.type) {
          case "NONE":
            this.condition.details = [];
            break;
          case "MODULE":
            this.condition.details = [];
            break;
          case "PATH":
            this.pathDetails = new KeyValue({name: "", value: "contains"});
            break;
        }
      },
      list() {
        if (this.projectId) {
          let url = "/api/module/list/" + this.projectId + "/HTTP";
          this.result = this.$get(url, (response) => {
            if (response.data !== undefined && response.data !== null) {
              this.moduleOptions = response.data;
            }
          });
        } else {    //创建环境时一开始没有传入projectId，就不请求数据了
          this.moduleOptions = [];
        }
      },
      setModule(id, data) {
        if (data && data.length > 0) {
          this.condition.details = [];
          data.forEach((item) => {
            this.condition.details.push(new KeyValue({name: item.name, value: item.id}));
          });
        } else {
          this.condition.ids = [];
          this.condition.details = [];
        }
      },
      update() {
        const index = this.httpConfig.conditions.findIndex((d) => d.id === this.condition.id);
        this.validateSocket(this.condition.socket);
        let obj = {
          id: this.condition.id, type: this.condition.type, domain: this.condition.domain, socket: this.condition.socket, headers: this.condition.headers,
          protocol: this.condition.protocol, details: this.condition.details, port: this.condition.port, time: this.condition.time
        };
        if (obj.type === "PATH") {
          this.httpConfig.conditions[index].details = [this.pathDetails];
        }
        if (index !== -1) {
          Vue.set(this.httpConfig.conditions[index], obj, 1);
          this.condition = {type: "NONE", details: [new KeyValue({name: "", value: "contains"})], protocol: "http", socket: "", domain: "", headers: [new KeyValue()]};
          this.reload();
        }
      },
      clear() {
        this.condition = {type: "NONE", details: [new KeyValue({name: "", value: "contains"})], protocol: "http", socket: "", domain: "", headers: [new KeyValue()]};
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        });
      },
      checkNode(id) {
        let index = 1;
        this.httpConfig.conditions.forEach(item => {
          if (item.type === "NONE") {
            if(!id || id !== item.id) {
              index++;
            }
          }
        })
        return index > 1;
      },
      add() {
        if (this.condition.type === "NONE" && this.checkNode()) {
          this.$warning("启用条件为 '无' 的域名已经存在，请更新！");
          return;
        }
        this.validateSocket();
        let obj = {
          id: getUUID(), type: this.condition.type, socket: this.condition.socket, protocol: this.condition.protocol, headers: this.condition.headers,
          domain: this.condition.domain, port: this.condition.port, time: new Date().getTime()
        };
        if (this.condition.type === "PATH") {
          obj.details = [JSON.parse(JSON.stringify(this.pathDetails))];
        } else {
          obj.details = this.condition.details ? JSON.parse(JSON.stringify(this.condition.details)) : this.condition.details;
        }
        this.httpConfig.conditions.unshift(obj);
        this.clearHisData();
      },
      remove(row) {
        const index = this.httpConfig.conditions.findIndex((d) => d.id === row.id);
        this.httpConfig.conditions.splice(index, 1);
        this.clearHisData();
      },
      copy(row) {
        if (row.type === "NONE") {
          this.$warning("启用条件为 '无' 的域名不支持复制！");
          return;
        }
        const index = this.httpConfig.conditions.findIndex((d) => d.id === row.id);
        let obj = {id: getUUID(), type: row.type, socket: row.socket, details: row.details, protocol: row.protocol, headers: JSON.parse(JSON.stringify(this.condition.headers)), domain: row.domain, time: new Date().getTime()};
        if (index != -1) {
          this.httpConfig.conditions.splice(index, 0, obj);
        } else {
          this.httpConfig.conditions.push(obj);
        }
      },
      validateSocket(socket) {
        if (!socket) return true;
        let urlStr = this.condition.protocol + "://" + socket;
        let url = {};
        try {
          url = new URL(urlStr);
        } catch (e) {
          return false;
        }
        this.condition.domain = decodeURIComponent(url.hostname);

        this.condition.port = url.port;
        let path = url.pathname === "/" ? "" : url.pathname;
        if (url.port) {
          this.condition.socket = this.condition.domain + ":" + url.port + path;
        } else {
          this.condition.socket = this.condition.domain + path;
        }
        return true;
      },
      validate() {
        let isValidate = false;
        this.$refs["httpConfig"].validate((valid) => {
          isValidate = valid;
        });
        return isValidate;
      },
    },
  };
</script>

<style scoped>
  .request-protocol-select {
    width: 90px;
  }

  .ms-env-span {
    margin-right: 10px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-el-form-item__content >>> .el-form-item__content {
    line-height: 20px;
  }
</style>
