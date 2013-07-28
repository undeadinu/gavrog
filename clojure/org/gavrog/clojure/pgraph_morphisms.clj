(ns org.gavrog.clojure.pgraph-morphisms
  (:import (org.gavrog.joss.pgraphs.basic PeriodicGraph$CoverNode)
           (org.gavrog.joss.pgraphs.io Net)
           (org.gavrog.joss.geometry Operator Vector)))

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
