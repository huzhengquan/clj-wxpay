(ns clj-wxpay.core
  (:import [java.util HashMap Map]
           [com.github.wxpay.sdk WXPay WXPayConfig WXPayConstants$SignType]))

;(set! *warn-on-reflection* true)

(defn- wxpay-config
  [{:keys [appid mch_id key cert]
    :or {appid (System/getProperty "clj.wxpay.appid")
         mch_id (System/getProperty "clj.wxpay.mch_id")
         key (System/getProperty "clj.wxpay.key")}}]
  (reify
    WXPayConfig
    (getAppID [_] appid)
    (getMchID [_] mch_id)
    (getKey [_] key)
    (getCertStream [_] cert)
    (getHttpConnectTimeoutMs [_] 10000)
    (getHttpReadTimeoutMs [_] 20000)))

(defn request
  "cmd: 
   - pay/unifiedorder
   - pay/orderquery
   - secapi/pay/reverse
   - pay/closeorder
   - secapi/pay/refund
   - pay/refundquery
   - pay/downloadbill
   - payitil/report
   - tools/shorturl
   - tools/authcodetoopenid
   "
  [^String cmd params & {:keys [cert api connectTimeoutMs readTimeoutMs sign_type]
                         :or {api "https://api.mch.weixin.qq.com/"
                              connectTimeoutMs 10000
                              readTimeoutMs 20000 }
                         :as config}]
  (let [wxpay (new WXPay
                ^WXPayConfig (wxpay-config config)
                (case sign_type
                  "MD5" WXPayConstants$SignType/MD5
                  "HMAC-SHA256" WXPayConstants$SignType/HMACSHA256
                  WXPayConstants$SignType/MD5)
                false)
        reqData (.fillRequestData wxpay (new HashMap ^Map params))
        res (if cert 
              (. wxpay requestWithCert ^String (str api cmd) ^Map reqData ^int connectTimeoutMs ^int readTimeoutMs)
              (. wxpay requestWithoutCert ^String (str api cmd) ^Map reqData ^int connectTimeoutMs ^int readTimeoutMs))]
    (.processResponseXml wxpay res)))
