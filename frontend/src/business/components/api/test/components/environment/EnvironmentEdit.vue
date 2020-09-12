<template>
  <el-main v-loading="result.loading">
    <el-form :model="environment" :rules="rules" ref="environment">

      <span>{{$t('api_test.environment.name')}}</span>
      <el-form-item
        prop="name">
        <el-input v-model="environment.name" :placeholder="this.$t('commons.input_name')" clearable></el-input>
      </el-form-item>
      <span>{{$t('api_test.environment.socket')}}</span>
      <el-form-item
        prop="socket">
        <el-input v-model="environment.socket" :placeholder="$t('api_test.request.url_description')" clearable>
          <template v-slot:prepend>
            <el-select v-model="environment.protocol" class="request-protocol-select">
              <el-option label="http://" value="http"/>
              <el-option label="https://" value="https"/>
            </el-select>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-switch
          v-model="envEnable"
          inactive-text="hosts">
        </el-switch>
      </el-form-item>

      <ms-api-host-table v-if="envEnable" :hostTable="environment.hosts" ref="refHostTable"/>

      <span>{{$t('api_test.environment.globalVariable')}}</span>
      <ms-api-scenario-variables :show-variable="false" :items="environment.variables"/>

      <span>{{$t('api_test.request.headers')}}</span>
      <ms-api-key-value :items="environment.headers" :suggestions="headerSuggestions"/>

      <div class="environment-footer">
        <ms-dialog-footer
          @cancel="cancel"
          @confirm="save()"/>
      </div>
    </el-form>
  </el-main>
</template>

<script>
  import MsApiScenarioVariables from "../ApiScenarioVariables";
  import MsApiKeyValue from "../ApiKeyValue";
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {REQUEST_HEADERS} from "../../../../../../common/js/constants";
  import {KeyValue} from "../../model/ScenarioModel";
  import MsApiHostTable from "../ApiHostTable";

  export default {
    name: "EnvironmentEdit",
    components: {MsApiHostTable, MsDialogFooter, MsApiKeyValue, MsApiScenarioVariables},
    props: {
      environment: Object,
    },
    data() {
      let socketValidator = (rule, value, callback) => {
        if (!this.validateSocket(value)) {
          callback(new Error(this.$t('commons.formatErr')));
        } else {
          callback();
        }
      }
      return {
        result: {},
        envEnable: false,
        rules: {
          name: [
            {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
            {max: 64, message: this.$t('commons.input_limit', [1, 64]), trigger: 'blur'}
          ],
          socket: [{required: true, validator: socketValidator, trigger: 'blur'}],
        },
        headerSuggestions: REQUEST_HEADERS
      }
    },
    watch: {
      environment: function (o) {
        this.envEnable = o.enable;
      }
    },
    methods: {
      save() {
        this.$refs['environment'].validate((valid) => {
          // 校验host列表
          let valHost = true;
          if (this.envEnable) {
            for (let i = 0; i < this.environment.hosts.length; i++) {
              valHost = this.$refs['refHostTable'].confirm(this.environment.hosts[i]);
            }
          }
          if (valid && valHost) {
            this._save(this.environment);
          } else {
            return false;
          }
        });

      },
      _save(environment) {
        let param = this.buildParam(environment);
        let url = '/api/environment/add';
        if (param.id) {
          url = '/api/environment/update';
        }
        this.result = this.$post(url, param, response => {
          if (!param.id) {
            environment.id = response.data;
          }
          this.$success(this.$t('commons.save_success'));
        });
      },
      buildParam(environment) {
        let param = {};
        Object.assign(param, environment);
        if (!(environment.variables instanceof String)) {
          param.variables = JSON.stringify(environment.variables);
        }
        if (!(environment.headers instanceof String)) {
          param.headers = JSON.stringify(environment.headers);
        }
        if (environment.hosts != undefined && !(environment.hosts instanceof String)) {
          let hosts = JSON.parse(JSON.stringify(environment.hosts));
          let validHosts = [];
          // 去除掉未确认的host
          hosts.forEach(host => {
            if (host.status === '') {
              validHosts.push(host);
            }
          });
          environment.hosts = validHosts;
          param.hosts = JSON.stringify(validHosts);
        }
        if (!this.envEnable) {
          param.hosts = null;
        }
        return param;
      },
      validateSocket(socket) {
        if (!socket) return;
        let urlStr = this.environment.protocol + '://' + socket;
        let url = {};
        try {
          url = new URL(urlStr);
        } catch (e) {
          return false
        }

        this.environment.port = url.port;
        this.environment.domain = decodeURIComponent(url.hostname);
        if (url.port) {
          this.environment.socket = this.environment.domain + ':' + url.port + url.pathname;
        } else {
          this.environment.socket = this.environment.domain + url.pathname;
        }
        return true;
      },
      cancel() {
        this.$emit('close');
      },
      clearValidate() {
        this.$refs["environment"].clearValidate();
      },
    },
  }
</script>

<style scoped>

  .el-main {
    border: solid 1px #EBEEF5;
    margin-left: 200px;
  }

  .request-protocol-select {
    width: 90px;
  }

  .el-row {
    margin-bottom: 15px;
  }

  .environment-footer {
    margin-top: 15px;
    float: right;
  }

  span {
    display: block;
    margin-bottom: 15px;
  }

  span:not(:first-child) {
    margin-top: 15px;
  }

</style>
