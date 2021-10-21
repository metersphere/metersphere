<template>

  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.info')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true"
             @close="handleClose" append-to-body>
    <div style="height: 700px;overflow: auto">
      <div v-if="detail.createUser">
        <p class="tip">{{ this.$t('report.user_name') }} ：{{ detail.createUser }}</p>
      </div>
      <div>
        <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
      </div>
      <div style="overflow: auto">
        <p class="tip">{{ this.$t('report.test_log_details') }} </p>
        <div v-if="!loading">
          {{ $t('commons.tag') }}：
          <ms-input-tag :read-only="true" :data="detail" ref="tag" style="width: 90%"/>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import MsInputTag from "./MsInputTag";

export default {
  name: "MsTagsHistoryDetail",
  components: {MsInputTag},
  props: {
    title: String,
  },
  data() {
    return {
      infoVisible: false,
      detail: {},
      loading: false,
    }
  },
  methods: {
    handleClose() {
      this.infoVisible = false;
      this.detail = {};
    },
    open(value) {
      this.infoVisible = true;
      this.detail = value;
      if (value != null && value.diffValue != 'null' && value.diffValue != '' && value.diffValue != undefined) {
        if (Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
          && Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
          let diffValue = JSON.parse(value.diffValue);
          if (diffValue) {
            this.detail.diffValue = diffValue.root;
            this.reload();
          }
        }
      }
      this.reload();
    },
    getType(type) {
      return this.LOG_TYPE_MAP.get(type);
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
  }
}
</script>

<style scoped>

</style>
