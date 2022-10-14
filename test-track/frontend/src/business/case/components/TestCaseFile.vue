<template>
  <el-dialog :visible.sync="dialogVisible" width="80%" :destroy-on-close="true" :before-close="close" :append-to-body="true">
    <div>
      <img :src="url" :alt="$t('test_track.case.img_loading_fail')" style="width: 100%;height: 100%;"
           v-if="file.type === 'JPG' || file.type === 'JPEG' || file.type === 'PNG'">
      <div v-if="file.type === 'PDF'">
        <test-case-pdf :file-id="file.id"/>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import TestCasePdf from "@/business/case/components/TestCasePdf";
import {generateModuleUrl} from "@/business/utils/sdk-utils";

export default {
  name: "TestCaseFiles",
  components: {TestCasePdf},
  props: {},
  data() {
    return {
      file: {
        id: '',
        type: ''
      },
      dialogVisible: false,
      url: ''
    }
  },
  methods: {
    open(file) {
      this.file = file;
      this.dialogVisible = true;
      this.url = generateModuleUrl('/attachment/preview/' + this.file.id + '/' + this.file.isLocal);
    },
    close() {
      this.file = {
        id: '',
        type: ''
      };
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
