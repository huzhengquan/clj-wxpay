(ns 
  ^{:author "Hu Zhengquan <huzhengquan@gmail.com>"
    :doc    "微信支付SDK的CLOJURE版本"}
  clj-wxpay.core
  (:import [java.util HashMap Map]
           [com.github.wxpay.sdk WXPay WXPayConfig WXPayConstants$SignType WXPayUtil])
  (:refer-clojure :exclude [key]))

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
  [^String cmd params & {:keys [cert api sign_type sandbox? connectTimeoutMs readTimeoutMs ]
                         :or {sandbox? (= (System/getProperty "clj.wxpay.sandbox") "true") 
                              connectTimeoutMs 10000
                              readTimeoutMs 20000 }
                         :as opts}]
  (let [api (or api 
                (and sandbox? "https://api.mch.weixin.qq.com/sandboxnew/")
                "https://api.mch.weixin.qq.com/")
        config (merge
                 opts
                 (when (and sandbox? (not= cmd "pay/getsignkey"))
                   (let [{:strs [return_code return_msg sandbox_signkey]} (apply request "pay/getsignkey" {} (apply concat opts))]
                     (when (or (not= return_code "SUCCESS") (empty? sandbox_signkey))
                       (throw (Exception. (str return_code ":" return_msg))))
                     {:key sandbox_signkey})))
        wxpay (new WXPay
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
    (dissoc (into {}
                  (cond
                    (and sandbox? (= cmd "pay/getsignkey")) (WXPayUtil/xmlToMap res)
                    (and (= cmd "pay/downloadbill") (clojure.string/starts-with? res "<")) (WXPayUtil/xmlToMap res)
                    (= cmd "pay/downloadbill") {"return_code" "SUCCESS", "return_msg" "ok", "data" res}
                    (= cmd "payitil/report") (WXPayUtil/xmlToMap res)
                    :else (.processResponseXml wxpay res)))
            "nonce_str" "sign")))
