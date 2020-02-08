(ns exporer-server.core
  (:require [clojure.java.shell :as sh]))

;; (sh/sh "beet" "ls" "-a" "added+" "'added:2020'" "-f" "'$added $albumartist - $album'")
(println (:out (sh/sh "beet" "ls" "-a" "mars")))
