import {post} from "@/common/js/ajax";
import {success} from "@/common/js/message";

export function apiCaseBatchRun(condition) {
  return post('/api/testcase/batch/run', condition, () => {
    success("执行成功，请稍后刷新查看");
  });
}
