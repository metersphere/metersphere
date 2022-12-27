<template>
  <div style="background-color:#F5F6F7;height: calc(100vh);">
    <div class="api-home-layout">
      <el-row style="margin-top: 12px;margin-bottom: 16px">
        <my-dashboard-card style="height: 182px" card-type="upcoming" ></my-dashboard-card>
      </el-row>
      <el-row style="margin-bottom: 16px">
        <my-dashboard-card style="height: 182px" card-type="focus"></my-dashboard-card>
      </el-row>
      <el-row :gutter=16 >
        <el-col :span="12" >
          <my-case-card style="height: 350px"></my-case-card>
        </el-col>
        <el-col :span="12" >
          <my-flaw-card style="height: 350px"></my-flaw-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>
<script>
import MyCaseCard from "./components/MyCaseCard";
import MyFlawCard from "./components/MyFlawCard";
import MyDashboardCard from "@/business/dashboard/components/MyDashboardCard";


export default {
  name: 'DashBoard',
  components: {
    MyCaseCard,
    MyFlawCard,
    MyDashboardCard
  },
  data() {
    return {}
  },
  mounted() {
  },
  methods: {}
}
</script>
<style scoped>
.api-home-layout {
  margin: 16px 24px;
  min-width: 1100px;
}

</style>
