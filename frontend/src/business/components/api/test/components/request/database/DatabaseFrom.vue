<template>
  <div class="database-from">
    <el-form :model="currentConfig" :rules="rules" label-width="150px" size="small" :disabled="isReadOnly" ref="databaseFrom">

      <el-form-item :label="$t('api_test.request.sql.dataSource')" prop="name">
        <el-input v-model="currentConfig.name" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.database_url')" prop="dbUrl">
        <el-input v-model="currentConfig.dbUrl" maxlength="500" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.database_driver')" prop="driver">
        <el-select v-model="currentConfig.driver" class="select-100" clearable>
          <el-option v-for="p in drivers" :key="p" :label="p" :value="p"/>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.username')" prop="username">
        <el-input v-model="currentConfig.username" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.password')" prop="password">
        <el-input v-model="currentConfig.password" maxlength="200" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.pool_max')" prop="poolMax">
        <el-input-number size="small" :disabled="isReadOnly" v-model="currentConfig.poolMax" :placeholder="$t('commons.please_select')" :max="1000*10000000" :min="0"/>
      </el-form-item>


      <el-form-item :label="$t('api_test.request.sql.timeout')" prop="timeout">
        <el-input-number size="small" :disabled="isReadOnly" v-model="currentConfig.timeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="small" class="addButton" @click="save">{{currentConfig.id ? $t('commons.save') : $t('commons.add')}}</el-button>
      </el-form-item>

    </el-form>

  </div>
</template>

<script>
    import {DatabaseConfig} from "../../../model/ScenarioModel";

    export default {
      name: "MsDatabaseFrom",
      components: {},
      props: {
        isReadOnly: {
          type: Boolean,
          default: false
        },
        config: {
          type: Object,
          default() {
            return new DatabaseConfig();
          }
        },
        callback: {
          type: Function
        },
      },
      watch: {
        config() {
          Object.assign(this.currentConfig, this.config);
        }
      },
      mounted() {
        Object.assign(this.currentConfig, this.config);
      },
      data() {
        return {
          drivers: DatabaseConfig.DRIVER_CLASS,
          currentConfig: new DatabaseConfig(),
          rules: {
            name: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
            ],
            driver: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
            ],
            password: [
              {max: 200, message: this.$t('commons.input_limit', [0, 200]), trigger: 'blur'}
            ],
            dbUrl: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'}
            ],
            username: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 200, message: this.$t('commons.input_limit', [0, 200]), trigger: 'blur'}
            ]
          }
        }
      },
      methods: {
        save() {
          this.$refs['databaseFrom'].validate((valid) => {
            if (valid) {
              if (this.callback) {
                this.callback(this.currentConfig);
              }
            } else {
              return false;
            }
          });
        }
      }
    }
</script>

<style scoped>

  .addButton {
    float: right;
  }

</style>
