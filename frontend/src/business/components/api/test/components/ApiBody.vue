<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio-button label="kv">
        {{$t('api_test.request.body_kv')}}
      </el-radio-button>
      <el-radio-button label="text">
        {{$t('api_test.request.body_text')}}
      </el-radio-button>
    </el-radio-group>

    <ms-api-key-value :items="body.kvs" v-if="isKV"/>

    <el-input class="textarea" type="textarea" v-model="body.text" :autosize="{ minRows: 10, maxRows: 25}" resize="none"
              v-else/>
  </div>
</template>

<script>
  import MsApiKeyValue from "./ApiKeyValue";

  export default {
    name: "MsApiBody",
    components: {MsApiKeyValue},
    props: {
      body: Object
    },

    data() {
      return {};
    },

    methods: {},

    computed: {
      isKV() {
        return this.body.type === "kv";
      }
    }
  }
</script>

<style scoped>
  .textarea {
    margin-top: 10px;
  }
</style>
