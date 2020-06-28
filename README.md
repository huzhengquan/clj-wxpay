# clj-wxpay

clojure版本的微信支付sdk。基于官方sdk做的包装，通过一个函数可灵活地使用官方文档涉及到的API。

## 安装

增加 `[huzhengquan/clj-wxpay "0.1.4"]` 到 `project.clj` 的 `dependencies` 之中。

## 使用方法

```clojure
(require '[clj-wxpay.core :as wxpay])
```

### 参数

```clojure
(wxpay/request cmd params & config)
```
- cmd 对应官方文档API列表中的*接口链接*后半部分，如`"pay/unifiedorder"`、`"secapi/pay/refund"`等
- params 为请求参数，参考官方文档中的*请求参数*部分
- config 为配置项，包括`api`、`cert`、`appid`、`mch_id`、`key`、`connectTimeoutMs`、`readTimeoutMs`、`sign_type`、`sandbox?`。

### 安全项

`appid`、`mch_id`、`key`是必有项，可在请求API时携带，也可通过系统属性来配置（System/setProperty）。

官方文档中的`appid`、`mch_id`、`key`、`nonce_str`、`sign`、`sign_type`参数可在第2个参数params内容中忽略。

#### 内部配置系统属性

```clojure
(System/setProperty "clj.wxpay.appid" "xxx")
(System/setProperty "clj.wxpay.mch_id" "xxx")
(System/setProperty "clj.wxpay.key" "xxx")
(System/setProperty "clj.wxpay.sign_type" "HMAC-SHA256") ; 缺省时为:MD5
(System/setProperty "clj.wxpay.sandbox" "true") ; 缺省时为:MD5
```

#### JVM 命令行配置

```shell
java -jar -Dclj.wxpay.appid=xxx -Dclj.wxpay.mch_id=xxx -Dclj.wxpay.key=xxx app.jar
```

#### 证书

如`secapi/pay/refund`（申请退款）接口要求使用API证书。注意使用无需证书的api时不要携带:cert参数。

```clojure
(let [cert (clojure.java.io/input-stream (clojure.java.io/file "path" "apiclient_cert.p12"))]
  (wxpay/request "secapi/pay/refund"
    {"transaction_id" "xxx", "out_refund_no" "xxx", "total_fee" "100", "refund_fee" "100"}
    :cert cert))
```

### config参数

- `:appid` 微信小程序后台APP的唯一标识
- `:mch_id` 微信支付商户号
- `:key` API密钥
- `:cert` 证书内容（InputStream）,在使用不需要证书API时要忽略此项。
- `:sign_type` 签名类型，目前支持`"HMAC-SHA256"`和`"MD5"`，默认为`"MD5"`
- `:connectTimeoutMs` 连接超时时间，单位是毫秒，默认`10000`
- `:readTimeoutMs` 读数据超时时间，单位是毫秒，默认`20000`
- `sandbox?` 仿真系统,默认`false` 

### 响应

无异常情况下，接口响应类型为HashMap。数据结构可以参考官方的文档。

[微信支付官方开发文档](https://pay.weixin.qq.com/wiki/doc/api/index.html)

如查询对帐单失败时响应内容：

```clojure
{"return_msg" "No Bill Exist", "error_code" "20002", "return_code" "FAIL"}
; 或
{"return_msg" "ok", "error_code" "SUCCESS", "data": "xxx"}
```

### API使用示例

```clojure
(require '[clj-wxpay.core :as wxpay])

; 查询订单
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :appid "xxx" :mch_id "xxx" :key "xxx")

; 配置了JVM系统属性appid mch_id key后，可以在使用接口时忽略该项

; 忽略appid和mch_id、key
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"})

; 在仿真系统中测试
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :sandbox? true)

; 使用指定超时配置
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :connectTimeoutMs 1000 :readTimeoutMs 1000)

; 指定signType, 目前支持“MD5”和“HMAC-SHA256”，缺省时为“MD5”
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :sign_type "HMAC-SHA256")
```
