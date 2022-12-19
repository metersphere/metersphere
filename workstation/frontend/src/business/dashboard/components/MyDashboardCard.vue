<template>
  <el-card  shadow="never"
           body-style="margin-top: 24px;padding: 0;" class="table-card">
    <el-row :gutter="10">
      <el-col :span="24">
        <span class="top-css">{{ title }}</span>
      </el-col>
    </el-row>
    <el-row>
      <div class="row-card">
        <el-card v-for="(option,index) in contentArray" :key="index"
                 body-style="padding-top: 16px; padding-left: 16px; padding-bottom: 16px;"
                 class="card-info" shadow="never"   @click.native="gotoDetail(option.name)">
          <div class="card-name">{{option.label}}</div>
          <div class="card-value">{{option.value}}</div>
        </el-card>
      </div>

    </el-row>
  </el-card>
</template>

<script>

import {getFollowTotalCount, getUpcomingTotalCount} from "@/api/workstation";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";

export default {
  name: "MyUpcomingCard",
  props: {
    cardType: {
      type: String,
      default() {
          return "upcoming"
        }
    }
  },
  data() {
    return {
      contentArray:[
        {
          name: 'track_case',
          value:0,
          label: this.$t('workstation.table_name.track_case'),
        },
        {
          name:"track_plan",
          value:0,
          label: this.$t('workstation.table_name.track_plan'),
        },
        {
          name:"track_review",
          value:0,
          label: this.$t('workstation.table_name.track_review'),
        },
        {
          name:"track_issue",
          value:0,
          label: this.$t('workstation.table_name.track_issue'),
        },
        {
          name:"api_definition",
          value:0,
          label: this.$t('workstation.table_name.api_definition'),
        },
        {
          name:"api_case",
          value:0,
          label: this.$t('workstation.table_name.api_case'),
        },
        {
          name:"api_automation",
          value:0,
          label: this.$t('workstation.table_name.api_automation'),
        },
        {
          name:"performance",
          value:0,
          label: this.$t('workstation.table_name.performance'),
        },
      ],
      title:"",
    };
  },
  methods:{
    gotoDetail(name){
      if (this.cardType === 'upcoming') {
        let upcoming =this.$router.resolve({
          path: "/workstation/upcoming",
          query: {name: name}
        });
        window.open(upcoming.href, '_blank');
      } else {
        let focus =this.$router.resolve({
          path: "/workstation/focus",
          query: {name: name}
        });
        window.open(focus.href, '_blank');
      }
    },
    getData(){
      if (this.cardType !== 'upcoming'){
        getFollowTotalCount(getCurrentWorkspaceId()).then(response => {
          let tableData = response.data
          if (tableData) {
            this.contentArray.forEach(m => {
              let countName = m.name;
              m.value = tableData[countName];
            });
          }
        });
      } else {
        getUpcomingTotalCount(getCurrentWorkspaceId()).then(response => {
          let tableData = response.data
          if (tableData) {
            this.contentArray.forEach(m => {
              let countName = m.name;
              m.value = tableData[countName];
            });
          }
        });
      }
    }
  },
  created() {
    if (this.cardType === 'upcoming') {
      this.title = this.$t('workstation.upcoming');
    } else {
      this.title = this.$t('workstation.focus');
    }
    this.getData();

  },

}
</script>

<style lang="scss" scoped>
.row-card{
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  flex-wrap: nowrap;
  margin-top: 24px;
  margin-left: 8px;
  margin-right: 24px
}
.card-info{
  background: #FFFFFF;
  border: 1px solid #DEE0E3;
  border-radius: 4px;
  height: 82px;
  margin-left: 16px;
  width: 100%;
  cursor: pointer;
  &:hover {
    background: #F5F6F7;
  }
}
.top-css {
  left: 24px;
  font-weight: 650;
  font-style: normal;
  font-size: 18px;
  align-self: flex-start;
  padding: 0px 0px 0px 0px;
  box-sizing: border-box;
  width: 100%;
  color: #1F2329;
  margin-left: 24px;
  line-height: 26px;
}

.card-name{
  color: #646A73;
  font-size: 14px;
  line-height: 22px;
  height: 22px;
  font-weight: 400
}
.card-value{
  color:  var(--primary_color);
  height: 28px;
  font-weight: 500;
  font-size: 20px;
  line-height: 28px;
  display: flex;
  align-items: center;
  letter-spacing: -0.01em;
}
.table-card{
  border: none;
  color: rgba(192, 196, 204, 0.98);
}
</style>
