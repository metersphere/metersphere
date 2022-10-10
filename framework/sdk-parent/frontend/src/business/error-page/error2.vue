<template>
  <div class="errPage-container-2">
    <div class="bullshit">
      <div class="bullshit__oops">OOPS!</div>
      <div class="bullshit__info">服务器崩溃了...</div>
      <div class="bullshit__info">Please check that the URL you entered is correct, or click the
        button below to return to the homepage.
      </div>
      <el-button round>返回首页</el-button>
      <span class="interval">{{ authTime }}s 后自动返回首页</span>
    </div>
    <div class="images">
      <img src="../../assets/404images/error2.png" width="400" alt="403">
    </div>
  </div>
</template>

<script>
export default {
  name: "error2",
  data() {
    return {
      authTime: 0 /*倒计时 计数器*/,
    };
  },
  created() {
    // 启动定时器
    // this.interval();
  },
  methods: {
    interval() {
      // 定义10秒
      this.authTime = 10;
      let authTimetimer = setInterval(() => {
        this.authTime--;
        if (this.authTime <= 0) {
          // 倒计时结束之后跳转到首页，也可以自行更改跳转到指定位置
          this.$router.push({path: "/"});
          clearInterval(authTimetimer);
        }
      }, 1000);
    },
  },
};
</script>

<style lang="scss" scoped>
.errPage-container-2 {
  width: 80%;
  margin: 70px auto 0;
  color: #666666;

  .interval {
    font-size: 12px;
    margin-left: 10px;
  }

  .images {
    display: inline-block;

    img {
      object-fit: cover;
    }
  }

  .bullshit {
    display: inline-block;
    width: 300px;
    padding: 30px 0;
    text-align: left;
    overflow: hidden;

    &__oops {
      font-size: 32px;
      font-weight: bold;
      line-height: 40px;
      color: #333333;
      opacity: 0;
      margin-bottom: 20px;
      animation-name: slideUp;
      animation-duration: 0.5s;
      animation-fill-mode: forwards;
    }

    &__headline {
      font-size: 20px;
      line-height: 24px;
      color: #222;
      font-weight: bold;
      opacity: 0;
      margin-bottom: 10px;
      animation-name: slideUp;
      animation-duration: 0.5s;
      animation-delay: 0.1s;
      animation-fill-mode: forwards;
    }

    &__info {
      font-size: 13px;
      line-height: 21px;
      color: grey;
      opacity: 0;
      margin-bottom: 30px;
      animation-name: slideUp;
      animation-duration: 0.5s;
      animation-delay: 0.2s;
      animation-fill-mode: forwards;
    }

    &__return-home {
      display: block;
      float: left;
      width: 110px;
      height: 36px;
      background: #1482f0;
      border-radius: 100px;
      text-align: center;
      color: #ffffff;
      opacity: 0;
      font-size: 14px;
      line-height: 36px;
      cursor: pointer;
      animation-name: slideUp;
      animation-duration: 0.5s;
      animation-delay: 0.3s;
      animation-fill-mode: forwards;
    }

    @keyframes slideUp {
      0% {
        transform: translateY(60px);
        opacity: 0;
      }
      100% {
        transform: translateY(0);
        opacity: 1;
      }
    }
  }
}
</style>
