# test-clj

Library which outputs test results in JUnit format with test execution time.

I needed some test reports with execution time. This is quite easy to make in clojure and macros.
I only needed to extend the original junit test macro.

(defn tests [n]
  `(deftest ~(symbol (str "subtraction" n))
     (is (= 1 (- 4 3)))))

(defmacro make-tests [n]
  `(do 
     ~@(map tests (range n))))

(make-tests 20)

## Usage

FIXME

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
