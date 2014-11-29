(ns clojawler-app.crawler
  	(:require [clj-http.client :as client])
  	(:require [net.cgrand.enlive-html :as html]))


(defn get-body
	[url]
	(try
		(html/html-snippet (:body (client/get url)))
		(catch Exception e e)))

(defn get-hrefs
	 [content]
    (map #(:href (:attrs %1)) (html/select content #{[:a]})))

(defn parse-page
	[p-node, url, depth]
	())


