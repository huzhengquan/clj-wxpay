(defproject huzhengquan/clj-wxpay "0.1.0"
  :description "FIXME: 微信支付SDK的clojure版本"
  :url "https://github.com/huzhengquan/clj-wxpay"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.github.wxpay/wxpay-sdk "0.0.3"]]
  :deploy-repositories [["releases" :clojars
                         :creds :gpg]]
  :aot :all
  :repl-options {:init-ns clj-wxpay.core})
