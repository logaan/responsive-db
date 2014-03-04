(ns responsive-db.core
  (:require [datomic.api :refer [db q] :as d]
            [clojure.pprint :refer [pprint]]))

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

(comment
 
  (def conn (seed "datomic:free://localhost:4334/responsive-db"))

  (def queue (d/tx-report-queue conn))

  (loop [tx (.take queue)]
    (let [id (id-of-created tx)
          e  (d/entity (:db-after tx) id)]
      (println (:thing/name e))
      (recur (.take queue))))

  (create conn {:thing/name "Logan"})

  (create conn {:thing/name "Bob"})

  )

