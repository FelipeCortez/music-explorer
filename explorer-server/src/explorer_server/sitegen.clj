(ns explorer-server.sitegen
  (:require [explorer-server.core :as data]
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

(comment
  (spit "/home/felipecortez/Dev/music-explorer/explorer-web/index.html"
        (document {:title       "Music Explorer"
                   :description "A music explorer"}
                  [:h1 "Music Explorer"])))
