(ns maths.pso
  (:require [clojure.math.numeric-tower :as math]
            [clojure.spec.alpha :as spec]
            [taoensso.timbre :as log]))

;; Specs =============================================================
(spec/def ::num-array-type (spec/+ number?))

(spec/def ::pos ::num-array-type)
(spec/def ::best-pos ::num-array-type)
(spec/def ::vel ::num-array-type)
(spec/def ::particle (spec/keys :req [::pos
                                      ::best-pos
                                      ::vel]))
(spec/def ::particles (spec/* ::particle))
(spec/def ::swarm (spec/keys :req [::particles
                                   ::best-pos]))

(spec/def ::omega (spec/and number? pos?))
(spec/def ::phi-p (spec/and number? pos?))
(spec/def ::phi-s (spec/and number? pos?))
(spec/def ::iterations (spec/and int? pos?))
(spec/def ::max-iterations (spec/and int? pos?))

(spec/def ::opts (spec/keys :req [::max-iterations
                                  ::omega
                                  ::phi-p
                                  ::phi-s]))
(spec/def ::state (spec/keys :req [::opts]
                             :opt [::swarm]))

;; Code ==============================================================
#_(defn compute-fitness
  "Glove Problem [[https://en.wikipedia.org/wiki/Glove_problem]]
  optimization

  G(M, N) = M + N − 2 if both M, N ≥ 2
  G(M, 1) = M
  G(1, N) = N
  G(1, 1) = 1"
  [pos]
  {:pre [(spec/valid? ::pos pos)
         (>= (count pos) 2)]}
  (let [[m n] pos
        m1? (= 1 m)
        n1? (= 1 n)]
    (cond
      (and m1? n1?) 1
      n1? m
      m1? n
      :else (- (+ m n) 2))))

(defn compute-fitness
  "10 is optimimum"
  [pos]
  {:pre [(spec/valid? ::pos pos)]}
  (* -1 (math/abs (- 10 (first pos)))))

(def m-compute-fitness (memoize compute-fitness))

(def sort-particles-by-pos (partial sort-by
                                    ::pos
                                    #(> (m-compute-fitness %1)
                                        (m-compute-fitness %2))))

(def sort-particles-by-best-pos (partial sort-by
                                         ::best-pos
                                         #(> (m-compute-fitness %1)
                                             (m-compute-fitness %2))))

(defn compute-velocity!
  "v_next = (+ (* omega v_dim)
               (* phi_self  r1 (- self_best  self_current) 
               (* phi_swarm r2 (- swarm_best self_current))"
  [opts vel pb sb cp]
  (let [{::keys [omega phi-p phi-s]} opts
        r1 (rand)
        r2 (rand)]
    (+ (* omega vel)
       (* phi-p r1 (- pb cp))
       (* phi-s r2 (- sb cp)))))

(defn next-particle-velocity!
  "Returns a velocity array following the PSO velocy computation."
  [state particle]
  (let [{::keys [pos vel best-pos]} particle
        opts (get state ::opts)
        swarm-best (get-in state [::swarm ::best-pos])
        n-vel (map (partial compute-velocity! opts)
                   vel best-pos swarm-best pos)]
    (assoc particle ::vel n-vel)))

(defn update-particle-pos
  "Return particle with the position updated based on its velocity."
  [particle]
  (let [{::keys [pos vel]} particle
        n-pos (map + pos vel)]
    (assoc particle ::pos n-pos)))

(defn update-particle-bkp
  "Compares the particles current position score to the previous best
  known score and updates the best-known-position."
  [particle]
  (if (> (m-compute-fitness (::pos particle))
         (m-compute-fitness (::best-pos particle)))
    (assoc particle ::best-pos (::pos particle))
    particle))

(defn next-particle
  "Returns a particles next state that is the aggregate of updated
  velocity, position, and best-known-position."
  [state particle]
  (let [next-vel (partial next-particle-velocity! state)]
    (->> particle
         next-vel
         update-particle-pos
         update-particle-bkp)))

;; Swarm Functions ===================================================
(defn generate-particle!
  [dims]
  (let [positions (map (fn [[mn mx]]
                          (rand-int (- mx mn)))
                       (seq dims))
        velocities (repeatedly (count dims) #(- (rand) 0.5))]
    {::pos positions
     ::best-pos positions
     ::vel velocities}))

(defn generate-swarm!
  [opts]
  (let [{::keys [size dims]} opts
        particles (sort-particles-by-pos
                    (repeatedly
                      size
                      (partial generate-particle! dims)))
        best-pos (::pos (first particles))]
    {::particles particles
     ::best-pos best-pos}))

(defn swarm->best-pos
  [swarm]
  {:pre [(spec/valid? ::swarm swarm)]}
  (-> swarm
      ::particles
      sort-particles-by-pos
      first
      ::pos))

(defn next-swarm-particles
  [state swarm]
  (let [{::keys [particles]} swarm
        next-p (mapv (partial next-particle state)
                     particles)]
    (assoc swarm ::particles next-p)))

(defn update-swarm-best
  [swarm]
  (let [{::keys [best-pos particles]} swarm
        current-best (-> particles
                         sort-particles-by-pos
                         first
                         ::pos)]
    (if (> (m-compute-fitness current-best)
           (m-compute-fitness best-pos))
      (assoc swarm ::best-pos current-best)
      swarm)))

(defn next-swarm
  [state swarm]
  (let [update-particles (partial next-swarm-particles state)]
    (->> swarm
         update-particles
         update-swarm-best)))

(defn next-state
  [state]
  (let [{::keys [swarm]} state
        n-swarm (next-swarm state swarm)]
    (assoc state ::swarm n-swarm)))

(defn print-end-state
  [state]
  (let [{::keys [max-iterations swarm]} state
        {::keys [best-pos]} swarm]
    (log/info "PSO Results")
    (log/info "Iterations:" max-iterations)
    (log/info "Best Pos:" best-pos)
    (try
      (log/info (format "Score: %.2f" (m-compute-fitness best-pos)))
      (catch java.util.IllegalFormatConversionException e
        (log/error "Error parsing score" e)))))

(defn run
  [init-state]
  (when-not (spec/valid? ::state init-state)
    (throw (ex-info "Invalid options"
                    (spec/explain-data ::state init-state))))
  (log/info "Running Optimization")
  (let [swarm (generate-swarm! {::size 3
                                ::dims [[-50 50]
                                        [1 10]]})]
    (loop [state (assoc init-state ::swarm swarm)
           iteration 0]
      (if (< (get-in state [::opts ::max-iterations] 100)
             iteration)
        state
        (recur (next-state state)
               (inc iteration))))))

#_(clojure.pprint/pprint (run {::opts {::max-iterations 1000
                                     ::omega 0.8
                                     ::phi-p 0.8
                                     ::phi-s 0.5}}))

