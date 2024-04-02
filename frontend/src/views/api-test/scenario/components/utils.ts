import { RequestResult } from '@/models/apiTest/common';
import { ScenarioStepItem } from '@/models/apiTest/scenario';
import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

/**
 * 调试或执行结束后，调用本方法更新步骤的执行状态
 * @param steps 响应式的步骤列表
 */
export default function updateStepStatus(
  steps: ScenarioStepItem[],
  stepResponses: Record<string | number, RequestResult[]>
) {
  for (let i = 0; i < steps.length; i++) {
    const node = steps[i];
    if (
      [
        ScenarioStepType.LOOP_CONTROLLER,
        ScenarioStepType.IF_CONTROLLER,
        ScenarioStepType.ONCE_ONLY_CONTROLLER,
        ScenarioStepType.API_SCENARIO,
      ].includes(node.stepType)
    ) {
      if (!node.executeStatus) {
        // 没有执行状态，说明未参与执行，直接跳过
        break;
      }
      // 逻辑控制器和场景内部可以放入任意步骤，所以它的最终执行结果是根据内部步骤的执行结果来判断的
      let hasNotExecuted = false;
      let hasFailure = false;
      if (!node.children || node.children.length === 0) {
        // 逻辑控制器内无步骤，则直接是未执行
        node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
      } else {
        for (let j = 0; j < node.children.length; j++) {
          const childNode = node.children[j];
          updateStepStatus([childNode], stepResponses);
          if (
            childNode.executeStatus &&
            [ScenarioExecuteStatus.EXECUTING, ScenarioExecuteStatus.UN_EXECUTE].includes(childNode.executeStatus)
          ) {
            // 子节点未执行或正在执行，则逻辑控制器也是未执行
            hasNotExecuted = true;
          } else if (childNode.executeStatus === ScenarioExecuteStatus.FAILED) {
            // 子节点有一个失败，逻辑控制器就是失败
            hasFailure = true;
          }
        }
        // 递归完子节点后，判断当前逻辑控制器的状态
        if (hasFailure) {
          node.executeStatus = ScenarioExecuteStatus.FAILED;
        } else if (hasNotExecuted) {
          node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
        } else {
          node.executeStatus = ScenarioExecuteStatus.SUCCESS;
        }
      }
    } else if (node.stepType === ScenarioStepType.CONSTANT_TIMER) {
      // 等待时间直接设置为成功
      node.executeStatus = ScenarioExecuteStatus.SUCCESS;
    } else if (node.executeStatus === ScenarioExecuteStatus.EXECUTING) {
      // 非逻辑控制器直接更改本身状态
      if (stepResponses[node.uniqueId] && stepResponses[node.uniqueId].length > 0) {
        node.executeStatus = stepResponses[node.uniqueId].some((report) => !report.isSuccessful)
          ? ScenarioExecuteStatus.FAILED
          : ScenarioExecuteStatus.SUCCESS;
      } else {
        node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
      }
    }
  }
}
