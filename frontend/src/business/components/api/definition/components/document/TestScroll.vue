<template>
  <div>
    <div class="questionList-content-list">
    </div>
  </div>
</template>

<script>


export default {
  name: "TestScroll",
  components: {
  },
  data() {
    return {
      seeThis:0,
      tabs:['tab0','tab1','tab2'],
    }
  },
  props: {
    projectId: String,
    documentId: String,
    moduleIds: Array,
    pageHeaderHeight:Number,
  },
  activated() {
  },
  created () {
    window.addEventListener('scroll',this.handleScroll)
  },
  mounted() {
  },
  computed: {
  },
  watch: {
  },
  methods: {
    goAnchor(index) { // 也可以用scrollIntoView方法， 但由于这里头部设置了固定定位，所以用了这种方法
      // document.querySelector('#anchor'+index).scrollIntoView()
      this.seeThis = index; var anchor = this.$el.querySelector('#anchor'+index)
      document.body.scrollTop = anchor.offsetTop-100
      document.documentElement.scrollTop = anchor.offsetTop-100
    },
    handleScroll(){
      let scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop // 滚动条偏移量
      let offsetTop = document.querySelector('#boxFixed').offsetTop; // 要滚动到顶部吸附的元素的偏移量
      var anchorOffset0 = this.$el.querySelector('#anchor0').offsetTop-100
      var anchorOffset1 = this.$el.querySelector('#anchor1').offsetTop-100
      var anchorOffset2 = this.$el.querySelector('#anchor2').offsetTop-100
      if(scrollTop>anchorOffset0&&scrollTop<anchorOffset1){
        this.seeThis = 0
      }
      if(scrollTop>anchorOffset1&&scrollTop<anchorOffset2){
        this.seeThis = 1
      }
      if(scrollTop>anchorOffset2){
        this.seeThis = 2
      }
    },
  },
}
</script>

<style scoped>
</style>
