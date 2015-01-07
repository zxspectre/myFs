package org.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.test.impl.SerializationTest;

/**
 * Created by nay on 1/7/2015.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerializationTest.class,
        FsSimpleTest.class,
        MultithreadedTest.class
})
public class FsTestSuite  {

}
