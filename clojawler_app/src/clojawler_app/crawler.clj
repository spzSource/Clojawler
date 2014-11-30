(ns clojawler-app.crawler
  	(:require [clj-http.client :as client])
  	(:require [net.cgrand.enlive-html :as html])
  	(:require [com.climate.claypoole :as cpool])
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
	(new-node "*", nil, depth, 200, nil, (atom []), urls))


(defn get-content
	[url]
	(try
		(client/get url { 
			:max-redirects 1 
			:socket-timeout 5000 
			:conn-timeout 5000 })
	(catch Exception e { 
		:status 404,
		:body ""
		:headers "" })))


(defn remove-nils
	[hrefs]
	(filter #(not (nil? %)), hrefs))


(defn exclude-self-refs
	[hrefs, url]
	(filter #(boolean (not (.contains %1 url))) hrefs))


(defn validate-hrefs
	[hrefs]
	(filter #(boolean (.contains %1 "http")) hrefs))


(defn convert-to-base
	[hrefs]
	(distinct (remove-nils (map #(re-find #"http.*\.[a-zA-Z]+/", %), hrefs))))


(defn get-hrefs
	[body, url]
    (let [snippets (html/html-snippet body)
    	hrefs (convert-to-base (validate-hrefs (exclude-self-refs (remove-nils
    			(map #(:href (:attrs %1)) (html/select snippets #{ [:a] }))), url)))]
    	(if (nil? hrefs)
    		[]
    		hrefs)))


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
	(println "create-new-child")
	(new-node url, parent, depth, 
		(determine-status content), (get-redirect-info content), 
		(atom []), (get-hrefs (:body content) url)))


(defn process-page
	[p-node, url, depth]
	(let [content (get-content url)
		  new-child (create-new-child url, p-node, depth, content)]
		(println "process-page")
	 	(swap! (:childs p-node) conj new-child)
	 	new-child))

;;
;; Oh nooo!!1
;; DOCS: pmap is implemented using Clojure futures.  See examples for 'future'
;; for discussion of an undesirable 1-minute wait that can occur before
;; your standalone Clojure program exits if you do not use shutdown-agents.
;;
;; NOTE: claypool/pmap uses the thread pool for pmap execution.
;; github: https://github.com/TheClimateCorporation/claypoole
(defn process-node-clilds
	[p-node, childs-urls, depth]
	(println "process-node-clilds")
	(cpool/pmap (+ 2 (cpool/ncpus)) #(process-page p-node %1 depth) childs-urls))


(defn process-node
	[p-node, depth, urls]
	(println (:url p-node) depth)
	(let [current-depth (dec depth)]
		(if (> current-depth 0)
			(doseq [created-child (process-node-clilds p-node, urls, current-depth)]
				(process-node created-child, current-depth, (:urls-to-process created-child)))
			 p-node)))


(defn walk
	[start-urls, depth]
	(let [root-node (new-root start-urls, depth)]
		(process-node root-node, depth, start-urls)
		root-node))
