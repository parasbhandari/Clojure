(defproject my-first-clojure-app "0.1.0-SNAPSHOT"
  :description "LangSmith API Key Rotation Bot"
  :url "https://github.com/yourusername/langsmith-key-rotator"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
               [etaoin "1.0.40"]
               [clojure.java-time "1.4.2"]
               [org.clojure/tools.logging "1.2.4"]]
  :main ^:skip-aot my-first-clojure-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[ch.qos.logback/logback-classic "1.4.7"]]}})