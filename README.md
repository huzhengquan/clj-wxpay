# clj-wxpay

clojure版本的微信支付sdk。基于官方sdk做的包装，通过一个函数可灵活地使用官方文档涉及到的API。

## 安装

增加 `[huzhengquan/clj-wxpay "0.1.0"]` 到 `project.clj` 的 `dependencies` 之中。

## 使用方法

```clojure
(require '[clj-wxpay.core :as wxpay])
```

### 参数

```clojure
(wxpay/request cmd params & config)
```
- cmd 对应官方文档API列表中的*接口链接*后半部分，如`pay/unifiedorder`、`secapi/pay/refund`等
- params 为请求参数，参考官方文档中的*请求参数*部分
- config 为配置项，包括`api`、`cert`、`appid`、`mch_id`、`key`、`connectTimeoutMs`、`readTimeoutMs`。

### 安全项

`appid`、`mch_id`、`key`是必有项，可在请求API时携带，也可通过系统属性来配置（System/setProperty）。

#### 内部配置系统属性

```clojure
(System/setProperty "clj.wxpay.appid" "xxx")
(System/setProperty "clj.wxpay.mch_id" "xxx")
(System/setProperty "clj.wxpay.key" "xxx")
```

#### JVM 命令行配置

```shell
java -jar -Dclj.wxpay.appid=xxx -Dclj.wxpay.mch_id=xxx -Dclj.wxpay.key=xxx app.jar
```

### API使用示例

```clojure
(require '[clj-wxpay.core :as wxpay])

; 查询订单（需要配置系统属性）
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"})

; 在仿真系统中测试（需要配置系统属性）
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :api "https://api.mch.weixin.qq.com/sandboxnew/")

; 使用指定超时配置（需要配置系统属性）
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :connectTimeoutMs 1000 :readTimeoutMs 1000)

; 在请求API时指定appid和mch_id、key
(wxpay/request "pay/orderquery" {"transaction_id" "xxx"} :appid "xxx" :mch_id "xxx" :key "xxx")
```
