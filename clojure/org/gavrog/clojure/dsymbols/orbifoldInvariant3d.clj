(ns org.gavrog.clojure.dsymbols.orbifoldInvariant3d
  (:use (org.gavrog.clojure.dsymbols
          delaney
          delaney2d
          fundamental)
        (org.gavrog.clojure.common
          partition)
        (org.gavrog.clojure.fpgroups
          cosets)))

(defn- orbifold-type [ds idcs D]
  (case (count idcs)
    0 "1"
    1 (if (= D (s ds (first idcs) D)) "1*" "1")
    2 (let [[i j] idcs, n (v ds i j D)]
        (if (orbit-loopless? ds idcs D)
          (if (= n 1) "1" (str n n))
          (if (= n 1) "1*" (str "*" n n)))) 
    3 (orbifold-symbol (orbit ds idcs D))))

(defn- sublists
  ([i j k] [[i j] [i k] [j k]])
  ([i j]   [[i] [j]])
  ([i]     []))

(defn- raw-orbifold-graph [ds]
  (let [[i j k m] (indices ds)
        index-combinations [[i] [j] [k] [m]
                            [i j] [i k] [i m] [j k] [j m] [k m]
                            [i j k] [i j m] [i k m] [j k m]]
        to-rep (into {} (for [idcs index-combinations
                              D (orbit-reps ds idcs)
                              E (orbit-elements ds idcs D)]
                          [[idcs E] [idcs D]]))
        sub-orbits (fn [idcs D]
                     (let [o (orbit ds idcs D)]
                       (for [is (apply sublists idcs)
                             D (orbit-reps o is)]
                         (to-rep [is D]))))]
    (into {} (for [idcs index-combinations
                   D (orbit-reps ds idcs)]
               [[idcs D] {:type (orbifold-type ds idcs D)
                          :adjs (sub-orbits idcs D)}]))))

(defn- filter-nodes [p g]
  (let [good (set (filter (comp p g) (keys g)))]
    (into {} (for [[k v] g :when (good k)]
               [k (assoc v :adjs (filter good (:adjs v)))]))))

(defn- filter-edges [p g]
  (into {} (for [[k v] g]
             [k (assoc v :adjs (filter #(p (g k) (g %)) (:adjs v)))])))

(defn- edges [g]
  (for [[a v] g
        b (:adjs v)]
    [a b]))

(defn- equivalence-classes [equiv pairs s]
  (let [join (fn [p [a b]] (if (equiv a b) (punion p a b) p))
        p (reduce join pempty pairs)
        seen (set (apply concat p))]
    (concat p (map #(set [%]) (filter (comp not seen) s)))))

(defn- quotient-graph [equiv g]
  (let [classes (sort-by (comp :type g first)
                         (equivalence-classes #(equiv (g %1) (g %2))
                                              (edges g) (keys g)))
        to-rep (into {} (for [i (range (count classes)), a (nth classes i)]
                          [a i]))]
    (into {} (for [cl classes
                   :let [key (to-rep (first cl))
                         adjs (set (for [a cl
                                         b (map to-rep (:adjs (g a)))
                                         :when (not= b key)]
                                     b))]]
               [key (assoc (g (first cl)) :adjs adjs)]))))

(defn orbifold-graph [ds]
  (->>
    (raw-orbifold-graph ds)
    (filter-nodes #(not= "1" (:type %)))
    (quotient-graph #(= (:type %1) (:type %2)))
    (filter-edges (fn [v w]
                    (let [[s t] (map :type [v w])]
                      (not (and (= "1*" t)
                                (or (not= \* (first s))
                                    (= 4 (count s))))))))
    sort))

(defn orbifold-invariant [ds]
  (let [g (orbifold-graph ds)
        inv (let [{:keys [nr-generators relators]} (fundamental-group ds)]
              (abelian-invariants nr-generators relators))
        a (map (comp :type second) g)
        b [(if (oriented? ds) 2 (if (weakly-oriented? ds) 1 0))
           (reduce + (map (comp count :adjs second) g))
           (count inv)]]
        (apply str (interpose "/" (concat [(count a)] a b inv [""])))))
