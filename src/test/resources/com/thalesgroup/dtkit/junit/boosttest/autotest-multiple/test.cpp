#define BOOST_TEST_MODULE MyTest
#include <boost/test/included/unit_test.hpp>

using namespace boost::unit_test;

BOOST_AUTO_TEST_CASE(MyTestCase)
{
	float x = 9.5f;
	BOOST_CHECK(x != 0.0f);
	BOOST_CHECK_EQUAL((int)x, 9);
	BOOST_CHECK_CLOSE(x, 9.5f, 0.0001f); // Checks differ no more then 0.0001%
}

