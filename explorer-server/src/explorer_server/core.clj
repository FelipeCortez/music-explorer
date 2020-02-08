(ns exporer-server.core
  (:require [clojure.java.shell :as sh]))

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
