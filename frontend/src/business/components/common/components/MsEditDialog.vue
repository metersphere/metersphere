<template>
  <el-dialog :close-on-click-modal="false"
             :title="title"
             :width="width"
             :visible="visible" destroy-on-close
             @close="handleClose">

    <slot name="header"></slot>

    <slot></slot>

    <template v-slot:footer>
      <slot name="footer">
        <div class="dialog-footer">
          <ms-dialog-footer
            @cancel="handleCancel"
            @confirm="handleConfirm"/>
        </div>
      </slot>
    </template>

  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
export default {
  name: "MsEditDialog",
  components: {MsDialogFooter},
  data() {
    return {
      visible: false
    }
  },
  props: {
    title: {
      type: String,
      default() {
        return 'title';
      }
    },
    width: {
      type: String,
      default() {
        return "50%";
      }
    }
  },
  methods: {
    open() {
      this.visible = true;
    },
    handleConfirm() {
      this.$emit('confirm');
    },
    handleCancel() {
      this.handleClose();
      this.$emit('cancel');
    },
    handleClose() {
      this.visible = false;
    },
  }
}
</script>

<style scoped>

</style>
