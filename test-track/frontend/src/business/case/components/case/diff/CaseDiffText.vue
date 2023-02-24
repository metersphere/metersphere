<!--
    文本类型对比
 -->
<template>
  <div class="diff-box">
    <div v-for="(info, i) in diffInfo" :key="i">
      <div
        class="diff-container"
        v-for="(item, index) in info.diffArr"
        :key="index"
      >
        <div class="change-wrap">
          <div
            :class="getStatusClassByType(item.status)"
            v-if="item.status != 0 && item.status != 3"
          >
            {{ getStatusLabel(item.status) }}
          </div>
          <div class="content-wrap">
            <div
              :class="getStatusClassByType(item.status)"
              v-if="item.status == 3"
            >
              {{ getStatusLabel(item.status) }}
            </div>
            <div
              v-if="
                item.body.type === 'ARRAY' &&
                Array.isArray(item.body.content) &&
                item.body.content.length > 0
              "
              class="array-box"
            >
              <div
                :class="
                  checkDelete(item.status) ? ['array', 'array-delete'] : 'array'
                "
                v-for="(e, i) in item.body.content"
                :key="i"
              >
                {{ e }}
              </div>
            </div>
            <div
              v-else
              :class="
                checkDelete(item.status) ? ['text', 'line-through'] : 'text'
              "
            >
              {{
                item.body.content == "" ||
                item.body.content == null ||
                item.body.content == undefined ||
                (Array.isArray(item.body.content) &&
                  item.body.content.length == 0)
                  ? "暂无"
                  : item.body.content
              }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "CaseDiffText",
  props: {
    diffInfo: {
      type: Array,
      default() {
        return [
          {
            // 0-正常 1-新增 2-删除 3-格式变化
            diffArr: [
              {
                status: 0,
                body: {
                  type: "TEXT",
                  content: this.$t("case.none"),
                },
              },
            ],
            diffType: "TEXT",
          },
        ];
      },
    },
  },
  data() {
    return {
      statusMap: new Map([
        [0, ""],
        [1, "create-row"],
        [2, "delete-row"],
        [3, "format-change-row"],
      ]),
      statusConvertLabelMap: new Map([
        [1, this.$t("新建")],
        [2, this.$t("删除")],
        [3, this.$t("格式变化")],
      ]),
    };
  },
  methods: {
    getStatusClassByType(type) {
      let arr = [];
      //   arr.push("status-row");
      arr.push(this.statusMap.get(type));
      return arr;
    },
    getStatusLabel(type) {
      return this.statusConvertLabelMap.get(type);
    },
    checkDelete(type) {
      return type === 2;
    },
  },
};
</script>
<style scoped lang="scss">
.diff-box {
  .diff-container {
    .change-wrap {
      display: flex;
      margin-top: 8px;
      .status-row {
        width: 32px;
        height: 16px;
      }
    }

    .content-wrap {
      margin-left: 2px;
      color: #1F2329;
      .text {
      }
    }
  }
}

.array-box {
  display: flex;
  flex-wrap: wrap;
  width: 196px;

  .array {
    padding: 1px 6px;
    width: 82px;
    height: 24px;
    margin-left: 4px;
    margin-bottom: 5px;
    background: rgba(120, 56, 135, 0.2);
    border-radius: 2px;
    color: #783887;
    line-height: 22px;

    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .array-delete {
    padding: 1px 6px;
    width: 82px;
    height: 24px;
    margin-left: 4px;
    margin-bottom: 5px;
    background: rgba(120, 56, 135, 0.2);
    border-radius: 2px;
    color: #783887;
    line-height: 22px;

    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    opacity: 0.5;
  }
}
</style>
