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

(defn year-to-pixel [first-year last-year
                     first-coord last-coord
                     year]
  (let [normalized-last (- last-coord first-coord)]
    normalized-last))

(year-to-pixel 1945 2020 16 800 1955)

(decades 1945 2020)

(defn timeline []
  (let [initial-year 1945 ;; https://en.wikipedia.org/wiki/History_of_sound_recording#The_Magnetic_Era_(1945_to_1975)
        end-year     (t/int (t/year (t/now)))]
    [:svg {:viewBox "0 0 840 360"
           :width   "840px"
           :height  "360px"
           :xlmns   "http://www.w3.org/2000/svg"}
     [:line {:x1     16.5 :x2 16.5
             :y1     0    :y2 300
             :stroke "black"}]
     [:text {:transform "translate(16, 320) rotate(45)"}
      initial-year]
     [:line {:x1     800.5 :x2 800.5
             :y1     0    :y2 300
             :stroke "black"}]
     [:text {:transform "translate(800, 320) rotate(45)"}
      end-year]]))

(let [albums-hiccup (for [album (data/last-added)]
                      [:li [:div
                            [:img {:src (str "http://127.0.0.1:8002" (:albums/artpath album))
                                   :width "150px"}]
                            [:h1 (str (:albums/albumartist album)
                                      " · "
                                      (:albums/album album))]
                            [:h2 (:albums/original_year album)]
                            [:hr]]])]

  (spit "/home/felipecortez/Dev/music-explorer/explorer-web/index.html"
        (document {:title       "Music Explorer"
                   :description "A music explorer"}
                  (list
                   (timeline)
                   [:ul albums-hiccup]))))
