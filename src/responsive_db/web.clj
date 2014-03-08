(ns responsive-db.web
  (:require [org.httpkit.server :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.io :as io]))

(defn websocket-handler [request]
  (with-channel request channel
    (on-close   channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (println data)
                          (send! channel data)))))

(defroutes all-routes
  (GET "/" [] (io/resource "public/index.html"))
  (context "/ws" []
           websocket-handler)
  (route/not-found "<h1>Page not found</h1>"))

(defn start []
  (run-server #'all-routes {:port 9090}))

(comment
  
  (def server (start))

  (server)
  
  )
