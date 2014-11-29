(ns clojawler-app.core
	(:gen-class)
	(:use clojawler-app.delimited-reader)
	(:use clojawler-app.crawler))

(defn get-urls-to-start
	[file-path]
	(retrieve-lines file-path))

(defn -main
  	[& args]
  	(doseq [elem (get-hrefs(get-body "http://habrahabr.ru"))]
  		(println elem)))
