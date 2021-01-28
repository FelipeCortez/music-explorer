(ns explorer-server.sitegen
  (:require [explorer-server.core :as data]
            [tick.alpha.api :as t]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 doctype include-css include-js]]))

(defn metadata [page-description]
  (list
   [:meta {"charset" hiccup.util/*encoding*}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
   [:meta {:name    "description"
           :content page-description}]
   [:meta {:name    "author"
           :content "Felipe Cortez"}]))

(defn analytics []
  [:script "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create', 'UA-49103022-1', 'auto');ga('send', 'pageview');"])

(defn head [page-title page-description js css]
  (let [css-base ["https://cloud.typography.com/7599838/6204212/css/fonts.css"
                  "/reset.css"
                  "/base.css"]
        css (concat css-base css)]
    (list
     [:head
      [:title page-title]
      (metadata page-description)
      (apply include-js js)
      (apply include-css css)
      (analytics)])))

(defn body [content]
  [:body content])

(defn document [{:keys [title css js description]} content]
  (str (:html5 doctype)
       (html [:html {:lang "en"}
              (head title description js css)
              (body content)])))

(defn decades [first last]
  (loop [coll    []
         current first]
    (if (< current last)
      (recur (conj coll current)
             (+ current (- 10 (mod current 10))))
      (conj coll last))))

(defn remap [[from-1 to-1] [from-2 to-2] n]
  (let [range-1 (- to-1 from-1)
        range-2 (- to-2 from-2)
        percentage (/ (- n from-1) range-1)]
    (+ from-2 (* range-2 percentage))))

(def data-example
  [{:title "Sketches of Spain" :year 1960}
   {:title "Patagonian Rats" :year 2010}
   {:title "Carlos, Erasmo" :year 1971}
   {:title "Heaven or Las Vegas " :year 1990}])

(mapv (fn [{:keys [year] :as m}]
        (assoc m :x (float (remap [1945 2020] [16 800] year))))
      data-example)

(defn together [& xs] (into [] (sequence cat xs)))

(defn timeline []
  (let [initial-year 1945 ;; https://en.wikipedia.org/wiki/History_of_sound_recording#The_Magnetic_Era_(1945_to_1975)
        end-year     (t/int (t/year (t/now)))
        year-range   [initial-year end-year]]
    (together
     [:svg {:viewBox "0 0 840 360"
            :width   "840px"
            :height  "360px"
            :xlmns   "http://www.w3.org/2000/svg"}]
     (let [strong (set (decades initial-year end-year))]
       (reduce (fn [coll year]
                 (let [x    (remap year-range [16.5 800.5] year)
                       snap (+ 0.5 (int x))]
                   (if (strong year)
                     (conj coll
                           [:line {:x1     snap :x2 snap
                                   :y1     0    :y2 300
                                   :stroke "gray"}]
                           [:text {:transform (format "translate(%s, 320) rotate(45)"
                                                      (int x))}
                            year])
                     (conj coll
                           [:line {:x1     snap :x2 snap
                                   :y1     0    :y2 300
                                   :stroke "lightgray"}]))))
               []
               (range initial-year (inc end-year))))
     (reduce (fn [coll {:keys [title year]}]
               (let [x (remap year-range [16.5 800.5] year)]
                 (conj coll
                       [:circle {:fill "#DD0000" :cx x :cy 150 :r 5}]
                       [:text {:transform (format "translate(%s, 170) rotate(45)" x)}
                        title])))
             []
             data-example))))

(spit "/home/felipecortez/Dev/music-explorer/explorer-web/index.html"
        (document {:title       "Music Explorer"
                   :description "A music explorer"}
                  (timeline)))
