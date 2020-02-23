#include <boost/test/included/unit_test.hpp>

using namespace boost::unit_test;

int add(int i, int j) { return i+j; }

static void test1()
{
    BOOST_CHECK_EQUAL(add(2, 2), 4);
}

static void test2()
{
    BOOST_CHECK_EQUAL(add(2, 3), 4);
}

static void test3()
{
    BOOST_FAIL("un test failed");
}

static void test4()
{
    BOOST_CHECK_EQUAL(add(2, 2), 4);
}

static void test5()
{
    BOOST_CHECK_EQUAL(add(2, 3), 4);
}

static void test6()
{
    BOOST_FAIL("un test failed");
}

struct returnFalse{
    assertion_result operator()(test_unit_id)
    {
        return assertion_result(false);
    }
};

test_suite *
init_unit_test_suite(int, char ** const)
{
    test_suite* ts_root = BOOST_TEST_SUITE("root");

    test_suite* ts1 = BOOST_TEST_SUITE("ts1");
    ts1->add_precondition(returnFalse());
    ts1->add(BOOST_TEST_CASE(&test1));
    ts1->add(BOOST_TEST_CASE(&test2));
    ts1->add(BOOST_TEST_CASE(&test3));
    //framework::master_test_suite().add(ts);
    ts_root->add(ts1);

    test_suite* ts2 = BOOST_TEST_SUITE("ts2");
    auto tc1 = BOOST_TEST_CASE(&test4);
    tc1->add_precondition(returnFalse());
    ts2->add(tc1);
    auto tc2 = BOOST_TEST_CASE(&test5);
    tc2->add_precondition(returnFalse());
    ts2->add(tc2);
    auto tc3 = BOOST_TEST_CASE(&test6);
    tc3->add_precondition(returnFalse());
    ts2->add(tc3);
    //framework::master_test_suite().add(ts);
    ts_root->add(ts2);

    return ts_root;
}
