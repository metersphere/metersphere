<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio-button :disabled="isReadOnly" :label="type.KV">
        {{$t('api_test.request.body_kv')}}
      </el-radio-button>
      <el-radio-button :disabled="isReadOnly" :label="type.RAW">
        {{$t('api_test.request.body_text')}}
      </el-radio-button>
    </el-radio-group>

    <ms-api-key-value :is-read-only="isReadOnly" :items="body.kvs" v-if="body.isKV()"/>

    <el-input :disabled="isReadOnly" class="textarea" type="textarea" v-model="body.raw" :autosize="{ minRows: 10, maxRows: 25}" resize="none"
              v-else/>
  </div>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";
  import {Body, BODY_TYPE} from "../model/ScenarioModel";

  export default {
    name: "MsApiBody",
    components: {MsApiKeyValue},
    props: {
      body: Body,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        type: BODY_TYPE
      };
    },

    methods: {},

    created() {
      if (this.body.type === null) {
        this.body.type = BODY_TYPE.KV;
      }
    }
  }
</script>

<style scoped>
  .textarea {
    margin-top: 10px;
  }
</style>
