(ns clojawler-app.core-test
  	(:require [clojure.test       :refer :all]
              [clojawler-app.core :refer :all])
  	(:use clojawler-app.crawler))


(deftest get-content-test-positive
	(testing "Unable to process get request."
		(is (= 200 (:status (get-content "http://habrahabr.ru"))))))

(deftest get-content-test-negative
	(testing "Missing status."
		(is (= 404 (:status (get-content "http://habrahabr.ruru1"))))))



(deftest determine-status-test-positive
	(testing "Unable to resolve a response status."
		(is (= 200 (determine-status (get-content "http://habrahabr.ru"))))))

(deftest determine-status-test-negative
	(testing "Unable to resolve a response status."
		(is (= 404 (determine-status (get-content "http://habrahabr.ruru1"))))))



(deftest get-redirect-info-test-positive
	(testing "Unable to get redirection info."
		(is (nil? (get-redirect-info (get-content "http://habrahabr.ru"))))))

(deftest get-redirect-info-test-negative
	(testing "Unable to get redirection info."
		(is (nil? (get-redirect-info (get-content "http://habrahabr.ruru1"))))))



(deftest parse-page-test-positive
	(testing "Unable to parse page."
		(let [new-child (parse-page 
				(new-node "http://habrahabr.ru", nil, 3, 200, nil, (atom[]), ["http://toster.ru/"]),
				"http://toster.ru/", 2)]
			(is (= 0 (compare (:url new-child), "http://toster.ru/")))
			(is (= 200 (:status new-child)))
			(is (= 2 (:depth new-child)))
			(is (nil? (:redirect-info new-child)))
			(is (= 0 (count @(:childs new-child))))
			(is (< 0 (count (:urls-to-process new-child)))))))
