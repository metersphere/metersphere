<template>
  <div v-if="loadIsOver" v-loading="loading">
    <el-card class="historyCard" v-for="item in reportData" :key="item.id">
      <div slot="header">
        <li style="color:var(--count_number); font-size: 18px">
          <el-input v-if="item.isEdit === 'edit'" size="mini" @blur="updateReport(item)" v-model="item.name"/>
          <el-link v-if="item.isEdit !== 'edit'"   type="info" @click="selectReport(item.id)" target="_blank" style="color:#303133; font-size: 14px">
            {{ item.name }}
          </el-link>
          <el-button v-if="showOptionsButton && item.isEdit !== 'edit'"  style="float: right; padding: 3px 0; border: 0px;margin-left: 5px" icon="el-icon-delete" size="mini" @click="deleteReport(item.id)"></el-button>
          <el-button v-if="showOptionsButton && item.isEdit !== 'edit'"  style="float: right; padding: 3px 0; border: 0px" icon="el-icon-edit" size="mini" @click="renameReport(item)"></el-button>
        </li>
      </div>
      <div class="text item">
        <span>{{ item.createTime | timestampFormatDate }}</span>
      </div>
    </el-card>
  </div>
</template>
<script>

export default {
  name: "HistoryReportDataCard",
  components: {},
  data() {
    return {
      loadIsOver: true,
      loading: false,
    }
  },
  props:{
    reportData:Array,
    showOptionsButton:Boolean
  },
  watch:{
    reportData:{
      handler:function (){
        this.loading = false;
      },
      deep:true
    }
  },
  methods: {
    reload(){
      this.loadIsOver = false;
      this.$nextTick(() => {
        this.loadIsOver = true;
      })
    },
    deleteReport(id){
      this.loading = true;
      this.$emit("deleteReport",id);
    },
    renameReport(item){
      item.isEdit = 'edit';
      this.reload();
    },
    selectReport(id){
      this.$emit("selectReport",id);
    },
    updateReport(item){
      let obj = {
        name: item.name,
        id: item.id
      };
      this.$post("/history/report/updateReport", obj, response => {
      });
      item.isEdit = "";
      this.reload();
    }
  },
}
</script>

<style scoped>

.historyCard{
  border: 0px;
}
/deep/ .el-card__header{
  border: 0px;
  padding-bottom: 0px;
  padding-top: 5px;
}

</style>
