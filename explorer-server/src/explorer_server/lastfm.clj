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
                 "&limit=100"
                 "&format=json"
                 "&page=" page)))

(comment (def recent-1 (get-recent 1)))

(def parse-int #(Integer/parseInt %))

(defn tracks->flat [tracks]
  (let [{:strs [artist album name mbid date]} tracks]
    [(get artist "#text")
     (get album "#text")
     name
     mbid
     (when date
       (from-unix-time (parse-int (get date "uts"))))]))

(let [{attrs "@attr", tracks "track"}
      (get (json/parse-string (:body recent-1))
           "recenttracks")

      {page "page", total-pages "totalPages"}
      attrs]
  [(Integer/parseInt page) (Integer/parseInt total-pages)
   (map tracks->flat tracks)])
