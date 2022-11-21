<template>
  <el-dialog
    :title="title"
    :visible.sync="deleteApiVisible"
    class="delete-body"
    width="30%"
  >
    <p>{{ this.content }}</p>
    <el-link style="color: #6c327a" @click="showRef" v-if="hasRef">{{
      $t('api_test.automation.view_ref')
    }}</el-link>
    <el-link
      style="color: #6c327a"
      v-if="showCase"
      @click="redirectPage('api', 'apiTestCase', 'ref')"
    >
      {{ $t('api_definition.view_case') }}
    </el-link>
    <el-link
      style="color: #6c327a"
      v-if="showScenario"
      @click="redirectPage('scenario', 'scenario', 'ref')"
      >{{ $t('api_definition.view_scenario') }}
    </el-link>
    <span slot="footer">
      <el-button @click="close" size="mini">{{
        $t('commons.cancel')
      }}</el-button>
      <el-button
        v-prevent-re-click
        type="primary"
        @click="handleDelete"
        @keydown.enter.native.prevent
        size="mini"
      >
        {{ $t('commons.confirm') }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getUUID } from 'metersphere-frontend/src/utils';

export default {
  name: 'ApiDeleteConfirm',
  components: {},
  data() {
    return {
      deleteApiVisible: false,
      title: null,
      deleteCurrentVersion: true,
      content: {},
      apiCase: {},
      selectIds: [],
    };
  },
  props: {
    hasRef: {
      type: Boolean,
      default: false,
    },
    showCase: {
      type: Boolean,
      default: false,
    },
    showScenario: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    open(content, title, row, selectIds) {
      this.content = content;
      this.title = title;
      this.deleteApiVisible = true;
      this.apiCase = row;
      this.selectIds = selectIds;
    },
    close() {
      this.deleteApiVisible = false;
    },
    handleDelete() {
      this.$emit('handleDeleteCase', this.apiCase);
    },
    showRef() {
      this.$emit('showCaseRef', this.apiCase);
    },
    redirectPage(redirectPage, dataType, selectRange) {
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home;
      switch (redirectPage) {
        case 'api':
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
            },
            query: { ids: this.selectIds },
          });
          break;
        case 'scenario':
          home = this.$router.resolve({
            name: 'ApiAutomationWithQuery',
            params: {
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
            },
            query: { ids: this.selectIds },
          });
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
      }
    },
  },
};
</script>

<style scoped>
.delete-body :deep(.el-dialog__body, .el-dialog__footer) {
  padding: 0px 20px;
}
</style>
