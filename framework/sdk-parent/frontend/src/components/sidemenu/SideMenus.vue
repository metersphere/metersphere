<template>
  <div v-show="taskStatus && noviceStatus">
    <!--    侧边任务按钮-->
    <div :class="language === 'en-US' ? 'parentBox parentBox-en' : 'parentBox'" @click="toggle(1)">
      <div class="contentsBox">
        <div :style="openBox ? (language === 'en-US' ? 'right: 0;width:140px;cursor: auto;white-space: nowrap;' :
        'right: 0;width:100px;cursor: auto;white-space: nowrap;') : ''" >
          <font-awesome-icon class="icon global focusing" :icon="['fas', 'compass']" spin style="color: #ffffff;" />
          <span :style="openBox ? 'display: block;color: #fff;cursor: pointer;' : ''">{{$t('side_task.novice_task')}}</span>
        </div>
      </div>
    </div>
    <ms-site-task ref="siteTask"  @closeBox="closeBox" @closeNovice="closeNovice"/>
  </div>
</template>

<script>
import MsSiteTask from "../../components/sidemenu/components/SiteTask";
import {getSideTask, updateStatus} from "../../api/novice";
import {TASK_DATA} from "../../utils/constants";
import {hasLicense, hasPermissions} from "../../utils/permission";


export default {
  name: "MsSideTask",
  components: { MsSiteTask },

  data() {
    return {
      taskStatus: false,
      noviceStatus: false,
      openBox:false,
      totalTask: 0,
      taskData:[],
      language: localStorage.getItem('language'),
      status: this.$route.query.status
    };
  },
  created() {
    this.initTaskData(1)
  },
  methods: {
    initTaskData(status) {
      getSideTask().then(res => {
        if (res.data.length > 0 && res.data[0].dataOption) {
          this.taskData = JSON.parse(res.data[0].dataOption)
          this.noviceStatus = res.data[0].status === 1
        } else {
          this.taskData = TASK_DATA
        }
        if (status === 2 && res.data[0].status === 0) {
          updateStatus(1).then(res => {
            this.noviceStatus = true
            localStorage.setItem("noviceStatus", "1")
          })
        }
        let microApp = JSON.parse(sessionStorage.getItem("micro_apps"));
        let num = 0
        let total = 0
        this.taskData.forEach(item => {
          item.rate = 0
          let subRTask = 0
          item.taskData.forEach(res => {
            if(hasPermissions(...res.permission) ){
              subRTask += 1
              if(res.status === 1){
                item.rate += 1
              }
            }
          });

          if (!(microApp && microApp[item.name]) || (item.name === 'ui' && !hasLicense()) ||
            !hasPermissions(...item.permission)) {
            item.status = -1
            total++
          } else {
            item.percentage = Math.floor(item.rate / subRTask * 100)
            if (item.percentage === 100) {
              item.status = 1
            } else if (100 > item.percentage && item.percentage > 0) {
              item.status = 2
            }
            num += item.rate
          }
        })
        if (total < this.taskData.length) {
          this.taskStatus = true
        }
        this.totalTask = num
        this.$refs.siteTask.taskData = this.taskData

        if(this.openBox){
          this.$refs.siteTask.open();
        }

        if (this.status) {
          this.$refs.siteTask.skipOpen(this.taskData,"/track/case/all");
        }
      })
    },
    toggle(status){
      this.openBox = !this.openBox
      if(this.openBox || status === 2){
        this.initTaskData(status)
      }else{
        this.$refs.siteTask.open();
      }
    },
    closeBox(status){
      this.openBox = status
    },
    closeNovice(status){
      this.openBox = false
      this.noviceStatus = status
    },
    skipOpen(path){
      this.initTaskData(1)
      this.$refs.siteTask.skipOpen(null,path);
    }
  }

}
</script>

<style scoped>

.parentBox {
  height: 100%;
  background: gainsboro;
  overflow: hidden;
  overflow-y: auto;
  z-index: 1000;
  position: fixed;
}
.parentBox .contentsBox div {
  transition: all 1s;
  position: fixed;
  right: 16px;
  width: 27px;
  border-radius: 50px;
  background-color: #783787;
  color: #fff;
  padding: 3px;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.parentBox .contentsBox div span {
  display: none;
  color: #fff;
}
.parentBox .contentsBox div span:last-child {
  margin-left: 10px;
}
.parentBox-en .contentsBox div span:last-child {
  margin-left: 5px;
}
.parentBox .contentsBox div:nth-child(1) {
  bottom: 125px;
}
.parentBox .contentsBox div:hover {
  right: 0;
  height: 28px;
  width: 100px;
  cursor: auto;
}
.parentBox-en .contentsBox div:hover {
  right: 0;
  height: 28px;
  width: 140px;
  cursor: auto;
}
.box {
  width: 100px;
}
.box-en {
  width: 140px;
}

.parentBox .contentsBox div:hover span {
  display: block;
  color: #fff;
  white-space: nowrap;
  cursor: pointer;
}
.parentBox .contentsBox div:not(:last-child) {
  border-bottom: 1px solid #fff;
}

/*隐藏浏览器滚动条*/
::-webkit-scrollbar {
  display: none;
}

.icon {
  width: 2em !important;
  height: 2em;
}

</style>
