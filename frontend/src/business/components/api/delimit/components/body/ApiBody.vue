<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio :disabled="isReadOnly" :label="type.KV">
        {{ $t('api_test.delimit.request.body_form_data') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.WWW_FORM">
        {{ $t('api_test.delimit.request.body_x_www_from_urlencoded') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.JSON">
        {{ $t('api_test.delimit.request.body_json') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.XML">
        {{ $t('api_test.delimit.request.body_xml') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.RAW">
        {{ $t('api_test.delimit.request.body_raw') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.BINARY">
        {{ $t('api_test.delimit.request.body_binary') }}
      </el-radio>
    </el-radio-group>

    <ms-dropdown :default-command="body.format" v-if="body.type == 'Raw'" :commands="modes" @command="modeChange"/>

    <ms-api-variable :is-read-only="isReadOnly"
                     :parameters="body.kvs"
                     :environment="environment"
                     type="body"
                     v-if="body.isKV()"/>

    <ms-api-from-url-variable :is-read-only="isReadOnly"
                              :parameters="body.fromUrlencoded"
                              :environment="environment"
                              type="body"
                              v-if="body.type == 'WWW_FORM'"/>

    <div class="body-raw" v-if="body.type == 'Raw'">
      <ms-json-code-edit v-if="body.format=='json'" @json-change="jsonChange" @onError="jsonError" :value="body.json" ref="jsonCodeEdit"/>
      <ms-code-edit v-else :mode="body.format" :read-only="isReadOnly" :data.sync="body.raw" :modes="modes" ref="codeEdit"/>
    </div>

    <ms-api-binary-variable :is-read-only="isReadOnly"
                            :parameters="body.binary"
                            :environment="environment"
                            type="body"
                            v-if="body.type == 'BINARY'"/>

  </div>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import {Body, BODY_FORMAT, BODY_TYPE, Scenario} from "../../model/ApiTestModel";
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsJsonCodeEdit from "../../../../common/components/MsJsonCodeEdit";

  import MsDropdown from "../../../../common/components/MsDropdown";
  import MsApiVariable from "../ApiVariable";
  import MsApiBinaryVariable from "./ApiBinaryVariable";
  import MsApiFromUrlVariable from "./ApiFromUrlVariable";

  export default {
    name: "MsApiBody",
    components: {
      MsApiVariable,
      MsDropdown,
      MsCodeEdit,
      MsApiKeyValue,
      MsApiBinaryVariable,
      MsApiFromUrlVariable,
      MsJsonCodeEdit
    },
    props: {
      body: Body,
      scenario: Scenario,
      environment: Object,
      extract: Object,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        type: BODY_TYPE,
        modes: ['text', 'json', 'xml', 'html']
      };
    },

    methods: {
      modeChange(mode) {
        this.body.format = mode;
      },
      jsonChange(json) {
        this.body.json = json;
      },
      jsonError(e) {
        this.$error(e);
      }
    },

    created() {
      if (!this.body.type) {
        this.body.type = BODY_TYPE.KV;
      }
      if (!this.body.format) {
        this.body.format = BODY_FORMAT.TEXT;
      }
      this.body.kvs.forEach(param => {
        if (!param.type) {
          param.type = 'text';
        }
      });
    }
  }
</script>

<style scoped>
  .textarea {
    margin-top: 10px;
  }

  .body-raw {
    padding: 15px 0;
    height: 400px;
  }

  .el-dropdown {
    margin-left: 20px;
    line-height: 30px;
  }

  .ace_editor {
    border-radius: 5px;
  }

</style>
