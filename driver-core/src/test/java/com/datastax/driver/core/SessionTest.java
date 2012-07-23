package com.datastax.driver.core;

import java.util.*;

import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class SessionTest {

    // I really think we should make sure the library doesn't complain about
    // log4j by default, but for now let's deal with it locally
    @BeforeClass
    public static void classSetUp() {
        Logger rootLogger = Logger.getRootLogger();
        if (!rootLogger.getAllAppenders().hasMoreElements()) {
            rootLogger.setLevel(Level.INFO);
            rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
        }
    }

    @Test
    public void SimpleExecuteTest() throws Exception {

        Cluster cluster = new Cluster.Builder().addContactPoint("localhost").build();
        Session session = cluster.connect();

        ResultSet rs;

        session.execute("CREATE KEYSPACE test_ks WITH strategy_class = SimpleStrategy AND strategy_options:replication_factor = 1");
        session.execute("USE test_ks");
        session.execute("CREATE TABLE test (k text PRIMARY KEY, i int, f float)");

        rs = session.execute("INSERT INTO test (k, i, f) VALUES ('foo', 0, 0.2)");
        assertTrue(rs.isExhausted());

        rs = session.execute("INSERT INTO test (k, i, f) VALUES ('bar', 1, 3.4)");
        assertTrue(rs.isExhausted());

        rs = session.execute("SELECT * FROM test");
        List<CQLRow> l = rs.fetchAll();
        assertEquals(2, l.size());

        CQLRow r;
        r = l.get(0);
        assertEquals("bar", r.getString(0));
        assertEquals("bar", r.getString("k"));
        assertEquals(1,     r.getInt("i"));
        assertEquals(3.4,   r.getFloat("f"), 0.01);

        r = l.get(1);
        assertEquals("foo", r.getString("k"));
        assertEquals(0,     r.getInt("i"));
        assertEquals(0.2,   r.getFloat("f"), 0.01);
    }
}