// ********************************************************************************
// window.DTFrameLogin method definition
// ********************************************************************************
window.DTFrameLogin = function(frameParams, loginParams, successCbk, errorCbk) {
    // frameParams: DOM container parameters
    // loginParams: unified login parameters
    // successCbk: callback function on login success
    // errorCbk: optional callback function on login failure
};
window.QRLogin= function(qrLogin) {

}

// ********************************************************************************
// DOM container parameters
// ********************************************************************************
// Note: The width and height parameters only set the QR code iframe element's size and do not affect the container size.
// The container's size and style must be set using CSS by the integrator.
const IDTLoginFrameParams = {
    id: '',       // Required, container element ID, without '#'
    width: 300,   // Optional, QR code iframe width, minimum 280, default 300
    height: 300   // Optional, QR code iframe height, minimum 280, default 300
};

// ********************************************************************************
// Unified login parameters
// ********************************************************************************
// The parameters are the same as those used for "splicing link to initiate login authorization" (some parameters are missing).
// Added the isPre parameter to set the running environment.
const IDTLoginLoginParams = {
    redirect_uri: '',     // Required, URL must be encoded
    response_type: 'code', // Required, fixed value 'code'
    client_id: '',        // Required
    scope: '',            // Required, if 'openid+corpid', org_type and corpId are required
    prompt: 'consent',   // Required, fixed value 'consent'
    state: '',           // Optional
    org_type: '',        // Optional, required if scope is 'openid+corpid'
    corpId: '',          // Optional, required if scope is 'openid+corpid'
    exclusiveLogin: '',  // Optional, if true, generates a QR code for exclusive organization use only
    exclusiveCorpId: ''  // Optional, required if exclusiveLogin is true, specifies exclusive organization corpId
};

// ********************************************************************************
// Login success result
// ********************************************************************************
const IDTLoginSuccess = {
    redirectUrl: '',   // Redirect URL after successful login, can be used directly for redirection
    authCode: '',      // authCode obtained after successful login, can be used directly for authentication
    state: ''          // Optional, state obtained after successful login
};
