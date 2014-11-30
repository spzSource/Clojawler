(ns clojawler-app.crawler
  	(:require [clj-http.client :as client])
  	(:require [net.cgrand.enlive-html :as html])
  	(:import java.lang.String))


(defn new-node
	[url, parent, depth, status, redirect-info, childs, urls]
	{ :url url,
	  :parent parent,
	  :depth depth,
	  :status status,
	  :redirect-info redirect-info,
	  :childs childs
	  :urls-to-process urls})


(defn new-root
	[urls, depth]
	(new-node "*", nil, depth, nil, nil, (atom []), urls))


(defn get-content
	[url]
	(try
		(client/get url)
	(catch Exception e { :status 404,
						 :body ""
						 :headers "" })))


(defn exclude-self-refs
	[hrefs, url]
	(filter #(boolean (not (.contains %1 url))) hrefs))


(defn validate-hrefs
	[hrefs]
	(filter #(boolean (.contains %1 "http")) hrefs))


(defn get-hrefs
	[body, url]
    (let [snippets (html/html-snippet body)
    	hrefs (validate-hrefs (exclude-self-refs (map #(:href (:attrs %1)) (html/select snippets #{ [:a] })), url))]
    	hrefs))


(defn determine-status
	[content]
	(let [status (:status content)]
	 	(if (nil? status)
	 		404
	 		status)))


(defn get-redirect-info
	[content]
	(let [status (determine-status content)]
    	(if (boolean (some #(= status %) '(301 302)))
      		(:location (:headers content))
      		nil)))
      

(defn create-new-child
	[url, parent, depth, content]
	(new-node url, parent, depth, 
		(determine-status content), (get-redirect-info content), 
		(atom []), (get-hrefs (:body content) url)))


(defn parse-page
	[p-node, url, depth]
	(let [content (get-content url)
		  new-child (create-new-child url, p-node, depth, content)]
		(print url)
	 	(swap! (:childs p-node) conj new-child)
	 	new-child))


(defn process-node-clilds
	[p-node, childs-urls, depth]
	(pmap #(parse-page p-node %1 depth) childs-urls))


(defn process-node
	[p-node, depth, urls]
	(let [current-depth (dec depth)]
		(if (> 0)
			(doseq [created-child (process-node-clilds p-node, urls, current-depth)]
				(process-node created-child, current-depth, (:urls-to-process created-child)))
			p-node)))


(defn walk
	[start-urls, depth]
	(let [root-node (new-root start-urls, depth)]
		(process-node root-node, depth, start-urls)))