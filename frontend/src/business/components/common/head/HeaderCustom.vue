<template>
  <el-dialog :title="$t('table.header_display_field')" :visible.sync="dialogTableVisible" :append-to-body="true">
    <tree-transfer :title="[$t('table.fields_to_be_selected'), $t('table.selected_fields')]"
                   :from_data='optionalFields'
                   :draggable="true"
                   :to_data='fieldSelected'
                   :defaultProps="{label:'label'}"
                   :placeholder="$t('api_test.request.parameters_mock_filter_tips')"
                   :allow-drop="allowDrop"
                   :default-checked-keys="defaultCheckedKeys"
                   :default-transfer="defaultTransfer"
                   :mode='mode' height='540px' filter openAll/>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="saveHeader"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentUser} from "@/common/js/utils";
import treeTransfer from 'el-tree-transfer'

export default {
  name: "HeaderCustom",
  components: {MsDialogFooter, treeTransfer},
  data() {
    return {
      dialogTableVisible: false,
      value: [],
      fieldSelected: [],
      defaultCheckedKeys: [],
      defaultTransfer: true,
      mode: "transfer", // transfer addressList
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
    allowDrop(draggingNode, dropNode, type) {
      return type !== 'inner';
    },
    open(items) {
      this.defaultCheckedKeys = []
      this.dialogTableVisible = true
      items.forEach(i => {
          this.defaultCheckedKeys.push(i.id)
        }
      )
      if(this.type==='api_list'||this.type==='api_case_list'||this.type==='api_scenario_list'||this.type==='test_plan_function_test_case'
        ||this.type==='test_plan_api_case'||this.type==='test_plan_load_case'||this.type==='test_plan_scenario_case'){
        this.fieldSelected=items
      }
    },
    saveHeader() {
      let param = {
        userId: getCurrentUser().id,
        type: this.type,
        props: JSON.stringify(this.fieldSelected)
      }
      this.$post("/system/save/header", param, response => {
        console.log(this.optionalFields)
        console.log(this.fieldSelected)
        this.$success(this.$t("commons.save_success"));
        this.initTableData()
        this.close();
      })
    },
    removeAt(idx) {
      this.list.splice(idx, 1);
    },
    close() {
      this.dialogTableVisible = false
    },


    // 切换模式 现有树形穿梭框模式transfer 和通讯录模式addressList
    // changeMode() {
    //   if (this.mode == "transfer") {
    //     this.mode = "addressList";
    //   } else {
    //     this.mode = "transfer";
    //   }
    // },
    // // 监听穿梭框组件添加
    // add(fromData, toData, obj){
    //   // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
    //   // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
    //   console.log("fromData:", fromData);
    //   console.log("toData:", toData);
    //   console.log("obj:", obj);
    // },
    // // 监听穿梭框组件移除
    // remove(fromData, toData, obj){
    //   // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
    //   // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
    //   console.log("fromData:", fromData);
    //   console.log("toData:", toData);
    //   console.log("obj:", obj);
    // }
  }
}
</script>

<style scoped>

</style>
