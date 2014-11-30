(ns clojawler-app.core
	(:gen-class)
	(:use clojawler-app.delimited-reader)
	(:use clojawler-app.crawler)
		(:require [net.cgrand.enlive-html :as html]))

(defn get-urls-to-start
	[file-path]
	(retrieve-lines file-path))

(defn -main
  	[& args]
  	(let [root (walk (retrieve-lines (first args)) 3)]
  		(shutdown-agents)
  		(walk-tree root)))