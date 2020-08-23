(ns explorer-server.lastfm
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [tick.alpha.api :as t]))

(defn from-unix-time [secs]
  (t/>> (t/epoch) (t/new-duration secs :seconds)))

(def api-key (slurp (io/resource "lastfm-key")))

(defn get-recent [page]
  (http/get (str "https://ws.audioscrobbler.com/2.0/"
                 "?api_key=" api-key
                 "&method=user.getrecenttracks"
                 "&user=FelipeSah"
                 "&period=1month"
                 "&limit=200"
                 "&format=json"
                 "&page=" page)))

(comment (def recent-1 (get-recent 1)))

(def parse-int #(Integer/parseInt %))

(defn tracks->flat [tracks]
  (let [{:strs [artist album name mbid date]} tracks]
    {:artist (get artist "#text")
     :album (get album "#text")
     :name name
     :mbid mbid
     :date (when date
             (from-unix-time (parse-int (get date "uts"))))}))

(defn vs->m
  "turn a seq of vectors into a map indexed by f (like group-by for groups of a single element)"
  [vs f]
  (into {} (map (fn [v] [(f v) v]) vs)))

(defonce results (atom {}))
(defonce raw-results (atom []))

(comment
  (reset! results {})
  (reset! raw-results [])
  (count @results)
  (take 50 @results)

  (take 50 @raw-results))

(def shy-merge #(merge %2 %1))

(defn parse-tracks [tracks]
  (-> (map tracks->flat tracks)
      (vs->m :date)
      (dissoc nil)))

(comment
  (count @raw-results)
  (count @results))

(comment
  (loop [page 864]
    (println "attempting " page)
    (let [{attrs "@attr", tracks "track"}
          (get (json/parse-string (:body (get-recent page))) "recenttracks")

          {page-api "page", total-pages "totalPages"} attrs]
      (println "got " page-api " out of " total-pages)
      (swap! results     shy-merge (parse-tracks tracks))
      (swap! raw-results conj      tracks)
      (if (= page-api total-pages)
        @results
        (do
          (Thread/sleep 2000)
          (recur (inc page)))))))
