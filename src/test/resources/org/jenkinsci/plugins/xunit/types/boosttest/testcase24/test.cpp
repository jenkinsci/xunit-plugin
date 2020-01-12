#define BOOST_TEST_MODULE ExampleTestSuite
#include <boost/test/included/unit_test.hpp>
#include <boost/test/unit_test.hpp>

BOOST_AUTO_TEST_SUITE(MyTestSuite_1)

BOOST_AUTO_TEST_CASE(MyTest_1_1) {
    // BOOST_TEST_MESSAGE("Running MyTest_1_1");
    int i = 1;
    BOOST_CHECK_EQUAL(1, i);
}

BOOST_AUTO_TEST_SUITE_END()

BOOST_AUTO_TEST_SUITE(MyTestSuite_2)

BOOST_AUTO_TEST_CASE(MyTest_2_1) {
    int i = 1;
    BOOST_CHECK_EQUAL(1, i);
}

BOOST_AUTO_TEST_SUITE_END()