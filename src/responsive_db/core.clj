(ns responsive-db.core
  (:require [datomic.api :refer [db q] :as d]
            [clojure.pprint :refer [pprint]]
            [responsive-db.web :refer [publish websockets]]))

(def schema
  [{:db/id                  #db/id[:db.part/db]
    :db/ident               :thing/name
    :db/valueType           :db.type/string
    :db/cardinality         :db.cardinality/one
    :db.install/_attribute  :db.part/db}])

(defn seed [uri]
  (let [res  (d/create-database uri)
        conn (d/connect uri)]
    @(d/transact conn schema)
    conn))

(defn create [conn thing]
  (let [id          (d/tempid :db.part/user)
        transaction @(d/transact conn [(merge {:db/id id} thing)])]
    (d/resolve-tempid (d/db conn) (:tempids transaction) id)))

(defn id-of-created [transaction]
  (first (vals (:tempids transaction))))

(def uri "datomic:free://localhost:4334/responsive-db")

(defn reader-repl []
  (let [conn  (d/connect uri)
        queue (d/tx-report-queue conn)]
    (loop [tx (.take queue)]
      (let [id    (id-of-created tx)
            e     (d/entity (:db-after tx) id)
            sub-e (select-keys e [:thing/name])]
        (publish websockets (str sub-e))
        (recur (.take queue))))))

(defn writer-repl []
  (let [conn (d/connect uri)]
    (create conn {:thing/name "Logan"})
    (create conn {:thing/name "Bob"})))

(comment
  
  (seed uri)

  (reader-repl)

  (writer-repl)
  
  )
