<template>

  <div>
      <el-row>
        <el-col :span="5" :offset="6">
          <span>浏览器</span>
        </el-col>
      </el-row>

      <el-row >
        <el-col :span="20" :offset="2">
            <el-radio-group v-model="browser.value" class="browser-radio">
              <el-radio v-for="item in browser.options" :key="item.label" :label="item.label">
                <img :src="item.url"/>
              </el-radio>
            </el-radio-group>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="5" :offset="6">
          <span>资源池</span>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="18" :offset="1">
          <el-select v-model="resourcePool.value" filterable placeholder="请选择">
            <el-option
              v-for="item in resourcePool.options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-col>
      </el-row>

  </div>
</template>

<script>

    export default {
      name: "ApiTestRuntimeConfig",
      data() {
        return {
          resourcePool: {
            options: [
              {
                value: '选项1',
                label: '资源池1'
              },
              {
                value: '选项2',
                label: '资源池3'
              },
              {
                value: '选项3',
                label: '资源池3'
            }],
            value: ''
          },
          browser: {
            options: [{
                url: require('@/assets/browser/firefox.svg'),
                label: 'firefox',
              },
              {
                url: require('@/assets/browser/chrome.svg'),
                label: 'chrome',
              },
              {
                url: require('@/assets/browser/ie.svg'),
                label: 'ie',
              },
              {
                url: require('@/assets/browser/opera.svg'),
                label: 'opera',
              }
            ],
            value: 'firefox'
          }
        }
      },
      methods: {
        validConfig() {
          if (this.resourcePool.value == '') {
            this.$message.error(this.$t('api_test.select_resource_pool'));
            return false;
          }
          return true;
        },
        configurations() {
          return {
            resourcePool: this.resourcePool,
            browser: this.browser
          }
        },
        cancelAllEdit() {
          this.browser.value = 'firefox';
          this.resourcePool.value = '';
        }
      }
    }
</script>

<style scoped>

  .el-row {
    margin-top: 30px;
    margin-bottom: 30px;
  }

  span {
    font-size: 20px;
    font-weight: bold;
    color: dimgray;
  }

</style>
