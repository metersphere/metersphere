<template>
  <el-dialog
    title="表头显示字段"
    :visible.sync="centerDialogVisible"
    width="50%"
  >
    <el-divider style="margin-top: 0;margin-right: 10px"></el-divider>
    <el-form :inline="true" :model="headerItem" class="demo-form-inline" type="selection">
      <el-row :gutter="20">
        <el-col :span="10">
          <label>可选择字段可选择字段:{{ this.headerItem.optionalField.length }}</label>
          <div class="grid-content bg-purple" style="margin-top: 25px">
            <el-form-item>
              <el-card class="box-card">
                <div v-for="item in headerItem.optionalField" :key="item.prop" class="text item"
                     @click="selectItem(item)">
                  <input type="checkbox"/>
                  <label>{{ item.name }}</label>
                </div>
              </el-card>
            </el-form-item>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="grid-content bg-purple" style="margin-top: 160px">
            <el-form-item>
              <el-button size="medium" round @click="addItem">添加</el-button>
              <!--               <i class="el-icon-arrow-right" @click="addItem"></i>-->
            </el-form-item>
            <el-form-item>
              <el-button size="medium" round>移除</el-button>
              <!--               <i class="el-icon-arrow-right" @click="addItem"></i>-->
            </el-form-item>
          </div>
        </el-col>
        <el-col :span="10">
          <label>已选择字段</label>
          <div class="grid-content bg-purple" style="margin-top: 25px">
            <el-form-item>
              <el-card class="box-card">
                <div v-for="item in headerItem.fieldSelected" :key="item.prop" class="text item"
                     @click="moveItem(item)">
                  <el-checkbox v-model="checked1">{{ item.name }}</el-checkbox>
                </div>
              </el-card>
            </el-form-item>
          </div>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" style="margin-right: 0" @click="save('headerItem')">保存</el-button>
      </el-form-item>
    </el-form>


  </el-dialog>
</template>

<script>
export default {
  name: "CustomHeader",
  data() {
    return {
      centerDialogVisible: false,
      headerItem: {
        optionalField: [
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
          {prop: 'name', name: '名称'},
        ],
        fieldSelected: []
      },
      selectItems: [],
      checked1: false


    };
  },
  methods: {
    open() {
      this.centerDialogVisible = true
    },

    selectItem(item) {
      this.checked1 = !this.checked1
      this.selectItems.push(item)

    },
    //移除
    moveItem(item) {
      this.headerItem.fieldSelected.remove(item)

    },
    //添加
    addItem() {
      console.log(this.selectItems)
      this.headerItem.fieldSelected = this.selectItems
      console.log(this.headerItem.fieldSelected)
    },
    save(headerItem) {

    }
  }
}
</script>

<style scoped>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.box-card {
  width: 250px;
  height: 400px;
  overflow: auto;
}
</style>
