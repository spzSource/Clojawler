(ns clojawler-app.crawler
  	(:require [clj-http.client :as client])
  	(:require [net.cgrand.enlive-html :as html]))


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
	(catch Exception e nil)))

(defn get-hrefs
	[content]
    (map #(:href (:attrs %1)) (html/select content #{ [:a] })))


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
      		(:location (:headers raw-content))
      		nil)))
      

(defn create-new-child
	[url, parent, depth, content]
	(new-node url, parent, depth, 
		(determine-status content), (get-redirect-info content), 
		(atom []), (get-hrefs (:body content))))


(defn parse-page
	[p-node, url, depth]
	(let [content (get-content url)
		new-child (create-new-child url, parent, depth, content)]
	 	(swap! (:childs p-node) conj new-clild))
		new-child)


(defn process-node-clilds
	[p-node, childs-urls, depth]
	(pmap #(parse-page p-node %1 depth) childs-urls))


(defn process-node
	[p-node, depth, urls]
	(let [current-depth (dec depth)]
		(if (> 0)
			(doseq [created-child (process-node-clilds urls, current-depth)]
				(process-node created-child, current-depth, (:urls-to-process created-child)))
			p-node)))


(defn walk
	[start-urls, depth]
	(let [root-node (new-root start-urls, depth)]
		(process-node root-node, depth, start-urls)))