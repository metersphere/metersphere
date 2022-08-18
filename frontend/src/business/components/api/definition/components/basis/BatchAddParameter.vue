<template>
  <div>
    <ms-drawer :visible="dialogVisible" :size="30" @close="handleClose" direction="right"
               :show-full-screen="false" :is-show-close="false">
      <div>
        <el-row>
          <el-col :span="14">
            <div v-html="$t('api_test.batch_add_parameter')"/>
            <div v-if="isScenario" v-html="$t('api_test.bach_add_type_info')"/>
          </el-col>
          <el-col :span="10" class="buttons">
            <el-button size="mini" @click="handleClose">{{ $t('commons.cancel') }}</el-button>
            <el-button type="primary" size="mini" @click="confirm" @keydown.enter.native.prevent>
              {{ $t('commons.confirm') }}
            </el-button>
          </el-col>
        </el-row>
        <div class="ms-code">
          <ms-code-edit class="ms-code" :enable-format="false" mode="text" :data.sync="parameters" theme="eclipse"
                        :modes="['text']"
                        ref="codeEdit"/>
        </div>
      </div>
    </ms-drawer>
  </div>
</template>

<script>
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsCodeEdit from "../../../../common/components/MsCodeEdit";
import MsDrawer from "../../../../common/components/MsDrawer";

export default {
  name: "BatchAddParameter",
  components: {
    MsDrawer,
    MsDialogFooter,
    MsCodeEdit
  },
  props: {},
  data() {
    return {
      dialogVisible: false,
      parameters: "",
      isScenario: false
    }
  },
  methods: {
    open(data) {
      if (data && data === 'scenario') {
        this.isScenario = true;
      }
      this.dialogVisible = true;
      listenGoBack(this.handleClose);
    },
    handleClose() {
      this.parameters = "";
      this.dialogVisible = false;
      removeGoBackListener(this.handleClose);
    },
    confirm() {
      let params = this.parameters.split("\n");
      let index = 1;
      let isNormal = true;
      params.forEach(item => {
        if (item) {
          let line = item.split(/：|:/);
          if (!line[0]) {
            isNormal = false;
            this.$warning(this.$t('api_test.params_format_warning', [index]) + " :" + this.$t('api_test.automation.variable_warning'));
            return;
          }
          index++;
        }
      });
      if (isNormal) {
        this.dialogVisible = false;
        this.$emit("batchSave", this.parameters);
        this.parameters = "";
      }
    }
  }
}
</script>

<style scoped>

.ms-drawer {
  padding: 10px 13px;
}

.ms-code {
  height: calc(100vh);
}

.buttons .el-button {
  float: right;
}

.buttons .el-button:nth-child(2) {
  margin-right: 15px;
}
</style>
