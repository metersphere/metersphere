<template>
  <div>
    <div class="csat-popup" v-if="cardVisible">
      <template v-for="(item,index) in taskInfo" >
        <el-card :key="item.id" v-if="(index + 1) === taskIndex" class="box-card" >
          <div slot="header" class="clearfix">
            <span class="text-header">{{$t(item.title)}}</span>
            <el-button style="float: right; padding: 5px 0;" class="moon" type="text" @click="open()">
              <font-awesome-icon :icon="['fa', 'chevron-down']" class="icon"/>
            </el-button>
            <el-button style="float: right; padding: 3px 0;margin-right: 25px" type="text" >
              <img v-for="num in completeNum" src="../../../assets/guide/moon-dark.png"
                   class="moon" alt="MS" :key="num + 'd'">
              <img v-for="num in ongoingNum" src="../../../assets/guide/moon-light.png"
                   class="moon" alt="MS" :key="num + 'l'">
              <img v-for="num in incompleteNum" src="../../../assets/guide/moon.png"
                   class="moon" alt="MS" :key="num">
            </el-button>
            <el-progress :percentage="item.percentage" color="#783787" class="progress-card"></el-progress>
          </div>
          <div style="height: 220px">
            <template v-for="(val,i) in item.taskData">
              <div class="text item" v-permission="val.permission" :key="i">
                <p v-if="val.status === 1">
                    <font-awesome-icon :icon="['far', 'check-circle']" style="color:#783887" />
                  <label> {{$t(val.name)}}</label>
                </p>
                <p v-else @click="openGif(val)">
                  <font-awesome-icon :icon="['far', 'circle']" class="title-icon" />
                  <span>  {{$t(val.name)}}
                </span>
                </p>
              </div>
            </template>
          </div>
          <div class="footer">
            <el-button v-if="taskIndex < totalNum" style="float: right; margin-left: 10px; padding: 15px 0" type="text" @click="next()">
              {{$t('side_task.next')}}
            </el-button>
            <el-button v-if="taskIndex > 1" style="float: right;margin-left: 10px; padding: 15px 0" type="text" @click="prev()">
              {{$t('side_task.prev')}}
            </el-button>
            <el-button style="float: right; padding: 15px 0;color:#8C8C8C" type="text" @click="skip()">
              {{$t('side_task.skip')}}
            </el-button>
          </div>
        </el-card>
      </template>
    </div>
    <div class="csat-popup-gif" v-if="gifVisible">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span style="float: right; padding: 5px 0;" class="moon"  @click="closeGif()">
            <font-awesome-icon :icon="['fa', 'times']" class="icon"/>
          </span>
        </div>
        <div class="text" style="text-align: center;height: 210px">
          <el-image
            style="width: 340px;border-radius: 4px;"
            :src="gifData.url"
            :preview-src-list="[gifData.url]" lazy>
            <div slot="placeholder" class="image-slot">
              加载中<span class="dot">...</span>
            </div>
          </el-image>
        </div>
        <div class="gif-footer">
          <el-button type="primary" round size="small" class="is-plain" @click="gotoPath(gifData.path)">
            {{$t(gifData.name)}}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import {hasLicense} from "../../../utils/permission";
import {TASK_DATA, TASK_MODULE} from "../../../utils/constants";
import {getSideTask} from "../../../api/novice";

