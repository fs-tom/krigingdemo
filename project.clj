(defproject smiletest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [spork "0.2.1.3-SNAPSHOT"]
                 [org.clojars.haifengl/smile "2.5.3"]
                 [com.github.haifengl/smile-interpolation "2.5.3"]
                 #_
                 [com.github.haifengl/smile-plot "2.5.3"]
                 #_
                 [org.bytedeco/openblas-platform "0.3.21-1.5.8"]
                 [org.bytedeco/openblas "0.3.21-1.5.8"]
                 [org.bytedeco/openblas "0.3.21-1.5.8" :classifier "windows-x86_64"]
                 [org.bytedeco/openblas "0.3.21-1.5.8" :classifier "linux-x86_64"]
                 ;;specialized deps.
                 ;;
                 #_
                 [com.github.haifengl/smile-mkl "2.5.3"]]
  :jvm-opts ["-Djavacpp.platform=windows-x86_64" "-Dopenblas.platform=windows-x86_64"])
