(ns smiletest.core
  (:require [spork.util [table :as tbl] [clipboard :as clip]]
            [spork.cljgui.components.swing :as gui])
  (:import [smile.interpolation
            KrigingInterpolation2D
            RBFInterpolation2D
            ShepardInterpolation2D
            CubicSplineInterpolation2D
            Interpolation2D]
           [smile.plot.swing Canvas Point ScatterPlot]))

(def sparse
  "AC-Supply	RC-Supply	Total
0	7	0.353944544
0	8	0.290257918
1	6	0.34257919
1	8	0.39724104
1	11	0.518849521
2	3	0.29061109
2	4	0.378298823
2	8	0.462225179
2	9	0.571473099
3	3	0.316451056
3	5	0.441681637
3	12	0.706440131
4	7	0.592229147
4	8	0.668243659
4	11	0.786585298
5	3	0.467315354
5	6	0.709651935
5	13	0.876288708
5	15	0.923932882
6	2	0.577013776
6	3	0.56227162
6	12	0.89387339
7	1	0.575226827
7	4	0.597366133
7	9	0.840845889
7	11	0.920962668
8	3	0.695241283
8	8	0.913048969
8	12	0.94290986
9	4	0.601671403
9	6	0.849695586
9	11	0.994671133
9	12	0.990560292
10	3	0.835806697
10	12	0.978310502
10	13	1
10	14	1
11	0	0.7825653
11	4	0.89318489
11	9	1
11	12	1
12	2	0.861296272
12	4	0.908295282
12	7	0.951026563
12	8	1
13	3	0.883247636
13	8	1
13	10	1
13	12	1
14	3	0.96122522
14	6	1
14	7	1
14	11	1
15	4	0.994292237
15	7	1
15	9	1
15	12	1
16	7	1
16	8	1
16	9	1
")

(defmacro deflerper [fname klass]
  (let [ctor   (symbol (str klass "."))
        ;tag    (symbol (.getName klass))
        lerper (with-meta (gensym "lerper") {:tag klass})
        xs     (with-meta (gensym "xs") {:tag 'doubles})
        ys     (with-meta (gensym "ys") {:tag 'doubles})
        zs     (with-meta (gensym "zs") {:tag 'doubles})
        x (with-meta (gensym "x") {:tag 'double})
        y (with-meta (gensym "y") {:tag 'double})
        argv [x y]]
    `(defn ~fname [~xs ~ys ~zs]
       (let [~lerper (~ctor ~xs ~ys ~zs)]
         (fn  ^{:tag ~'double} lerper# ~argv
           (.interpolate ~lerper ~(with-meta x {}) ~(with-meta y {})))))))

(defn ->krig [^doubles xs ^doubles ys ^doubles zs]
  (let [^KrigingInterpolation2D krig (KrigingInterpolation2D. xs ys zs)]
    (fn  ^double [^double x ^double y]
      (.interpolate krig x y))))

;;error when all scores were the same?
(deflerper ->kriger KrigingInterpolation2D)
;;error when called
(deflerper ->cubic  CubicSplineInterpolation2D)
(deflerper ->shepard ShepardInterpolation2D)

;;error
;;(deflerper ->rbf     RBFInterpolation2D)

;;bleh! just compute bounds here maybe but then would need to pipe through...
(defn lerped [f & {:keys [knowns bounds] :or {knowns (fn [_] nil)}}]
  (vec (for [
             x (range 0 17)
             y (range 0 16)]
         [x y (or (knowns [x y])
                  (f x y))])))

(defn assess [x]
  (cond (>= x 0.95) 3
        (>= x 0.85) 2
        (>= x 0.60) 1
        :else 0))

(defn new-points [f & {:keys [knowns assessor bounds] :or {knowns (fn [_]
                                                             nil)
                                                    assessor assess}}]
  (vec (for [[ac rc total] (lerped f :knowns knowns :bounds bounds)]
         (let [total (max (min total 1.0)
                          0)]
           {:AC-Supply ac :RC-Supply rc :Total total :color (assessor total)}))))

(defn variables->arrays
  [table]
  [(-> (tbl/get-field :AC-Supply table ) :AC-Supply double-array)
  (-> (tbl/get-field :RC-Supply table ) :RC-Supply double-array)
   (-> (tbl/get-field :Total table ) :Total double-array)])

(defn interpolate-data
  [lerper table & {:keys [assessor bounds] :or {assessor assess}}]
  (let [[xs ys zs] (variables->arrays table)
        {:keys [ymax ymin xmax xmin] :or
         ;;Set the bounds of interpolation to be the max and mins
         {ymax (reduce max ys) ymin (reduce min ys)
          xmax (reduce max xs) xmin (reduce min xs) :as bs}} bounds
        points (map (fn [x y z] [(mapv long [x y]) z]) xs ys zs)]
    (new-points (lerper xs ys zs)
                :knowns (into {[0 0] 0} points)
                :assessor assessor
                :bounds bs
                )))

;;demo
(def data (tbl/tabdelimited->table sparse))
(def k-points (interpolate-data ->kriger data))
(def s-points (interpolate-data ->shepard data))

;;error
;;(def c-points (interpolate-data ->cubic data))
