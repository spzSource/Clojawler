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
  	(let [file-path (first args) 
  		  depth (read-string (last args))
  		  start-urls (retrieve-lines file-path) 
  		  p-root (walk  start-urls, depth)]
  			(shutdown-agents)
  			(print-nodes p-root, 0)))