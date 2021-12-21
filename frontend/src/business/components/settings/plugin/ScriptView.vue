<template>
  <div v-if="!loading">
    <el-dialog :title="$t('plugin.script_view')" :visible.sync="dialogVisible" @close="close">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane :label="$t('commons.form_config')" name="formOption">
          <ms-code-edit
            height="400px"
            :data.sync="plugin.formOption"
            :modes="modes"
            :mode="'json'"
            ref="codeEdit"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('commons.form_content')" name="formScript">
          <ms-code-edit
            height="400px"
            :data.sync="plugin.formScript"
            :modes="modes"
            :mode="'json'"
            ref="codeEdit"/>
        </el-tab-pane>
      </el-tabs>

    </el-dialog>
  </div>
</template>

<script>
import MsCodeEdit from "../../common/components/MsCodeEdit";

export default {
  name: "ScriptView",
  components: {
    MsCodeEdit
  },
  data() {
    return {
      dialogVisible: false,
      activeName: "formScript",
      modes: ['text', 'json', 'xml', 'html'],
      plugin: {},
      loading: false,
    }
  },
  methods: {
    getPlugin(id) {
      if (id) {
        this.$get('/plugin/get/' + id, response => {
          if (response.data) {
            this.plugin = response.data;
            this.reload();
          } else {
            this.$warning(this.$t('plugin.warning_tip'));
          }
        });
      }
    },
    open(id) {
      this.getPlugin(id);
      this.dialogVisible = true;
    },
    close() {
      this.dialogVisible = false;
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      });
    },
    handleClick() {

    }
  }
}
</script>

<style scoped>

</style>
