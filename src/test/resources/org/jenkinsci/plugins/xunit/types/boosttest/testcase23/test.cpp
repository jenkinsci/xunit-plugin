#define BOOST_TEST_MODULE MyTest
#include <boost/test/included/unit_test.hpp>
#include <boost/test/data/test_case.hpp>

using namespace boost::unit_test;

std::vector<int> cases { 1, 2 };

BOOST_DATA_TEST_CASE(MyTestCase, data::make(cases))
{
    if (sample == 2) {
        assert(false);
    }
    BOOST_TEST_MESSAGE("testing");
    BOOST_CHECK(sample == 1);
}
