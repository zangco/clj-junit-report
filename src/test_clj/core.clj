(ns test-clj.core
  (:use [clojure.test])
  (:require [clojure.test.junit :as tj]
            [clojure.xml :as xml]))

(def ^:dynamic *current-test*)
(def ^:dynamic *current-suite*)
(def ^:dynamic *total-tests*)

(defn set-state! [m] (set! *current-test* (assoc *current-test* :state  m)))

;; Multi method  overrides test-is/report
(defmulti ^:dynamic junit-time-report :type)

(defmethod junit-time-report :begin-test-var [m]
  (let [var (:var m)]
    (set! *current-test* {:tag "testcase"
                          :name (name (:name (meta var)))
                          :class (name (ns-name (:ns (meta var))))
                          :start (java.time.Instant/now)})))

(defmethod junit-time-report :end-test-var [m]
  (let [end-test (assoc *current-test* :end (java.time.Instant/now))]
    (set! *current-suite* (update-in *current-suite* [:tests] #(conj % end-test)))))

(defmethod junit-time-report :end-test-ns [m]
  (set! *total-tests* (conj *total-tests* *current-suite*)))

(defmethod junit-time-report :begin-test-ns [m]
  (let [[package classname] (tj/package-class (name (ns-name (:ns m))))]
    (set! *current-suite* {:tag "testsuite"
                           :name classname
                           :package package
                           :tests []})))

(defmethod junit-time-report :pass [m] (set-state! m))
(defmethod junit-time-report :fail [m] (set-state! m))
(defmethod junit-time-report :error [m] (set-state! m))
(defmethod junit-time-report :summary [m])

;; Conversion methods to xml dictionary
(defn message [k state] (str (name k) ": " (k state)))

(defn messages [state]
  (mapv #(message % state) [:expected :actual :message]))

(defn state-type->str [state]
  (case (:type state) :fail "failure" (name (:type state))))

(defn state->xml [state]
  {:tag (state-type->str state)
   :content (messages state)})

(defn time-diff [test]
  (- (.toEpochMilli (:end test)) (.toEpochMilli (:start test))))

(defn test-attributes
  "Remove attributes which are handle otherwise"
  [test]
  (into {}
        (filter (fn [k]
                  (not (contains? #{:tag :start :end :state} (first k)))) test)))

(defn message->xml
  [test]
  (if (= :pass (:type (:state test)))
    []
    [(state->xml (:state test))]))

(defn test->xml [test]
  { :tag (:tag test)
   :attrs (assoc (test-attributes test) :time (time-diff test))
   :content (message->xml test) })

(defn suite-attributes
  [suite]
  (filterv (fn [k] (not (contains? #{:tag :tests} (first k) ))) suite))

(defn suite->xml [suite]
  { :tag (:tag suite)
   :attrs (suite-attributes suite)
   :content (mapv test->xml  (:tests suite))})

(defn suites->xml [tests]
  {:tag "testsuites"
   :content (map #(suite->xml %) tests)})

;; output macro
(defmacro with-junit-time-output
  [& body]
  `(binding [report junit-time-report
             *current-test* nil
             *current-suite* nil
             *total-tests* []]
     (do ~@body)
     (xml/emit (suites->xml *total-tests*))))


