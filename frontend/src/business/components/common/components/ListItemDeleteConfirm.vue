<template>
  <el-dialog
    :title="title"
    :visible.sync="deleteApiVisible"
    :show-close="false"
    width="30%"
  >
    <el-radio-group v-model="deleteCurrentVersion">
      <el-radio :label="true">{{ $t('commons.delete_current_version') }}</el-radio>
      <el-radio :label="false">{{ $t('commons.delete_all_version') }}</el-radio>
    </el-radio-group>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="handleDelete">
      </ms-dialog-footer>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

export default {
  name: "ListItemDeleteConfirm",
  components: {MsDialogFooter},
  data() {
    return {
      deleteApiVisible: false,
      title: null,
      deleteCurrentVersion: true,
      api: {}
    };
  },
  methods: {
    open(api, title) {
      this.api = api;
      this.deleteCurrentVersion = true;
      this.title = title + ' ' + api.name + '?';
      this.deleteApiVisible = true;
    },
    close() {
      this.deleteApiVisible = false;
    },
    handleDelete() {
      this.$emit('handleDelete', this.api, this.deleteCurrentVersion);
    },
  }
};
</script>

<style scoped>

</style>
