<template>
  <ms-edit-dialog
    :visible.sync="visible"
    width="20%"
    :title="title"
    :with-footer="false"
    :close-on-click-modal="false">
    {{tip}}
    <template v-slot:footer>
      <el-button type="primary" @click="save" @keydown.enter.native.prevent>{{$t('保存')}}</el-button>
      <el-button @click="cancel">{{$t('不保存')}}</el-button>
    </template>
  </ms-edit-dialog>
</template>

<script>
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
export default {
  name: "IsChangeConfirm",
  components: {MsEditDialog},
  data() {
    return {
      visible: false,
      data: {}
    }
  },
  props: ['title','tip', 'versionEnable'],
  methods: {
    open(item) {
      this.visible = true;
      this.data = item;
    },
    save() {
      this.$emit('confirm', true);
      this.visible = false;
    },
    cancel() {
      this.$emit('confirm', false);
      this.visible = false;
    }
  }

}
</script>

<style scoped>

</style>
