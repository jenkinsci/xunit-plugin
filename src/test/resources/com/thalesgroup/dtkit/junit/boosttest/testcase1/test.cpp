#include <boost/test/included/unit_test.hpp>
            
using namespace boost::unit_test;

int add( int i, int j ) { return i+j; }

static void test1(){
    BOOST_CHECK( add( 2,2 ) == 4 );        // #1 continues on error     
}

static void test2(){
    BOOST_CHECK( add( 2,3 ) == 4 );        // #1 continues on error     
}

static void test3(){
    BOOST_FAIL( "un test failed" );        // #1 continues on error     
}


test_suite *
init_unit_test_suite(int, char ** const) {
    test_suite* ts = BOOST_TEST_SUITE("array test");
    ts->add(BOOST_TEST_CASE(&test1));
    ts->add(BOOST_TEST_CASE(&test2));
    ts->add(BOOST_TEST_CASE(&test3));
    //framework::master_test_suite().add(ts);
    return ts;
}

