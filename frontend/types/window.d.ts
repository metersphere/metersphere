// ********************************************************************************
// DOM包裹容器相关参数
// ********************************************************************************
// 注意！width与height参数只用于设置二维码iframe元素的尺寸，并不会影响包裹容器尺寸。
// 包裹容器的尺寸与样式需要接入方自己使用css设置
interface IDTLoginFrameParams {
  id: string; // 必传，包裹容器元素ID，不带'#'
  width?: number; // 选传，二维码iframe元素宽度，最小280，默认300
  height?: number; // 选传，二维码iframe元素高度，最小280，默认300
}

interface qrLogin {
  id: string;
  goto: string;
  width: string;
  height: string;
  style: string; // 可选的，二维码html标签的style属性
}

// ********************************************************************************
// 统一登录参数
// ********************************************************************************
// 参数意义与“拼接链接发起登录授权”的接入方式完全相同（缺少部分参数）
// 增加了isPre参数来设定运行环境
interface IDTLoginLoginParams {
  redirect_uri: string; // 必传，注意url需要encode
  response_type: string; // 必传，值固定为code
  client_id: string; // 必传
  scope: string; // 必传，如果值为openid+corpid，则下面的org_type和corpId参数必传，否则无法成功登录
  prompt: string; // 必传，值为consent。
  state?: string; // 选传
  org_type?: string; // 选传，当scope值为openid+corpid时必传
  corpId?: string; // 选传，当scope值为openid+corpid时必传
  exclusiveLogin?: string; // 选传，如需生成专属组织专用二维码时，可指定为true，可以限制非组织帐号的扫码
  exclusiveCorpId?: string; // 选传，当exclusiveLogin为true时必传，指定专属组织的corpId
}

// ********************************************************************************
// 登录成功后返回的登录结果
// ********************************************************************************
interface IDTLoginSuccess {
  redirectUrl: string; // 登录成功后的重定向地址，接入方可以直接使用该地址进行重定向
  authCode: string; // 登录成功后获取到的authCode，接入方可直接进行认证，无需跳转页面
  state?: string; // 登录成功后获取到的state
}
declare interface Window {
  kity: any;
  angular: any;
  HotBox: any;
  kityminder: Record<string, any>;
  minderProps: Record<string, any>;
  editor: Record<string, any>;
  minder: Record<string, any>;
  minderEditor: Record<string, any>;
  km: Record<string, any>;
  canvg: (canvas: HTMLCanvasElement, xml: string, option: Record<string, any>) => void;
  minderHistory: {
    reset: () => void;
    undo: () => void;
    redo: () => void;
    hasUndo: () => boolean;
    hasRedo: () => boolean;
  };
  DTFrameLogin: (
    frameParams: IDTLoginFrameParams, // DOM包裹容器相关参数
    loginParams: IDTLoginLoginParams, // 统一登录参数
    successCbk: (result: IDTLoginSuccess) => void, // 登录成功后的回调函数
    errorCbk?: (errorMsg: string) => void // 登录失败后的回调函数
  ) => void;
  QRLogin: (QRLogin: qrLogin) => Record<any, any>;
}
