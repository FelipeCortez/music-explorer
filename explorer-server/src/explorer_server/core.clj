(ns exporer-server.core
  (:require [clojure.java.shell :as sh]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [tick.alpha.api :as t]))

(def fields [:added :albumartist :album :country :path])

(defn fields->field-str [fields]
  (->> fields
       (map (fn [s] (str "$" (name s))))
       (clojure.string/join "\t")))

(fields->field-str fields)

(defn sh-result->m [sh-result]
  (->> sh-result
       clojure.string/split-lines
       (map (fn [s] (clojure.string/split s #"\t")))
       (map (fn [coll] (zipmap fields coll)))))

(sh-result->m (:out (sh/sh "beet" "ls" "-a" "added:2020" "-f" (fields->field-str fields))))

(def datasource (jdbc/get-datasource "jdbc:sqlite:/data/Music/lib.db"))

(into []
      (map (fn [row] (-> row
                         (update :albums/artpath #(String. %))
                         (update :albums/added (comp from-unix-time int)))))
      (jdbc/plan datasource ["SELECT albumartist, album, country, added, artpath FROM albums ORDER BY added DESC LIMIT 10;"]))

(defn ->unix-time [timestamp]
  (t/seconds (t/between (t/epoch) timestamp)))

(defn from-unix-time [secs]
  (t/>> (t/epoch) (t/new-duration secs :seconds)))

(from-unix-time last-added)

(->unix-time (t/now))
