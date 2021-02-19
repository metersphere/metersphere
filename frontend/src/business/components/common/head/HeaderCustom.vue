<template>
  <el-dialog title="表头显示字段" :visible.sync="dialogTableVisible"  :append-to-body="true">
    <template>
      <el-transfer
        :titles="['待选字段', '已选字段']"
        v-model="value"
        :props="{
           key: 'prop',
           label: 'label'
          }"
        :data="optionalField"
        style="margin-left: 10%"
      ></el-transfer>
    </template>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveHeader"
      />
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentUser} from "@/common/js/utils";
import {TEST_CASE_LIST} from "@/common/js/constants";
import {Track_Test_Case} from "@/business/components/common/model/JsonData";
export default {
  name: "HeaderCustom",
  components: {MsDialogFooter},
  data() {
    return {
      dialogTableVisible: false,
      optionalField: this.optionalFields,
      value: [],
      fieldSelected: []
    }
  },
  props: {
    initTableData: {
      type: Function,
      default: null
    },
    optionalFields: Array,
    type: String
  },
  methods: {
    open(items) {
      this.dialogTableVisible = true
      /*this.optionalField = items*/
    },
    saveHeader() {
      console.log(this.type)
      let param = {
        userId: getCurrentUser().id,
        type: this.type,
        props: JSON.stringify(this.value)
      }
      this.$post("/system/save/header", param, response => {
        this.$success(this.$t("commons.save_success"));
        this.dialogTableVisible = false
        this.initTableData()
      })
    },
    close() {
      this.dialogTableVisible = false
    }
  }
}
</script>

<style scoped>

</style>
