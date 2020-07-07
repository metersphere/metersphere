<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px">

    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input :disabled="isReadOnly" v-model="request.name" maxlength="100" show-word-limit/>
    </el-form-item>

    <el-form-item v-if="!request.useEnvironment" :label="$t('api_test.request.url')" prop="url" class="adjust-margin-bottom">
      <el-input :disabled="isReadOnly" v-model="request.url" maxlength="500"
                :placeholder="$t('api_test.request.url_description')" @change="urlChange" clearable>
        <template  v-slot:prepend>
          <ApiRequestMethodSelect :is-read-only="isReadOnly" :request="request" @change="methodChange"/>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item v-if="request.useEnvironment" :label="$t('api_test.request.path')" prop="path">
      <el-input :disabled="isReadOnly" v-model="request.path" maxlength="500"
                :placeholder="$t('api_test.request.path_description')" @change="pathChange" clearable>
        <template  v-slot:prepend>
          <ApiRequestMethodSelect :is-read-only="isReadOnly" :request="request" @change="methodChange"/>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item v-if="request.useEnvironment" :label="$t('api_test.request.address')" class="adjust-margin-bottom">
      <el-tag class="environment-display">
        <span class="environment-name">{{request.environment ? request.environment.name + ': ' : ''}}</span>
        <span class="environment-url">{{displayUrl}}</span>
        <span v-if="!displayUrl" class="environment-url-tip">{{$t('api_test.request.please_configure_environment_in_scenario')}}</span>
      </el-tag>
    </el-form-item>

    <el-form-item>
      <el-switch
        v-model="request.useEnvironment"
        :active-text="$t('api_test.request.refer_to_environment')" @change="useEnvironmentChange">
      </el-switch>
    </el-form-item>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.request.parameters')" name="parameters">
        <ms-api-key-value :is-read-only="isReadOnly" :items="request.parameters"
                          :description="$t('api_test.request.parameters_desc')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
        <ms-api-key-value :is-read-only="isReadOnly" :items="request.headers"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.body')" name="body" v-if="isNotGet">
        <ms-api-body :is-read-only="isReadOnly" :body="request.body"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions.label')" name="assertions">
        <ms-api-assertions :is-read-only="isReadOnly" :assertions="request.assertions"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.extract.label')" name="extract">
        <ms-api-extract :is-read-only="isReadOnly" :extract="request.extract"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import MsApiBody from "./ApiBody";
  import MsApiAssertions from "./assertion/ApiAssertions";
  import {KeyValue, Request} from "../model/ScenarioModel";
  import MsApiExtract from "./extract/ApiExtract";
  import ApiRequestMethodSelect from "./collapse/ApiRequestMethodSelect";

  export default {
    name: "MsApiRequestForm",
    components: {ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiBody, MsApiKeyValue},
    props: {
      request: Request,
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
        activeName: "parameters",
        rules: {
          name: [
            {max: 100, message: this.$t('commons.input_limit', [0, 100]), trigger: 'blur'}
          ],
          url: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
            {validator: validateURL, trigger: 'blur'}
          ],
          path: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
          ]
        }
      }
    },

    methods: {
      urlChange() {
        if (!this.request.url) return;
        let url = this.getURL(this.addProtocol(this.request.url));
        if (url) {
          this.request.url = decodeURIComponent(url.origin + url.pathname);
        }
      },
      pathChange() {
        if (!this.request.path) return;
        if (!this.request.path.startsWith('/')) {
          this.request.path = '/' + this.request.path;
        }
        let url = this.getURL(this.displayUrl);
        this.request.path = decodeURIComponent(url.pathname);
        this.request.urlWirhEnv = decodeURIComponent(url.origin + url.pathname);
      },
      getURL(urlStr) {
        try {
          let url = new URL(urlStr);
          url.searchParams.forEach((value, key) => {
            if (key && value) {
              this.request.parameters.splice(0, 0, new KeyValue(key, value));
            }
          });
          return url;
        } catch (e) {
          this.$error(this.$t('api_test.request.url_invalid'), 2000);
        }
      },
      methodChange(value) {
        if (value === 'GET' && this.activeName === 'body') {
          this.activeName = 'parameters';
        }
      },
      useEnvironmentChange(value) {
        if (value && !this.request.environment) {
          this.$error(this.$t('api_test.request.please_add_environment_to_scenario'), 2000);
          this.request.useEnvironment = false;
        }
      },
      addProtocol(url) {
        if (url) {
          if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
            return "https://" + url;
          }
        }
        return url;
      }
    },

    computed: {
      isNotGet() {
        return this.request.method !== "GET";
      },
      displayUrl() {
        return this.request.environment ? this.request.environment.protocol + '://' + this.request.environment.socket + (this.request.path ? this.request.path : '') : '';
      }
    }
  }
</script>

<style scoped>
  .request-method-select {
    width: 110px;
  }

  .el-tag {
    width: 100%;
    height: 40px;
    line-height: 40px;
  }

  .environment-display {
    font-size: 14px;
  }

  .environment-name {
    font-weight: bold;
    font-style: italic;
  }

  .adjust-margin-bottom {
    margin-bottom: 10px;
  }

  .environment-url-tip {
    color: #F56C6C;
  }

</style>
