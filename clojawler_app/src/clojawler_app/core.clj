(ns clojawler-app.core
	(:gen-class)
	(:use clojawler-app.delimited-reader)
	(:use clojawler-app.crawler))

(defn get-urls-to-start
	[file-path]
	(retrieve-lines file-path))

(defn -main
  	[& args]
  	(println (get-body "http://habrahabr1.ru")))
