<template>
  <div>
    <el-dialog :close-on-click-modal="false" z-index="1000" :width="nextVisible ? '50%' : '40%'"
               :visible.sync="resVisible" :class="nextVisible ? 'api-import-next' : 'api-import'" destroy-on-close @close="closeDialog">
      <div class="card" v-if="!nextVisible">
        <img src="/assets/guide/visual-collaboration.png" class="image" alt="MS">
        <div class="content">
          <p class="title" ><img src="../../../assets/guide/hard.png" alt="MS">{{ $t("guide.home.title") }}</p>
          <div class="bottom clearfix">
            <p class="desc">{{ $t("guide.home.desc") }}</p>
            <el-button type="primary" round size="small" class="button" @click="openNext">
              {{ $t("guide.home.button") }}
            </el-button>
          </div>
        </div>
      </div>
      <div class="card-next" v-else>
        <el-carousel trigger="click" :autoplay="false" :loop="false" indicator-position="outside" arrow="never"
                     height="350px" ref="carousel" @change="toggleCarousel">
          <el-carousel-item v-for="item in carouseData" :key="item.id" >
            <img :src="'/assets/guide/' + item.url" class="image-next" alt="MS">
          </el-carousel-item>
        </el-carousel>
        <template v-for="item in carouseData">
          <div class="bottom clearfix" :key="item.id" v-if="item.id === carouselId">
            <p class="title-next">{{ $t(item.title) }}</p>
            <p class="desc-next" v-html="$t(item.desc)" />
            <el-row>
              <el-col v-if="carouselId === 3" :span="24">
                <el-button type="primary" round size="small" class="button" @click="gotoTurn()">
                  {{ $t(item.button) }}
                </el-button>
              </el-col>
              <template v-else>
                <el-col :span="8">
                  <br v-if="carouselId === 0">
                  <el-button type="primary" v-else round size="small" class="is-plain"
                             @click="toggleCarousel(carouselId - 1)">
                    {{ $t("guide.go_prev") }}
                  </el-button>
                </el-col>
                <el-col :span="8">
                  <br>
                </el-col>
                <el-col :span="8">
                  <el-button type="primary" round size="small" class="button" @click="toggleCarousel(carouselId + 1)">
                    {{ $t(item.button) }}
                  </el-button>
                </el-col>
              </template>
            </el-row>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {hasPermissions} from "../../../utils/permission";

export default {
  name: "MsIntroduction",
  data() {
    return {
      carouselId: 0,
      resVisible: false,
      nextVisible: false,
      carouseData: [
        {
          url: 'track.png',
          title: 'guide.test.title',
          desc: 'guide.test.desc',
          button: 'guide.test.button',
          id: 0
        },
        {
          url: 'api.png',
          title: 'guide.api.title',
          desc: 'guide.api.desc',
          button: 'guide.api.button',
          id: 1
        },
        {
          url: 'ui.png',
          title: 'guide.ui.title',
          desc: 'guide.ui.desc',
          button: 'guide.ui.button',
          id: 2
        },
        {
          url: 'performance.png',
          title: 'guide.performance.title',
          desc: 'guide.performance.desc',
          button: 'guide.performance.button',
          id: 3
        }
      ]
    }
  },
  created() {
  },
  methods: {
    openNext() {
      this.carouselId = 0
      this.resVisible = true;
      this.nextVisible = true;
    },
    closeDialog() {
      localStorage.setItem("introduction", 'false');
      this.resVisible = false;
      this.nextVisible = false;
    },
    toggleCarousel(index){
      this.carouselId = index
      this.$refs.carousel.setActiveItem(index);
    },
    gotoTurn(){
      this.closeDialog()
      if(localStorage.getItem("noviceStatus") && localStorage.getItem("noviceStatus") === "1"  && hasPermissions("PROJECT_TRACK_CASE:READ+CREATE")){
        if(this.$route.path.includes("track")){
          this.$emit("skipOpen", "/track/case/all")
          this.$router.push("/track/case/all")
        }else{
          this.$router.push({
            path: '/track/case/all',
            query: { status: true },
          })
        }
      }else{
        this.$router.push("/track/case/all")
      }
    }
  }
}
</script>


<style scoped>
::v-deep .api-import .el-dialog {
  border-radius: 8px;
  background-image: linear-gradient(to bottom, #f4f4f4 45%, #FFF 0);
}

::v-deep .api-import .el-dialog__body {
  padding: 1px 5px 0 5px;
}
::v-deep .api-import-next .el-dialog__body {
  padding: 24px 5px 0 5px;
}

::v-deep .api-import-next .el-dialog {
  margin-top: 10vh !important;
  border-radius: 8px;
  padding-bottom: 1px !important;
  background-image: linear-gradient(to bottom, #783887 65%, #FFF 0);
}

::v-deep .api-import-next .el-dialog__headerbtn:focus .el-dialog__close {
  color: #fff !important;
}

::v-deep .api-import-next .el-dialog__headerbtn:hover .el-dialog__close {
  color: #fff !important;
}
.card {
  height: 350px;
  text-align: center;
}

.image {
  width: 217px;
  height: 140px;
}

.title {
  margin: 0;
  font-size: 220%;
  font-weight: 700;
}
.title img {
  width: 53px;
  height: 50px;
}

.desc {
  font-size: 18px;
  font-weight: 300;
  margin-bottom: 40px;
}
.card-next {
  height: 560px;
  text-align: center;
}
.image-next {
  width: 600px;
  height: 300px;
  padding-top: 5px;
  border-radius: 4px;
  border: 1px solid #fff;
  box-shadow: 0px 0px 10px hsl(0deg 0% 100%);
  background-clip: padding-box;
}

.title-next {
  margin: 0;
  font-size: x-large;
  font-weight: 700;
}

.desc-next {
  line-height: initial;
  font-size: 14px;
  font-weight: 300;
  padding: 0 15px;
  margin-bottom: 20px;
}


</style>
<style scoped>
::v-deep .el-carousel__indicator--horizontal .el-carousel__button {
  width: 10px;
  height: 10px;
  background: #8c8c8c;
  border: 1px solid #ffffff;
  border-radius: 50%;
  opacity: 0.5;
}
::v-deep .el-carousel__indicator--horizontal.is-active .el-carousel__button {
  width: 10px;
  height: 10px;
  background: #783887;
  border-radius: 50%;
  opacity: 1;

}

::v-deep .el-dialog__headerbtn {
  top: 10px;
  right: 10px;
}
</style>

