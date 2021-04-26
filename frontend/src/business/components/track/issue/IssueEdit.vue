<template>

  <el-drawer
    class="field-template-edit"
    :before-close="handleClose"
    :visible.sync="visible"
    :with-header="false"
    size="100%"
    :modal-append-to-body="false"
    ref="drawer"
    >
    <template v-slot:default="scope">
      <template-component-edit-header :show-edit="false" :template="{}" prop="title" @cancel="handleClose" @save="save"/>
      <issue-edit-detail @refresh="$emit('refresh')" @close="handleClose" ref="issueEditDetail"/>
    </template>
  </el-drawer>
</template>
<script>

import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import IssueEditDetail from "@/business/components/track/issue/IssueEditDetail";
export default {
  name: "IssueEdit",
  components: {IssueEditDetail, TemplateComponentEditHeader},
  data() {
    return {
      visible: false
    }
  },
  methods: {
    open(data) {
      this.visible = true;
      this.$nextTick(() => {
        this.$refs.issueEditDetail.open(data);
      })
    },
    handleClose() {
      this.visible = false;
    },
    save() {
      this.$refs.issueEditDetail.save();
    }
  }
};
</script>

<style scoped>

</style>
