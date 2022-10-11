<template>
  <div>
    <div class="report-detail-title">
      <div v-if="!reportDetail.nameIsEdit" style="line-height: 30px">
        <span style="margin-left: 5px">{{ reportDetail.name }}</span>
        <i class="el-icon-edit" @click="editAttachDataName(true)"></i>
        <i class="el-icon-delete" @click="deleteDetail" style="float: right;margin-top: 6px; margin-right: 10px"></i>
      </div>
      <el-input v-else v-model="reportDetail.name" @blur="editAttachDataName(false)"></el-input>
    </div>
    <ms-mark-down-text prop="content" :data="reportDetail" :disabled="readOnly" :auto-review="true"
                       :default-open="null"
                       class="mavon-editor" ref="md"/>
  </div>

</template>

<script>

import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";

export default {
  name: "ReportAttachInfo",
  components: {MsMarkDownText},
  data() {
    return {
      result: {loading: false},
    };
  },
  props: {
    reportDetail: Object,
    readOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
  },
  methods: {
    deleteDetail() {
      this.$emit("deleteDetail", this.reportDetail);
    },
    editAttachDataName() {
      this.reportDetail.nameIsEdit = !this.reportDetail.nameIsEdit;
    },
    initData() {
      let innerText = this.$refs.md.getContent();
      this.reportDetail.previewContext = innerText;
    },
    updateData() {
      this.$nextTick(() => {
        let innerText = this.$refs.md.getContent();
        this.reportDetail.previewContext = innerText;
      });
    }
  }
}
</script>

<style scoped>
.report-detail-title {
  background-color: #783887;
  font-size: 16px;
  color: white;
  margin: 5px;
}

.remark-item {
  padding: 0px 15px;
}
</style>
