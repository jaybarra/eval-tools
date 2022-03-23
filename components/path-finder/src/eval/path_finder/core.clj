(ns eval.path-finder.core
  (:require
   [clojure.set :as set]
   [clojure.math.numeric-tower :as math]))

(defn ant
  "Data representing an Ant"
  [opts]
  (let [this-ant {:pheremone (rand 10.0)
                  :distance-preference (rand 10.0)}]
    (merge this-ant
           (when-let [position (:position opts)]
             {:position position
              :visited [position]})
           (when-let [dist-pow (:distance-preference opts)]
             {:distance-preference dist-pow})
           (when-let [pheromone (:pheromone opts)]
             {:pheromone pheromone}))))

(defn next-pheromone-concentrations
  [concentration evaporation-rate]
  (* concentration (- 1 evaporation-rate)))

(defn ^:private dist-between
  "Distance between two points"
  [a b]
  (let [pos-a (:position a)
        pos-b (:position b)]
    (->> [pos-a pos-b]
         (apply map (comp #(Math/pow % 2.0) -))
         (reduce +)
         Math/sqrt)))

(def inverse (partial / 1))

(defn desirability
  "Desirability of a distance.
  A larger dst-pow results in a stronger prefernce to closer points."
  [dist dst-pow]
  (-> dist
      inverse
      (Math/pow dst-pow)))

(comment
  (desirability (dist-between {:position [0 0]}
                              {:position [1 1]})
                3.0))

(defn next-ant
  [state ant]
  (if-let [potentials (seq (set/difference (set (:points state)) (set (:visited ant))))]
    (let [next-position (->> potentials
                             (sort-by (fn [p]
                                        (let [dist (dist-between (:position ant) p)]
                                          (desirability dist (:distance-preference ant)))))
                             (take 3)
                             rand-nth)]
      (-> ant
          (update :visited conj next-position)
          (assoc :position next-position)))
     ant))

(defn ant-travel-dist
  [ant]
  (reduce + (map dist-between
                 (:visited ant)
                 (drop 1 (:visited ant)))))

(defn generate-path
  "Ant Colony Optimization strategy"
  [points]
  (let [state {:points points
               :colony (repeatedly 10 #(ant {:position (rand-nth points)}))}
        final-state (loop [n#points (count points)
                           state state]
                      (if (>= 0 n#points)
                        state
                        (recur (dec n#points)
                               (assoc state :colony (pmap (partial next-ant state) (:colony state))))))]
    (->> final-state
         :colony
         (sort-by ant-travel-dist)
         first
         :visited)))

(comment
  (generate-path [{:id "a" :position [0 0]}
                  {:id "b" :position [0 1]}
                  {:id "c" :position [0 2]}
                  {:id "d" :position [3 1]}])

  (def my-points (repeatedly 100 #(hash-map :position [(rand 100) (rand 100)])))
  (generate-path my-points)

  )
