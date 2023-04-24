<template>
  <el-card shadow="never" body-style="margin-top: 24px;padding: 0;border:none;" class="table-card">
    <el-row :gutter="10" >
      <el-col :span="24">
        <span class="top-css">{{ $t('workstation.creation_issue') }}</span>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col v-if="showCount">
<!--        <div class="number">
          {{totalCount.toString()}}
        </div>-->
        <ms-border-pie-chart :pie-data="loadCharData" :autoresize="true" :text="totalCount.toString()" :text-title="$t('workstation.issues_count')"
                             :subtext="subtextStr" :radius="['70%', '96%']" :color="this.color" :link-text="linkText"
                             :height="255" :change-color="true"/>
      </el-col>
      <el-col v-else>
        <img style="height: 100px;width: 100px;padding-top: 10%;padding-left: 40%;"
             src="/assets/module/figma/icon_none.svg"/>
        <p class="right-other-css"  v-permission="['PROJECT_TRACK_ISSUE:READ']">{{ $t('workstation.creation_issue_tip') }}&nbsp;&nbsp;<span style="color: var(--primary_color)" @click="toCreatIssue()">{{$t('permission.project_track_issue.create') }}</span> </p>
      </el-col>
    </el-row>
  </el-card>
</template>
<script>

import MsBorderPieChart from "metersphere-frontend/src/components/MsBorderPieChart";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getIssuesCount, getIssuesWeekCount} from "@/api/issue";

export default {
  name: "MyFlawCard",
  components: {MsBorderPieChart},
  data() {
    return {
      showCount :false,
      condition: {
        combine: {
          creator: {
            operator: "current user",
            value: "current user",
          },
        },
        workspaceId: getCurrentWorkspaceId()
      },
      loadCharData: [],
      totalCount: 0,
      weekTotalCount: 0,
      subtextStr:"",
      color:['#AA4FBF', '#F290C8','#FAD355','#FFE668', '#14E1C6','#4CFDE0', '#4E83FD','#96BDFF', '#50CEFB', '#76F0FF','#935AF6','#DC9BFF','#FFA53D','#FFC75E','#F76964','#FF9E95','#F14BA9','#FF89E3','#C3DD40','#D9F457','#616AE5','#ACADFF','#62D256','#87F578'],
      textColor:'#263238',
      linkText:"/#/workstation/creation?name=track_issue"
    };
  },
  methods: {
    getIssuesCount() {
      this.result = getIssuesCount(this.condition).then(response => {
        let tableData = response.data;
        let countMap = new Map();
        let nameMap = this.getNameMap();
        tableData.forEach(item => {
          if (item.statusValue) {
            let statusName = nameMap.get(item.statusValue);
            statusName = statusName ? statusName : item.statusValue;
            let count = countMap.get(statusName);
            if (!count) {
              count = 0;
            }
            count += item.count;
            countMap.set(statusName, count);
          }
        });
        countMap.forEach((count, name) => {
          this.totalCount += count;
          this.loadCharData.push({
            name: name,
            value: count
          });
        });
        if (this.totalCount>0) {
          this.getWeekCreateCount();
        }
      });
    },
    getNameMap() {
      let nameMap = new Map();
      nameMap.set('待办', this.$t('test_track.issue.status_upcoming'));
      nameMap.set('reopened', this.$t('test_track.issue.status_upcoming'));
      nameMap.set('active', this.$t('test_track.issue.status_active'));
      nameMap.set('new', this.$t('commons.create'));
      nameMap.set('created', this.$t('commons.create'));
      nameMap.set('closed', this.$t('test_track.issue.status_closed'));
      nameMap.set('已关闭', this.$t('test_track.issue.status_closed'));
      nameMap.set('resolved', this.$t('test_track.issue.status_resolved'));
      nameMap.set('已解决', this.$t('test_track.issue.status_resolved'));
      nameMap.set('delete', this.$t('test_track.issue.status_delete'));
      nameMap.set('删除', this.$t('test_track.issue.status_delete'));
      nameMap.set('rejected', this.$t('test_track.issue.status_rejected'));
      nameMap.set('已拒绝', this.$t('test_track.issue.status_rejected'));
      nameMap.set('in_progress', this.$t('test_track.issue.status_in_progress'));
      nameMap.set('正在进行', this.$t('test_track.issue.status_in_progress'));
      nameMap.set('接受/处理', this.$t('test_track.issue.status_in_progress'));
      nameMap.set('suspended', this.$t('test_track.issue.status_suspended'));
      nameMap.set('verified', this.$t('test_track.issue.status_verified'));
      return nameMap;
    },
    toCreatIssue() {
      let issueData = this.$router.resolve({
        path: '/track/issue',
        query:{type:'create'}
      });
      window.open(issueData.href, '_blank');
    },
    getWeekCreateCount(){
      getIssuesWeekCount(getCurrentWorkspaceId()).then(response => {
        let tableData = response.data;
        if (tableData){
          this.weekTotalCount = tableData;
          this.subtextStr = "本周：+"+this.weekTotalCount+" >" ;
        }
        this.showCount = true;
      })
    },
    refresh() {
      this.getIssuesCount();
    }
  },
  created() {

    this.getIssuesCount();
  }
}
</script>
<style scoped>
.right-css {
  text-align: right;
  margin-top: 100px;

}

.right-two-css {
  font-weight: 650;
  color: #783987;
  font-size: 21px;
}

.right-one-css {
  font-weight: 700;
  font-size: 43px;
  color: #783987;
}

.top-css {
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

.right-other-css {
  color: #969393;
  cursor: pointer;
  padding-left: 34%;
}
.table-card{
  border: none;
  color: rgba(192, 196, 204, 0.98);
}
.number{
  font-weight: 500;
  font-size: 32px;
  line-height: 40px;
  color: #263238;
  display: flex;
  align-items: center;
  text-align: center;
  position: absolute;
  margin-top: 123px;
  z-index: 9999;
}
</style>
