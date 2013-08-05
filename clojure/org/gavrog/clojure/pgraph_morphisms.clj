(ns org.gavrog.clojure.pgraph-morphisms
  (:use (org.gavrog.clojure.common [util :only [bfs]]))
  (:import (org.gavrog.joss.pgraphs.basic
             PeriodicGraph$CoverNode Morphism Morphism$NoSuchMorphismException)
           (org.gavrog.joss.pgraphs.io Net)
           (org.gavrog.joss.geometry Operator Vector)
           (org.gavrog.jane.compounds Matrix)
           (org.gavrog.jane.numbers Whole)))

(defn nets [filename]
  (iterator-seq (Net/iterator filename)))

(defn identity-matrix [net]
  (Operator/identity (.getDimension net)))

(defn barycentric-positions [net]
  (into {} (.barycentricPlacement net)))

(defn distances [net]
  "Pair-wise distances between vertices using the Floyd-Warshall algorithm"
  (let [nodes (iterator-seq (.nodes net))
        edges (iterator-seq (.edges net))
        n (count nodes)
        dist (into {} (for [v nodes, w nodes] [[v w] (if (= v w) 0 n)]))
        dist (into dist (for [e edges, e* [e (.reverse e)]]
                          [[(.source e*) (.target e*)] 1]))]
    (reduce (fn [d [u v w]]
              (assoc d [v w] (min (d [v w]) (+ (d [v u]) (d [u w])))))
            dist
            (for [u nodes, v nodes, w nodes] [u v w]))))

(defn diameter [net]
  (apply max (vals (distances net))))

(defn cover-node [net node]
  (PeriodicGraph$CoverNode. net node))

(defn cover-node-position [pos node]
  (.plus (pos (.getOrbitNode node)) (.getShift node)))

(defn adjacent [node]
  (map #(.target %) (iterator-seq (.incidences node))))

(defn next-shell-pair [[prev this]]
  [this (set (for [u this, v (adjacent u) :when (not (prev v))] v))])

(defn shells [net node]
  (conj
    (map second (iterate next-shell-pair [#{node} (set (adjacent node))]))
    #{node}))

(defn shell-positions [net pos node]
  (let [shift (.minus (pos node) (.modZ (pos node)))
        pos* (fn [v] (.minus (cover-node-position pos v) shift))]
    (map (comp sort (partial map pos*)) (shells net (cover-node net node)))))

(defn classify-once [items2seqs]
  (for [[k c] (group-by first (for [[item s] items2seqs]
                                [(first s) [item (rest s)]]))]
    [k (map second c)]))

(defn classify-recursively [items2seqs]
  (loop [classes (sorted-set [(- (count items2seqs)) [] items2seqs])]
    (let [[n k c] (first classes)]
      (if (or (nil? n) (>= n -1))
        (zipmap (map second classes) (map #(map first (last %)) classes))
        (recur (into (disj classes (first classes))
                     (for [[key cl] (classify-once c)]
                       (if (nil? key)
                         [0 k cl]
                         [(- (count cl)) (conj k key) cl]))))))))

(defn node-signatures [net]
  (let [pos (barycentric-positions net)
        nodes (iterator-seq (.nodes net))
        dia (diameter net)
        shells (for [v nodes]
                 (vec (map vec (take (inc dia) (shell-positions net pos v)))))]
    (classify-recursively (zipmap nodes shells))))

(defn extend-matrix [M]
  (let [n (.numberOfRows M)
        m (.numberOfColumns M)
        M* (Matrix/zero (inc n) (inc m))]
    (.setSubMatrix M* 0 0 M)
    (.set M* n m (Whole/ONE))
    M*))

(defn morphism [v w M]
  (if (.isUnimodularIntegerMatrix M)
    (try
      (Morphism. v w (Operator. (extend-matrix M)))
      (catch Morphism$NoSuchMorphismException ex nil))))

(defn symmetries [net]
  (let [bases (iterator-seq (.iterator (.characteristicBases net)))
        b (first bases)
        start #(.source (.get % 0))
        mat #(.differenceMatrix net %)
        iso #(morphism (start b) (start %) (Matrix/solve (mat b) (mat %)))]
    (->> bases (map iso) (filter identity))))
