<template>
  <el-dialog
    :title="title"
    :visible.sync="dialogVisible"
    @close="close"
    :width="width ? width : '75%'"
    v-loading="result"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :fullscreen="fullScreen"
    top="50px"
    append-to-body>
    <template #title>
      <slot name="title" :title="title"></slot>
    </template>

    <el-header class="header-btn">
      <slot name="headerBtn"></slot>
    </el-header>

    <el-container class="main-content">
      <el-aside class="ms-aside-container"
                :style="{
        'min-width': '300px',
        'max-width': '600px',
        'height': calHeight,
        'max-height': calHeight,
        }">
        <slot name="aside"></slot>
        <ms-horizontal-drag-bar v-if="draggable"/>
      </el-aside>


      <el-container>
        <el-main class="case-content">
          <slot></slot>
        </el-main>
      </el-container>
    </el-container>

    <template v-slot:footer>
      <slot name="footer"></slot>
    </template>
  </el-dialog>
</template>

<script>
import MsHorizontalDragBar from "metersphere-frontend/src/components/dragbar/MsLeft2RightDragBar"
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import SelectMenu from '@/business/commons/SelectMenu';

export default {
  name: 'RelevanceDialog',
  components: {
    SelectMenu,
    MsDialogFooter,
    MsHorizontalDragBar
  },
  data() {
    return {
      result: false,
      dialogVisible: false,
    };
  },
  props: {
    title: {
      type: String
    },
    width: {
      type: String
    },
    fullScreen: {
      type: Boolean
    },
    draggable: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    calHeight() {
      return 'calc(75vh - 50px)'
    }
  },
  methods: {
    open() {
      this.dialogVisible = true;
    },
    close() {
      this.dialogVisible = false;
    },
  },
};
</script>

<style scoped>
.el-dialog {
  min-height: 600px;
}

.tree-aside {
  max-height: 600px;
}

.el-dialog :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.header-btn {
  position: absolute;
  top: 40px;
  right: 30px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  height: 30px;
}

.ms-aside-container {
  border: 1px solid #E6E6E6;
  padding: 10px;
  border-radius: 2px;
  box-sizing: border-box;
  background-color: #FFF;
  border-right: 0;
  position: relative;
  overflow: visible;
}
</style>
