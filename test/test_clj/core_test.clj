(ns test-clj.core-test
  (:require [clojure.test :refer :all]
            [test-clj.core :refer :all]
            [clojure.xml :as xml]))

(deftest conversion->xml
  (testing "Conversion of one test to xml"
    (is (= {:tag "failure" :content ["expected: " "actual: " "message: a"]}
           (state->xml {:type :fail :message "a"})))))

(deftest messages-output
  (testing "The message output for message, expected and failure"
    (is (=["expected: " "actual: " "message: a"]
         (messages {:message "a"} )))))

(deftest message-output
  (testing "Output for one message"
    (is (= "expected: abc" (message :expected {:expected "abc"}))))
  (testing "Keyword not in state"
    (is (= "not: " (message :not {:expected "abc"})))))

(deftest state-type
  (testing "Test test state conversion to string"
    (is (= "failure" (state-type->str {:type :fail})))
    (is (= "success" (state-type->str {:type :success})))))

(deftest error-message-only-on-error
  (testing "Only message on error"
    (is (= [] (message->xml {:state {:type :pass}})))
    (is (= [{:tag "failure" :content ["expected: " "actual: " "message: "]}]
           (message->xml {:state {:type :fail}})))))

(deftest suite-conversion
  (testing "Test suite to xml conversion")
  (is (= {:tag "testsuite", :attrs [], :content [{:tag "testcase", :attrs {:time 0}, :content []}]}
         (suite->xml {:tag "testsuite"
                      :tests
                      [{:tag "testcase"
                        :start (java.time.Instant/now)
                        :end (java.time.Instant/now)
                        :state {:type :pass}}]}))))

