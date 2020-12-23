(defproject huzhengquan/clj-wxpay "3.0.9.0"
  :description "FIXME: 微信支付SDK的clojure版本, 同步微信官方SDK"
  :url "https://github.com/huzhengquan/clj-wxpay"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.apache.httpcomponents/httpclient "4.5.13"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.slf4j/slf4j-simple "1.7.30"]]
  :java-source-paths ["srcj"]
  :deploy-repositories [["releases" :clojars
                         :creds :gpg]]
  :aot :all
  :repl-options {:init-ns clj-wxpay.core})
