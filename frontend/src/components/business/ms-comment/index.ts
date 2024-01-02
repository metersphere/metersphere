import _Comment from './comment';
import type { App } from 'vue';

const MsComment = Object.assign(_Comment, {
  install: (app: App) => {
    app.component(_Comment.name, _Comment);
  },
});

export type CommentInstance = InstanceType<typeof _Comment>;

export { default as CommentInput } from './input.vue';
export default MsComment;
