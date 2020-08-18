(ns explorer-server.core
  (:require [clojure.java.shell :as sh]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [tick.alpha.api :as t]
            [explorer-server.countries :refer [iso-code->country]]))

(def fields [:added :albumartist :album :country :path])

(defn fields->field-str [fields]
  (->> fields
       (map (fn [s] (str "$" (name s))))
       (clojure.string/join "\t")))

(defn sh-result->m [sh-result]
  (->> sh-result
       clojure.string/split-lines
       (map (fn [s] (clojure.string/split s #"\t")))
       (map (fn [coll] (zipmap fields coll)))))

(defn ->unix-time [timestamp]
  (t/seconds (t/between (t/epoch) timestamp)))

(defn from-unix-time [secs]
  (t/>> (t/epoch) (t/new-duration secs :seconds)))

(def datasource (jdbc/get-datasource "jdbc:sqlite:/data/Music/lib.db"))

(defn descriptors []
  (into {}
        (mapv (fn [row] [(:album_attributes/entity_id row) {(keyword "albums" (str "f-" (:album_attributes/key row)))
                                                            (:album_attributes/value row)}])
              (jdbc/execute! datasource ["SELECT id, entity_id, key, value FROM album_attributes LIMIT 200;"]))))

(get (descriptors) 2235)
;; => #:album_attributes{:id 1, :entity_id 2232, :key "descriptors", :value "soft, instrumental, calm"}

(defn last-added []
  (let [descriptors (descriptors)]
   (mapv (fn [row]
          (-> row
              (update :albums/artpath #(when % (String. % "UTF-8")))
              (assoc :albums/real-country (iso-code->country (:albums/country row)))
              (merge (get descriptors (:albums/id row)))
              (update :albums/added (comp from-unix-time int))))
        (jdbc/execute! datasource ["SELECT id, albumartist, albumartist_sort, album, year, month, day, original_year, original_month, original_day, albumtype, label, country, added, artpath FROM albums ORDER BY added DESC;"]))))

(def all-albums (last-added))

(filter (fn [{:albums/keys [albumartist]}]
          (re-seq #"Ronin" albumartist))
        all-albums)

(comment
  (sh-result->m (:out (sh/sh "beet" "ls" "-a" "added:2020" "-f" (fields->field-str fields))))

  (from-unix-time last-added)

  (->unix-time (t/now))

  (last-added)

  (require '[datascript.core :as d])

  (def conn (d/create-conn {:name {:db/unique :db.unique/identity}}))

  (d/transact! conn [{:name "Felipe"
                      :likes "playing the guitar"}
                     {:name "Toni"
                      :likes "talking"}])

  (d/q '[:find ?n ?l
         :where
         [?e :name ?n]
         [?e :likes ?l]]
       @conn)
  )
