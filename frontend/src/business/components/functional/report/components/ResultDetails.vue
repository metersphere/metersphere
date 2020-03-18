<template>
  <div>
    <video id="video" controls="" autoplay="" class="embed-responsive-item">
      <source id="video-source" :src="videoUrl" type="video/mp4">
    </video>
  </div>
</template>

<script>
  export default {
    name: "ResultDetails",
    props: {
      videoUrl: {
        type: String
      }
    }
  }
</script>

<style scoped>

  #video {
    height: 50%;
    width: 50%;
  }

</style>
