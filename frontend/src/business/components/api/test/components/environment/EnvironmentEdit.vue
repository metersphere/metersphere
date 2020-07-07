<template>
  <el-main v-loading="result.loading">
    <el-form :model="environment" :rules="rules" ref="from">

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

      <span>{{$t('api_test.environment.globalVariable')}}</span>
      <ms-api-scenario-variables :items="environment.variables"/>

      <span>{{$t('api_test.request.headers')}}</span>
      <ms-api-key-value :items="environment.headers"/>

      <div class="environment-footer">
        <el-button type="primary" @click="save">{{this.$t('commons.save')}}</el-button>
      </div>

    </el-form>
  </el-main>
</template>

<script>
    import MsApiScenarioVariables from "../ApiScenarioVariables";
    import MsApiKeyValue from "../ApiKeyValue";

    export default {
      name: "EnvironmentEdit",
      components: {MsApiKeyValue, MsApiScenarioVariables},
      props: {
        environment: {
          type: Object,
          default() {
            return {variables: [{}], headers: [{}], protocol: 'https'};
          }
        }
      },
      data() {
        let socketValidator = (rule, value, callback) => {
          if (!this.validateSocket(value)) {
            callback(new Error(this.$t('commons.formatErr')));
          } else {
            callback();
          }
        };
        return {
          result: {},
          rules: {
            name :[{required: true, message: this.$t('commons.input_name'), trigger: 'blur'}],
            socket :[{required: true, validator: socketValidator, trigger: 'blur'}],
          },
        }
      },
      methods: {
        save() {
          this.$refs['from'].validate((valid) => {
            if (valid) {
             this._save();
            } else  {
              return false;
            }
          });
        },
        _save() {
          let param = this.buildParam();
          let url = '/api/environment/add';
          if (param.id) {
            url = '/api/environment/update';
          }
          this.result = this.$post(url, param,  response => {
            this.environment.id = response.data;
            this.$success(this.$t('commons.save_success'));
          });
        },
        buildParam() {
          let param = {};
          Object.assign(param, this.environment);
          param.variables = JSON.stringify(this.environment.variables);
          param.headers = JSON.stringify(this.environment.headers);
          return param;
        },
        validateSocket(socket) {
          if (!socket) return;
          let socketInfo = socket.split(":");
          if (socketInfo.length > 2) {
            return false;
          }
          let host = socketInfo[0];
          let port = socketInfo[1];
          if (!this.validateHost(host) || !(port == undefined || this.validatePort(port))) {
            return false;
          }
          this.environment.domain = host;
          this.environment.port = port;
          return true;
        },
        validateHost(host) {
          let hostReg =  /^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/;
          if (hostReg.test(host) || host === 'localhost') {
            return true;
          }
          return false;
        },
        validatePort(port) {
          let portReg = /^[1-9]\d*$/;
          if (portReg.test(port) && 1 <= 1*port && 1*port <= 65535){
            return true
          }
          return false;
        }
      }
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
