(ns 
  ^{:author "Hu Zhengquan <huzhengquan@gmail.com>"
    :doc    "微信支付SDK的CLOJURE版本"}
  clj-wxpay.core
  (:import [java.util HashMap Map]
           [com.github.wxpay.sdk WXPay WXPayConfig WXPayConstants 
               WXPayConstants$SignType WXPayUtil IWXPayDomain WXPayRequest]
           [org.slf4j Logger LoggerFactory]
           cljwxpay.AppWxpayConfig
           )
  (:refer-clojure :exclude [key]))

(set! *warn-on-reflection* true)
(def ^Logger logger (LoggerFactory/getLogger "clj-wxpay"))

(def wxpay-config
  (let [appid (System/getProperty "clj.wxpay.appid")
        mch_id (System/getProperty "clj.wxpay.mch_id")
        key (System/getProperty "clj.wxpay.key")
        cert_path (System/getProperty "clj.wxpay.cert_path")
        httpConnectTimeoutMs (System/getProperty "clj.wxpay.http_connect_timeout_ms")
        httpReadTimeoutMs (System/getProperty "clj.wxpay.http_read_timeout_ms")]
    (when (not cert_path)
      (.warn logger "cert_path not found"))
    (if (and appid mch_id key)
      (let [wxconfig (new cljwxpay.AppWxpayConfig appid mch_id key)]
        (when cert_path (.setCert wxconfig cert_path))
        (when httpConnectTimeoutMs (.setHttpConnectTimeoutMs wxconfig (Integer/parseInt httpConnectTimeoutMs)))
        (when httpReadTimeoutMs (.setHttpReadTimeoutMs wxconfig (Integer/parseInt httpReadTimeoutMs)))
        wxconfig)
      (.warn logger "appid\\mch_id\\key is empty"))))


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
  [^String cmd params & {:keys [sandbox? connectTimeoutMs readTimeoutMs cert? report? signType]
                         :or {sandbox? (= (System/getProperty "clj.wxpay.sandbox") "true")
                              cert? false
                              report? false
                              signType "MD5"}
                         :as opts}]
  (when (not wxpay-config)
    (throw (Exception. "WXPayConfig Fail!")))
  (let [url-suffix (str (when sandbox? "/sandboxnew") "/" cmd)
        config (if (and sandbox? (not= cmd "pay/getsignkey"))
                 (let [{:strs [return_code return_msg sandbox_signkey]} (apply request "pay/getsignkey" {} (apply concat opts))]
                   (if (or (not= return_code "SUCCESS") (empty? sandbox_signkey))
                     (throw (Exception. (str return_code ":" return_msg))))
                   (new cljwxpay.AppWxpayConfig
                        (.getAppID ^WXPayConfig wxpay-config)
                        (.getMchID ^WXPayConfig wxpay-config)
                        sandbox_signkey))
                 wxpay-config)
        wxpay (new WXPay
                ^WXPayConfig config
                report?
                sandbox?
                (case signType
                  "MD5" WXPayConstants$SignType/MD5
                  WXPayConstants$SignType/HMACSHA256))
        reqData (.fillRequestData ^WXPay wxpay (new HashMap ^Map params))
        connectTimeoutMs (or connectTimeoutMs (.getHttpConnectTimeoutMs ^WXPayConfig wxpay-config))
        readTimeoutMs (or readTimeoutMs (.getHttpReadTimeoutMs ^WXPayConfig wxpay-config))
        res (if cert? 
              (. wxpay requestWithCert ^String url-suffix ^Map reqData ^int connectTimeoutMs ^int readTimeoutMs)
              (. wxpay requestWithoutCert ^String url-suffix ^Map reqData ^int connectTimeoutMs ^int readTimeoutMs))]
    (dissoc (into {}
                  (cond
                    (and sandbox? (= cmd "pay/getsignkey")) (WXPayUtil/xmlToMap ^String res)
                    (and (= cmd "pay/downloadbill") (clojure.string/starts-with? res "<")) (WXPayUtil/xmlToMap ^String res)
                    (= cmd "pay/downloadbill") {"return_code" "SUCCESS", "return_msg" "ok", "data" res}
                    (= cmd "payitil/report") (WXPayUtil/xmlToMap ^String res)
                    :else (.processResponseXml ^WXPay wxpay ^String res)))
            "nonce_str" "sign")))
