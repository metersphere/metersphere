<template>
  <el-card :class="['view-card', 'view-card--' + type]" :shadow="shadow" v-bind="$attrs">
    <div slot="header" v-if="$slots.header || header">
      <slot name="header">
        <i :class="icon" v-if="icon"/>{{ header }}
      </slot>
    </div>
    <slot></slot>
  </el-card>
</template>

<script>
export default {
  name: "ViewCard",
  props: {
    icon: String,
    header: String,
    type: String,
    shadow: {
      type: String,
      default: "hover"
    },
    labelWidth: {
      type: String,
      default: "120px"
    }
  },
  provide() {
    return {
      itemLabelWidth: this.labelWidth
    }
  },
}
</script>

<style lang="scss">
@import "~../../styles/common/variables.scss";
@import "~../../styles/common/mixins.scss";

.view-card {
  border-radius: $--border-radius-base;
  border: none;

  .el-card__header {
    padding: 12px 20px;
    font-weight: 600;
    border: 1px solid $--border-color-base;
    border-top-left-radius: $--border-radius-base;
    border-top-right-radius: $--border-radius-base;

    i {
      margin-right: 5px;
    }
  }

  .el-card__body {
    border: 1px solid $--border-color-base;
    border-radius: $--border-radius-base;
  }

  .el-card__header + .el-card__body {
    border-top: none;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
  }
}

.view-card--primary {
  .el-card__header {
    @include variant($--color-white, $--color-primary, $--color-primary);
  }
}

.view-card--success {
  .el-card__header {
    @include variant($--color-white, $--color-success, $--color-success);
  }
}

.view-card--danger {
  .el-card__header {
    @include variant($--color-white, $--color-danger, $--color-danger);
  }
}

.view-card--warning {
  .el-card__header {
    @include variant($--color-white, $--color-warning, $--color-warning);
  }
}

.view-card--info {
  .el-card__header {
    @include variant($--color-white, $--color-info, $--color-info);
  }
}
</style>
