<template>
  <div>
    <el-row>
      <span class="text-class">{{ $t('api_test.mock.req_param') }}</span>
      <div style="color: var(--primary_color);margin-top: 8px;">
        <el-checkbox v-model="fromData.headers" @change="changeEvent"><span class="text-class">{{
            'Header' + '\xa0\xa0'
          }}</span></el-checkbox>
        <el-checkbox v-model="fromData.query" @change="changeEvent">
          <span class="text-class">{{ $t('api_test.definition.request.query_param') }}</span>
        </el-checkbox>
        <el-checkbox v-model="fromData.rest" @change="changeEvent">
          <span class="text-class">{{ $t('api_test.definition.request.rest_param') }}</span>
        </el-checkbox>
        <el-checkbox v-model="fromData.body" @change="changeEvent"><span
          class="text-class">{{ $t('api_test.request.body') }}</span></el-checkbox>
      </div>
    </el-row>
    <el-row style="margin-top: 16px">
      <span class="text-class">{{ $t('commons.other') }}</span>
      <div style="color: var(--primary_color);margin-top: 8px;">
        <el-checkbox v-model="fromData.delNotSame" @change="changeEvent">
          <span class="text-class">{{ $t('workstation.delNotSame') }}</span>
        </el-checkbox>
      </div>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'SyncSetting',
  props: {
    syncData: {
      type: Object,
      default() {
        return {
          method: true,
          path: true,
          headers: true,
          query: true,
          rest: true,
          body: true,
          delNotSame: true,
        };
      },
    },
  },
  data() {
    return {
      fromData: this.syncData,
    };
  },
  methods: {
    changeEvent() {
      this.$emit('updateSyncData', this.fromData);
    },
  },
};
</script>

<style scoped>
.text-class {
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}
</style>
