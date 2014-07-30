#include <boost/test/included/unit_test.hpp>

using namespace boost::unit_test;

static void test_1() {
     int i = 1;
     int j = 1;

     BOOST_CHECK_EQUAL(i, j);
     BOOST_FAIL( "un test failed" ); 

}

static void test_2() {
     int i = 1;
     int j = 2;
     int k = 1;	

     BOOST_CHECK_EQUAL(i, k);

     BOOST_CHECK_EQUAL(i, j);

     BOOST_CHECK_EQUAL(j, k);

}

test_suite *
init_unit_test_suite(int, char ** const) {
     test_suite *ts = BOOST_TEST_SUITE("test_suite");
     ts->add(BOOST_TEST_CASE(&test_1));
     ts->add(BOOST_TEST_CASE(&test_2));
     framework::master_test_suite().add(ts);

     return 0;
} 