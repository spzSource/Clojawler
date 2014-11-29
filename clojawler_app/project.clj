(defproject clojawler_app ""
  	:description ""
  	:url ""
  	:license {
  		:name ""
        :url ""
    }
  	:dependencies [
  		[org.clojure/clojure "1.6.0"],
  		[clj-http "1.0.1"],
  		[enlive "1.1.5"]
  	]
  	:main ^:skip-aot clojawler-app.core
  	:target-path "target/%s"
  	:profiles {
  		:uberjar {
  			:aot :all
  		}
  	})
