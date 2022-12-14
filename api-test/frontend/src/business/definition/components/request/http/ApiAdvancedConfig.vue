<template>
  <div>
    <el-row style="margin: 20px">
      <span style="margin-right: 10px"> {{ $t('api_test.request.connect_timeout') }}: </span>
      <span style="margin-right: 10px">
        <el-input-number
          size="small"
          :disabled="isReadOnly"
          v-model="request.connectTimeout"
          :placeholder="$t('commons.millisecond')"
          :max="1000 * 10000000"
          :min="0" />
      </span>
      <span style="margin-right: 10px"> {{ $t('api_test.request.response_timeout') }}: </span>
      <span style="margin-right: 10px">
        <el-input-number
          size="small"
          :disabled="isReadOnly"
          v-model="request.responseTimeout"
          :placeholder="$t('commons.millisecond')"
          :max="1000 * 10000000"
          :min="0" />
      </span>
    </el-row>
    <el-row style="margin: 20px">
      <span style="margin-right: 10px"> {{ $t('api_test.request.cert_alias') }}: </span>
      <span style="margin-right: 10px">
        <el-input size="small" style="width: 350px" v-model="request.alias" :disabled="isReadOnly"/>
      </span>
    </el-row>
    <el-row style="margin: 20px">
      <span style="margin-right: 10px">
        <el-checkbox class="follow-redirects-item" v-model="request.followRedirects" @change="changeFollow" :disabled="isReadOnly">{{
          $t('api_test.request.follow_redirects')
        }}</el-checkbox>
      </span>
      <span style="margin-left: 10px; margin-right: 10px">
        <el-checkbox class="follow-redirects-item" v-model="request.autoRedirects" @change="changeAuto" :disabled="isReadOnly">{{
          $t('api_definition.request.auto_redirects')
        }}</el-checkbox>
      </span>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'MsApiAdvancedConfig',
  props: {
    request: Object,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    changeFollow() {
      if (this.request.followRedirects) {
        this.request.autoRedirects = false;
      }
    },
    changeAuto() {
      if (this.request.autoRedirects) {
        this.request.followRedirects = false;
      }
    },
  },
};
</script>

<style scoped></style>
