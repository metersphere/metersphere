<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio :disabled="isReadOnly" :label="type.KV" @change="modeChange">
        {{ $t('api_test.definition.request.body_form_data') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.WWW_FORM" @change="modeChange">
        {{ $t('api_test.definition.request.body_x_www_from_urlencoded') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.JSON" @change="modeChange">
        {{ $t('api_test.definition.request.body_json') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.XML" @change="modeChange">
        {{ $t('api_test.definition.request.body_xml') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.RAW" @change="modeChange">
        {{ $t('api_test.definition.request.body_raw') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.BINARY" @change="modeChange">
        {{ $t('api_test.definition.request.body_binary') }}
      </el-radio>
    </el-radio-group>
    <ms-api-variable :is-read-only="isReadOnly"
                     :parameters="body.kvs"
                     :isShowEnable="isShowEnable"
                     type="body"
                     v-if="body.type == 'KeyValue'"/>

    <ms-api-from-url-variable :is-read-only="isReadOnly"
                              :parameters="body.fromUrlencoded"
                              type="body"
                              v-if="body.type == 'WWW_FORM'"/>

    <div class="ms-body" v-if="body.type == 'JSON'">
      <ms-json-code-edit @json-change="jsonChange" @onError="jsonError" :value="body.json" ref="jsonCodeEdit"/>
    </div>

    <div class="ms-body" v-if="body.type == 'XML'">
      <ms-code-edit :read-only="isReadOnly" :data.sync="body.xml" :modes="modes" ref="codeEdit"/>
    </div>


    <div class="ms-body" v-if="body.type == 'Raw'">
      <ms-code-edit :read-only="isReadOnly" :data.sync="body.raw" :modes="modes" ref="codeEdit"/>
    </div>

    <ms-api-binary-variable :is-read-only="isReadOnly"
                            :parameters="body.binary"
                            :isShowEnable="isShowEnable"
                            type="body"
                            v-if="body.type == 'BINARY'"/>

  </div>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import {BODY_FORMAT, BODY_TYPE, KeyValue} from "../../model/ApiTestModel";
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
      body: {},
      headers: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      isShowEnable: {
        type: Boolean,
        default: true
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
        switch (this.body.type) {
          case "JSON":
            this.body.format = "json";
            this.setContentType("application/json");
            break;
          case "XML":
            this.body.format = "xml";
            this.setContentType("text/xml");
            break;
          case "WWW_FORM":
            this.body.format = "form";
            this.setContentType("application/x-www-form-urlencoded");
            break;
          case "BINARY":
            this.body.format = "binary";
            this.setContentType("application/octet-stream");
            break;
          default:
            this.removeContentType();
            this.body.format = mode;
            break;
        }
      },
      setContentType(value) {
        let isType = false;
        this.headers.forEach(item => {
          if (item.name === "Content-Type") {
            item.value = value;
            isType = true;
          }
        })
        if (!isType) {
          this.headers.unshift(new KeyValue({name: "Content-Type", value: value}));
        }
      },
      removeContentType() {
        for (let index in this.headers) {
          if (this.headers[index].name === "Content-Type") {
            this.headers.splice(index, 1);
            return;
          }
        }
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

  .ms-body {
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