export default {
  name: "SiteTask",
  data() {
    return {
      cardVisible: false,
      gifVisible: false,
      gifData:'',
      taskIndex: 1,
      taskInfo: [],
      interfaceData: '',
      performanceData: '',
      projectData: '',
      uiData: '',
      completeNum: 0,
      ongoingNum: 0,
      incompleteNum: 0,
      totalNum:0,
      status: this.$route.query.status
    }
  },
  props: {
    taskData: Array
  },

  created() {
    console.log("this.status")
    console.log(this.status)
    if(this.status){
      this.skipOpen("/track/case/all")
    }
  },
  methods: {
    taskNum() {
      let totalNum = 0
      let completeNum = 0
      let ongoingNum = 0
      this.taskInfo = []
      // status -1 服务未启动 0服务启动，任务未开始  1完成 2进行中
      this.taskData.forEach(item=>{
        if(item.status === 1){
          completeNum++;
        }else if(item.status === 2){
          ongoingNum++;
        }
        if(item.status !== -1) {
          totalNum++
          this.taskInfo.push(item)
        }
      })
      let incompleteNum = totalNum - completeNum - ongoingNum
      this.completeNum = completeNum
      this.ongoingNum = ongoingNum
      this.incompleteNum = incompleteNum
      this.totalNum = totalNum
    },

    open() {
      this.taskIndex = 1
      this.taskNum()
      this.cardVisible = !this.cardVisible;
      this.$emit("closeBox", this.cardVisible)
      this.gifVisible = this.cardVisible ? this.gifVisible : this.cardVisible;
    },
    openGif(gif) {
      this.gifVisible = true
      this.gifData = gif
    },
    closeGif(){
      this.gifVisible = false
    },
    prev() {
      this.taskIndex = this.taskIndex - 1
      this.taskIndex = this.taskIndex < 1 ? 1 : this.taskIndex
    },
    next() {
      this.taskIndex = this.taskIndex + 1
      this.taskIndex = this.taskIndex > this.taskData.length ? this.taskData.length : this.taskIndex
    },
    skip() {
      this.open()
    },
    gotoPath(path){
      this.$router.push(path)
    },
    skipOpen(path){
      if(path){
        this.taskData.forEach(val=>{
          val.taskData.forEach(item=>{
            if(item.path === path){
              this.taskIndex = 1
              this.taskNum()
              this.cardVisible = true
              this.$emit("closeBox", this.cardVisible)
              this.openGif(item)
            }
          })
        })
      }
    }
  }
}
</script>

<style scoped>

.text {
  font-size: 16px;
  font-weight: 300;
}

.item {
  margin-bottom: 10px;
  margin-left: 35px;
}

.item p {
  margin-top: 0;
  margin-bottom: 10px;
}
.item p label {
  color:#783887;
  padding: 3px 6px;
}

.item p span {
  padding: 3px 6px;
}
.title-icon {
  color: #303133
}
.item p:hover {
  color:#783887;
  cursor:pointer;
}
.icon {
  color: #0a0a0a;
}
.icon:hover {
  color: #783787;
}
.item p:hover .title-icon {
  color:#783887;
}

.progress-card {
  margin-top: 10px;
  margin-right: 40px;
}

.footer {
  width: 95%;
  margin-bottom: 20px;
}

.gif-footer {
  width: 100%;
  margin: 10px 0 30px;
  text-align: center;
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
  width: 400px;
}

.csat-popup {
  position: fixed;
  right: 16px;
  bottom: 160px;
  width: 400px;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1000;
  -webkit-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #EBEEF5;
  background-color: #fff;
  -webkit-transition: .3s;
  transition: .3s;
}

.csat-popup-gif {
  position: fixed;
  right: 426px;
  bottom: 160px;
  width: 400px;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1000;
  -webkit-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #EBEEF5;
  background-color: #fff;
  -webkit-transition: .3s;
  transition: .3s;
}

.text-header {
  font-weight: 700;
  font-size: 24px;
}
.moon {
  width: 20px;
  height: 20px;
}

.circle {
  width: 12px;
  height: 12px;
  margin-right: 5px;
}

::v-deep .csat-popup .el-card__header {
  border-bottom: none;
  padding: 20px 24px 10px 24px;
}

::v-deep .csat-popup-gif .el-card__header {
  border-bottom: none;
  padding: 20px 20px 10px 24px;
}

::v-deep .el-progress__text{
  color: #783787 !important;
  float: left;
  margin-left: 0;
  margin-right: 10px
}


</style>
