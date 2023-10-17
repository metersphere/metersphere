/**
 * Generation packaging analysis
 * 生成打包分析
 */
import { isReportMode } from '../utils';
import visualizer from 'rollup-plugin-visualizer';

export default function configVisualizerPlugin() {
  if (isReportMode()) {
    return visualizer({
      filename: './node_modules/.cache/visualizer/stats.html',
      open: true,
      gzipSize: true,
      brotliSize: true,
    });
  }
  return [];
}
